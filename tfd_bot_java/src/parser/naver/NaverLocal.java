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
	private int start, display;
	private String nextXmlData;
	
	public NaverLocal(){
		super();
		start = 1;
		display = 100;
	}
	
	/**
	 * 한번 호출될 때마다 최대 100개씩 장소 리스트를 가져온다
	 * @param keyword 장소 검색을 위한 키워드
	 * @return 성공 : 장소 리스트, 실패 : null
	 */
	public Object getResult(String keyword) {
		NaverConnector connector;
		String xmlData;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		 
		connector = (NaverConnector) Connector.getInstance(Connector.NAVER_LOCAL);
		
//		if(start == 1)
			xmlData = (String)connector.connect(keyword);
//		else
//			xmlData = nextXmlData;
		
		xmlData = (String)connector.connect(keyword, start, display);

		start += 100;
		if(start == 901)
			display = 99; // 901~999
		else if(start == 1001){
            start = 1000;
            display = 100; // 1000~1099
		}
		
		if(xmlData == null)
			return null;
		
//		nextXmlData = (String)connector.connect(keyword);
		
		return _getData(xmlData);
	}
	
	/**
	 * 네이버 API의 결과 XML로 부터 필요한 블로그 정보를 모두 파싱
	 * @param xmlData 네이버 API에서 제공된 장소 XML
	 * @return
	 */
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
			resultMap.put("pointx", String.format("%.8f", gp.getX()));
			resultMap.put("pointy", String.format("%.8f", gp.getY()));
			resultMap.put("image_url", "");
			resultList.add(resultMap);
		}
		return resultList;
	}
	
	/**
	 * 네이버 API 결과 XML에서 장소 홈페이지 URL을 추출
	 * @param item 네이버 API에서 제공된 XML의 <item>태그 내용
	 * @return 추출된 URL
	 */
	private String _getLink(String item){
		int start, end;
		start = item.indexOf("<link />") + 8;
		end = item.indexOf("<category>");
		item = item.substring(start, end);
		return item;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException{
		ArrayList<Map<String, String>> query = new ArrayList<Map<String, String>>();
		NaverSearch ns = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_LOCAL);
		ArrayList<Map<String, String>> r = (ArrayList<Map<String, String>>) ns.getResult("홍대 관람");
		Map<String, String> temp;
		for(Map<String, String> elem : r){
			temp = new HashMap<String, String>();
			temp.put("sub_group", elem.get("category"));
			temp.put("c_group", "");
			query.add(temp);
			temp.clear();
		}
		DBController dbcon = DBController.newInstance(DBController.Type.TFD);
		dbcon.insertData("category", query);
		dbcon.insertData("place_info", r);
	}

	@Override
	public Object getCurrentState() {
		// TODO Auto-generated method stub
		return null;
	}
}
