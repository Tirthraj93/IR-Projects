/**
 * 
 */
package webCrawling.service;

import java.util.ArrayList;

import webCrawling.model.Frontier;
import webCrawling.model.WebPage;

/**
 * @author Tirthraj
 *
 */
public interface QueueingService {
	public abstract ArrayList<Frontier> getFrontierList(int queuefetchsize);
	public abstract void enqueueOutLinks(WebPage webPage, String query);
	public abstract void removeExtraUrls(int maxQueueSize);
}
