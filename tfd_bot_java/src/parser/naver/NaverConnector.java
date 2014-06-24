/**
 * @FileName	: NaverConnector.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 23.
 * @Author		: Taeyong
 */
package parser.naver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import parser.Connector;

/**
 * @Class	: NaverConnector
 * @Date	: 2014. 05. 23. 
 * @Author	: Taeyong
 */
public class NaverConnector extends Connector{
	final private String _API_KEY = "a0504c8ef72934d16be6d2a29e5b69aa";
	final private String _BASE_URL;
	private static int _start;
	private static int _display;
//	private static boolean _isMaximum;
	
	public NaverConnector(int type){
		Map<Integer, String> target = new HashMap<Integer, String>();
		target.put(Connector.NAVER_LOCAL, "local");
		target.put(Connector.NAVER_BLOG, "blog");
		target.put(Connector.NAVER_IMAGE, "image");
		_BASE_URL = "http://openapi.naver.com/search?target=" + target.get(type);
		_start = 1;
		_display = 100;
//		_isMaximum = true;
	}
	
	@Override
	public Object connect(String keyword) {
		// TODO Auto-generated method stub
		String completeUrl;
		String xmlData = "";
		try {
			// Initialize API URL.
			 completeUrl = _BASE_URL + "&" + _getParam(keyword);
			 
			// Get XML data from naver search API.
			URL url = new URL(completeUrl);
			URLConnection connection = url.openConnection();
			BufferedReader bufferedData = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			// Convert data of BufferedReader type to String type
			String tmp;
			while((tmp = bufferedData.readLine()) != null)
				xmlData += tmp;
			if(xmlData.indexOf("error xmlns") != -1)
				return null;
			return xmlData;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("MalformedURLException is occured");
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("IOException is occured");
		}
		// When exception is occured null is returned.
		return null;
	}
	
	public Object connect(String keyword, int start, int display){
		this._start = start;
		this._display = display;
		return connect(keyword);
	}
	
	private String _getParam(String keyword){
		return String.format("key=%s&query=%s&display=%d&start=%d",
				_API_KEY, keyword.replace(" ", "+"), _display, _start);
	}
	
	public void setRange(int start, int display){
		this._start = start;
		this._display = display;
//		this._isMaximum = false;
	}
}
