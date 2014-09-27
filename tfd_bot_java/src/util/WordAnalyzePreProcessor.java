/**
 * @FileName	: WordAnalyzePreProcessor.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 7. 16
 * @Author		: Taeyong
 */
package util;

/**
 * @Class		: WordAnalyzePreProcessor
 * @Date		: 2014. 7. 16
 * @Author		: Taeyong
 */
public class WordAnalyzePreProcessor {
	private final static String _CONSONANT = "ㄱ|ㄴ|ㄷ|ㄹ|ㅁ|ㅂ|ㅅ|ㅇ|ㅈ|ㅊ|ㅋ|ㅌ|ㅍ|ㅎ";
	private final static String _VOWEL = "ㅏ|ㅑ|ㅓ|ㅕ|ㅗ|ㅛ|ㅜ|ㅠ|ㅡ|ㅣ|ㅐ|ㅒ|ㅔ|ㅖ|ㅢ|ㅟ|ㅚ|ㅙ|ㅞ";
	private final static String  _WORD_CLASS = "(.*NNG.*|.*NNP.*|.*NNB.*|.*NR.*|.*NP.*|.*VV.*|.*VA.*|.*VX.*|.*MAG.*|.*XR.*)";
	
	/**
	 * 문자열에서 자음, 모음이 단독으로 쓰인 경우 공백으로 대체
	 * @param sentence
	 * @return
	 */
	public static String removeConsonantAndVowel(String sentence){
		try{
			sentence = sentence.replaceAll(_CONSONANT, "");
			sentence = sentence.replaceAll(_VOWEL, "");
			return sentence;
		} catch (NullPointerException e){
			return null;
		}
	}
	
	/**
	 * _WORD_CLASS 상수에 기록된 단어품사와 일치하는 단어인지를 반환
	 * @param wordClass
	 * @return 일치 : true, 불일치 : false
	 */
	public static boolean isValidWordClass(String wordClass){
		if(wordClass.matches(_WORD_CLASS))
			return true;
		else
			return false;
	}
}
