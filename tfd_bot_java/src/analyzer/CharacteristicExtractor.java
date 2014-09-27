package analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.chasen.mecab.Tagger;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import controller.DBController;

public class CharacteristicExtractor extends TextMining{
	private final DBController _DBCON;
	private final String _PLACE_TABLE_NAME = "place_info_2";
	private final String _BLOG_TABLE_NAME = "blog_3";
	private final String _PLACE_CHAR_TABLE_NAME = "place_characteristics_3"; 
	private final String _CATEGORY_TABLE_NAME = "category_2";
	private final String _CHAR_DICT_TABLE_NAME = "characteristic_dictionary_2";
	private final String _DICT_PATH = "/usr/local/lib/mecab/dic/mecab-ko-dic";
	private final Tagger _TAGGER;
	private final Map<String, Set<String>> _CHAR_DICT;
	private String _currentPlaceName;
	private String _currentPlaceCategory;
	private int _currentProcCount;
	private int _targetRecordCount;
	
	enum DictName {
		CAFE("카페"), RESTAURANT("음식점");
		String value;
		DictName(String value) {
			this.value = value;
		}
	}
	
	public CharacteristicExtractor() {
		_DBCON = DBController.newInstance(DBController.Type.TFD);
		_CHAR_DICT = new HashMap<String, Set<String>>();
		_TAGGER = new Tagger("-d" + _DICT_PATH);
		
		for(DictName value : DictName.values()) // 카테고리 사전 설정
			_CHAR_DICT.put(value.value, _getDictionaryOfCategory(value.value));
	}
	
	/**
	 * 블로그 정보를 갖고 있는 장소의 특성을 추출한다 
	 * @return 더 추출할 장소가 있으면 true 없으면 false
	 */
	public boolean extractCharacteristics() {
		// DB로 부터 한 장소의 모든 블로그를 가져옴
		List<String> tempResult = _getBlogContentFromDB();
		if(tempResult == null)
			return false;
		// 블로그 리스트로 부터 단어 카운트를 수행한 결과를 가져옴
		Map<String, Integer> result = wordCount(tempResult);
		if(result == null)
			return true;
		
		List<Map<String, String>> query = new ArrayList<Map<String, String>>();
		Map<String, String> record;
		String[] tempString;
		
		try {
			// 카운팅된 내용을 DB에 입력
			for(String parsedWord : result.keySet()) {
				record = new HashMap<String, String>();
				tempString = parsedWord.split("\t");
				record.put("characteristic", tempString[0]);
				record.put("word_feature", tempString[1]);
				record.put("place_name", _currentPlaceName);
				record.put("count", String.valueOf(result.get(parsedWord)));
				query.add(record);
			}
			
			// DB입력 성공시에 update flag를 2로 바꿈
			if(result.size() > 0) {
				_DBCON.insertData(_PLACE_CHAR_TABLE_NAME, query);
				_DBCON.queryExecute("update " + _PLACE_TABLE_NAME + " set update_flag=2 where name='" + _currentPlaceName + "'");
			}
			return true;
		} catch(NullPointerException e) {
			return false;
		}
	}	
	
	/**
	 * 아직 특성추출이 완료안된 장소의 블로그를 반환해 줌 
	 * @return
	 */
	private List<String> _getBlogContentFromDB() {
		List<Map<String, String>> queryResult = null;
		List<String> result;
		
		try {
			// 무한반복하지 않도록 place_info테이블에 있는 레코드 중 update flag가 1인 장소의 갯수 만큼만 반복
			_targetRecordCount = Integer.parseInt(_DBCON.getData("select count(*) from " + _PLACE_TABLE_NAME + " where update_flag=1").get(0).get("count(*)"));
			while(_targetRecordCount > (++_currentProcCount)) {
				// place_info 테이블에서 update flage가 1인 아무 장소 레코드를 하나 가져옴
				queryResult = _DBCON.getData("select name, category from " + _PLACE_TABLE_NAME + " where update_flag=1 limit " + _currentProcCount + ", 1");
				_currentPlaceName = queryResult.get(0).get("name");
				
				// 가져온 장소의 큰 분류명을 가져옴
				_currentPlaceCategory = _DBCON.getData("select c_group from " + _CATEGORY_TABLE_NAME + " where sub_group='" + queryResult.get(0).get("category") + "'").get(0).get("c_group");
				// 가져온 장소의 모든 블로그를 가져옴
				queryResult = _DBCON.getData("select content from " + _BLOG_TABLE_NAME + " where place_name='" + _currentPlaceName + "'");
			
				if(queryResult == null)
					continue; // 가져온 블로그가 없으면 다른 장소에 대해 처리
				else
					break; // 가져온 블로그가 있으면 while문 종료
			}
				
			// 가져온 블로그의 모든 본문 내용을 리스트에 담아서 반환
			result = new ArrayList<String>();
			for(Map<String, String> r : queryResult)
				result.add(r.get("content"));
			return result;
//		} catch (CommunicationsException e) {
//			return null;
//		} catch (NullPointerException e) {
//			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 매개변수로 넘어온 카테고리에 대한 특성사전을 생성
	 * @param category
	 * @return
	 */
	private Set<String> _getDictionaryOfCategory(String category) {
		Set<String> result = new HashSet<String>();
		List<Map<String, String>> queryResult;
		try {
			queryResult = _DBCON.getData("select word, feature from " + _CHAR_DICT_TABLE_NAME + " where category='" + category + "'");
			for(Map<String, String> dict : queryResult)
				result.add(dict.get("word") + "\t" + dict.get("feature"));
		} catch (CommunicationsException e) {
			result = null;
		}
		
		return result;
	}

	@Override
	Map<String, Integer> wordCount(String text) {
		return null;
	}

	/**
	 * list에 있는 문장들을 형태소 분석한 뒤에 특성사전을 참고해 문장에 존재하는 특성 단어를 카운트하여 반환 
	 */
	@Override
	Map<String, Integer> wordCount(List<String> list) {
		Map<String, Integer> result;
		Map<String, Integer> tempResult = new HashMap<String, Integer>();
		String[] parsedWords;
		
		// 처리한 내용은 메모리에서 제거 될 수 있도록 리스트의 뒤쪽에서 부터 진행
		for(int i = list.size() - 1; i >= 0; i--) {
			parsedWords = _TAGGER.parse(list.get(i)).split("\n");
			list.remove(i); // 형태소 분석이 된 블로그는 리스트에서 제거
			for(int j = parsedWords.length - 1; j >= 0 ; j--) {
				try {
					// 현재의 단어가 장소분류에 해당하는 사전에 존재하면 단어 카운팅
					if(_CHAR_DICT.get(_currentPlaceCategory).contains(parsedWords[j])) {
						if(!tempResult.containsKey(parsedWords[j]))
							tempResult.put(parsedWords[j], 0);
						tempResult.put(parsedWords[j], tempResult.get(parsedWords[j]) + 1);
					}
				} catch(NullPointerException e) {}
				parsedWords[j] = null;
			}
		}
		
		// 단어 출현 횟수에 대해 내림차순 정렬을 한다
//		result = new TreeMap<String, Integer>(new WordCountComparator(tempResult));
//		result.putAll(tempResult);
//		return result;
		
		// 정렬을 하지 않고 반환한다
		return tempResult;
	}
	
	public static void main(String[] args) {
		CharacteristicExtractor ce = new CharacteristicExtractor();
		while(ce.extractCharacteristics());
	}
}

/**
 * word count 를 내림차순으로 정렬하기 위한 Comparator 인터페이스를 구현한 클래스
 * @author taeyong
 *
 */
class WordCountComparator implements Comparator<String> {
	private Map<String, Integer> base;
	
	public WordCountComparator(Map<String, Integer> baseMap) {
		this.base = baseMap;
	}
	
	@Override
	public int compare(String o1, String o2) {
		return base.get(o2) - base.get(o1); // 내림차순 정렬
	}
	
}