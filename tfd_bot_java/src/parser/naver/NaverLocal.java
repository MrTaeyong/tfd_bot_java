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
import util.CoordinatesConverter;
import util.GeoPoint;
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
		int start = 1, display = 100;
		NaverConnector connector;
		String xmlData;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		 
		connector = (NaverConnector) Connector.getInstance(Connector.NAVER_LOCAL);
		
		// Receive all XML data possible and generate resulList.
//		xmlData = (String)connector.connect(keyword);
		while(true){
			xmlData = (String)connector.connect(keyword, start, display);
			if(xmlData == null)
				break;
			resultList.addAll(_getData(xmlData));
			start += 100;
			if(start == 901)
				display = 99; // 901~999
			else if(start == 1001){
	            start = 1000;
	            display = 100; // 1000~1099
			}
		}
		
		// Return result list.
		if(resultList != null && resultList.size() > 0)
			return resultList;
		return null;
	}
	
	private ArrayList<Map<String, String>> _getData(String xmlData){
		if(xmlData == null || xmlData.length() < 1)
			return null;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		Map<String, String> resultMap;
		Document doc = Jsoup.parse(xmlData);
		Elements elements = doc.getElementsByTag("item");
		for(Element e : elements){
			resultMap = new HashMap<String, String>();
			resultMap.put("name", e.getElementsByTag("title").text().replaceAll("(<b>|</b>)", ""));
			resultMap.put("category", e.getElementsByTag("category").text());
			resultMap.put("telephone", e.getElementsByTag("telephone").text());
			resultMap.put("address", e.getElementsByTag("address").text());
			resultMap.put("description", e.getElementsByTag("description").text().replaceAll("(<b>|</b>)", ""));
			resultMap.put("url", _getLink(e.toString()));
			GeoPoint gp = CoordinatesConverter.katechToWgs84(Integer.parseInt(e.getElementsByTag("mapx").text()), Integer.parseInt(e.getElementsByTag("mapy").text()));
			resultMap.put("pointx", String.format("%f", gp.getX())); 
			resultMap.put("pointy", String.format("%f", gp.getY()));
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
		ArrayList<Map<String, String>> r = (ArrayList<Map<String, String>>) ns.getResult("홍대 공원");
		DBController dbcon = DBController.newInstance(DBController.Type.TFD);
		dbcon.insertData("place_info_test", r);
	}
}
