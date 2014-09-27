/**
 * @FileName	: TwitterConnector.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 06. 02.
 * @Author		: Taeyong
 */
package parser.twitter;

/**
 * @Class	:TwitterConnector
 * @Date	: 2014. 06. 02.
 * @Author	: Taeyong
 */
import parser.Connector;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * J4Twitter 라이브러리를 사용하여 트위터 내용을 파싱하는 클래스
 * (현재 사용되지 않음)
 * @author taeyong
 *
 */
public class TwitterConnector extends Connector{
	private final String _OAUTH_CONSUMER_KEY = "XPxsGQGrLsXJCqVo4Kx7VvCtD";
	private final String _OAUTH_CONSUMER_SECRET = "0U6APNdUXLbKv8GC6jNME0qL3jXCfNfeW6d6MABEwmJx8ANV58";
	private final String _OAUTH_ACCESS_TOKEN = "159841182-ZQs9t8YDDXPJlH3FrUV0cWiGtKYiOBsseZVHTAVg";
	private final String _OAUTH_ACCESS_TOKEN_SECRET = "a4XmsENKXoEgBgdrgioyHfpDedGYhgSttQ1tlJW1rS8gI";
	
	@Override
	public Object connect(String keyword) {
//		ArrayList<String> result = new ArrayList<String>();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(_OAUTH_CONSUMER_KEY);
		cb.setOAuthConsumerSecret(_OAUTH_CONSUMER_SECRET);
		cb.setOAuthAccessToken(_OAUTH_ACCESS_TOKEN);
		cb.setOAuthAccessTokenSecret(_OAUTH_ACCESS_TOKEN_SECRET);
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		Query query = new Query(keyword);
		query.count(100);
		query.setSince("2010-01-01");
		QueryResult queryResult;
		try {
			queryResult = twitter.search(query);
			return queryResult;
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
