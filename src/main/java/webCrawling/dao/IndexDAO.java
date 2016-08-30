/**
 * 
 */
package webCrawling.dao;

import webCrawling.model.Index;

/**
 * @author Tirthraj
 *
 */
public interface IndexDAO {
	public abstract void storeIndex(Index index);
	public abstract long getIndexedDocumentsCount();
}
