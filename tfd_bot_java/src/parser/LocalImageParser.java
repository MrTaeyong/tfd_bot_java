/**
 * @FileName	: LocalImageParser.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 6. 23.
 * @Author		: Taeyong
 */
package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import parser.naver.NaverSearch;
import controller.DBController;

/**
 * @Class	: LocalImageParser
 * @Date	: 2014. 6. 23.
 * @Author	: Taeyong
 */
public class LocalImageParser {
	public static void main(String[] args) {
		ArrayList<Map<String, String>> searchResult, insertQueryList;
		Map<String, String> queryResult;
		DBController controller = DBController.newInstance(DBController.Type.TFD);
		NaverSearch ns = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_IMAGE);
		queryResult = controller.getData("select id, name from place_info_test");
		insertQueryList = new ArrayList<Map<String, String>>();
		for(Entry e : queryResult.entrySet()){
			String id = (String) e.getKey();
			String name = (String) e.getValue();
			searchResult = (ArrayList<Map<String, String>>) ns.getResult(name);
			try{
			for(Map<String, String> nr : searchResult){
				nr.put("place_id", id);
				insertQueryList.add(nr);
			}
			}catch(NullPointerException exception){
				continue;
			}
			controller.insertData("place_image_test", insertQueryList);
			insertQueryList.clear();
		}
	}
}
