/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dasi.bench.helper;

import com.dasi.bench.input.InputController;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 *
 * @author obsidiam
 */
public class DiagSignalHandler implements SignalHandler {
    private SignalHandler oldHandler;
    private InputController inputController;
    
    public static DiagSignalHandler install(String signalName) {
        Signal diagSignal = new Signal(signalName);
        DiagSignalHandler diagHandler = new DiagSignalHandler();
        diagHandler.oldHandler = Signal.handle(diagSignal, diagHandler);
        return diagHandler;
    }

    @Override
    public void handle(Signal signal) {
        DatabaseHelper hlpr = this.inputController.getDatabaseHelper();
        if(hlpr != null){
            if(!inputController.checkMarker()){
                hlpr.cancelTestExecution();
            }else{
                hlpr.terminateForever();
                Runtime.getRuntime().halt(0);
            }
        }else{
            Runtime.getRuntime().halt(0);
        }
    }
    
    public void setInputController(InputController inputController){
        this.inputController = inputController;
    }
}
