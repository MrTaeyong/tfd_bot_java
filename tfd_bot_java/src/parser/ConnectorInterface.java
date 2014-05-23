package parser;

import java.util.Map;

public interface ConnectorInterface {
	public Object connect(String url, Map<String, String> param);
}
