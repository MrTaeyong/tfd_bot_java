/**
 * @FileName	: PosNagWordCounter.java
 * @Package		: analyzer
 * @Date		: 2014. 6. 28.
 * @Author		: Taeyong
 */
package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.chasen.mecab.Node;
import org.chasen.mecab.Tagger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import controller.DBController;

/**
 * @Class	: PosNagWordCounter
 * @Date	: 2014. 6. 28.
 * @Author	: Taeyong
 */
public class PosNagWordCounter extends TextMining{
	// 워드 카운드를 할 품사태그
//	private String _wordClass = "(.*NNG.*|.*NNP.*|.*NNB.*|.*NR.*|.*NP.*|.*VV.*|.*VA.*|.*MAG.*|.*XR.*)";
	private String _wordClass = "(.*VA.*|.*MAG.*|.*XR.*)";
	// 은전한닢 형태소 분석기의 사전 경로
	private String _dictionaryPath = "/usr/local/lib/mecab/dic/mecab-ko-dic";
	private Tagger tagger = new Tagger("-d" + _dictionaryPath);
	
	@Override
	Map<String, Integer> wordCount(String text) {
		return null;
	}

	@Override
	Map<String, Integer> wordCount(ArrayList<String> list) {
		return null;
	}
	
	/**
	 * @description	평점 + "\t" + 댓글 형태의 문자열 리스트에서 단어의 갯수를 셈 
	 * @param list 평점 + "\t" + 댓글 형태 문자열의 리스트
	 * @return 전체 리스트에서 동일한 단어가 등장한 횟수를 단어와 횟수를 묶어 반환
	 */
	public Map<String, Word> wordCount(List<String> list){		
		Map<String, Word> word = new TreeMap<String, Word>(new _ValueComparator());
		
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
					String key = tmp[0] + "\t" + tmp[1];
					if(!word.containsKey(key)){ // 기존에 카운트된 단어가 아니면 word Map에 입력
						word.put(key, new Word(key));
					}
					word.get(key).addCount(Integer.parseInt(rating)); // 워드 카운트를 1 올림
				}
			}
		}
		return pickOutValidWords(word);
	}
	
	/**
	 * @description 댓글에 존재하는 모든 단어를 추출
	 * @param list 평점 + "\t" + 댓글 형태 문자열의 리스
	 * @return 중복되지 않는 단어 셋
	 */
	public Set<String> getWordOfRatedComments(List<String> list){
		Set<String> result = new HashSet<String>();
		
		for(int i = 0; i < list.size(); i++){
			String text = list.get(i);
			String[] token = text.split("\t");
			String taggedWords = tagger.parse(token[1]);
			
			token = taggedWords.split("\n");
			for(String t : token){
				if(t.matches(_wordClass)){
					result.add(t);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 유효한 워드를 걸러내는 메서드
	 * @param wordList 단어와 단어가 긍정, 부정, 중립 글에서 출현한 횟수의 묶음 리스트
	 * @return 단어 출현 비도에 따른 계산식에 의해 걸러진 단어 리스트
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
			badRate -= (double)word.totalCount / Word.total;
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
	
	/**
	 * DB로 부터 평점, 댓글을 가져와 문자열 리스트를 반환
	 * @param dbcon rating과 comment필드가 있는 table을 가진 DB컨트롤러
	 * @return 평점 + "\t" + 댓글 형태 문자열의 리스트
	 */
	public List<String> getTextFromDB(DBController dbcon){
		List<String> result = new ArrayList<String>();
		List<Map<String, String>> queryResult = null;
		queryResult = dbcon.getData("select rating, comment from external_comment");
		for(int i = queryResult.size() - 1; i >= 0; i--){
			result.add(queryResult.get(i).get("rating") + "\t" + queryResult.get(i).get("comment"));
			queryResult.remove(i);
		}
		
		if(result.size() > 0)
			return result;
		return null;
	}
	
	/**
	 * DB로 부터 평점, 댓글을 긍정 부정의 갯수를 맞춰 가져와 문자열 리스트를 반환
	 * @param dbcon rating과 comment필드가 있는 table을 가진 DB컨트롤러
	 * @return 평점 + "\t" + 댓글 형태 문자열의 리스트
	 */
	public List<String> getBalancedTextFromDB(DBController dbcon){
		// DB에 있는 댓글에서 호의적 댓글과 부정정 댓글의 갯수를 맞추어서 추출
		List<String> result = new ArrayList<String>();
		int size = 0;
		List<Map<String, String>> queryResult = null;
		queryResult = dbcon.getData("select rating, comment from external_comment where rating < 4");
		
		
		size = queryResult.size();
		System.out.println("bad : " + size);
		for(int i = queryResult.size() - 1; i >= 0; i--){
			result.add(queryResult.get(i).get("rating") + "\t" + queryResult.get(i).get("comment"));
			queryResult.remove(i);
		}		
		
		queryResult = dbcon.getData("select rating, comment from external_comment where rating > 7 limit " + size);
		System.out.println("good : " + queryResult.size());
		for(int i = queryResult.size() - 1; i >= 0; i--){
			result.add(queryResult.get(i).get("rating") + "\t" + queryResult.get(i).get("comment"));
			queryResult.remove(i);
		}
		
		if(result.size() > 0)
			return result;
		return null;
	}
	
	/**
	 * xml형태로 된 comment list를 "rating \t comment 형태의 리스트로 변경/리턴
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
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getTextFromXml(File ratingData){
		return getTextFromXml(ratingData, "utf-8");
	}
	
	public List<String> getTextFromXml(File[] ratingData){
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < ratingData.length; i++)
			result.addAll(getTextFromXml(ratingData[i]));
		return result;
	}
	
	public List<String> getTextFromXml(File[] ratingData, String encoding){
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < ratingData.length; i++)
			result.addAll(getTextFromXml(ratingData[i], encoding));
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
	    public int compare(String a, String b) {
	        return a.compareTo(b);
	    }
	}
	
	/**
	 * 워드카운트를 가지는 클래스
	 * @author taeyong
	 *
	 */
	public static class Word{
		static int total = 0;
		double wordScore;
		String word, wordClass, originalContent;
		int goodCommentCount, badCommentCount, normalCommentCount, totalCount;
		Word(String originalContent, String word){
			this(word);
			this.originalContent = originalContent;
		}
		
		Word(String word){
			this.word = word;
			totalCount = goodCommentCount = normalCommentCount = badCommentCount = 0;
			total++;
		}
		
		void addCount(int rating){
			totalCount++;
			switch(rating){
			case 1:	case 2:	case 3:
				badCommentCount++; break;
			case 4: case 5 : case 6: case 7: 
				normalCommentCount++; break;
			case 8: case 9:	case 10:
				goodCommentCount++; break;
			}
		}
	}
	
	/**
	 * 긴 문장에서 출현한 단어들을 단어에 품사태깅하여 반환 
	 * @param sentenceList 문장 리스트
	 * @return 중복되진 않는 단어 셋
	 */
	public Set<String> getWordOfSentences(List<String> sentenceList){
		Set<String> result = new HashSet<String>();
		for(int i = sentenceList.size() - 1; i >= 0; i--){
			if(sentenceList.get(i) == null)
				continue;
			String sentence = WordAnalyzePreProcessor.removeConsonantAndVowel(sentenceList.get(i));
			Node taggedWords = tagger.parseToNode(sentence);
			for(; taggedWords != null; taggedWords = taggedWords.getNext()){
				if(taggedWords.getFeature().matches(_wordClass)){
					String word = taggedWords.getSurface() + "\t" + taggedWords.getFeature();
					result.add(word);
				}
			}
			sentenceList.remove(i);
		}
		
		if(result.size() > 0)
			return result;
		return null;
	}
	
	public List<String> getSentenceFromFile(File file) throws FileNotFoundException{
		List<String> result = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String sentence;
		try {
			while((sentence = br.readLine()) != null){
				br.readLine();
				sentence = br.readLine();
				result.add(sentence);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {}
		}
		
		if(result.size() > 0)
			return result;
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		FileWriter fw = new FileWriter(new File("/Users/taeyong/Desktop/cafe.txt"));
		PosNagWordCounter counter = new PosNagWordCounter();
		
		// xml을 통해 평점 계산방법
//		String[] path = {"haeundae_opinion.xml", "thirst_opinion.xml"};
//		String[] path = {"/Users/taeyong/Desktop/진영_음식점.xml"};
//		File files = new File("/Volumes/Macintosh HD/Users/taeyong/Desktop/rating.xml");
//		File[] files = counter.getFileArray(path);
//		List<String> list = counter.getTextFromXml(files, "euc-kr");
		
		// DB를 통해 평점 계산방법
//		List<String> list = counter.getTextFromDB(new LocalDBController());
//		List<String> list = counter.getBalancedTextFromDB(new LocalDBController());
		
//		Map<String, Word> result = counter.wordCount(list);
//		fw.write("total : " + Word.total + "\n");
//		fw.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\n", "Words", "total","GoodComment", "NormalComment", "BadComment", "WordScore"));
//		for(Entry e : result.entrySet()){
//			Word word = (Word) e.getValue();
//			String tmp = String.format("%s\t%d\t%d\t%d\t%d\t%.5f\n", word.word, word.totalCount, word.goodCommentCount, word.normalCommentCount, word.badCommentCount, word.wordScore);
//			fw.write(tmp);
//		}
//		fw.close();
		
//		Set<String> result = counter.getWord(list);
//		for(String s : result){
//			fw.write(s + "\n");
//		}
//		fw.close();
		
		Set<String> result = counter.getWordOfSentences(counter.getSentenceFromFile(new File("/Users/taeyong/Desktop/blogTest3.txt")));
		for(String word : result)
			fw.write(word + "\n");
		fw.close();
	}
}
