/**
 * @FileName	: TwitterSearch.java
 * @Project		: tfd_java_bot
 * @Date		: 2014. 06. 02.
 * @Author		: Taeyong
 */
package parser.twitter;

import java.util.ArrayList;
import java.util.List;

import parser.Connector;
import parser.ParserInterface;
import twitter4j.QueryResult;
import twitter4j.Status;

/**
 * @Class	:TwitterSearch
 * @Date	: 2014. 06. 02.
 * @Author	: Taeyong
 */
public class TwitterSearch implements ParserInterface{
	private QueryResult queryResult;
	
	@Override
	public Object getResult(String keyword) {
		// TODO Auto-generated method stub
		ArrayList<String> result = new ArrayList<String>();
		Connector twitterConnector = Connector.getInstance(Connector.TWITTER);
		queryResult = (QueryResult) twitterConnector.connect(keyword);
		List<Status> tweets = queryResult.getTweets();
		for(Status tweet : tweets){
			String tmp;
			tmp = Integer.toString(tweet.getFavoriteCount());
			tmp += ", " + tweet.getLang();
			tmp += " : " + tweet.getText();
			result.add(tmp);
//			result.add(tweet.getText());
		}
		return result;
	}
}
