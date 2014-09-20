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
	
	public static NaverSearch getInstance(SearchType type){
		switch(type){
		case NAVER_LOCAL : return new NaverLocal();
		case NAVER_BLOG : return new NaverBlog();
		case NAVER_IMAGE : return new NaverImage();
		default : return null;
		}
	}
}
