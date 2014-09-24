package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.naver.FieldName;
import parser.naver.NaverSearch;
import controller.DBController.Type;

public class BlogParseManager extends Thread {
	private final String _BLOG_TABLE_NAME = "blog_2";
	private final String _PLACE_INFO_TABLE_NAME = "place_info_2";
	private final String _IMAGE_TABLE_NAME = "place_image_2";
	private static DBController _dbcon;
	
	private String currentPlaceName;
	private int currentStartNumber;
	
	static {
		_dbcon = DBController.newInstance(Type.TFD);
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * DB에 있는 장소 중 업데이트 되지 않은 장소 하나에 대해서 블로그 파싱을 진행함.
	 * @return 성공 : true, DB연결 실패나 파싱 실패시 false
	 */
	public boolean getBlog(){
		NaverSearch naverBlogSearcher = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_BLOG);
		ArrayList<Map<String, String>> result = null;
		Map<String, String> place, temp;
		String placeName, addr;
		
		try {
			place = _dbcon.getData("select name, address from " + _PLACE_INFO_TABLE_NAME + " where update_flag=0 order by rand() limit 1").get(0);
			placeName = place.get("name");
			addr = place.get("address");
			currentPlaceName = placeName;
			_dbcon.queryExecute("update " + _PLACE_INFO_TABLE_NAME + " set update_flag=1 where name='" + placeName + "'"); // 파싱하는 동안 다른 사용자가 동일한 장소를 파싱하지 못하도록 설정
		} catch(com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
			return false;
		} catch(Exception e) {
			return false;
		}

		System.out.println(placeName + " is being processed.");
		currentStartNumber = 1;
		
		while((result = (ArrayList<Map<String, String>>)naverBlogSearcher.getResult(placeName)) != null) {
			try {
				Thread.sleep(0); // 외부에서 interrupt() 요청시 여기에서 멈출 수 있도록 sleep 삽입
			} catch (InterruptedException e) {}
			result = (ArrayList<Map<String, String>>) _filterBlog(result, placeName, addr);
			
			temp = new HashMap<String, String>();
			ArrayList<Map<String, String>> query = new ArrayList<Map<String, String>>();
			for(Map<String, String> blogContent : result){
				// 장소정보 입력을 위한 작업
				temp.put("place_name", placeName);
				temp.put("title", blogContent.get(FieldName.TITLE.value));
				temp.put("writer", blogContent.get(FieldName.BLOGGER_NAME.value));
				temp.put("url", blogContent.get(FieldName.BLOG_LINK.value));
				temp.put("content", blogContent.get(FieldName.BLOG_CONTENT.value));
				temp.put("date", blogContent.get(FieldName.BLOG_DATE.value));
				temp.put("today_count", blogContent.get(FieldName.BLOG_TODAY_COUNT.value));
				temp.put("sympathy_count", blogContent.get(FieldName.BLOG_SYMPATHY_COUNT.value));
				query.add(temp);
				_dbcon.insertData(_BLOG_TABLE_NAME, query);
				temp.clear();
				query.clear();
				// 이미지 입력을 위한 작업
				query = (ArrayList<Map<String, String>>) _generateQueryForImageTable(blogContent.get(FieldName.BLOG_IMAGES.value), placeName);
				if(query != null)
					_dbcon.insertData(_IMAGE_TABLE_NAME, query);
				query.clear();
			}
			// 장소정보에 현재 장소가 정보 업데이트가 되었음을 표시
//			if(result != null && result.size() > 0)
//				_dbcon.queryExecute("update " + _PLACE_INFO_TABLE_NAME + " set update_flag=1 where name='" + placeName + "'");
			result.clear();
			
			currentStartNumber = (Integer)naverBlogSearcher.getCurrentState();
		}
		return true;
	}
	
	/**
	 * 제목에서 상점명을 검사하고 본문에서 주소를 검사하여 블로그의 유효성 여부를 판단
	 * @param title 
	 * @param content
	 * @param placeName
	 * @param placeAddr
	 * @return 블로그 유효성에 대해 boolean값 반환
	 */
	private boolean _isValidBlog(String title, String content, String placeName, String placeAddr){
		/*
		 * 1. 제목에서 상호명과 지역명을 포함하는 블로그 파싱 > 부정확
		 * 2. 제목에서 상호명을 포함하고 지점명이나 지역명을 포함하는 블로그 중 본문에 지점명or지역명or상호의 주소를 포함 > 부정확
		 * 3. 제목에서 상호명을 포함하는 블로그 중 본문에 상호의 주소를 포함 > 정확 (but 갯수가 적음)
		 */
		String[] placeNameToken = placeName.split(" ");
		String branch; // 상점의 분점 표시
		int branchIndex = -1; // -1은 분점이 아님을 표시
		
		try {
			// 공백으로 토큰분리 후에 분점인지 확인
			for(int i = 1; i < placeNameToken.length; i++){
				branchIndex = -1;
				if(placeNameToken[i].endsWith("점")) {
					branch = placeNameToken[i];
					branchIndex = i; // 분점일 경우 몇번째 인덱스가 분점 단어인지 표시
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
		// 제목에 분점 정보있을 때 분점 직전까지의 상점명으로 검사
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
		
		// 블로그 본문에서 주소 검사
		if(content.indexOf(_splitAddress(placeAddr)) >= 0)
			return true;
		
		return false;
	}
	
	/**
	 * 제목에서 장소명을 검사하고 본문에서 주소를 검사하여 블로그를 필터링
	 * @param blogData
	 * @param placeName
	 * @param local
	 * @return 유효한 블로그 정보만을 리스트로 반환
	 */
	private List<Map<String, String>> _filterBlog(List<Map<String, String>> blogData, String placeName, String placeAddr){
//		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		String title, content;
		for(int i = blogData.size() - 1; i >= 0; i--){
			title = blogData.get(i).get(FieldName.TITLE.value).toLowerCase().replaceAll(" ", "");
			content = blogData.get(i).get(FieldName.BLOG_CONTENT.value);
			if(!_isValidBlog(title, content, placeName.toLowerCase(), placeAddr))
				blogData.remove(i);
		}
		return blogData;
	}
	
	/**
	 * 하나의 문자열로 되어있는 이미지 주소 리스트를 리스트 형태로 변경
	 * @param delimitedLink
	 * @param placeName
	 * @return 이미지 주소를 리스트에 담아 반환
	 */
	private List<Map<String, String>> _generateQueryForImageTable(String delimitedLink, String placeName){
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		Map<String, String> temp;
		String[] splitedLinks = delimitedLink.split("\t");
		
		for(String link : splitedLinks){
			temp = new HashMap<String, String>();
			temp.put(FieldName.PLACE_NAME.value, placeName);
			temp.put(FieldName.PLACE_IMAGE.value, link);
			result.add(temp);
			temp.clear();
		}
		
		if(result.size() <= 0)
			result = null;
		return result;
	}
	
	/**
	 * 주소의 시와 지번을 잘라낸 나머지 주소를 생성 
	 * @param fullAddress
	 * @return 시와 지번을 제외한 주소를 반환
	 */
	private String _splitAddress(String fullAddress) {
		String[] addr = fullAddress.split("\\s\\d"); // 지번앞에서 문자열을 자름
		try {
			addr = addr[0].split(" "); // 지번을 제외한 문자열을 시,구,동으로 나눔
			return addr[1] + " " + addr[2]; // 구와 동을 반환
		} catch (Exception e) {
			return ""; // 변환할 수 없을 때 공백 반환
		}
	}
	
	public String getCurrentPlaceName() {
		return currentPlaceName;
	}
	
	public int getCurrentStartNumber() {
		return currentStartNumber;
	}

	public void run() {
		getBlog();
	}
}
