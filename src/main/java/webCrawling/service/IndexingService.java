/**
 * 
 */
package webCrawling.service;

import webCrawling.model.WebPage;

/**
 * @author Tirthraj
 *
 */
public interface IndexingService {
	public abstract void storeIndex(WebPage webPage);
	public abstract void storeLinks(WebPage webPage);
	public abstract long getIndexedDocumentsCount();
}
