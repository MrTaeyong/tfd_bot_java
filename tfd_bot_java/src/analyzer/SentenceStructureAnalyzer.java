/**
 * @FileName	: SentenceStructure.java
 * @Package		: analyzer
 * @Date		: 2014. 8. 15.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.chasen.mecab.Tagger;

import util.WordAnalyzePreProcessor;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import controller.DBController;

/**
 * @Class	: SentenceStructureAnalyzer
 * @Date	: 2014. 8. 15.
 * @Author	: Taeyong
 */
public class SentenceStructureAnalyzer {
//	private static Map<String, Double> _wordEmotionDictionary;
	private static Set<String> _wordEmotionDictionary;
	private final static String _wordEmotionDictionaryPath = "/Users/taeyong/Desktop/OpinionMining/카테고리별사전/맛사전.txt";
//	private final static String _wordClass = "(.*NNG.*|.*NNP.*|.*NNB.*|.*NR.*|.*NP.*|.*VV.*|.*VA.*|.*MAG.*|.*XR.*)";
	private final static String _endWordClass = "(.*MAJ.*|.*EF.*)";
	private final static String _unnecessaryWordClass = "(.*SF.*|.*SE.*|.*SSO.*|.*SSC.*|.*SC.*|.*SY.*)";
	private final static String _dictionaryPath = "/usr/local/lib/mecab/dic/mecab-ko-dic";
	private final Tagger tagger;
	
	static {
		System.load(System.getProperty("java.library.path") + "/libMeCab.so");
		
		// 단어 감정 사전을 로드
//		_wordEmotionDictionary = new HashMap<String, Double>();
//		BufferedReader br;
//		try {
//			br = new BufferedReader(new FileReader(_wordEmotionDictionaryPath));
//			String temp;
//			while((temp = br.readLine()) != null){
//				String[] token = temp.split("\t");
//				String key = token[0] + "\t" + token[1];
//				_wordEmotionDictionary.put(key, Double.parseDouble(token[6]));
//			}
//			br.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
		_wordEmotionDictionary = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(_wordEmotionDictionaryPath));
			String temp;
			while((temp = br.readLine()) != null){
				_wordEmotionDictionary.add(temp);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public SentenceStructureAnalyzer(){
		tagger = new Tagger("-d " + _dictionaryPath);
	}
	
	/**
	 * 문장을 문단으로 나누고 문단 중에 사전에 존재하는 단어를 포함하는 문단만을 골라내는 메소드 
	 * @param sentence
	 * @param validityCheck 사전에 의해 문단을 골라낼지 말지를 결정
	 * @return
	 */
	public List<String> getParagraphs(String sentence, boolean validityCheck){
		List<String> result = new ArrayList<String>();
		
		boolean isOpinion = false;
		int periodCount = 0;
		
		sentence = WordAnalyzePreProcessor.removeConsonantAndVowel(sentence);
		String[] taggedWords = tagger.parse(sentence).split("\n"); // 품사태깅된 단어별로 배열에 담음
		
		// 품사태깅된 단어들을 문단 단위로 묶어서 리스트에 담음
		String tempString = "";
		for(String taggedWord : taggedWords){
			if(taggedWord.matches(_endWordClass)){
				tempString += taggedWord + "\n";
				result.add(tempString);
				if(validityCheck && !isOpinion) // 의견이 반영된 문장만 리스트에 넣음
					result.remove(result.size() - 1);
				tempString = "";
				isOpinion = false;
				periodCount = 0;
			}
			else if(!taggedWord.matches(_unnecessaryWordClass)){
				if(periodCount == 1 && tempString.length() > 0){
					result.add(tempString);
					if(validityCheck && !isOpinion)
						result.remove(result.size() -1);
					isOpinion = false;
					tempString = "";
				}
				tempString += taggedWord + "\n";
				if(validityCheck && _wordEmotionDictionary.contains(taggedWord))
					isOpinion = true;
				periodCount = 0;
			}
			else if(taggedWord.matches(".*SF.*|.*SY.*") && taggedWord.startsWith(".")){
				periodCount++;
			}
		}
		
		// tempString에 남은 문자열을 결과에 추가함
		if(tempString.length() > 0 && isOpinion)
			result.add(tempString);
		
		if(result.size() > 0)
			return result;
		return null;
	}
	
	public List<String> getParagraphs(String sentence){
		return getParagraphs(sentence, false);
	}
	
	/**
	 * 문장에서 사전에 있는 단어들을 추출하여 반환하는 메소드
	 * @param sentence
	 * @return
	 */
	public List<String> getDictWordOfSentence(String sentence){
		List<String> result = new ArrayList<String>();
		
		String[] taggedWords = tagger.parse(sentence).split("\n");
		for(String taggedWord : taggedWords){
//			if(_wordEmotionDictionary.containsKey(taggedWord)){
			if(_wordEmotionDictionary.contains(taggedWord)){
				result.add(taggedWord);
			}
		}
		
		return result;
	}
	
	/**
	 * 파일에 있는 블로그 내용에서 사전에 있는 단어들을 추출하여 장소에 대한 특징을 추출하는 메소드
	 * @throws IOException
	 */
	public void getDictWordFromSentenceInFile(File file) throws IOException{
		FileWriter fw = new FileWriter(file.getCanonicalPath() + "out");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String temp;
		int count = 0;
		while((temp = br.readLine()) != null){
			if(temp.length() > 0 && temp.charAt(0) == '='){
				String title = br.readLine();
//				br.readLine(); br.readLine();
				temp = br.readLine();
				List<String> result = getDictWordOfSentence(temp);
				fw.write(title + "\n");
				for(String s : result)
					fw.write(s + "\n");
				fw.write("\n\n\n");
				fw.flush();
				count++;
			}
		}
		System.out.println(count);
		br.close();
		fw.close();
	}
	
	
	/**
	 * DB에 모여진 블로그 내용에서 사전에 있는 단어들을 추출하여 장소에 대한 특징을 추출하는 메소드
	 * @param placeName 특징 추출을 원하는 장소의 이름
	 * @param dbcon
	 * @throws CommunicationsException 
	 */
	public boolean getDictWordFromSentenceInDB(String placeName, DBController dbcon, File outputFile) throws FileNotFoundException, CommunicationsException{
		List<Map<String, String>> blogContentOfPlace = dbcon.getData("select title, content from blog where place_name='" + placeName + "'");
		FileWriter fw = null;
		try {
			fw = new FileWriter(outputFile);
			for(Map<String, String> record : blogContentOfPlace){
				List<String> result = getDictWordOfSentence(record.get("content"));
				fw.write(record.get("title") + "\n");
				for(String s : result)
					fw.write(s + "\n");
				if(result.size() > 0){
					fw.write("\n\n\n");
					fw.flush();
				}
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean getDictWordFromSentenceInDB(String placeName, DBController dbcon) throws CommunicationsException{
		try {
			if(getDictWordFromSentenceInDB(placeName, dbcon, new File(placeName + ".txt")))
				return true;
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args) {
		SentenceStructureAnalyzer analyzer = new SentenceStructureAnalyzer();
		try {
			analyzer.getDictWordFromSentenceInDB("스무디킹 홍대점", DBController.newInstance(DBController.Type.TFD));
		} catch (CommunicationsException e) {
			e.printStackTrace();
		}
	}
}
