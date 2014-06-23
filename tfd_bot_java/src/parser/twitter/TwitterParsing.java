/**
 * @FileName	: TwitterParsing.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 06. 19.
 * @Author		: Taeyong
 */
package parser.twitter;

/**
 * @Class	:TwitterParsing
 * @Date	: 2014. 06. 19.
 * @Author	: Taeyong
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TwitterParsing {
	public static void main(String[] args) {
		TwitterSearch ts = new TwitterSearch();
		ArrayList<String> result = (ArrayList<String>)ts.getResult("홍대");
		
		try {
			FileWriter bw = new FileWriter(new File("./result.txt"));
			for(String s : result){
				bw.write(s + "\n\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
