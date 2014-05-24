package parser;

import java.util.HashMap;
import java.util.Map;

import parser.naver.NaverConnector;

abstract public class Connector {
	public final static int NAVER_LOCAL = 0;
	public final static int NAVER_BLOG = 1;
	public final static int GOOGLE_PLACE = 2;
	
	protected String _baseURL;
	protected int type;
	public static Connector getInstance(int type){
		Map<Integer, Connector> instanceMap = new HashMap<Integer, Connector>();
	
		// init instance
		instanceMap.put(NAVER_LOCAL, new NaverConnector(NAVER_LOCAL));
		instanceMap.put(NAVER_BLOG, new NaverConnector(NAVER_BLOG));
		instanceMap.put(GOOGLE_PLACE, new GooglePlaceConnector());
		
		return instanceMap.get(type);
	}
	abstract public Object connect(String keyword);
}
