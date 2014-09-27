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

	/**
	 * 네이버 이미지 검색에서 이미지 리스트를 가져온다
	 * @param keyword 이미지 검색을 위한 키워드
	 * @return 성공 : 이미지 리스트, 실패 : null
	 */
	@Override
	public Object getResult(String keyword) {
		int start = 1, display = 1;
		NaverConnector connector;
		String xmlData;
		ArrayList<Map<String, String>> resultList = null;
		
		connector = (NaverConnector) Connector.getInstance(Connector.NAVER_IMAGE);
		
		// Receive all XML data possible and generate resulList.
		xmlData = (String)connector.connect(keyword, start, display);
		resultList = _getData(xmlData);
		
		// Return result list.
		if(resultList != null && resultList.size() > 0)
			return resultList;
		return null;
	}
	
	/**
	 * 네이버 이미지 검색 API의 결과인 XML에서 이미지 주소와 이미지 사이즈를 가져온다.
	 * @param xmlData 네이버 이미지 검색 API 결과 XML
	 * @return XML에 포함된 이미지의 정보를 반환
	 */
	private ArrayList<Map<String, String>> _getData(String xmlData){
		if(xmlData == null || xmlData.length() < 1)
			return null;
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
	
	/**
	 * 네이버 API에서 준 이미지 링크에서 필요한 부분을 추출
	 * @param link 네이버 API에서 제공한 이미지 링크
	 * @return
	 */
	private String _getImageLink(String link){
		int start = link.indexOf("ugc?q=") + 6;
		return link.substring(start);
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException{
		String keyword = "더부페";
		ArrayList<Map<String, String>> result;
		NaverSearch ns = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_IMAGE);
		result = (ArrayList<Map<String,String>>)ns.getResult(keyword);
		
		FileWriter fw = new FileWriter(new File("test.txt"));
		fw.write(keyword + "\n\n");
		for(Map<String, String> r : result){
			fw.write("link\t:" + r.get("link") + "\n");
			fw.write("width\t:" + r.get("width") + "\n");
			fw.write("height\t:" + r.get("height") + "\n\n");
		}
		fw.close();
	}

	@Override
	public Object getCurrentState() {
		// TODO Auto-generated method stub
		return null;
	}
}
