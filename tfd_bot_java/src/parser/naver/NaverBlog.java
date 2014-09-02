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
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parser.Connector;
import controller.DBController;

/**
 * @Class	: NaverBlog
 * @Date	: 2014. 05. 25. 
 * @Author	: Taeyong
 */
class NaverBlog extends NaverSearch{
	private static final DBController _dbcon = DBController.newInstance(DBController.Type.TFD);
	public static enum FieldName {
		TITLE("title"), BLOG_CONTENT("blogContent"), BLOGGER_NAME("bloggerName"),
		BLOGGER_LINK("bloggerLink"), BLOG_DATE("date"), BLOG_IMAGES("blogImage"),
		PLACE_NAME("place_name"), PLACE_IMAGE("link");
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
	}

	public Object getResult(String keyword) {
		int start = 1, display = 100;
		NaverConnector connector;
		String xmlData;
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		 
		connector = (NaverConnector) Connector.getInstance(Connector.NAVER_BLOG);
		
		// Receive all XML data possible and generate resulList.
		while(true){
			xmlData = (String)connector.connect(keyword, start, display);
			if(xmlData == null)
				break;
			List<Map<String, String>> tempResult = _getData(xmlData);
			if(tempResult == null)
				break;
			resultList.addAll(tempResult);
			start += 100;
			if(start == 901)
				display = 99; // 901~999
			else if(start == 1001){
	            start = 1000;
	            display = 100; // 1000~1099
			}
			else if(start >= 1100)
				break;
		}
		
		// Return result list.
		if(resultList.size() > 0)
			return resultList;
		return null;
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
			// 블로그 중복 검사
			String title = e.getElementsByTag("title").text().replaceAll("(<b>|</b>)", "");
			String writer = e.getElementsByTag("bloggername").text();
//			List<Map<String, String>> duplicationCheck = _dbcon.getData("select title from blog_test where title=\"" + title + "\" and writer=\"" + writer + "\"");
//			if(duplicationCheck != null){
//				fail++;
//				continue;	// 중복되면 다음 블로그로..
//			}
			
			String link;
			try{
				link = _getLink(e.toString());
			}catch (StringIndexOutOfBoundsException exception){
				fail++;
				continue;
			}
			
			Map<String, String> dateAndContent = _getBlogContent(link);
			if(dateAndContent == null){
				fail++;
				continue;
			}
			success++;
			resultMap = new HashMap<String, String>();
			resultMap.putAll(dateAndContent); // Add blog content and date
			resultMap.put(FieldName.TITLE.value, title); // Add title
			resultMap.put(FieldName.BLOGGER_NAME.value, writer); // Add blogger name
			resultMap.put(FieldName.BLOGGER_LINK.value, e.getElementsByTag("bloggerlink").text()); // Add blog link
			resultList.add(resultMap);
		}
		
		System.out.printf("Success : %d, Fail : %d\n", success, fail);
		if(resultList.size() > 0)
			return resultList;
		return null;
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
			
			// Extracting the address contained in the mainFrame
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
			result.put(FieldName.BLOG_CONTENT.value, doc.getElementById("post-view" + logNo).text().trim());
			result.put(FieldName.BLOG_IMAGES.value, _getImageLinkOfBlog(logNo, doc));
			
			if(result.size() > 0)
				return result;
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
//			System.out.println("It is not naver blog.");
		} catch (IndexOutOfBoundsException e) {
//			System.out.println("Date is not exists");
		}
		return null;
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
	
	public static void main(String[] args) throws IOException {
		NaverBlog nb = new NaverBlog();
//		nb.getAllPlaceOfCategory(NaverBlog.CategoryName.CAFFE);
//		nb.getAllPlaceOfCategory(NaverBlog.CategoryName.CULTURE);
//		nb.getAllPlaceOfCategory(NaverBlog.CategoryName.PUB);
	}
}
