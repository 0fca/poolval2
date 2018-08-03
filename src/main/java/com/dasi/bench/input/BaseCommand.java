/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.input;

/**
 *
 * @author obsidiam
 */
public enum BaseCommand {
    STANDARD_BENCH("standardBench"),
    HELP("printHelp"),
    VERSION("printVersion"),
    SET_PORT("setPort"),
    SET_IP("setIp"),
    SHOW_CURRENT_DB_CONF("showCurrentDatabaseConfiguration"),
    SHOW_CURRENT_IP("showCurrentIp"),
    SET_URL("setUrl");
    
    private String flag;
    
    
    BaseCommand(String switchFlag){
        this.flag = switchFlag;
    }
    
    public String getFlagStringRep(){
        return this.flag;
    }
   
}
