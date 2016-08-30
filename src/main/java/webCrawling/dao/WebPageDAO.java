/**
 * 
 */
package webCrawling.dao;

import org.jsoup.Connection.Response;

import webCrawling.model.WebPage;

public interface WebPageDAO {
	public abstract WebPage getWebPage(String url, int waveNo, Response response);
}
