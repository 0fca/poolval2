/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.helper;

import java.util.logging.Handler;
import java.util.logging.LogRecord;


/**
 *
 * @author obsidiam
 */
final public class LogHelper extends Handler {
    @Override
    public void publish(LogRecord record) {
        System.out.println(this.getFormatter().format(record));
    }

    @Override
    public void flush() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws SecurityException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
