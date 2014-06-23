package parser.naver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parser.Connector;

public class NaverImage extends NaverSearch {
	public NaverImage(){
		super();
	}

	@Override
	public Object getResult(String keyword) {
		// TODO Auto-generated method stub
		Connector connector;
		String xmlData;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		
		connector = Connector.getInstance(Connector.NAVER_IMAGE);
		((NaverConnector)connector).setStart(1);
		((NaverConnector)connector).setDisplay(5);
		
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
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		Map<String, String> resultMap;
		Document doc = Jsoup.parse(xmlData);
		Elements elements = doc.getElementsByTag("item");
		for(Element e : elements){
			resultMap = new HashMap<String, String>();
			resultMap.put("link", _getImageLink(e.getElementsByTag("thumbnail").text()));
			resultMap.put("width", e.getElementsByTag("sizewidth").text());
			resultMap.put("height", e.getElementsByTag("sizeheight").text());
			resultList.add(resultMap);
		}
		return resultList;
	}
	
	private String _getImageLink(String link){
		int start = link.indexOf("ugc?q=") + 6;
		return link.substring(start);
	}
	
	public static void main(String[] args) throws IOException{
		ArrayList<Map<String, String>> result;
		NaverSearch ns = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_IMAGE);
		result = (ArrayList<Map<String,String>>)ns.getResult("구이가 홍대");
		
		FileWriter fw = new FileWriter(new File("test.txt"));
		fw.write("구이가 홍대\n\n");
		for(Map<String, String> r : result){
			fw.write("link\t:" + r.get("link") + "\n");
			fw.write("width\t:" + r.get("width") + "\n");
			fw.write("height\t:" + r.get("height") + "\n\n");
		}
		fw.close();
	}
}
