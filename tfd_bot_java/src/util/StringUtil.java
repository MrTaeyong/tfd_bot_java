/**
 * @FileName 	: StringUtil.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 5. 23.
 * @Author 		: NCri
 */
package util;

import java.util.List;

/**
 * @Class		: StringUtil
 * @Date 		: 2014. 5. 23.
 * @Author 		: NCri
 */
public class StringUtil {
	/**
	 * 
	 * @method Name	: join
	 * @date   		: 2014. 5. 23. 
	 * @author   	: NCri
	 * @description :
	 * @param join
	 * @param strings
	 * @return
	 * @example
	 * 			String joined;
	 * 			joined = join(" and ","7, 7, 7", "Bill", "Bob", "Steve", "[Bill]", "1,2,3", "Apple ][","~,~");
	 * 			joined = join(" and ", array);
	 */
	public static String join(List<String> list, String delim) {
	    StringBuilder sb = new StringBuilder();

	    String loopDelim = "";

	    for(String s : list) {

	        sb.append(loopDelim);
	        sb.append(s);            

	        loopDelim = delim;
	    }

	    return sb.toString();
	}
}
