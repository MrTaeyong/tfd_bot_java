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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @Class	: NaverBlog
 * @Date	: 2014. 05. 25. 
 * @Author	: Taeyong
 */
class NaverBlog extends NaverSearch{
	private int start;
	private int display;
	private String nextXmlData; 
	
	private WebClient webClient = new WebClient();
	public static enum FieldName {
		TITLE("title"), BLOG_CONTENT("blogContent"), BLOGGER_NAME("bloggerName"),
		BLOG_LINK("bloggerLink"), BLOG_DATE("date"), BLOG_IMAGES("blogImage"),
		PLACE_NAME("place_name"), PLACE_IMAGE("link"), BLOG_TODAY_COUNT("today_count"),
		BLOG_SYMPATHY_COUNT("sympathy_count");
		String value;
		FieldName(String value){
			this.value = value;
		}
		public String getValue(){
			return value;
		}
	}
	
	public static enum CategoryName {
		CAFFE("카페"), RESTAURANT("음식점"), CULTURE("문화,예술"), PUB("술집");
		String value;
		CategoryName(String value){
			this.value = value;
		}
	}
	
	public NaverBlog(){
		super();
		start = 1;
		display = 100;
	}

	public Object getResult(String keyword) {
		NaverConnector connector;
		String xmlData;
		 
		connector = (NaverConnector) Connector.getInstance(Connector.NAVER_BLOG);
		
		// Receive XML data possible and generate resulList.
		if(start == 1)
			xmlData = (String)connector.connect(keyword, start, display);
		else
			xmlData = nextXmlData;
		
		start += 100;
		if(start == 901)
			display = 99; // 901~999
		else if(start == 1001){
            start = 1000;
            display = 100; // 1000~1099
		}
		
		if(xmlData == null)
			return null;
		
		nextXmlData = (String)connector.connect(keyword, start, display);
		
		return _getData(xmlData);
	}
		
	private ArrayList<Map<String, String>> _getData(String xmlData){
		int success = 0, fail = 0;
		if(xmlData == null || xmlData.length() < 1)
			return null;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		Map<String, String> resultMap;
		Document doc = Jsoup.parse(xmlData);
		Elements elements = doc.getElementsByTag("item");
		
		for(Element e : elements){
			String title = e.getElementsByTag("title").text().replaceAll("(<b>|</b>)", "");
			String writer = e.getElementsByTag("bloggername").text();
			
			String link;
			try{
				link = _getLink(e.toString());
			}catch (StringIndexOutOfBoundsException exception){
				fail++;
				continue;
			}
			
			Map<String, String> blogContent = _getBlogContent(link);
			if(blogContent == null){
				fail++;
				continue;
			}
			success++;
			resultMap = new HashMap<String, String>();
			resultMap.putAll(blogContent); // Add blog content
			resultMap.put(FieldName.TITLE.value, title); // Add title
			resultMap.put(FieldName.BLOGGER_NAME.value, writer); // Add blogger name
			resultList.add(resultMap);
			blogContent = null;
			resultMap = null;
		}
		
		System.out.printf("Success : %d, Fail : %d\n", success, fail);
		if(resultList.size() <= 0)
			resultList = null;
		return resultList;
	}
	
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
	
	private int _getBlogTodayCount(Document blogDoc, String blogUrl) {
		try {
			Element element = blogDoc.getElementById("blog-counter");
			element.text();
//			WebClient webClient = new WebClient();
			HtmlPage page = webClient.getPage(blogUrl);
			DomElement visit = page.getElementById("blog-counter");
			String[] count = visit.asText().split("\n"); 
			return Integer.parseInt(count[0].replaceAll(",", ""));
		} catch (Exception e) {
			return 0;
		} finally {
			webClient.closeAllWindows();
		}
		
	}
	
	private int _getBlogSympathyCount(Document blogDoc, String logNo) {
		try {
			return Integer.parseInt(blogDoc.getElementById("Sympathy" + logNo).text().substring(3));
		} catch (Exception e) {
			return 0;
		}
	}
	
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
	
	private String _getImageLinkOfBlog(String logNo, Document doc){
		String result = "";
		Elements imageLinks = doc.getElementById("post-view" + logNo).getElementsByTag("img");
		for(Element image : imageLinks)
			if(image.attr("class").equals("_photoImage"))
				result += image.attr("id") + "\t";
		
		return result;
	}
	
	private String _getLink(String item) throws StringIndexOutOfBoundsException {
		int start, end;
		start = item.indexOf("<link />");
		end = item.indexOf("<description>");
		item = item.substring(start+8, end);
		return item;
	}
	
	public boolean hasNext() {
		if(nextXmlData == null && start != 1)
			return false;
		return true;
	}

	@Override
	public Object getCurrentState() {
		return start;
	}
}
