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
	
	public static String removeConsonantAndVowel(String sentence){
		try{
			sentence = sentence.replaceAll(_CONSONANT, "");
			sentence = sentence.replaceAll(_VOWEL, "");
			return sentence;
		} catch (NullPointerException e){
			return null;
		}
	}
	
	public static boolean isValidWordClass(String wordClass){
		if(wordClass.matches(_WORD_CLASS))
			return true;
		else
			return false;
	}
}
