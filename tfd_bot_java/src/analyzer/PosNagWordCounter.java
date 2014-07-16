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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	
	public Map<String, Word> wordCount(List<String> list){		
//		Map<String, Word> word = new HashMap<String, Word>();
		Map<String, Word> word = new TreeMap<String, Word>(new _ValueComparator());
		
		Tagger tagger = new Tagger("-d " + _dictionaryPath);
		
		for(int i = 0; i < list.size(); i++){
			String text = list.get(i);
			String[] token = text.split("\t");
			if(token.length <= 1) // "raing \t comment 형식의 문자열을 받음.
				continue;
			
			String rating = token[0];
			String taggedWords = tagger.parse(token[1]); // comment의 형태소 분석

			token = taggedWords.split("\n");
			for(String t : token){
				if(t.matches(_wordClass)){ // 원하는 품사태그를 가진 단어를 처리
					String[] tmp = t.split("\t");
					if(!word.containsKey(tmp[0])){ // 기존에 카운트된 단어가 아니면 word Map에 입력
						word.put(tmp[0], new Word(tmp[0]));
					}
					word.get(tmp[0]).addCount(Integer.parseInt(rating)); // 워드 카운트를 1 올림
				}
			}
		}
		return pickOutValidWords(word);
	}
	
	/**
	 * @discription 유효한 워드를 걸러내는 메서드
	 * @param wordList
	 * @return
	 */
	private Map<String, Word> pickOutValidWords(Map<String, Word> wordList){
		Set<String> keyset = new HashSet<String>();
		for(String key : wordList.keySet()){
			Word word = wordList.get(key);
			
			if(!_isValidWord(word)){
				keyset.add(key);
				continue;
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
	
	public List<String> getTextFromXml(File[] ratingData){
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < ratingData.length; i++){
			result.addAll(getTextFromXml(ratingData[i]));
		}
		return result;
	}
	
	public File[] getFileArray(String[] path){
		File[] result = new File[path.length];
		for(int i = 0; i < path.length; i++){
			result[i] = new File(path[i]);
		}
		return result;
	}
	
	private class _ValueComparator implements Comparator<String> {

//	    Map<String, Word> base;
//	    _ValueComparator(Map<String, Word> base) {
//	        this.base = base;
//	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
//	    public int compare(String a, String b) {
//	        return base.get(a).totalCount - base.get(b).totalCount;
//	    }
	    public int compare(String a, String b) {
	        return a.compareTo(b);
	    }
	}
	
	/**
	 * @discription 워드카운트를 가지는 클래스
	 * @author taeyong
	 *
	 */
	public static class Word{
		static int total = 0;
//		int wordScore;
		double wordScore;
		String word;
		int goodCommentCount, badCommentCount, normalCommentCount, totalCount;
		Word(String word){
			this.word = word;
			totalCount = goodCommentCount = normalCommentCount = badCommentCount = 0;
			total++;
		}
		
		void addCount(int rating){
			totalCount++;
			switch(rating){
			case 1:	case 2:	case 3:	case 4: case 5 : case 6:
				badCommentCount++; break;
			case 7: case 8:
				normalCommentCount++; break;
			case 9:	case 10:
				goodCommentCount++; break;
			}
		}
	}
	
	private boolean _isValidWord(Word word){
		double goodRate = 0.0, badRate = 0.0;
		int diff = word.goodCommentCount - word.badCommentCount;
		// goodComment와 badComment의 비율이 2배이상 차이 날때 유효 워드로 인정
		if((double)word.totalCount / Word.total < 0.001)
			return false;
		if(diff > 0 && ((double)word.badCommentCount / word.goodCommentCount) <= 0.5){
			// 현재 word에서 goodComment의 비율을 구함
			goodRate = (double)word.goodCommentCount/ (word.totalCount - word.normalCommentCount);
			// goodComment의 비율에 전체 word카운트에서 현재 word의 비율을 구해서 더해줌
			goodRate += (double)word.totalCount / Word.total;
		}
		else if(diff < 0 && ((double)word.goodCommentCount / word.badCommentCount) <= 0.5){
			badRate = (double)word.badCommentCount / (word.totalCount - word.normalCommentCount);
			badRate += (double)word.totalCount / Word.total;
		}
		else{
			return false;
		}
		
		if(goodRate <= 0.5 && badRate <= 0.5){
			return false;
		}
		
		// 양수 점수 == goodComment
		// 음수 점수 == badComment
		if(goodRate > 0.0)
			word.wordScore = goodRate * 10 - 5;
		else
			word.wordScore = (badRate * 10) * -1 + 5;
			
		
		return true;
	}
	
	public static void main(String[] args) throws IOException {
		FileWriter fw = new FileWriter(new File("/Volumes/Macintosh HD/Users/taeyong/Desktop/test1.txt"));
		PosNagWordCounter counter = new PosNagWordCounter();
//		String[] path = {"haeundae_opinion.xml", "thirst_opinion.xml"};
		String[] path = {"/Volumes/Macintosh HD/Users/taeyong/Desktop/진영_음식점.xml"};
//		File files = new File("/Volumes/Macintosh HD/Users/taeyong/Desktop/rating.xml");
		File[] files = counter.getFileArray(path);
		List<String> list = counter.getTextFromXml(files);
		Map<String, Word> result = counter.wordCount(list);
		fw.write("total : " + Word.total + "\n");
		fw.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\n", "Words", "total","GoodComment", "NormalComment", "BadComment", "WordScore"));
//		for(String key : result.keySet()){
		for(Entry e : result.entrySet()){
//			Word word = result.get(key);
			Word word = (Word) e.getValue();
			String tmp = String.format("%s\t%d\t%d\t%d\t%d\t%.5f\n", word.word, word.totalCount, word.goodCommentCount, word.normalCommentCount, word.badCommentCount, word.wordScore);
			fw.write(tmp);
		}
		fw.close();
	}
}
