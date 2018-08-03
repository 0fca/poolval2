/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.helper;

import com.dasi.bench.PoolVal;
import com.dasi.bench.config.Config;
import com.dasi.bench.output.OutputController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author obsidiam
 */
final public class DatabaseHelper {//Use singleton, no need to make more than one connection to db.
    private volatile static DatabaseHelper databaseHelperInstance;
    private ServiceBuilder serviceBuilder;
    private Connection connection;
    private static Config runningConfig;

    
    private DatabaseHelper(){
        
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("# FAIL [Driver]");
	    e.printStackTrace();
            return;
        }
        loadConfig();
    }
    
    public static synchronized DatabaseHelper getHelperInstance(Config conf){
        runningConfig = conf;
        if(databaseHelperInstance == null){
            databaseHelperInstance = new DatabaseHelper();
        }
        return databaseHelperInstance;
    }
    
    public void configReload(){
       if(runningConfig != null){
           loadConfig();
       }
    }
    
    public Future executeTest(){
        this.serviceBuilder = new ServiceBuilder();
     
        this.serviceBuilder.prepareExecutorService(() -> {return PoolVal.sqlTest(connection, false, false);});
        return this.serviceBuilder.getServiceResult();//as this is a Future class object we can get it here and just wait on it checking its state for the result.
    }
    
    public boolean cancelTestExecution(){
        return (this.serviceBuilder.cancelExecution() == 0);
    }

    private void loadConfig() {
        String dbURL = "jdbc:postgresql://"+runningConfig.ipProperty().get()+":"+runningConfig.portProperty().get()+"/"+runningConfig.databaseNameProperty().get();
        Properties dbProps = new Properties();
	dbProps.setProperty("user",runningConfig.usernameProperty().get());
	dbProps.setProperty("password",runningConfig.passProperty().get());
	dbProps.setProperty("ssl","false");
        try {
            connection = DriverManager.getConnection(dbURL, dbProps);
        } catch (SQLException ex) {
            OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE, ex.getLocalizedMessage()));
        }
    }
    
    private class ServiceBuilder{//impl of semi-Builder pattern as of there is a class Service so we use it as a product of this Builder class without any other layer of abstraction on it.
        private final ExecutorService localInstance;
        private Future futureInstance;
        
        private ServiceBuilder(){
           localInstance = Executors.newSingleThreadExecutor();   
        }
        
        private void prepareExecutorService(Callable<Boolean> r){//pass the scope to the Builder so it can inject it to the executor, surely we will use lambdas.
            this.futureInstance = this.localInstance.submit(r);
        }
        
        private Future getServiceResult(){//okay, so now we are Back in the future?
            localInstance.shutdown();
            return futureInstance;
        }
        
        private int cancelExecution(){//It returns just size of list of awaiting tasks, and because contract of this helper states that one test on execution so this value informs if execution ever started.
            return localInstance.shutdownNow().size();
        }
    }
}
