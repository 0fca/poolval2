/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.output;

import com.dasi.bench.helper.LogHelper;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author obsidiam
 */
final public class OutputController {
    private static volatile OutputController controllerInstance = new OutputController();
    final private static OutputFormatter localFormatter = new OutputFormatter();
    
    private OutputController(){}
    
    public static synchronized OutputController getControllerInstance(){
        
        return controllerInstance;
    }
    
    public void printMessage(LogRecord record){
        LogHelper logHelper = new LogHelper();
        logHelper.setFormatter(localFormatter);
        if(record.getLevel() == Level.WARNING || record.getLevel() == Level.SEVERE) printSeparator(record.getMessage().length());
        logHelper.publish(record);
        if(record.getLevel() == Level.WARNING || record.getLevel() == Level.SEVERE) printSeparator(record.getMessage().length());
    }
    
    public void printPrompt(){
        System.out.print(">");
    }
    
    public void printSeparator(int messageLength){
        String separator = "";
        for(int i = 0; i < 40; i++){
            separator += "=";
        }
        System.out.println(separator);
    }
}
