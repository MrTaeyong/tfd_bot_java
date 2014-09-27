/**
 * @FileName	: NaverSearch.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 23.
 * @Author		: Taeyong
 */
package parser.naver;

import parser.ParserInterface;

/**
 * @Class	: NaverSearch
 * @Date	: 2014. 05. 23. 
 * @Author	: Taeyong
 */
abstract public class NaverSearch implements ParserInterface{
	public static enum SearchType { NAVER_LOCAL, NAVER_BLOG, NAVER_IMAGE }

	@Override
	abstract public Object getResult(String keyword);
	abstract public Object getCurrentState();
	
	/**
	 * 요청한 타입의 검색 객체를 반환
	 * @param type 원하는 검색 객체 타입
	 * @return
	 */
	public static NaverSearch getInstance(SearchType type){
		switch(type){
		case NAVER_LOCAL : return new NaverLocal();
		case NAVER_BLOG : return new NaverBlog();
		case NAVER_IMAGE : return new NaverImage();
		default : return null;
		}
	}
}
