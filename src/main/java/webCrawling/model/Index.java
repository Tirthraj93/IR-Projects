package webCrawling.model;

public class Index {
	
	public static final String INDEX = "1512_great_mordenist_artist";
	public static final String TYPE = "document";
	
	public static final String FIELD_HTML = "HTML";
	public static final String FIELD_HTTP_HEADERS = "HTTP_HEADERS";
	public static final String FIELD_LAST_UPDATED = "LAST_UPDATED";
	public static final String FIELD_TEXT = "TEXT";
	public static final String FIELD_TITLE = "TITLE";
	
	private String id;
	private String html;
	private String htmlHeaders;
	private String lastUpdated;
	private String text;
	private String title;
	
	
	public Index(String id, String html, String htmlHeaders, String lastUpdated, String text, String title) {
		super();
		this.id = id;
		this.html = html;
		this.htmlHeaders = htmlHeaders;
		this.lastUpdated = lastUpdated;
		this.text = text;
		this.title = title;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public String getHtmlHeaders() {
		return htmlHeaders;
	}
	public void setHtmlHeaders(String htmlHeaders) {
		this.htmlHeaders = htmlHeaders;
	}
	public String getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
