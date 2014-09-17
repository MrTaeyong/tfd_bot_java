package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.naver.NaverSearch;
import controller.DBController.Type;

public class BlogParseManager {
	private final String _BLOG_TABLE_NAME = "blog_2";
	private final String _PLACE_INFO_TABLE_NAME = "place_info_2";
	private final String _IMAGE_TABLE_NAME = "place_image_2";
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
		List<Map<String, String>> placeList = _dbcon.getData("select name, address from " + _PLACE_INFO_TABLE_NAME + " where update_flag=0");
		
		for(Map<String, String> place : placeList){
			
			String placeName = place.get("name");
			String addr = place.get("address");
			
			System.out.println(placeName + " is being processed.");
			
			result = (ArrayList<Map<String, String>>) naverBlogSearcher.getResult(placeName);
			
			if(result == null)
				continue;
			
			result = (ArrayList<Map<String, String>>) _filterBlog(result, placeName, addr);
			
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
				_dbcon.insertData(_BLOG_TABLE_NAME, query);
				query = (ArrayList<Map<String, String>>) _generateQueryForImageTable(blogContent.get(FieldName.BLOG_IMAGES.value), placeName);
				if(query != null)
					_dbcon.insertData(_IMAGE_TABLE_NAME, query);
			}
			_dbcon.queryExecute("update " + _PLACE_INFO_TABLE_NAME + " set update_flag=1 where name='" + placeName + "'");
		}
	}
	
	/**
	 * Method for Checking validity of blog content using confirm place name and local.
	 * @param blogContent Sentence of blog
	 * @param checkString Checking string 
	 * @return Return boolean value about validity of blog
	 */
	private boolean _isValidBlog(String title, String content, String placeName, String placeAddr){
		String[] placeNameToken = placeName.split(" ");
		String branch;
		int branchIndex = -1;
		try {
			for(int i = 1; i < placeNameToken.length; i++){
				branchIndex = -1;
				if(placeNameToken[i].endsWith("점")) {
					branch = placeNameToken[i];
					branchIndex = i;
				}
			}
		} catch(IndexOutOfBoundsException e) {
			branchIndex = -1;
		}
		
		// 모두 and 조건으로 검사 (제목에 분점 정보가 없음)
		if(branchIndex < 0) {
			for(String place : placeNameToken)
				if(title.indexOf(place) < 0)
					return false;
		}
		else {
			for(int i = 0; i < branchIndex; i++)
				if(title.indexOf(placeNameToken[0]) < 0)
					return false;
			
//			// 지점, 지역 or 조건 검사
//			int i;
//			for(i = 1; i < placeNameToken.length; i++)
//				if(title.indexOf(placeNameToken[i]) >= 0)
//					break;
//			
//			if(i >= placeNameToken.length && title.indexOf(local) < 0)
//				return false;
		}
		
		// 본문 주소 검사
		if(content.indexOf(_splitAddress(placeAddr)) >= 0)
			return true;
		
		return false;
	}
	
	/**
	 * Method for blog filtering by inspecting title of blog
	 * @param blogData
	 * @param placeName
	 * @param local
	 * @return
	 */
	private List<Map<String, String>> _filterBlog(List<Map<String, String>> blogData, String placeName, String placeAddr){
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		for(int i = 0; i < blogData.size(); i++){
			String title = blogData.get(i).get(FieldName.TITLE.value).toLowerCase().replaceAll(" ", "");
			String content = blogData.get(i).get(FieldName.BLOG_CONTENT.value);
			if(_isValidBlog(title, content, placeName.toLowerCase(), placeAddr))
				result.add(blogData.get(i));
		}
		return result;
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
	
	/**
	 * @method Name	: splitAddress
	 * @date		: 2014. 9. 17.
	 * @author		: taeyong
	 * @description	:
	 * @param fullAddress
	 * @return Large scope address of splited address
	 */
	private String _splitAddress(String fullAddress) {
		String[] addr = fullAddress.split("\\s\\d");
		try {
			addr = addr[0].split(" ");
			return addr[1] + " " + addr[2];
		} catch (Exception e) {
			return "";
		}
	}
	
	public static void main(String[] args) {
		new BlogParseManager().getBlogAboutAllPlaceOfCategory();
	}
}
