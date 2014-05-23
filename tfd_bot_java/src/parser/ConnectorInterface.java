package parser;

public interface ConnectorInterface {	
	public ConnectorInterface getInstance(int type);
	public Object connect();
}
