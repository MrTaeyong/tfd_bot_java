package parser;

import java.util.HashMap;
import java.util.Map;

import parser.google.GooglePlaceConnector;
import parser.naver.NaverConnector;
import parser.twitter.TwitterConnector;

abstract public class Connector {
	public final static int NAVER_LOCAL = 0;
	public final static int NAVER_BLOG = 1;
	public final static int GOOGLE_PLACE = 2;
	public final static int TWITTER = 3;
	
	protected String _baseURL;
	protected int type;
	public static Connector getInstance(int type){
		Map<Integer, Connector> instanceMap = new HashMap<Integer, Connector>();
	
		// init instance
		instanceMap.put(NAVER_LOCAL, new NaverConnector(NAVER_LOCAL));
		instanceMap.put(NAVER_BLOG, new NaverConnector(NAVER_BLOG));
		instanceMap.put(GOOGLE_PLACE, new GooglePlaceConnector(GooglePlaceConnector.TEXT_SEARCH));
		instanceMap.put(TWITTER, new TwitterConnector());
		
		return instanceMap.get(type);
	}
	abstract public Object connect(String keyword);
}
