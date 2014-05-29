package parser.naver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parser.Connector;
import util.CoordinateConverter;
import util.GeoPoint;

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
	
	public static void main(String[] args){
//		NaverSearch ns = NaverSearch.getInstance(NAVER_LOCAL);
//		ArrayList<Map<String, String>> r = (ArrayList<Map<String, String>>) ns.getResult("홍대 식당");
//		for (Map<String, String> obj : r){
//			URL url;
//			try {
//				System.out.println(obj.get("url"));
//				url = new URL(obj.get("url"));
//				URLConnection connection = url.openConnection();
//				connection.connect();
//				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//				BufferedWriter bw = new BufferedWriter(new FileWriter("/Volumes/Macintosh/urltest.txt", true));
//				String tmp;
//				while((tmp = br.readLine()) != null){
//					bw.write(tmp + "\n");
//				}
//				bw.write("\n\n\n\n\n\n\n\n");
//				bw.close();
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
//		
//		for (Map<String, String> obj : r){
//			System.out.println(obj);
//		}
//		DBController dbcon = (DBController) Controller.newInstance(Controller.DATABASE);
//		dbcon.insertData("place_info_test", r);
//		for(Map<String, String> a : r){
//			System.out.println("title : " + a.get("title"));
//			System.out.println("category : " + a.get("category"));
//			System.out.println("telephone : " + a.get("telephone"));
//			System.out.println("address : " + a.get("address"));
//			System.out.println("description : " + a.get("description"));
//			System.out.println("mapx : " + a.get("mapx"));
//			System.out.println("mapy : " + a.get("mapy"));
//			System.out.println("link : " + a.get("link"));
//		}
	}
}
