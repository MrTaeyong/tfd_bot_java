/**
 * @FileName	: NaverConnector.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 23.
 * @Author		: Taeyong
 */
package parser.naver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
	
	public NaverConnector(int type){
		Map<Integer, String> target = new HashMap<Integer, String>();
		target.put(Connector.NAVER_LOCAL, "local");
		target.put(Connector.NAVER_BLOG, "blog");
		target.put(Connector.NAVER_IMAGE, "image");
		_BASE_URL = "http://openapi.naver.com/search?target=" + target.get(type);
		_start = 1;
		_display = 100;
	}
	
	/**
	 * 네이버 API를 리퀘스트하여 결과를 얻는 메소드
	 * @param keyword 네이버 API에 사용할 키워드
	 * @return 네이버 API의 결과 XML을 String형태로 반환
	 */
	@Override
	public Object connect(String keyword) {
		// TODO Auto-generated method stub
		String completeUrl;
		try {
			// Initialize API URL.
			completeUrl = _BASE_URL + "&" + _getParam(keyword);
			 
			URL url = new URL(completeUrl);
			Document doc = Jsoup.parse(url, 10000);
			
			// Check range of contents
			Elements elements = doc.getElementsByTag("total");
			try{
				if(Integer.parseInt(elements.get(0).text()) < _start)
					return null;
			} catch(Exception exception){
				return null;
			}
			
			return doc.toString();
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
	
	/**
	 * keyword인자만 받는 connect함수에 start, display인자를 설정하고 싶을 때 사용할 수 있는 메소드
	 * @param keyword 네이버 API에 사용할 키워드
	 * @param start 네비어 API에서 검색 시작 번호
	 * @param display 네이버 API에서 화면표시 리스트 갯수
	 * @return 네이버 API의 결과 XML을 String형태로 반환
	 */
	public Object connect(String keyword, int start, int display){
		this._start = start;
		this._display = display;
		return connect(keyword);
	}
	
	/**
	 * 네이버 API를 리퀘스트할 주소를 만드는 메소드
	 * @param keyword
	 * @return
	 */
	private String _getParam(String keyword){
		return String.format("key=%s&query=%s&display=%d&start=%d",
				_API_KEY, keyword.replace(" ", "+"), _display, _start);
	}
	
	public void setRange(int start, int display){
		this._start = start;
		this._display = display;
	}
}
