package parser.naver;

import java.util.HashMap;
import java.util.Map;

import parser.Connector;

public class NaverLocalConnector extends Connector{
	final private String _API_KEY = "a0504c8ef72934d16be6d2a29e5b69aa";
	final private String _LOCAL_BASE_URL = "http://openapi.naver.com/search?target=local";
	private String keyword;
	
	public NaverLocalConnector(){
		  
	}
	public Object connect() {
		// TODO Auto-generated method stub
		String url;
		Map<String, String> param = new HashMap<String, String>();
		
		return null;
	}
	@Override
	public Object connect(String keyword) {
		// TODO Auto-generated method stub
		return null;
	}	
}
