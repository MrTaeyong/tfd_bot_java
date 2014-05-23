/**
 * @FileName 	: StringUtil.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 5. 23.
 * @Author 		: NCri
 */
package util;

/**
 * @Class		: StringUtil
 * @Date 		: 2014. 5. 23.
 * @Author 		: NCri
 */
public class StringUtil {
	public static String join(String join, String... strings) {
	    if (strings == null || strings.length == 0) {
	        return "";
	    } else if (strings.length == 1) {
	        return strings[0];
	    } else {
	        StringBuilder sb = new StringBuilder();
	        sb.append(strings[0]);
	        for (int i = 1; i < strings.length; i++) {
	            sb.append(join).append(strings[i]);
	        }
	        return sb.toString();
	    }
	}
}
