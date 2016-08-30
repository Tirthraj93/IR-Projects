package webCrawling.dao;

import java.io.IOException;
import java.util.Set;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import webCrawling.model.WebPage;
import webCrawling.util.HTMLProcessing;

public class WebPageDAOImpl implements WebPageDAO {

	@Override
	public WebPage getWebPage(String url, int waveNo, Response response) {
		WebPage webpage = null;
		Document document;
		try {
			document = response.parse();
			String html = document.outerHtml();
			String headers = response.headers().toString();
			String title = document.title();
			String text = document.body().text();
			Set<String> outLinks = HTMLProcessing.getOutLinks(document);
			
			webpage = new WebPage(url, html, headers, title, text, outLinks, waveNo);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return webpage;
	}
}
