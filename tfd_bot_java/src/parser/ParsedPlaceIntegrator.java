/**
 * @FileName	: ParsedPlaceIntergrator.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 06. 22.
 * @Author		: Taeyong
 */
package parser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
		Collections.sort(set1, _comparator);
		Collections.sort(set2, _comparator);
		_set1 = set1;
		_set2 = set2;
	}
	
	/**
	 * @description : 객체를 생성한 후 해당 메소드를 호출하면 두개의 리스트에서 중복된 지역을 제거하여 하나로 합쳐진 리스트를 반환
	 * @return		: 중복제거된 리스트
	 */
	public List<Map<String, String>> Intergrate(){
		for(Map<String, String> element : _set1){
			_removeDuplicatePlace(element);
		}
		List<Map<String, String>> result = _set1;
		result.addAll(_set2);
		return result;
	}
	
	private boolean _removeDuplicatePlace(Map<String, String> element){
		int left = 0, right = _set2.size();
		int mid = right / 2;
		while(left != right){
			Map<String, String> temp = _set2.get(mid);
			if(temp.get("telephone").compareTo(element.get("telephone")) != 0){
				if(temp.get("name").compareTo(element.get("name")) > 0){
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
}
