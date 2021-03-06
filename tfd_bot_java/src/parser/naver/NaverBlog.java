/**
 * @FileName	: NaverBlog.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 25.
 * @Author		: Taeyong
 */
package parser.naver;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parser.Connector;

/**
 * @Class	: NaverBlog
 * @Date	: 2014. 05. 25. 
 * @Author	: Taeyong
 */
class NaverBlog extends NaverSearch{
	private int start;
	private int display;
//	private String nextXmlData; 
	
//	private WebClient webClient = new WebClient(); //JavaScript 파싱을 위한 객체
	
	public NaverBlog(){
		super();
		start = 1;
		display = 100;
	}

	/**
	 * 한번 호출될 때마다 최대 100개씩 블로그 리스트를 가져온다
	 * @param keyword 블로그 검색을 위한 키워드
	 * @return 성공 : 블로그 리스트, 실패 : null
	 */
	public Object getResult(String keyword) {
		NaverConnector connector;
		String xmlData;
		 
		connector = (NaverConnector) Connector.getInstance(Connector.NAVER_BLOG);
		
//		if(start == 1)
			xmlData = (String)connector.connect(keyword, start, display);
//		else
//			xmlData = nextXmlData;
		
		start += 100;
		if(start == 901)
			display = 99; // 901~999
		else if(start == 1001){
            start = 1000;
            display = 100; // 1000~1099
		}
		
		if(xmlData == null)
			return null;
		
//		nextXmlData = (String)connector.connect(keyword, start, display);
		
		return _getData(xmlData);
	}
	
	/**
	 * 블로그 리스트가 담긴 xmlData를 통해 기본정보를 파싱하고, 기본정보에서 블로그의 주소를 파싱하여 추가 내용을 파싱한다
	 * @param xmlData
	 * @return 제목, 블로거, 블로그 본문, 작성일, 블로그의 주소, 이미지 리스트, 방문자수, 공감수를 반환
	 */
	private ArrayList<Map<String, String>> _getData(String xmlData){
		int success = 0, fail = 0;
		if(xmlData == null || xmlData.length() < 1)
			return null;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		Map<String, String> resultMap;
		Map<String, String> blogContent;
		String title, writer, link;
		Document doc = Jsoup.parse(xmlData);
		Elements elements = doc.getElementsByTag("item");
		
		for(Element e : elements){
			title = e.getElementsByTag("title").text().replaceAll("(<b>|</b>)", "");
			writer = e.getElementsByTag("bloggername").text();
			
			try{
				link = _getLink(e.toString()); // 블로그의 주소를 
			}catch (StringIndexOutOfBoundsException exception){
				fail++;
				continue;
			}
			
			blogContent = _getBlogContent(link); // 블로그에 직접 접속하여 필요한 정보를 파싱
			if(blogContent == null){
				fail++;
				continue;
			}
			success++;
			
			resultMap = new HashMap<String, String>();
			resultMap.putAll(blogContent);
			resultMap.put(FieldName.TITLE.value, title);
			resultMap.put(FieldName.BLOGGER_NAME.value, writer);
			resultList.add(resultMap);
			blogContent = null;
			resultMap = null;
		}
		
		System.out.printf("Success : %d, Fail : %d\n", success, fail);
		if(resultList.size() <= 0)
			resultList = null;
		return resultList;
	}
	
	/**
	 * 네이버 블로그의 주소를 받아 세부적인 정보를 파싱한다
	 * @param naverAPIUrl
	 * @return 블로그 작성일, 블로그 본문, 이미지 리스트, 방문자수, 공감수, 블로그 주소를 반환
	 */
	private Map<String, String> _getBlogContent(String naverAPIUrl){
		Document doc;
		String src, logNo;
		Map<String, String> result = new HashMap<String, String>();
		int start, end;
		
		try {
			// Jsoup의 커넥터를 이용하면 네이버API에서 주는 주소를 인식 못해 오류발생..
			URL url = new URL(naverAPIUrl);
			doc = Jsoup.parse(url, 5000);
			
			// mainFrame에 포함되어 있는 주소를 추출
			src = doc.getElementById("mainFrame").toString().replace("&amp;", "&");
			start = src.indexOf("src=") + 5;
			end = src.indexOf("&beginTime");
			src = src.substring(start, end);
			src = "http://blog.naver.com" + src;
			start = src.indexOf("logNo=") + 6;
			logNo = src.substring(start); // Parse post number
			
			// URL의 커넥터를 사용하면 Jsoup에서 본문내용을 파싱하지 못함...
			doc = Jsoup.connect(src).get();
			result.put(FieldName.BLOG_DATE.value, doc.getElementsByClass("_postAddDate").text().trim().replaceAll("/", "-"));
			Element el = doc.getElementById("post-view" + logNo);
			String content = el.text().trim() + "\n" + _getAddressInBlog(el);
			result.put(FieldName.BLOG_CONTENT.value, content);
//			result.put(FieldName.BLOG_CONTENT.value, doc.getElementById("post-view" + logNo).text().trim());
			result.put(FieldName.BLOG_IMAGES.value, _getImageLinkOfBlog(logNo, doc));
			result.put(FieldName.BLOG_TODAY_COUNT.value, String.valueOf(_getBlogTodayCount(doc, src)));
			result.put(FieldName.BLOG_SYMPATHY_COUNT.value, String.valueOf(_getBlogSympathyCount(doc, logNo)));
			result.put(FieldName.BLOG_LINK.value, src);
			
			if(result.size() <= 0)
				result = null;
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
//			System.out.println("It is not naver blog.");
		} catch (IndexOutOfBoundsException e) {
//			System.out.println("Date is not exists");
		}
		return null;
	}
	
	/**
	 * 블로그에서 위젯에 포함된 방문자 수를 파싱한다
	 * @param blogDoc 블로그 페이지의 Document
	 * @param blogUrl 블로그 주소
	 * @return 성공 : 방문자수, 실패 : 0
	 */
	private int _getBlogTodayCount(Document blogDoc, String blogUrl) {
		return 0;
//		try {
//			Element element = blogDoc.getElementById("blog-counter");
//			element.text();
//			HtmlPage page = webClient.getPage(blogUrl);
//			DomElement visit = page.getElementById("blog-counter");
//			String[] count = visit.asText().split("\n"); 
//			return Integer.parseInt(count[0].replaceAll(",", ""));
//		} catch (Exception e) {
//			return 0;
//		} finally {
//			webClient.closeAllWindows();
//		}
		
	}
	
	/**
	 * 블로그에서 블로그 공감 갯수를 파싱한다.
	 * @param blogDoc 블로그 페이지 Document
	 * @param logNo 네이버 블로그에서 블로그의 번호
	 * @return 성공 : 공감 수, 실패 : 0
	 */
	private int _getBlogSympathyCount(Document blogDoc, String logNo) {
		try {
			return Integer.parseInt(blogDoc.getElementById("Sympathy" + logNo).text().substring(3));
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * 블로그에 포함된 네이버 지도에서 주소를 파싱한다
	 * @param element 네이버 지도가 포함되어있는 Element
	 * @return 성공 : 파싱된 주소, 실패 : 공백
	 */
	private String _getAddressInBlog(Element element) {
		try {
			Elements elements = element.getElementsByTag("iframe");
			String mapUrl = "";
			
			// 지도가 포함된 <iframe> 태그를 찾음
			for(int i = 0; i < elements.size(); i++) {
				if(elements.get(i).attr("title").equals("포스트에 첨부된 지도"))
					mapUrl =  elements.get(i).attr("src");
			}
			if(mapUrl.equals(""))	//지도가 없는 블로그일 때 공백 반환
				return "";
			
			// 네이버 지도로 접속하여 지도에 포함된 주소를 파싱
			URL url = new URL(mapUrl);
			Document doc = Jsoup.parse(url, 5000);
			String javascript = doc.toString();
			int start = javascript.indexOf("address")+12;
			int end = javascript.indexOf(",", start)-2;
			return javascript.substring(start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 블로그에 포함된 이미지 중 유의미한 이미지를 파싱한다
	 * @param logNo 네이버 블로그에서 블로그 번호
	 * @param doc 이미지가 포함되어있는 블로그의 Document
	 * @return 이미지 주소 리스트
	 */
	private String _getImageLinkOfBlog(String logNo, Document doc){
		String result = "";
		// 본문에서 img태그를 모두 파싱한 후 "_photoImage" class 속성을 포함한 이미지의 URL을 파싱
		Elements imageLinks = doc.getElementById("post-view" + logNo).getElementsByTag("img");
		for(Element image : imageLinks)
			if(image.attr("class").equals("_photoImage"))
				result += image.attr("id") + "\t";
		
		return result;
	}
	
	/**
	 * 네이버 API를 통해 얻어온 XML에서 블로그 주소를 파싱 (JSoup이 파싱하지 못함..)
	 * @param item 네이버 API에서 넘겨주는 XML에서 <item>태그에 포함된 내용
	 * @return 블로그 주소
	 * @throws StringIndexOutOfBoundsException
	 */
	private String _getLink(String item) throws StringIndexOutOfBoundsException {
		int start, end;
		start = item.indexOf("<link />");
		end = item.indexOf("<description>");
		item = item.substring(start+8, end);
		return item;
	}

	/**
	 * 현재 진행 중인 start번호를 반환
	 */
	@Override
	public Object getCurrentState() {
		return start;
	}
}
