/**
 * @FileName	: PosNagWordCounter.java
 * @Package		: analyzer
 * @Date		: 2014. 6. 28.
 * @Author		: Taeyong
 */
package analyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.chasen.mecab.Tagger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @Class	: PosNagWordCounter
 * @Date	: 2014. 6. 28.
 * @Author	: Taeyong
 */
public class PosNagWordCounter extends TextMining{
	// 워드 카운드를 할 품사태그
	private String _wordClass = "(.*NNG.*|.*NNP.*|.*NNB.*|.*NR.*|.*NP.*|.*VV.*|.*VA.*|.*VX.*|.*MAG.*|.*XR.*)";
	// 은전한닢 형태소 분석기의 사전 경로
	private String _dictionaryPath = "/usr/local/lib/mecab/dic/mecab-ko-dic";
	
	@Override
	Map<String, Integer> wordCount(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Map<String, Integer> wordCount(ArrayList<String> list) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Map<String, _Word> wordCount(List<String> list){
//		FileWriter fw;
//		try{
//		fw = new FileWriter(new File("test1.txt"));
//		}catch(Exception e){return null;}
		
		Map<String, _Word> word = new HashMap<String, _Word>();
		Tagger tagger = new Tagger("-d " + _dictionaryPath);
		
		for(int i = 0; i < list.size(); i++){
			String text = list.get(i);
			String[] token = text.split("\t");
			if(token.length <= 1) // "raing \t comment 형식의 문자열을 받음.
				continue;
			
			String rating = token[0];
			String taggedWords = tagger.parse(token[1]); // comment의 형태소 분석
//			try {
//				fw.write("평점 : " + rating + "\n댓글 : " + token[1] + "\n");
//				fw.write(taggedWords + "\n\n\n\n\n\n\n");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			token = taggedWords.split("\n");
			for(String t : token){
				if(t.matches(_wordClass)){ // 원하는 품사태그를 가진 단어를 처리
					String[] tmp = t.split("\t");
					if(!word.containsKey(tmp[0])){ // 기존에 카운트된 단어가 아니면 word Map에 입력
						word.put(tmp[0], new _Word(tmp[0]));
					}
					word.get(tmp[0]).addCount(Integer.parseInt(rating)); // 워드 카운트를 1 올림
				}
			}
		}
//		_ValueComparator comp = new _ValueComparator(word);
//		Map<String, _Word> result = new TreeMap<String, _Word>(comp);
//		result.putAll(word);
		return pickOutValidWords(word);
	}
	
	/**
	 * @discription 유효한 워드를 걸러내는 메서드
	 * @param wordList
	 * @return
	 */
	private Map<String, _Word> pickOutValidWords(Map<String, _Word> wordList){
		Set<String> keyset = new HashSet<String>();
		for(String key : wordList.keySet()){
			_Word word = wordList.get(key);
			
//			if(word.totalCount / _Word.total >= 0.01){
//				continue;
//			}
			if(!_isValidWord(word)){
				keyset.add(key); continue;
			}
			
		}

		for(String key : keyset)
			wordList.remove(key);
		
		return wordList;
	}
	
	/**
	 * @discription xml형태로 된 comment list를 "rating \t comment 형태의 리스트로 변경/리턴
	 * @param ratingData
	 * @param encoding
	 * @return
	 */
	public List<String> getTextFromXml(File ratingData, String encoding){
		List<String> result = new ArrayList<String>();
		try {
			Document doc = Jsoup.parse(ratingData, encoding);
			Elements elements = doc.getElementsByTag("opinion");
			for(Element e : elements){
				String rating = e.getElementsByTag("rating").text();
				String comment = e.getElementsByTag("comment").text();
				result.add(rating + "\t" + comment);
			}
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getTextFromXml(File ratingData){
		return getTextFromXml(ratingData, "utf-8");
	}
	
	private class _ValueComparator implements Comparator<String> {

	    Map<String, _Word> base;
	    _ValueComparator(Map<String, _Word> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        return base.get(a).totalCount - base.get(b).totalCount;
	    }
	}
	
	/**
	 * @discription 워드카운트를 가지는 클래스
	 * @author taeyong
	 *
	 */
	private static class _Word{
		static int total = 0;
		int wordScore;
		String word;
		int goodCommentCount, badCommentCount, normalCommentCount, totalCount;
		_Word(String word){
			this.word = word;
			totalCount = goodCommentCount = normalCommentCount = badCommentCount = 0;
			total++;
		}
		
		void addCount(int rating){
			totalCount++;
			switch(rating){
			case 1:	case 2:	case 3:	case 4: case 5:
				badCommentCount++; break;
			case 6: case 7: case 8:
				normalCommentCount++; break;
			case 9:	case 10:
				goodCommentCount++; break;
			}
		}
	}
	
	private boolean _isValidWord(_Word word){
		double goodRate = 0.0, badRate = 0.0;
		int diff = word.goodCommentCount - word.badCommentCount;
		if(diff > 0 && ((double)word.badCommentCount / word.goodCommentCount) <= 0.5){
			goodRate = ((double)word.goodCommentCount + word.badCommentCount) / word.totalCount;
		}
		else if(diff < 0 && ((double)word.goodCommentCount / word.badCommentCount) <= 0.5){
			badRate = ((double)word.goodCommentCount + word.badCommentCount) / word.totalCount;
		}
		else{
			return false;
		}
		
		if(goodRate < 0.5 && badRate < 0.5){
			return false;
		}
		
		if(goodRate > 0.0)
			word.wordScore = (int) (goodRate * 10);
		else
			word.wordScore = 10 - (int) (badRate * 10);
			
		
		return true;
	}
//	
//	private int _getWordScore(int rate){
//		if(rate > 0.9)
//			return 10;
//		else if(rate > 0.8)
//			return 9;
//	}
	
	public static void main(String[] args) throws IOException {
		FileWriter fw = new FileWriter(new File("test2.txt"));
		PosNagWordCounter counter = new PosNagWordCounter();
		List<String> list = counter.getTextFromXml(new File("haeundae_opinion.xml"));
		Map<String, _Word> result = counter.wordCount(list);
		fw.write("total : " + _Word.total + "\n");
		fw.write(String.format("%-10s%-15s%-15s%-15s%-15s%-15s\n", "Words", "total","GoodComment", "NormalComment", "BadComment", "WordScore"));
		for(String key : result.keySet()){
			_Word word = result.get(key);
			String tmp = String.format("%-10s\t%-15d\t%-15d\t%-15d\t%-15d\t%-15d\n", word.word, word.totalCount, word.goodCommentCount, word.normalCommentCount, word.badCommentCount, word.wordScore);
			fw.write(tmp);
		}
		fw.close();
	}
}
