package webCrawling.model;

import java.util.HashSet;
import java.util.Set;

public class WebPage {
	private String url;
	private String html;
	private String headers;
	private String title;
	private String text;
	private Set<String> outLinks = new HashSet<String>();
	private int waveNo;
	
	public WebPage(String url, String html, String headers, String title, String text, Set<String> outLinks, int waveNo) {
		super();
		this.url = url;
		this.html = html;
		this.headers = headers;
		this.title = title;
		this.text = text;
		this.outLinks = outLinks;
		this.waveNo = waveNo;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public String getHeaders() {
		return headers;
	}
	public void setHeaders(String headers) {
		this.headers = headers;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Set<String> getOutLinks() {
		return outLinks;
	}
	public void setOutLinks(Set<String> outLinks) {
		this.outLinks = outLinks;
	}
	public int getWaveNo() {
		return waveNo;
	}
	public void setWaveNo(int waveNo) {
		this.waveNo = waveNo;
	}
	
}
