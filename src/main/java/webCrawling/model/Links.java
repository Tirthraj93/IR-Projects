package webCrawling.model;

public class Links {
	
	public static final String INDEX = "links";
	public static final String TYPE = "map";
	
	public static final String FIELD_SRC_LINKS = "SRC_LINKS";
	public static final String FIELD_DST_LINKS = "DST_LINKS";
	
	private String id;
	private String srcLinks;
	private String dstLinks;
	
	public Links(String id, String srcLinks, String dstLinks) {
		super();
		this.id = id;
		this.srcLinks = srcLinks;
		this.dstLinks = dstLinks;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSrcLinks() {
		return srcLinks;
	}
	public void setSrcLinks(String srcLinks) {
		this.srcLinks = srcLinks;
	}
	public String getDstLinks() {
		return dstLinks;
	}
	public void setDstLinks(String dstLinks) {
		this.dstLinks = dstLinks;
	}
}
