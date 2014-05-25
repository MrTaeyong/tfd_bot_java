/**
 * @FileName 	: GooglePlace.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 5. 23.
 * @Author 		: NCri
 */
package parser.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import parser.Connector;
import parser.ParserInterface;

/**
 * @Class		: GooglePlace
 * @Date 		: 2014. 5. 23.
 * @Author 		: NCri
 */
public class GooglePlace implements ParserInterface{
	/* (non-Javadoc)
	 * @see parser.ParserInterface#getResult()
	 */
	@Override
	public Object getResult(String keyword){
		// TODO Auto-generated method stub
		Connector conn = Connector.getInstance(Connector.GOOGLE_PLACE);
		BufferedReader jsonData = (BufferedReader) conn.connect(keyword);
		
		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonData);
			// Next Page Token을 가져온다.
			String nextPageToken = (String) jsonObject.get("next_page_token");
			System.out.println(nextPageToken);
			
			// 장소 정보 리스트를 가져온다.
			JSONArray array = (JSONArray) jsonObject.get("results");
			Iterator i = array.iterator();
			while (i.hasNext()) {
				JSONObject obj = (JSONObject) i.next();
				System.out.println(obj);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main(String[] args){
		GooglePlace gp = new GooglePlace();
		gp.getResult("홍대 식당");
	}
}
