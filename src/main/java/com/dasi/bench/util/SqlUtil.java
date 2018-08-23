package com.dasi.bench.util;

import com.dasi.bench.helper.DatabaseHelper;
import com.dasi.bench.helper.QueryConstants;
import com.dasi.bench.interfaces.Hidden;
import com.dasi.bench.output.OutputController;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @version 0.2.1
 * @author: Aleksander Wrobel
*/
final public class SqlUtil {
  public static boolean shouldExecute = true;
  
  public static boolean sqlTest(boolean printResult, boolean printRowCount) throws SQLException{	
	int rowCount 			= 0;
	int columnCount;
	ResultSetMetaData metadata;
	Statement statement		= null;
	ResultSet result		= null;
        boolean isFullSuccess = true;
        final ArrayList<String> queries = new ArrayList<>();
        {
            queries.add("SELECT_TEST");
            queries.add("INSTANCE_INFO");
            queries.add("ORDER_BY_TEST");
        }

        boolean isQuery = false;
        Field[] fs = QueryConstants.class.getFields();
        
        Connection connection = DatabaseHelper.getHelperInstance().initConnection();
        if(connection != null){
            for(Field f : fs){
                    try{
                        statement = connection.createStatement();
                    }
                    catch (SQLException e){
                            OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE,"# FATAL - NO STATEMENT"));
                            //e.printStackTrace();
                            isFullSuccess = false;
                    }
                    String sql = "";
                    try {
                        sql = f.get(null).toString();
                        isQuery = queries.contains(f.getName());
                        //System.out.println(f.getName()+" : "+isQuery);

                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE, ex.getLocalizedMessage()));
                    }

                    try{
                        if(shouldExecute){
                            result = executeSql(statement, isQuery, sql, f.getName());
                            OutputController.getControllerInstance().printMessage(new LogRecord(Level.INFO, "Executing: "+f.getName()));
                        }else{
                            break;
                        }
                        //System.out.println(result);
                    }catch (SQLException e){
                        OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE,"# FATAL - NOT A QUERY - "+f.getName()+" : "+e.getLocalizedMessage()));
                        //e.printStackTrace();
                        isFullSuccess = false;
                    } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(SqlUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                if(isQuery){
                        try{
                            metadata = result.getMetaData();
                            columnCount = metadata.getColumnCount();
                            if(printResult) System.out.print(" > ");
                            while (result.next()){
                                    rowCount++;
                                    if(printResult){
                                            for(int i=1; i<=columnCount; i++){
                                                    System.out.print(result.getString(i)+" ");
                                            }
                                            System.out.println();
                                    }
                            }
                            if(printRowCount) System.out.println("Row count: "+rowCount);
                            rowCount=0;		
                        }catch (SQLException e){
                                OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE, "# FATAL - RESULT: "+e.getLocalizedMessage()));
                                isFullSuccess = false;
                        }
                }


            }
        }else{
            OutputController.getControllerInstance().printMessage(new LogRecord(Level.SEVERE, "# FATAL - RESULT: Connection wasn't established properly!"));
            return !isFullSuccess;
        }
        DatabaseHelper.getHelperInstance().disconnect();
        return isFullSuccess;
  }	
  
    private static ResultSet executeSql(Statement statement, boolean query, String sql, String type) throws SQLException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ResultSet result = null;
        int tests = 0;
        
        Field obj = QueryConstants.class.getDeclaredField(type);
        
        if (!obj.isAnnotationPresent(Hidden.class)) {
            if(query){
                result = statement.executeQuery(sql);
            }else{
                if(type.equals("INSERT_TEST") || type.equals("DELETE_TEST") || type.equals("UPDATE_SELECT_RAND")){

                    while(tests < 1000){
                        switch(type){
                            case "INSERT_TEST":
                                sql = QueryConstants.INSERT_TEST;
                                sql += tests+","+tests+");";
                                break;
                            case "DELETE_TEST":
                                sql = QueryConstants.DELETE_TEST;
                                sql += tests+";";
                                break;
                            case "UPDATE_SELECT_RAND":
                                sql = QueryConstants.UPDATE_SELECT_RAND;
                                sql += tests+"';";
                                break;
                        }
                        statement.executeUpdate(sql);
                        tests++;
                    }
                }else{
                    statement.executeUpdate(sql);
                }
            }
        }
        return result;
    }
}
