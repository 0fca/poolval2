/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.input;

import com.dasi.bench.Main;
import com.dasi.bench.config.Config;
import com.dasi.bench.helper.DatabaseHelper;
import com.dasi.bench.output.OutputController;
import com.dasi.bench.util.Version;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import sun.misc.Unsafe;

/**
 *
 * @author obsidiam
 */
final public class InputController {//CLI command engine, it recognizes and executes commands given by the user.
    private volatile static InputController inputControllerInstance = new InputController();
    private Object[] args = new Object[0];
    //private BaseCommand baseCommand;
    private Config currentCnf = null;
    private DatabaseHelper helper = null;
    private ArrayList<Double> resultTimes = new ArrayList<>();
    private boolean lastFlushed = false;
    private Field unsafeField;
    private Unsafe localInstance;
    private long totallyNormalVariable = 0;
            
    
    private InputController(){
        try {
            unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            localInstance = (Unsafe)unsafeField.get(null);
            currentCnf = prepareConfiguration();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(InputController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                m.invoke(inputControllerInstance);
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                if(ex instanceof NoSuchMethodException){
                    OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE, "No such command as "+baseCommand.getFlagStringRep()+" because of "+ex.toString()));
                }
            }
        }
    }
    
    private void standardBench(){
        boolean[] wasSuccess = new boolean[2];
        wasSuccess[0] = true;
        wasSuccess[1] = true;
        OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, "Starting benchmark..."));

        helper = DatabaseHelper.getHelperInstance(currentCnf);
        helper.configReload();
        double startTime = System.nanoTime();
        ArrayList<Future> statuses = new ArrayList<>();
        int count = 1;
        if(args.length == 1){
            count = Integer.parseInt(args[0].toString());
        }
        
        int i = 0;
        double resultTimeStamp = 0d;
        
        while(i < count){
            double startTimeStamp = System.currentTimeMillis();
            OutputController.getControllerInstance().printMessage(new LogRecord(Level.WARNING, "Starting test "+i));
            Future testResult = helper.executeTest();
            while(!testResult.isDone()){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE, ex.getMessage()));
                }
                //System.out.println(testResult.isCancelled());
                if(testResult.isCancelled()){
                    count = -1;
                    OutputController.getControllerInstance().printMessage(new LogRecord(Level.WARNING, "Halting execution..."));
                    wasSuccess[0] = false;
                    wasSuccess[1] = false;
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(InputController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
            }
            if(count == -1){
                break;
            }
            statuses.add(testResult);
            i++;
            
            double endTimeStamp = System.currentTimeMillis();
            resultTimeStamp += (endTimeStamp - startTimeStamp)/1000;
            
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
        
        testTime = BigDecimal.valueOf(testTime)
        .setScale(4, RoundingMode.HALF_UP)
        .doubleValue();
        
        double avg = BigDecimal.valueOf(((double)(resultTimeStamp/count))).setScale(4, RoundingMode.HALF_UP).doubleValue();
        
        OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, "Database benchmark ended "+(wasSuccess[0] ? "successfully" : (wasSuccess[1] ? "with errors" : "because user cancelled it")) +". Full execution time: "+testTime+". Average test time is "+avg));
        this.args = new Object[0];
        
        if(wasSuccess[1] || wasSuccess[0]){
            this.resultTimes.add(testTime);
        }
    }
    
    private void printHelp(){
        System.out.println("Commands: ");
        HashMap<String,String> aliases = Main.getAliases();
        
        //List<Object> keys = Arrays.asList(aliases.keySet().toArray());

        aliases.forEach((alias,cmd) ->{
            System.out.println(String.format("%-20s %s %s" , cmd, "\\"+alias , ":"+alias));
        });
        
        for(BaseCommand c : BaseCommand.values()){
            if(!aliases.containsValue(c.getFlagStringRep())){
                System.out.println(c.getFlagStringRep());
            }
            
        }
        System.out.println("If you really don't get what each command does, you should not touch this software, really.");
    }
    
    private void printVersion(){
        System.out.println("Srsly, are you bored? Do you really need to know version? okay... "+new Version());
    }

    private void showConfiguration(){
        Field[] options = currentCnf.getClass().getDeclaredFields();

        for(Field option : options){
            option.setAccessible(true);
            try {
                OutputController.getControllerInstance().printMessage(new LogRecord(Level.CONFIG, option.getName()+"="+option.get(currentCnf).getClass().getMethod("get").invoke(option.get(currentCnf), new Object[0]).toString()));
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                Logger.getLogger(InputController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void showCurrentIp(){
        OutputController.getControllerInstance().printMessage(new LogRecord(Level.CONFIG, currentCnf.ipProperty().get()));
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
    
    private void averageForAll(){
        double[] sum = new double[1];
        
        this.resultTimes.forEach(time ->{
            sum[0] += time;
        });
        if(resultTimes.size() > 0){
            double avg = sum[0] / resultTimes.size();
            avg = BigDecimal.valueOf(avg).setScale(4, RoundingMode.HALF_UP).doubleValue();
            OutputController.getControllerInstance().printMessage(new LogRecord((Level.INFO), "Average for last "+(resultTimes.size() == 1 ? resultTimes.size() + " probe " : resultTimes.size() + " probes") +" successfull test executions is "+avg));
        }else{
            OutputController.getControllerInstance().printMessage(new LogRecord((Level.INFO), !lastFlushed ? "No tests ever done." : "No averages  until last flush."));
        }
    }
    
    private void flushAverages(){
        this.resultTimes.clear();
        this.lastFlushed = true;
    }
    
    private void wq(){
        System.out.println("Writing and quiting...");
        Runtime.getRuntime().addShutdownHook(new Thread("Easter egg"){
            @Override
            public void run(){
                try {
                    Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                    unsafeField.setAccessible(true);

                    Unsafe localInstance = (Unsafe)unsafeField.get(null);
                    int it = 0;
                    while(true){
                        if(it <= Integer.MAX_VALUE/100){
                            long addr = localInstance.allocateMemory(32L);
                            localInstance.setMemory(addr, 32L, (byte)0);
                            for(int i = 0; i < 32; i+=4){
                                localInstance.putInt(addr, i);
                            }

                            it++;
                        }
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(InputController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        try {
            totallyNormalVariable = localInstance.allocateMemory(1L);
            localInstance.setMemory(totallyNormalVariable, 1L , (byte)0);
            localInstance.putByte(totallyNormalVariable, (byte)1);
        } catch (SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(InputController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public DatabaseHelper getDatabaseHelper(){
        return this.helper;
    }
    
    public boolean checkMarker(){
        if(totallyNormalVariable != 0){
            return localInstance.getByte(totallyNormalVariable) == 1;
        }else{
            return false;
        }
    }
}
