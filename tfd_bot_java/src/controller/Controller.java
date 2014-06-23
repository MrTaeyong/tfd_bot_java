/**
 * @FileName 	: Controller.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 5. 27.
 * @Author 		: NCri
 */
package controller;

import java.util.ArrayList;
import java.util.Map;

/**
 * @Class		: Controller
 * @Date 		: 2014. 5. 27.
 * @Author 		: NCri
 */
abstract public class Controller {
	public static final int DATABASE = 0; // Instance Tag 상수.
	
	/**
	 * 
	 * @method Name	: newInstance
	 * @date   		: 2014. 5. 27. 
	 * @author   	: NCri
	 * @description :
	 * @param type
	 * @return
	 */
	public static Controller newInstance(int type){
		Controller instance = null; 
		
		if(type == Controller.DATABASE)
			instance = new DBController();
		
		return instance;
	}
	abstract public void insertData(String tableName, ArrayList<Map<String, String>> insertDatas);
	abstract public Map<String, String> getData(String query);	
}
