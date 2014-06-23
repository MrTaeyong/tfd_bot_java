package parser;

import java.util.ArrayList;
import java.util.Map;

import controller.Controller;
import parser.naver.NaverSearch;

public class LocalImageParser {
	public static void main(String[] args) {
		ArrayList<Map<String, String>> queryResult, searchResult;
		Controller controller = Controller.newInstance(Controller.DATABASE);
		NaverSearch ns = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_IMAGE);
//		result = (ArrayList<Map<String, String>>) ns.getResult("");
		queryResult = (ArrayList<Map<String, String>>) controller.getData("select id, name from place_info_test");
		for(Map<String, String> r : queryResult){
			String id = r.get("id");
			String name = r.get("name");
			searchResult = (ArrayList<Map<String, String>>) ns.getResult(name);
			for(Map<String, String> nr : searchResult){
				nr.put("id", id);
			}
			controller.insertData("place_image_test", searchResult);
		}
	}
}
