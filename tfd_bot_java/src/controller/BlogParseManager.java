package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.naver.NaverSearch;
import controller.DBController.Type;

public class BlogParseManager {
	private static DBController _dbcon;
	
	static {
		_dbcon = DBController.newInstance(Type.TFD);
	}
	
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
	
	@SuppressWarnings("unchecked")
	public void getBlogAboutAllPlaceOfCategory(){
		NaverSearch naverBlogSearcher = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_BLOG);
		ArrayList<Map<String, String>> result;
		List<Map<String, String>> placeList = _dbcon.getData("select name, local from place_info where update_flag=0");
		
		for(Map<String, String> place : placeList){
			
			String placeName = place.get("name");
			String local = place.get("local");
			
			System.out.println(placeName + " is being processed.");
			
			result = (ArrayList<Map<String, String>>) naverBlogSearcher.getResult(local + " " + placeName);
			
			if(result == null)
				continue;
			
			result = (ArrayList<Map<String, String>>) _filterBlog(result, placeName, local);
			
			for(Map<String, String> blogContent : result){					
				ArrayList<Map<String, String>> query = new ArrayList<Map<String, String>>();
				Map<String, String> temp = new HashMap<String, String>();
				temp.put("place_name", placeName);
				temp.put("title", blogContent.get(FieldName.TITLE.value));
				temp.put("writer", blogContent.get(FieldName.BLOGGER_NAME.value));
				temp.put("url", blogContent.get(FieldName.BLOGGER_LINK.value));
				temp.put("content", blogContent.get(FieldName.BLOG_CONTENT.value));
				temp.put("date", blogContent.get(FieldName.BLOG_DATE.value));
				query.add(temp);
				_dbcon.insertData("blog_test", query);
				query = (ArrayList<Map<String, String>>) _generateQueryForImageTable(blogContent.get(FieldName.BLOG_IMAGES.value), placeName);
				if(query != null)
					_dbcon.insertData("place_image", query);
			}
			_dbcon.queryExecute("update place_info set update_flag=1 where name='" + placeName + "'");
		}
	}
	
	private boolean _markUpdateFlag(List<String> places){
		if(places.size() <= 0 || places.size() < 100)
			return false;
		
		String updateQuery = "update place_info set updated_flag=1 where name in (" + places.get(0);
		for(int i = 1; i < places.size(); i++){
			updateQuery +=  places.get(i) + ",";
		}
		updateQuery += ")";
		
		_dbcon.queryExecute(updateQuery);
		return true;
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
			if(blogContent.indexOf(local) < 0)
				return false;
			
			String[] placeNameToken = placeName.split(" ");
			for(String token : placeNameToken){
				if(blogContent.indexOf(token) < 0)
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Method for blog filtering by inspecting title of blog
	 * @param blogData
	 * @param placeName
	 * @param local
	 * @return
	 */
	private List<Map<String, String>> _filterBlog(List<Map<String, String>> blogData, String placeName, String local){
		List<Integer> indexOfDeletedData = new ArrayList<Integer>();
		for(int i = 0; i < blogData.size(); i++){
			String title = blogData.get(i).get(FieldName.TITLE.value);
			if(!_isValidBlog(title, placeName, local))
				indexOfDeletedData.add(i);
		}
		for(int i = 0; i < indexOfDeletedData.size(); i++)
			blogData.remove(indexOfDeletedData.get(i));
		return blogData;
	}
	
	private List<Map<String, String>> _generateQueryForImageTable(String delimitedLink, String placeName){
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		String[] splitedLinks = delimitedLink.split("\t");
		
		for(String link : splitedLinks){
			Map<String, String> temp = new HashMap<String, String>();
			temp.put(FieldName.PLACE_NAME.value, placeName);
			temp.put(FieldName.PLACE_IMAGE.value, link);
			result.add(temp);
		}
		
		if(result.size() > 0)
			return result;
		return null;
	}
	
	public static void main(String[] args) {
		new BlogParseManager().getBlogAboutAllPlaceOfCategory();
	}
}
