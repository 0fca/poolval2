/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.output;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author obsidiam
 */
public class OutputFormatter extends Formatter{

    @Override
    public String format(LogRecord record) {
        return new SimpleDateFormat("dd-MM-YY HH:mm:ss").format(new Date(record.getMillis()))+" : "+record.getLevel().getLocalizedName()+" : "+record.getMessage();
    }
}
