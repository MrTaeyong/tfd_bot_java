/**
 * @FileName	: NaverLocal.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 24.
 * @Author		: Taeyong
 */
package parser.naver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parser.Connector;
import controller.Controller;
import controller.DBController;

/**
 * @Class	: NaverLocal
 * @Date	: 2014. 05. 24. 
 * @Author	: Taeyong
 */
class NaverLocal extends NaverSearch{	
	public NaverLocal(){
		super();
	}
	
	public Object getResult(String keyword) {
		Connector connector;
		String xmlData;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		 
		connector = Connector.getInstance(Connector.NAVER_LOCAL);
		
		// Receive all XML data possible and generate resulList.
		xmlData = (String)connector.connect(keyword);
		while(xmlData != null){
			resultList.addAll(_getData(xmlData));
			xmlData = (String)connector.connect(keyword);
		}
		
		// Return result list.
		if(resultList.size() > 0)
			return resultList;
		return null;
	}
	
	private ArrayList<Map<String, String>> _getData(String xmlData){
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		Map<String, String> resultMap;
		Document doc = Jsoup.parse(xmlData);
		Elements elements = doc.getElementsByTag("item");
		for(Element e : elements){
			resultMap = new HashMap<String, String>();
			resultMap.put("name", e.getElementsByTag("title").text().replaceAll("(<b>|</b>)", ""));
//			resultMap.put("title", e.getElementsByTag("title").text());
			resultMap.put("category", e.getElementsByTag("category").text());
			resultMap.put("telephone", e.getElementsByTag("telephone").text());
			resultMap.put("address", e.getElementsByTag("address").text());
			resultMap.put("description", e.getElementsByTag("description").text().replaceAll("(<b>|</b>)", ""));
//			resultMap.put("description", e.getElementsByTag("description").text());
			resultMap.put("url", _getLink(e.toString()));
//			resultMap.put("pointX", e.getElementsByTag("mapx").text());
//			resultMap.put("pointY", e.getElementsByTag("mapy").text());
			resultList.add(resultMap);
		}
		return resultList;
	}
	
	private String _getLink(String item){
		int start, end;
		start = item.indexOf("<link />") + 8;
		end = item.indexOf("<category>");
		item = item.substring(start, end);
		return item;
	}
	
	public static void main(String[] args) throws IOException{
		NaverSearch ns = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_LOCAL);
		ArrayList<Map<String, String>> r = (ArrayList<Map<String, String>>) ns.getResult("문래동 식당");
		DBController dbcon = (DBController) Controller.newInstance(Controller.DATABASE);
		dbcon.insertData("place_info_test", r);
	}
}
