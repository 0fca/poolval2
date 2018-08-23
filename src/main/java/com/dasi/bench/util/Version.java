/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.util;

/**
 *
 * @author obsidiam
 */
public class Version {
    final public static int MAJOR = 0;
    final public static int MINOR = 4;
    final public static int REVISION = 2;
    
    @Override
    public String toString(){
        return MAJOR+"."+MINOR+"."+REVISION;
    }
}
