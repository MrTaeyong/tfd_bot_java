/**
 * @FileName	: SentenceStructure.java
 * @Package		: analyzer
 * @Date		: 2014. 8. 15.
 * @Author		: Taeyong
 */
package analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chasen.mecab.Tagger;

import controller.DBController;

/**
 * @Class	: SentenceStructureAnalyzer
 * @Date	: 2014. 8. 15.
 * @Author	: Taeyong
 */
public class SentenceStructureAnalyzer {
	private static Map<String, Double> _wordEmotionDictionary;
//	private final static String _wordEmotionDictionaryPath = "/Users/taeyong/Desktop/wordEmotionDict.txt";
	private final static String _wordEmotionDictionaryPath = "/Users/taeyong/Desktop/맛사전.txt";
	private final static String _wordClass = "(.*NNG.*|.*NNP.*|.*NNB.*|.*NR.*|.*NP.*|.*VV.*|.*VA.*|.*MAG.*|.*XR.*)";
//	private final String _endWordClass = "(.*MAJ.*|.*EF.*|.*EC.*)";
	private final static String _endWordClass = "(.*MAJ.*|.*EF.*)";
	private final static String _unnecessaryWordClass = "(.*SF.*|.*SE.*|.*SSO.*|.*SSC.*|.*SC.*|.*SY.*)";
	private final static String _dictionaryPath = "/usr/local/lib/mecab/dic/mecab-ko-dic";
	private final static Tagger tagger;
	
	static {
		System.load(System.getProperty("java.library.path") + "/libMeCab.so");
		tagger = new Tagger("-d " + _dictionaryPath);
//		System.loadLibrary("libMeCab.so");
//		System.out.println(System.getProperty("java.library.path"));
		
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
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		_wordEmotionDictionary = new HashMap<String, Double>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(_wordEmotionDictionaryPath));
			String temp;
			while((temp = br.readLine()) != null){
				_wordEmotionDictionary.put(temp, 1.0);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
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
//				if(validityCheck && taggedWord.matches(_wordClass) && _wordEmotionDictionary.containsKey(taggedWord))
//					isOpinion = true;
//				if(validityCheck && taggedWord.matches(_emotionalWordClass))
				if(validityCheck && _wordEmotionDictionary.containsKey(taggedWord))
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
	
	public List<String> getDictWordOfSentence(String sentence){
		List<String> result = new ArrayList<String>();
		Tagger tagger = new Tagger("-d " + _dictionaryPath);
		
		String[] taggedWords = tagger.parse(sentence).split("\n");
		for(String taggedWord : taggedWords){
			if(_wordEmotionDictionary.containsKey(taggedWord)){
				result.add(taggedWord);
			}
		}
		
		return result;
	}
	
	public void getDictWordOfSentenceTestProcedure() throws IOException{
		FileWriter fw = new FileWriter("/Users/taeyong/Desktop/blogtest4.txt");
		BufferedReader br = new BufferedReader(new FileReader("/Users/taeyong/Desktop/blogTest3.txt"));
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
	
	public void getDictWordOfSentenceFromDB(String placeName, DBController dbcon){
		List<Map<String, String>> blogContentOfPlace = dbcon.getData("select title, content from blog where place_name='" + placeName + "'");
		FileWriter fw = null;
		try {
			fw = new FileWriter(placeName + ".txt");
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
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		SentenceStructureAnalyzer analyzer = new SentenceStructureAnalyzer();
//		String sentence = "오랜만에여친과신나게홍대데이트쿵쿵짝짝룰룰랄랄♪신나게놀다보니어느새저녁시간이되버렸네요!너무배고픈나머지홍대주변을서성거리다찾은홍대맛집［돈코보쌈］이라는음식점을발견☆_☆바로들어갔지요!너무배고파들어간곳에오잉!홍대맛집으로 유명한 곳이더라구요. 유명한 연예인들도 많이 오고 맛집프로에서 많이 출현하고 오 기대가 너무 되더라구요. ​ ​ ​ ​ 여친과 저는 일단 이곳의 사이드메뉴인 ㄷΗ구납작만두, 밥전, 화산계란찜을 일단 주문하고 주 메뉴로 모듬보쌈과 돈코보쌈에만 있다는 곱창보쌈을 주문했지요! 주문을 하니 밑반찬이 두두둥~ 밑반찬도 맛있고 정말 잘왔다는 생각이 들었어요. ​ ​   화산계란찜을 냠냠 정말 맛있더라구요! 집에서 계란찜하면 막 짜게 해먹고 그랬는데 이곳에 화산계란찜은 짜지 않고 부드럽다는 ​간이 딱 배여있다고 해야할까요~침샘을 어택어택!! ​ ﻿ ​ ​ ㄷΗ구납작만두도 나왔어요. 납작만두라 참 신기했는데 먹어보니 꿀맛꿀맛!! 돈코보쌈에서는 납작만두를 정말 맛있게 먹는 법이 있다고하더라구요. 그건 있다가 보여드릴께요! 밥전이 나왔어요. 김치전인 줄았는데 밥이라니 정말 밥으로 만들었는 지 궁금해서 한입 냠냠 밥으로 만들어서 그런지 담백하고 고소한 맛이!! 사이드메뉴가 이렇게 맛있으니 홍대맛집으로 유명할 수 밖에 없다는 생각이 들더군요. ​ ​   저희들이 주문한 모듬보쌈이 나왔어요. 보쌈에는 쌈장과 채소가 빠질 수 없죠. 쌈장도 그냥 찍어먹었는데 맛있고! 상추와 깻입도 싱싱하다는!! ​ ​ ​ ​ 모듬보쌈은 훈제오리고기와 직접 만든 순대, 수육 이렇게 나왔는데 여러 고기를 나눠서 골라먹는 재미가 있어서 좋더라구요. ​3인3색의 맛이라 확실히 킹왕짱짱!! ​ ​   깻잎에 수육과 훈제오리를 싸서 입안으로 골인~정말 맛있어요!!! ​ ​ ​ ​ ​마지막으로 나온 음식은 곱창보​쌈!!! 돈코보쌈에만 있다고 하니 정말 신기했다는. 곱창과 보쌈의 조합이라 뭔가 안어울릴 거 같은데 같이 먹으니 오오~~~ 진심 짱 침샘을 자극하는 맛이였어요. ​곱창과 보쌈이 의리를 제대로 맞은 듯!!! 사장님은 이런 조합을 어디서 찾은건지 음식에 대한 연구를 많이 하시니 그래서 홍대맛집이 된 거 같다는 생각이 들더라구요. ​ ​     자 대구납작만두를 정말 맛있게 먹는 법을 이제 공개합니다! 두구두구 당황하지 않고 침착하게 대구납작만두에 곱창과 수육을 넣으면 끝!!! 저를 심쿵하게 하는 맛이라는!!! 저와 여친의 허기를 달래주고 입맛을 돌아오게 한 홍대맛집 돈코보쌈 최고최고!! 혹시 홍대데이트 중 보쌈이 땡기신 다면 돈코보쌈을 찾아주세용!! ​ 서울 마포구 연남동 568-38 돈코보쌈 전화번호 ▶ 02-333-9279";
//		String sentence = "    돈코보쌈 홍대유명한맛집이라고 해서 다녀왔어요 ^^ ​ 딸이랑 홍대나들이를 많이하는데 오늘은  남편이랑 온가족 ~ 더운데 주말 뭐 먹을까 하다..~ 한끼는 휘리릭~~~맛난거 먹자해서  평소에 안가본곳 가보고 싶어 홍대유명한맛집으로 연예인들 젊은이들이 많이간다는 돈코보쌈을 가봤어요 . ​ 홍대하면.. 사실 홍익대학교의 그 앞쪽의 거리가 유명하잖아요 젊음이 있고 먹거리가 있고 예술이 함께하는 그런데 금요일부터 주말은 정말 씨래빠~~ㅋㅋ 만 있어도 다 모이는지 진짜 사람이 많습니다. 특히 12시 지나면 . 젊음도 좋고 에너지 충전도 좋지만 너무 사람이 많음 저는 그걸 다 보느라 머리가 뱅뱅 돌기도 해요 ​ ​ 그래서 좀 조용한곳이 없을까 해서 알아본곳이 이 돈코보쌈이더라구요 . ​ 홍대메인street 건너편쪽에 또 홍대의맛집 멋집들이 있더라구요 . 좀더 강남필이 안나도 해야 할까요 ? 조용한 만남과 맛있는 만남을 원할때는 이쪽 street 좋을것같아요 ​ 위치가  홍대정문은 지하철 8번 9번출구로 나가신다면 이쪽 맛집거리는 1번과 2번 으로 나가시면 됩니다. ​ ​   ​ ​ ﻿저희는 차로 갔어요  주차는 가게 앞에 하시면되는데 ​ 늦점을 먹으로 갔을때도 차가 앞에 많이 주차되어있더군요 . 다른집들은 아직 오픈전인집들도 많았구요 . 저희 먹고 나올때쯤 저녁장사를 위해 여시는듯 많이 문을 열고 계시더라구요 ​ 방송출연이 지겹다니 ㅎㅎㅎ ​ 전 연예인들 많이 간다는 그런 맛집...다 돌아다니고 싶던데 . ​ 이궁 .. 콜레스테롤많다고 내원하라는데 이렇게 맛집만 찾아다니니 큰일입니다. ​             ﻿실내분위기는  가정집을 개조해서 식당으로 만드신듯했어요 . ​ 남편이 음식을 기다리는데 이집이 64년됐다네? 하는거예요  1951년 부터 했다는 이야긴데 .. .와.. ​ 3대째  하고 있는 집이군요 오호라~ ​ ​         ﻿나오면서 여기저기 살짝 담아봤어요 . 많은 연예인들이 오시는곳 싸인도 이리 많네요 . 아마도 메인 거리보다 이곳이 좀더 한적하고 여유롭게 이야기 나누실수 있어 그런걸까 싶었는데 전통이 있는 집이라 그분들 사이에도 인기가 있나보다 싶더군요 . ﻿ 왼쪽 , 오른쪽 양쪽으로 큰 방이 있구요 . 야외에서 드실수도 있더라구요 ​               ﻿저희는 안쪽 방으로 ^^ 시원하게 ~ 조용하게 ~ 은밀하게 ㅋㅋㅋㅋ ​       ﻿가격도 그리 비싸지 않고 착한가격에 양도 푸짐하고 반찬으로 나오는것에 이미 포만감이 차버려 ㅎㅎㅎ 저흰 시킨 음식 다 먹지도 못하고 말았어요 절대 반찬에 욕심내지 마세요 !!! 전 ..이놈의 떡볶이사랑... 떡볶기가 반찬으로 나오는건 또 뭐란말입니까~ 그런데.. 또 맛이있기까지 . 두접시 먹고 나니 배가 후덜덜 ~~ ​ ​ ​     ﻿갈때는 보쌈이 많이 올라와있길래 보쌈을 먹으로 갔는데요 . 사장님께 뭐 가 맛냐냐고 여쭤보니 신개발한 ~ ^^ 곱창모듬보쌈이 인기있다고 하시네요 . 접 곱창도 좋아라하는데 냄새나면 딱 질색인지라 돼지가 냄새 안나기 참 어려운 일이잖아요 ​ 돼지..냄새 안나요?~ 하고 여쭤보니 안나요 ~~하십니다 ​ 그래서 그걸로 낙점 ~~^^ 궁금한 ​밥전을 주문했어요       ​ ​ 요것이 저희가 시킨 곱창보쌈이랍니다 이름이 이게 정확한지는 ㅎㅎㅎ 비쥬얼 기가 막히죠? 그리고 먹는내내 불을 지펴서 먹어서따뜻하게 드실수 있어요 매운 곱창은 제가 맛난 보쌈은 남편과 아이가 먹었어요 . ​ 주시는 메뉴판은 오히려 진짜 나오는 실물보다 들 맛나게 보이더라구요 안타깝습니다 ㅎㅎㅎㅎ 제 사진이 더 리얼하고 실물과 흡사해요 ​     ﻿양이 아주 푸짐하죠? ​ 보쌈도 보쌈이고 곱창은 전혀 냄새가 안나고 맛있었어요 . 살짝 달큰하기도 했구요 ​     ﻿ 메인 보쌈요리가 나오기전에 반찬들이 나오는데요 양도 수북히 주시고 무엇보다 부추부침개와 떡볶이를 주십니다 이를 어쩌면 좋냔말이요 ~~~ 이 두가지 저를 살찌게 해준 주범들이거든요 . ​         ﻿그리고 돈코보쌈에서 놀랄 또한가지  !!! ​ 화산계란찜 . 완전대박 ㅎㅎㅎㅎ 폼페이 화산 폭발이라 명하노니 ~ 양도 모양도 ㅎㅎㅎ 즐겁네요 ​       ﻿어찌 저것이 넘치지 않고 저리 올라가서 꺼지지도 않고 있을까요 ? 주방에 들어가서 빼꼼히 보고 싶더이다 . ​         ﻿그리고 ... 돈코보쌈의 밥전 전 밥전은 처음 먹어봤어요  물론 집에서는 이렇게도 해먹지만 식당에서 이렇게 판매하시는건 첨이였거든요 . 사실 두려운 마음으로 시켜보고먹어봤~~~~습니다. ​ 딱 나올때 노릿하게 누룽지처럼 구워나오지  않아서 ... 이게 맛있을까 싶었거든요 ? 그런데 80%는 제가 혼자 다 먹었습니다 .ㅎㅎㅎ ​ 맛있어요 맛있어요 ㅎㅎㅎㅎ 워낙 제가 부침류도 좋아하지만 . 김치가 마구 들어간것도 아닌데 김치부침개맛도 나고 암튼 뭐라 해야 하나 . 맛있다!!!입니다. ​ 이렇게 먹으니 메인인 곱창보쌈이 양껏 다 못들어가죠 ~~​     ﻿ 깻잎에 요�게 넣어서 먹으니 ㅋㅋ 맛나더이다 . 가족이 이렇게  맛집 찾으며 먹는 것도 즐거운것같아요 . ​ ​ 이곳 돈코보쌈이 64년전통이라는게 그냥 나오는게 아니고 홍대유명한맛집으로 이름난것도 그냥 되는건 아닌것같아요 . 어머니의 손맛은 일단 양이 푸져야 하잖아요 . 대학생들 둘러앉아 소주한병 꼴깍 따면서 배도 채우고 착한 가격으로 밤지새우면 나라를 걱정하고 ㅎㅎㅎ 애인을 걱정하며 나름의 진지한 걱정을 나누는 장소로 아주 좋겠어요 . 남편과 한 이야기가  대학생들 와서 먹기 딱 좋겠네 였어요 . ​ ﻿뭐 먹으러가자면 좋아라 하는 딸래미 . 아주 열심히 먹고 차에 오르니 바로 주무시고 ㅎㅎㅎ 주말인데도 홍대에서 여유를 부리며 ..왔어요 .   ​   ​ 이쁜 커피집도 많고 이쪽 거리도 꽤나 홍대인들에게는 유명한 곳이 많아 자주 올것같아요 ~ ";
//		String sentence = "[홍대고기집] 홍대 2번 출구 보쌈 맛집 돈코보쌈.     홍대고기집 중 2번 출구 쪽 최고의 맛집이라 소개 받은 돈코보쌈에 다녀왔습니다.   현수막부터 자부심이 대단하네요.   과연 맛은 어떨지 고기귀신 멋곰이 출동합니다.   점심시간에 방문해서 그런지 바빠보이네요.   연예인들의 사인도 가득합니다.   돈코보쌈의 메뉴.   곱창보쌈이 새로 나왔다는데, 다이어터라 모듬보쌈으로 주문했어요.   사유리양도 다녀갔네요.   그녀의 솔직한 평이 궁금해집니다. 역시 음식점은 금연이어야만 하죠.   메뉴판 보고 밥전도 추가 주문합니다. 화산 계란찜은 점심에 가서 서비스라는!!!   여기까지는 맛있는 식사 할 생각에 기분이 매우 좋았으나......   급격히 기분이 상하는 사건이 발생하죠.   뭐 이건 하단에 적겠습니다.   모둠보쌈 2인분.   보쌈 외에 다양한 메뉴를 먹을 수 있는 구성이라 좋네요.   다이어터라 못 먹은 전.   밥전도 나옵니다.   전 안에 밥알이 보이네요.   말 그대로 밥 전입니다.   병천순대와 비슷한 느낌의 수제순대.   이건 조금 호불호가 갈리겠네요.   그리고 지인이 강추한 술국.   경상도식 육개장 비슷한 음식이라고 합니다.   일행의 평은 맛있다고 합니다.   화산계란찜.   이런 비쥬얼로 만드는 것이 기술이라고 하더군요.   보쌈.   칠레산 치고 맛있습니다.   수입 고기 치고 냄새도 많이 제거한 편이구요.   맛은 있는 집임에 틀림 없습니다.   함께 먹는 이 김치가 정말 물건.   이것과 함께라면 모든 보쌈이 맛있을 것 같은 맛!!         맛은 있는 집이니 추천합니다.   하지만 바쁜 시간에 가면 제대로 된 서비스를 못 받을 수도 있다는 점.   살다 살다 이런 대접은 처음이네요.   돈코보쌈. 서울시 마포구 연남동 568-38. 02-333-9279   이 골목으로 들어가서 20 m 전진하면 오른편에 보입니다.";
//		List<String> result = analyzer.getParagraphs(sentence, true);
//		
//		for(String paragraph : result){
//			System.out.println(paragraph + "\n\n\n\n\n");
//		}
//		analyzer.getDictWordOfSentenceFromDB("스무디킹 홍대점", DBController.newInstance(Type.TFD));
		try {
			analyzer.getDictWordOfSentenceTestProcedure();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
