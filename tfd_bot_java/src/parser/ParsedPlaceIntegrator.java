/**
 * @FileName	: ParsedPlaceIntergrator.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 06. 22.
 * @Author		: Taeyong
 */
package parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import parser.naver.NaverSearch;

/**
 * @Class	: ParsedPlaceIntegrator
 * @Date	: 2014. 06. 22.
 * @Author	: Taeyong
 */
public class ParsedPlaceIntegrator {
	private List<Map<String, String>> _set1;
	private List<Map<String, String>> _set2;
	private Comparator<Map<String, String>> _comparator;
	
	/**
	 * @description : 지역정보가 담긴 리스트 두개로 객체를 생성하면 객체는 중복제거를 위한 초기 작업을 수행
	 * @param set1	: 지역정보가 담긴 리스트 1
	 * @param set2	: 지역정보가 담긴 리스트 2
	 */
	public ParsedPlaceIntegrator(List<Map<String, String>> set1, List<Map<String, String>> set2){
		_comparator = new Comparator<Map<String, String>>(){
			@Override
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				// TODO Auto-generated method stub
				return o1.get("name").compareTo(o2.get("name"));
			}
		};
//		Collections.sort(set1, _comparator);
		Collections.sort(set2, _comparator);
		_set1 = set1;
		_set2 = set2;
	}
	
	/**
	 * @description : 객체를 생성한 후 해당 메소드를 호출하면 두개의 리스트에서 중복된 지역을 제거하여 하나로 합쳐진 리스트를 반환
	 * @return		: 중복제거된 리스트
	 */
	public List<Map<String, String>> intergrate(){
		for(Map<String, String> element : _set1){
			_removeDuplicatePlace(element);
		}
		_set1.addAll(_set2);
		return _set1;
	}
	
	private boolean _removeDuplicatePlace(Map<String, String> element){
		int left = 0, right = _set2.size() - 1;
		int mid = right / 2;
		while(left <= right){
			Map<String, String> temp = _set2.get(mid);
			if(temp.get("telephone").compareTo(element.get("telephone")) != 0){
				if(temp.get("name").compareTo(element.get("name")) < 0){
					left = mid + 1;
					mid = (left + right) / 2;
				}
				else{
					right = mid - 1;
					mid = (left + right) / 2; 
				}
			}
			else{
				_set2.remove(mid);
				return true;
			}
		}
		return false;
	}
	
	
	public static void main(String[] args){
		ArrayList<Map<String, String>> result1, result2, result3;
		FileWriter fw;
//		GooglePlace gp = new GooglePlace();
		NaverSearch n = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_LOCAL);
		NaverSearch naver = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_LOCAL);
		
//		result1 = (ArrayList<Map<String, String>>)gp.getResult("홍대 식당");
		result1 = (ArrayList<Map<String, String>>) n.getResult("강남 식당");
		result2 = (ArrayList<Map<String, String>>) naver.getResult("강남 음식점");
		
		if(result1 == null || result2 == null){
			System.out.println("검색실패");
			return;
		}
		System.out.println("검색완료");
		try {
			fw = new FileWriter(new File("result1.txt"));
			for(Map<String, String> r : result1){
				fw.write(r.get("name") + "\n");
				fw.write(r.get("category") + "\n");
				fw.write(r.get("address") + "\n");
				fw.write(r.get("telephone") + "\n");
				fw.write(r.get("description") + "\n\n\n");
			}
			fw.close();
			fw = new FileWriter(new File("result2.txt"));
			for(Map<String, String> r : result2){
				fw.write(r.get("name") + "\n");
				fw.write(r.get("category") + "\n");
				fw.write(r.get("address") + "\n");
				fw.write(r.get("telephone") + "\n");
				fw.write(r.get("description") + "\n\n\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		System.out.println("result1, result2 쓰기 완료");
		ParsedPlaceIntegrator intergrator = new ParsedPlaceIntegrator(result1, result2);
		result3 = (ArrayList<Map<String, String>>) intergrator.intergrate();
		
		System.out.println("통합완료");
		try {
			fw = new FileWriter(new File("result3.txt"));
			for(Map<String, String> r : result3){
				fw.write(r.get("name") + "\n");
				fw.write(r.get("category") + "\n");
				fw.write(r.get("address") + "\n");
				fw.write(r.get("telephone") + "\n");
				fw.write(r.get("description") + "\n\n\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("최종완료");
	}
}
