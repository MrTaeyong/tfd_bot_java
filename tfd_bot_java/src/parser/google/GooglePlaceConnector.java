/**
 * @FileName 	: GoogleConnector.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 5. 23.
 * @Author 		: NCri
 */
package parser.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

import parser.Connector;
import util.StringUtil;

/**
 * @Class		: GoogleConnector
 * @Date 		: 2014. 5. 23.
 * @Author 		: NCri
 */

public class GooglePlaceConnector extends Connector{
	private final String _API_KEY = "AIzaSyCJQXicui49gzhAwbGWGmm3OJEpKPjUeJE";
	private final String _BASE_URL = "https://maps.googleapis.com/maps/api/place";	
 
	private String _returnType = "json";
	private String _language = "ko";
	private String _sensor = "true";
	private ArrayList<String> _function;
	private String _keyword;
	
	private int _type = -1;

	public final int PLACE = 0;

	public GooglePlaceConnector(int type){
		_type = type;

		// init api function 
		_function = new ArrayList<String>();
		_function.add("textsearch");
		_function.add("details");
		_function.add("photo");
	}
	/* (non-Javadoc)
	 * @see parser.Connector#connect(java.lang.String)
	 */
	@Override
	public Object connect(String keyword) {
		// TODO Auto-generated method stub
		
		_keyword = keyword;
		
		try {
			// GooglePlace API 호출 주소를 초기화.
			URL url = _getURL();
			URLConnection connection = url.openConnection();
			connection.connect();

			// GooglePlace API Query에서 전달받은
			// JSON데이터를 맴버에 저장.
			BufferedReader jsonData = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			return jsonData;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	protected URL _getURL(){
		/*
		 * https://maps.googleapis.com/maps/api/place/textsearch/json?query=""&sensor=true&language=ko&key=""
		 * https://maps.googleapis.com/maps/api/place/details/json?reference=""&sensor=true&language=ko&key=""
		 * https://maps.googleapis.com/maps/api/place/photo?maxwidth=""&photoreference=""&sensor=true&language=ko&key=""
		 */
		String spec = _BASE_URL; 
		return null;
	}
	
	protected Map jsonToMap(BufferedReader json){
		return null;
	}

}
