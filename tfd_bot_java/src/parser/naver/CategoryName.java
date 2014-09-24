package parser.naver;

public enum CategoryName {
	CAFFE("카페"), RESTAURANT("음식점"), CULTURE("문화,예술"), PUB("술집");
	public String value;
	CategoryName(String value){
		this.value = value;
	}
}
