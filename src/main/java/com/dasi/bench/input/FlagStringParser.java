/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.input;

import com.dasi.bench.config.Config;

/**
 *
 * @author obsidiam
 */
@SuppressWarnings("unused")
final public class FlagStringParser {
    private String localFlagString, flags;
    private Config cnf;
    
    public FlagStringParser(String flagString, String flags){
        this.localFlagString = flagString;
        this.flags = flags;
    }
    
    private void parseFlags(){

    }
    
    public Config getConfig(){
        return cnf;
    }
}
