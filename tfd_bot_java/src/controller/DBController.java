/**
 * @FileName 	: DBController.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 5. 27.
 * @Author 		: NCri
 */
package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @Class : DBController
 * @Date : 2014. 5. 27.
 * @Author : NCri
 */
public abstract class DBController{
	public static enum Type{
		TFD
	}
	public static DBController newInstance(Type db){
		DBController instance = null; 
		
		if(db == DBController.Type.TFD)
			instance = new TFDDBController();
		
		return instance;
	}
	
	abstract public void insertData(String tableName, List<Map<String, String>> insertDatas);
//	abstract public Map<String, String> getData(String query);
	abstract public List<Map<String, String>> getData(String query) throws com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
	abstract public void queryExecute(String query);
}
