/**
 * @FileName 	: Controller.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 5. 27.
 * @Author 		: NCri
 */
package controller;

/**
 * @Class		: Controller
 * @Date 		: 2014. 5. 27.
 * @Author 		: NCri
 */
abstract public class Controller {
	public static final int DATABASE = 0;
	
	public static Controller newInstance(int type){
		Controller instance = null; 
		
		if(type == Controller.DATABASE)
			instance = new DBController();
		
		return instance;
	}
}
