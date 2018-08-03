/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.input;

import com.dasi.bench.config.Config;
import com.dasi.bench.helper.DatabaseHelper;
import com.dasi.bench.helper.QueryConstants;
import com.dasi.bench.output.OutputController;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author obsidiam
 */
final public class InputController {//CLI command engine, it recognizes and executes commands given by the user.
    private volatile static InputController inputControllerInstance = new InputController();
    private Object[] args = new Object[0];
    //private BaseCommand baseCommand;
    private Config currentCnf = null;
    
    private InputController(){
        currentCnf = prepareConfiguration();
    }
    
    public static InputController getControllerInstance(){
        return inputControllerInstance;
    }
    
    public void execute(BaseCommand baseCommand, Object[] args) throws IllegalArgumentException, IllegalAccessException{
        if(baseCommand == null){//no base command so we assume that user wants just to execute standard bench
            standardBench();
        }else{
            try {
                this.args = args;
                Method m = InputController.class.getDeclaredMethod(baseCommand.getFlagStringRep());
                m.setAccessible(true);
                m.invoke(InputController.this);

            } catch (NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE, "No such command as "+baseCommand.getFlagStringRep()+" because of "+ex.toString()));
            }
        }
    }
    
    private void standardBench(){
        boolean[] wasSuccess = new boolean[1];
        wasSuccess[0] = true;
        OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, "Starting standard benchmark..."));
        if(currentCnf == null){
            //currentCnf = prepareConfiguration();
        }
        Field[] fs = QueryConstants.class.getFields();
        DatabaseHelper helper = DatabaseHelper.getHelperInstance(currentCnf);
        helper.configReload();
        double startTime = System.nanoTime();
        ArrayList<Future> statuses = new ArrayList<>();
        OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, "Test execution started."));
        int count = 1;
        if(args.length == 1){
            count = Integer.parseInt(args[0].toString());
        }
        
        int i = 0;

        while(i < count){
            OutputController.getControllerInstance().printMessage(new LogRecord(Level.WARNING, "Starting test number: "+i));
            Future testResult = helper.executeTest();
            while(!testResult.isDone()){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(InputController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            statuses.add(testResult);
            i++;
            
        }
        
        while(statuses.size() > 0){
            statuses.removeIf(filter ->{
                try {
                    if(!((boolean)filter.get())){
                        wasSuccess[0] = false;
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, ex.getMessage()));
                }
                return filter.isDone();
            });
        }
        double endTime = System.nanoTime();
        double testTime = (endTime - startTime)/1000000000.0;
        OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, "Database benchmark ended "+(wasSuccess[0] ? "successfully" : "with errors") +". Full test time: "+testTime));
        
    }
    
    private void printHelp(){
        System.out.println("Commands: ");
        for(BaseCommand c : BaseCommand.values()){
            System.out.println(c.getFlagStringRep());
        }
        System.out.println("If you really don't get what each command does, you should not touch this software, really.");
    }
    
    private void printVersion(){
        System.out.println("Srsly, are you bored? Do you really need to know version? okay... v.0.2.1");
    }

    private void showCurrentDatabaseConfiguration(){
        Field[] options = currentCnf.getClass().getDeclaredFields();

        for(Field option : options){
            option.setAccessible(true);
            try {
                OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, option.getName()+"="+option.get(currentCnf).getClass().getMethod("get").invoke(option.get(currentCnf), new Object[0]).toString()));
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                Logger.getLogger(InputController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void showCurrentIp(){
        OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, currentCnf.ipProperty().get()));
    }
    
    private void setPort(){
        
        if(args.length == 1){
            
            currentCnf.portProperty().set(Integer.parseInt(args[0].toString()));
        }
    }
    
    private void setIp(){
        if(args.length == 1){
            currentCnf.ipProperty().set(args[0].toString());
        }
    }
    
    private void setUrl(){
        System.out.println(args.length);
        if(args.length == 2){
            currentCnf.ipProperty().set(args[0].toString());
            currentCnf.portProperty().set(Integer.parseInt(args[1].toString()));
        }
    }

    private Config prepareConfiguration() {
        Config cnf = new Config("test","192.168.17.64","testuser","password", 5432);
        
        if(args.length > 0){
            switch(args.length){
                case 1:
                    cnf.ipProperty().set(args[0].toString());
                    break;
                case 2:
                    cnf.ipProperty().set(args[0].toString());
                    cnf.portProperty().set(Integer.parseInt(args[1].toString()));
                    break;
            }
        }
        return cnf;
    }
}
