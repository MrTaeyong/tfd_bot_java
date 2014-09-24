package parser.naver;

public enum FieldName {
	TITLE("title"), BLOG_CONTENT("blogContent"), BLOGGER_NAME("bloggerName"),
	BLOG_LINK("bloggerLink"), BLOG_DATE("date"), BLOG_IMAGES("blogImage"),
	PLACE_NAME("place_name"), PLACE_IMAGE("link"), BLOG_TODAY_COUNT("today_count"),
	BLOG_SYMPATHY_COUNT("sympathy_count");
	public String value;
	FieldName(String value){
		this.value = value;
	}
}
