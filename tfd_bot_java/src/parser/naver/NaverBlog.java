/**
 * @FileName	: NaverBlog.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 25.
 * @Author		: Taeyong
 */
package parser.naver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
	public static enum FieldName {
		TITLE("title"), BLOG_CONTENT("blogContent"), BLOGGER_NAME("bloggerName"), BLOGGER_LINK("bloggerLink");
		String value;
		FieldName(String value){
			this.value = value;
		}
		public String getValue(){
			return value;
		}
	}
	
	public NaverBlog(){
		super();
	}

	public Object getResult(String keyword) {
		int start = 1, display = 100;
		NaverConnector connector;
		String xmlData;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		 
		connector = (NaverConnector) Connector.getInstance(Connector.NAVER_BLOG);
		
		// Receive all XML data possible and generate resulList.
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
			String link;
			try{
				link = _getLink(e.toString());
			}catch (StringIndexOutOfBoundsException exception){
				System.out.println(exception);
				continue;
			}
//			String blogContent = _getBlogContent(_getLink(e.toString()));
			String blogContent = _getBlogContent(link);
			if(blogContent == null){
				fail++;
				continue;
			}
			success++;
			resultMap = new HashMap<String, String>();
			resultMap.put("title", e.getElementsByTag("title").text().replaceAll("(<b>|</b>)", ""));
			resultMap.put("blogContent", blogContent);
			resultMap.put("bloggerName", e.getElementsByTag("bloggername").text());
			resultMap.put("bloggerLink", e.getElementsByTag("bloggerlink").text());
			resultList.add(resultMap);
		}
		
		System.out.printf("Success : %d, Fail : %d\n", success, fail);
		if(resultList.size() > 0)
			return resultList;
		return null;
	}
	
	private String _getBlogContent(String naverAPIUrl){
		Document doc;
		Element element;
		String content = "", tmp, src, logNo;
		int start, end;
		
		try {
			// Jsoup의 커넥터를 이용하면 네이버API에서 주는 주소를 인식 못해 오류발생..
			URL url = new URL(naverAPIUrl);
			URLConnection connection = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			// Convert from BufferedReader to String
			while((tmp = br.readLine()) != null)
				content += tmp + "\n";
			
			// Extracting the address contained in the mainFrame
			doc = Jsoup.parse(content);
			src = doc.getElementById("mainFrame").toString().replace("&amp;", "&");
			start = src.indexOf("src=") + 5;
			end = src.indexOf("&beginTime");
			src = src.substring(start, end);
			src = "http://blog.naver.com" + src;
			start = src.indexOf("logNo=") + 6;
			logNo = src.substring(start); // Parse post number
			
			// URL의 커넥터를 사용하면 Jsoup에서 본문내용을 파싱하지 못함...
			doc = Jsoup.connect(src).get();
			element = doc.getElementById("post-view" + logNo);
//			System.out.println(element.text());
			return element.text();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e){
			System.out.println("It is not naver blog.");
//			e.printStackTrace(); // If blog is not naver
		}		
		return null;
	}
	
	private String _getLink(String item){
		int start, end;
		start = item.indexOf("<link />");
		end = item.indexOf("<description>");
		item = item.substring(start+8, end);
		return item;
	}
	
	public static void main(String[] args) throws IOException {
		NaverSearch ns = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_BLOG);
		ArrayList<Map<String, String>> result;
		String keyword = "돈코보쌈";
		
		result = (ArrayList<Map<String, String>>) ns.getResult(keyword);
		FileWriter fw = new FileWriter(new File("blogtest.txt"));
		
		fw.write(keyword + "\n\n");
		for(Map<String, String> r : result){
			fw.write(r.get(NaverBlog.FieldName.TITLE.getValue()) + "\n");
			fw.write(r.get(NaverBlog.FieldName.BLOGGER_NAME.getValue()) + "\n");
			fw.write(r.get(NaverBlog.FieldName.BLOGGER_LINK.getValue()) + "\n");
			fw.write(r.get(NaverBlog.FieldName.BLOG_CONTENT.getValue()) + "\n\n\n\n\n");
			fw.write("==================================================================================\n");
		}
		fw.close();
	}
}
