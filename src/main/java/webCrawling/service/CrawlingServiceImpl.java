package webCrawling.service;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.jsoup.Connection.Response;

import webCrawling.dao.WebPageDAOImpl;
import webCrawling.model.Frontier;
import webCrawling.model.WebPage;
import webCrawling.util.Constants;

import org.jsoup.Jsoup;

public class CrawlingServiceImpl implements CrawlingService {

	@Override
	public WebPage getCrawledUrlPage(Frontier frontier) {
		WebPage webPage = null;
		try {
				String url = frontier.getId();
				
				String encodedUrl = URLEncoder.encode(url, "UTF-8");
				String decodedUrl  = URLDecoder.decode(encodedUrl, "UTF-8");
				
				Response response = Jsoup.connect(decodedUrl)
						.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
						.followRedirects(true)
						.timeout(Constants.urlConnectionTimeout)
						.ignoreHttpErrors(true)
						.execute();
				
				if(response.statusCode() != 404 || response != null) {
					WebPageDAOImpl webPageDAOImpl = new WebPageDAOImpl();
					webPage = webPageDAOImpl.getWebPage(decodedUrl, frontier.getDiscoveryWaveNo(), response);
				}
				
		}
		catch(Exception e) {
			//System.out.println("Can't connect to url - " + decodedUrl);
		}
		return webPage;
	}
}
