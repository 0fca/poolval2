/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.config;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author obsidiam
 */
public class Config {//just configuration of benchmark test
    final private SimpleStringProperty databaseName, ip, userName, pass;
    final private SimpleIntegerProperty port;
    
    public Config(String databaseName, String ip, String userName, String pass, int port){
        this.databaseName = new SimpleStringProperty(databaseName);
        this.ip = new SimpleStringProperty(ip);
        this.userName = new SimpleStringProperty(userName);
        this.pass = new SimpleStringProperty(pass);
        this.port = new SimpleIntegerProperty(port);
    }
    
    public SimpleStringProperty databaseNameProperty(){
        return this.databaseName;
    }
    
    public SimpleStringProperty ipProperty(){
        return this.ip;
    }
    
    public SimpleStringProperty usernameProperty(){
        return this.userName;
    }
    
    public SimpleStringProperty passProperty(){
        return this.pass;
    }
    
    public SimpleIntegerProperty portProperty(){
        return this.port;
    }
    
    @Override
    public String toString(){
        return "Bench configuration is:\nDatabasename: "+this.databaseName.get()+"\nIP or Domain name: "+this.ip.get()+"\nUsername: "+this.userName.get();
    }
}
