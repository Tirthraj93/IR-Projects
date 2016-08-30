package webCrawling.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Frontier {
	
	public static final String INDEX = "frontier";
	public static final String TYPE = "queue";
	
	public static final String FIELD_DISCOVERY_WAVE_NO = "DISCOVERY_WAVE_NO";
	public static final String FIELD_DOMAIN_NAME = "DOMAIN_NAME";
	public static final String FIELD_IN_LINK_CNT = "IN_LINK_CNT";
	public static final String FIELD_PARENT_SCORE = "PARENT_SCORE";
	public static final String FIELD_VISITED = "VISITED";
	public static final String FIELD_VISITED_DOMAIN_NAME = "VISITED_DOMAIN_NAME";
	public static final DateFormat visitedDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
	
	private String id;
	private int discoveryWaveNo;
	private	String domainName;
	private int inLinkCount; 
	private float parentScore;
	private boolean visited; 
	private String visitedDomainName;
	
	public Frontier(String id, int discoveryWaveNo, String domainName, int inLinkCount, float parentScore,
			boolean visited, String visitedDomainName) {
		super();
		this.id = id;
		this.discoveryWaveNo = discoveryWaveNo;
		this.domainName = domainName;
		this.inLinkCount = inLinkCount;
		this.parentScore = parentScore;
		this.visited = visited;
		this.visitedDomainName = visitedDomainName;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getDiscoveryWaveNo() {
		return discoveryWaveNo;
	}
	public void setDiscoveryWaveNo(int discoveryWaveNo) {
		this.discoveryWaveNo = discoveryWaveNo;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public int getInLinkCount() {
		return inLinkCount;
	}
	public void setInLinkCount(int inLinkCount) {
		this.inLinkCount = inLinkCount;
	}
	public float getParentScore() {
		return parentScore;
	}
	public void setParentScore(float parentScore) {
		this.parentScore = parentScore;
	}
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	public String getVisitedDomainName() {
		return visitedDomainName;
	}
	public void setVisitedDomainName(String visitedDomainName) {
		this.visitedDomainName = visitedDomainName;
	}
	
}
