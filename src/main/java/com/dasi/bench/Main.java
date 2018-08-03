package com.dasi.bench;

import com.dasi.bench.input.BaseCommand;
import com.dasi.bench.input.InputController;
import com.dasi.bench.output.OutputController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author obsidiam
 */
public class Main {//Entry class.
    final private static HashMap<String,String> aliases = new HashMap<>();
    
    static{
        aliases.put("\\l", "showCurrentDatabaseConfiguration");
        aliases.put("\\h", "printHelp");
        aliases.put("\\s", "standardBench");
        aliases.put("\\c", "setUrl");
    }
    
    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException{
        InputController inputController = InputController.getControllerInstance();
        
        Scanner s = new Scanner(System.in);
        OutputController.getControllerInstance().printMessage(new LogRecord(Level.ALL, "PoolVal 2 for DASI by Obsidiam. Thanks to power of pikas! 2018"));
        //OutputController.getControllerInstance().printPrompt();
        
        while(true){
            OutputController.getControllerInstance().printPrompt();
            final String baseCommandStr = s.nextLine();
            if(baseCommandStr.contains("quit") || baseCommandStr.contains("\\q") || baseCommandStr.contains("exit")){
                break;
            }
            
            if(baseCommandStr.isEmpty()){
                inputController.execute(null, null);
                continue;
            }
            
            boolean[] commandExists = new boolean[1];
            //commandExists[0] = true;
            List<BaseCommand> l = Arrays.asList(BaseCommand.values());
            if(baseCommandStr.startsWith("\\")){
                String changedCommand = baseCommandStr;
                if(baseCommandStr.contains("\\?")) changedCommand = baseCommandStr.replace('?', 'h');
                
                String baseCommand = aliases.get(changedCommand.split(" ")[0]);
                
                Object[] o = l.stream().filter(predicate ->{return predicate.getFlagStringRep().equals(baseCommand);}).toArray();
                inputController.execute((BaseCommand)(o.length > 0 ? o[0] : BaseCommand.HELP), prepareArguements(baseCommandStr));
                //inputController.execute((BaseCommand)l.stream().filter(predicate ->{return commandExists[0] = predicate.getFlagStringRep().equals(baseCommand);}).toArray()[0], prepareArguements(baseCommandStr));
            }else{
                Object[] o = l.stream().filter(predicate ->{return baseCommandStr.contains(predicate.getFlagStringRep());}).toArray();
                inputController.execute((BaseCommand)(o.length > 0 ? o[0] : BaseCommand.HELP), prepareArguements(baseCommandStr));
            }
            
            
//            if(commandExists[0]){
//                OutputController.getControllerInstance().printMessage(new LogRecord(Level.ALL, "There is no such command."));
//                inputController.execute(BaseCommand.HELP, args);
//            }
        }
        Runtime.getRuntime().halt(0);
    }

    private static Object[] prepareArguements(String commandString) {
       Object[] args;
       String[] parts = commandString.split(" ");
       args =  new Object[parts.length - 1];
       System.arraycopy(parts, 1, args, 0, parts.length - 1);
       
       return args;
    }
}
