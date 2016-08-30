/**
 * 
 */
package webCrawling.service;

import webCrawling.model.Frontier;
import webCrawling.model.WebPage;

/**
 * @author Tirthraj
 *
 */
public interface CrawlingService {
	public abstract WebPage getCrawledUrlPage(Frontier frontier);
}
