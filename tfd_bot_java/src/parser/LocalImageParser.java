/**
 * @FileName	: LocalImageParser.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 6. 23.
 * @Author		: Taeyong
 */
package parser;

import java.util.ArrayList;
import java.util.Map;

import parser.naver.NaverSearch;
import controller.DBController;

/**
 * @Class	: LocalImageParser
 * @Date	: 2014. 6. 23.
 * @Author	: Taeyong
 */
public class LocalImageParser {
	public static void main(String[] args) {
		ArrayList<Map<String, String>> queryResult, searchResult;
		DBController controller = DBController.newInstance(DBController.Type.TFD);
		NaverSearch ns = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_IMAGE);
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
