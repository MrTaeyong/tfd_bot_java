/**
 * @FileName	: NaverBlog.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 25.
 * @Author		: Taeyong
 */
package parser.naver;

import java.io.BufferedReader;
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
	public NaverBlog(){
		super();
	}

	public Object getResult(String keyword) {
		Connector connector;
		String xmlData;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		 
		connector = Connector.getInstance(Connector.NAVER_BLOG);
		
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
			String blogContent = _getBlogContent(_getLink(e.toString()));
			if(blogContent == null)
				continue;
			resultMap = new HashMap<String, String>();
			resultMap.put("title", e.getElementsByTag("title").text().replaceAll("(<b>|</b>)", ""));
			resultMap.put("blogContent", blogContent);
			resultMap.put("bloggerName", e.getElementsByTag("bloggername").text());
			resultMap.put("bloggerLink", e.getElementsByTag("bloggerlink").text());
			resultList.add(resultMap);
		}
		
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
			e.printStackTrace(); // If blog is not naver
		}
//		try {
//			URL url = new URL(naverAPIUrl);
//			URLConnection connection = url.openConnection();
//			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String content = "", tmp;
//			while((tmp = br.readLine()) != null)
//				content += tmp + "\n";
//			Document doc = Jsoup.parse(content);
//			Element element = doc.getElementById("mainFrame");
//			String src;
//			try{
//				src = element.toString();
//			}catch(NullPointerException e){
//				return null;
//			}
//			int start, end;
//			src = src.replace("&amp;", "&");
//			start = src.indexOf("src=") + 5;
//			end = src.indexOf("&beginTime");
//			src = src.substring(start, end);
//			src = "http://blog.naver.com" + src;
//			start = src.indexOf("logNo=") + 6;
//			String logNum = src.substring(start);
//			
//			url = new URL(src);
//			connection = url.openConnection();
//			br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			content = "";
//			while((tmp = br.readLine()) != null)
//				content += tmp + "\n";
//			doc = Jsoup.parse(content);
//			element = doc.getElementById("post-view" + logNum);
//			System.out.println(element.text());
//			return element.text();
//			
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return null;
	}
	
	private String _getLink(String item){
		int start, end;
		start = item.indexOf("<link />");
		end = item.indexOf("<description>");
		item = item.substring(start+8, end);
		return item;
	}
}
