/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.helper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author obsidiam
 */
final public class NetHelper {//Typical final helper, just to ping hosts in the LAN.
    private static volatile NetHelper netHelperInstance = new NetHelper();
    
    private NetHelper(){}
    
    public static synchronized NetHelper getHelperInstance(){
        return netHelperInstance;
    }
    
    public boolean pingHost(String ip) throws UnknownHostException, IOException, InterruptedException{
        boolean b = InetAddress.getByName(ip).isReachable(100);
        Thread.sleep(100);
        return b;
    }
}
