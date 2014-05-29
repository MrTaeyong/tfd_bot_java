package parser.naver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import parser.Connector;

public class NaverConnector extends Connector{
	final private String _API_KEY = "a0504c8ef72934d16be6d2a29e5b69aa";
	final private String _BASE_URL;
	private static int _start;
	private static int _display;
	
	public NaverConnector(int type){
		String[] target = {"local", "blog"};  
		_BASE_URL = "http://openapi.naver.com/search?target=" + target[type];
		_start = 1;
		_display = 100;
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
			
			// Modify static variable _start and _display for next step.
			_start += 100;
			if(_start == 901)
				_display = 99; // 901~999
			else if(_start == 1001){
                _start = 1000;
                _display = 100; // 1000~1099
			}
			else if(_start > 1100){
				_start = 1;
				_display = 100;
				return null;	// When reached limit of Naver API, null is returned.
			}
//			else if(_start == 201)
//				return null;
			
			// Convert data of BufferedReader type to String type
			String tmp;
			while((tmp = bufferedData.readLine()) != null)
				xmlData += tmp;
			return xmlData;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// When exception is occured null is returned.
		return null;
	}	
	
	private String _getParam(String keyword){
		return String.format("key=%s&query=%s&display=%d&start=%d",
				_API_KEY, keyword.replace(" ", "+"), _display, _start);
	}
}
