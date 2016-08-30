/**
 * 
 */
package webCrawling.dao;

import java.util.ArrayList;

import webCrawling.model.Frontier;

/**
 * @author Tirthraj
 *
 */
public interface FrontierDAO {
	public abstract ArrayList<Frontier> getAndDeleteNFrontiers(int n);
	public abstract void storeNFrontiers(ArrayList<Frontier> frontierList);
	public abstract void removeExtraFrontierEntries(int maxQueueSize);
}
