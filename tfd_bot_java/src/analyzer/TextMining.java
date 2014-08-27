/**
 * @FileName 	: TextMining.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 6. 26.
 * @Author 		: NCri
 */
package analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Class		: TextMining
 * @Date 		: 2014. 6. 26.
 * @Author 		: NCri
 */
abstract public class TextMining {
	static {
		System.load(System.getProperty("java.library.path") + "/libMeCab.so");
//		System.loadLibrary("libMeCab.so");
//		System.out.println(System.getProperty("java.library.path"));
	}
	
	public static enum Type{
		BLOG
	}
	public static TextMining newInstance(Type t){
		TextMining instance = null;
		if(t == Type.BLOG)
			instance = new Blog();
		return instance;
	}
	
	abstract Map<String, Integer> wordCount(String text);
	abstract Map<String, Integer> wordCount(ArrayList<String> list);
	
	
}
