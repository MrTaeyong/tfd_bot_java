package parser.naver;

import java.util.ArrayList;

import parser.ParserInterface;

abstract public class NaverSearch implements ParserInterface{
	public final static int NAVER_LOCAL = 0;
	public final static int NAVER_BLOG = 1;

	@Override
	public Object getResult(String keyword) {
		// TODO Auto-generated method stub
		// Child class must overriding this method.
		return null;
	}
	
	public static NaverSearch getInstance(int type){
		ArrayList<NaverSearch> instance = new ArrayList<NaverSearch>();
		instance.add(new NaverLocal());
		instance.add(new NaverBlog());
		return instance.get(type);
	}
}
