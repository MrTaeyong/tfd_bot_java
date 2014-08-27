/**
 * @FileName	: NaverBlog.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 25.
 * @Author		: Taeyong
 */
package parser.naver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
		TITLE("title"), BLOG_CONTENT("blogContent"), BLOGGER_NAME("bloggerName"), BLOGGER_LINK("bloggerLink"), BLOGG_DATE("date");
		String value;
		FieldName(String value){
			this.value = value;
		}
		public String getValue(){
			return value;
		}
	}
	
	public static enum CategoryName {
		CAFFE("카페"), RESTAURANT("음식점");
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
			List<Map<String, String>> duplicationCheck = _dbcon.getData("select title from blog_test where title=\"" + title + "\" and writer=\"" + writer + "\"");
			if(duplicationCheck != null){
				fail++;
				continue;	// 중복되면 다음 블로그로..
			}
			
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
			resultMap.put("title", title); // Add title
			resultMap.put("bloggerName", writer); // Add blogger name
			resultMap.put("bloggerLink", e.getElementsByTag("bloggerlink").text()); // Add blog link
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
			result.put("date", doc.getElementsByClass("_postAddDate").text().trim().replaceAll("/", "-"));
			result.put("blogContent", doc.getElementById("post-view" + logNo).text().trim());
			
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
	
	private String _getLink(String item) throws StringIndexOutOfBoundsException {
		int start, end;
		start = item.indexOf("<link />");
		end = item.indexOf("<description>");
		item = item.substring(start+8, end);
		return item;
	}
	
	@SuppressWarnings("unchecked")
	public void getAllPlaceOfCategory(CategoryName category){
		Queue<String> jobQueue = new LinkedList<String>();
		File jobFile = new File(category.value + ".txt");
		NaverSearch naverBlogSearcher = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_BLOG);
		ArrayList<Map<String, String>> result;
		
		// 이미 진행중인 Job이 없으면 DB에서 리스트를 생성
		if(!jobFile.exists())
			_extractPlaceName(category);
		
		// Job Queue에 작업을 채움
		try {
			BufferedReader br = new BufferedReader(new FileReader(jobFile));
			String jobString;
			while((jobString = br.readLine()) != null)
				jobQueue.offer(jobString);
			br.close();
		} catch (IOException e) {}
		
		while(!jobQueue.isEmpty()){
			String[] placeNameAndLocal = jobQueue.peek().split("\t");
			if(placeNameAndLocal.length < 2) { // 형식이 다를 때 다음 작업으로 넘어감
				jobQueue.remove();
				_saveRemainingJob(jobQueue, category);
				continue;
			}
			
			String placeName = placeNameAndLocal[0];
			String local = placeNameAndLocal[1];
			
			System.out.println(placeName + " is being processed.");
			
			result = (ArrayList<Map<String, String>>) naverBlogSearcher.getResult(local + " " + placeName);
			
			if(result == null){
				jobQueue.remove();
				_saveRemainingJob(jobQueue, category);
				continue;
			}
			
			result = (ArrayList<Map<String, String>>) _filterBlog(result, placeName, local);
			
			for(Map<String, String> blogContent : result){					
//				if(!_isValidBlog(blogContent.get(NaverBlog.FieldName.BLOG_CONTENT.value), placeName, local)){
//					try {
//						FileWriter fw = new FileWriter("log.txt", true);
//						fw.write(placeName + "\n");
//						fw.write(blogContent.get(NaverBlog.FieldName.BLOG_CONTENT.value) + "\n\n\n");
//						fw.close();
//					} catch (IOException e){}
//					continue;
//				}
				ArrayList<Map<String, String>> query = new ArrayList<Map<String, String>>();
				Map<String, String> temp = new HashMap<String, String>();
				temp.put("place_name", placeName);
				temp.put("title", blogContent.get(NaverBlog.FieldName.TITLE.value));
				temp.put("writer", blogContent.get(NaverBlog.FieldName.BLOGGER_NAME.value));
				temp.put("url", blogContent.get(NaverBlog.FieldName.BLOGGER_LINK.value));
				temp.put("content", blogContent.get(NaverBlog.FieldName.BLOG_CONTENT.value));
				temp.put("date", blogContent.get(NaverBlog.FieldName.BLOGG_DATE.value));
				query.add(temp);
				_dbcon.insertData("blog_test", query);
			}
			jobQueue.remove();
			_saveRemainingJob(jobQueue, category);			
		}
	}
	
	private void _saveRemainingJob(Queue<String> jobQueue, CategoryName category){
		try {
			FileWriter fw = new FileWriter(category.value + ".txt");
			for(Iterator<String> iter = jobQueue.iterator(); iter.hasNext();)
				fw.write(iter.next() + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method for extracting place name and local related with category from DB to file.
	 * @param category
	 */
	private void _extractPlaceName(CategoryName category){
		List<Map<String, String>> nameAndCategoryOfPlace = _dbcon.getData("select name, local from place_info where category in (select sub_group from category where c_group='" + category.value + "')");
		try {
			FileWriter fw = new FileWriter(category.value + ".txt");
			for(Map<String, String> record : nameAndCategoryOfPlace)
				fw.write(record.get("name") + "\t" + record.get("local") + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method for Checking validity of blog content using confirm place name and local.
	 * @param blogContent Sentence of blog
	 * @param placeName Place name of blog content.
	 * @param local Local of place of blog content.
	 * @return Return boolean value about validity of blog
	 */
	private boolean _isValidBlog(String blogContent, String placeName, String local){
		if(blogContent.indexOf(local) >= 0){
			String[] placeNameToken = placeName.split(" ");
			for(String token : placeNameToken){
				if(blogContent.indexOf(token) >= 0)
					return true;
			}
		}
		return false;
	}
	
	private List<Map<String, String>> _filterBlog(List<Map<String, String>> blogData, String placeName, String local){
		List<Integer> indexOfDeletedData = new ArrayList<Integer>();
		for(int i = 0; i < blogData.size(); i++){
			String title = blogData.get(i).get("title");
			if(!_isValidBlog(title, placeName, local))
				indexOfDeletedData.add(i);
		}
		for(int i = 0; i < indexOfDeletedData.size(); i++)
			blogData.remove(indexOfDeletedData.get(i));
		return blogData;
	}
	
	public static void main(String[] args) throws IOException {
		NaverBlog nb = new NaverBlog();
		nb.getAllPlaceOfCategory(NaverBlog.CategoryName.CAFFE);
	}
}
