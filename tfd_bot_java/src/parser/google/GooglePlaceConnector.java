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
	private String _keyword;
	
	private ArrayList<String> _functions;
	private ArrayList<String> _urlFormats;
	
	private int _type = -1;

	public final static int TEXT_SEARCH = 0;
	// DETAIL 검색과 사진 정보는 TEXT_SEARCH를 한 후 Reference Token
	// 을 통해서 이뤄지기 때문에 TEXT_SEARCH 가 Public으로 해두는 것이 맞다.
	private final static int DETAIL_SEARCH = 1;
	private final static int INFO_PHOTO = 2;
	

	public GooglePlaceConnector(int type){
		_type = type;

		// init api function 
		_functions = new ArrayList<String>();
		_functions.add("textsearch");
		_functions.add("details");
		_functions.add("photo");
		
		// init url
		_urlFormats = new ArrayList<String>();
		_urlFormats.add("https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&sensor=true&language=ko&key=%s");
		_urlFormats.add("https://maps.googleapis.com/maps/api/place/details/json?reference=%s&sensor=true&language=ko&key=%s");
		_urlFormats.add("https://maps.googleapis.com/maps/api/place/photo?maxwidth=%s&photoreference=%s&sensor=true&language=ko&key=%s");
		
	}
	/* (non-Javadoc)
	 * @see parser.Connector#connect(java.lang.String)
	 */
	@Override
	public Object connect(String keyword) {
		// TODO Auto-generated method stub
		
		_keyword = keyword.replace(" ", "+");
		
		try {
			/*
			 * https://maps.googleapis.com/maps/api/place/textsearch/json?query=""&sensor=true&language=ko&key=""
			 * https://maps.googleapis.com/maps/api/place/details/json?reference=""&sensor=true&language=ko&key=""
			 * https://maps.googleapis.com/maps/api/place/photo?maxwidth=""&photoreference=""&sensor=true&language=ko&key=""
			 */
			
			// GooglePlace API 호출 주소를 초기화.
			String urlFormat = _urlFormats.get(_type);
			URL url = new URL(String.format(urlFormat, _keyword, _API_KEY));
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
	
	protected Map jsonToMap(BufferedReader json){
		return null;
	}

}
