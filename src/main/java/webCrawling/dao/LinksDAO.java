/**
 * 
 */
package webCrawling.dao;

import java.util.ArrayList;

import webCrawling.model.Links;

/**
 * @author Tirthraj
 *
 */
public interface LinksDAO {
	public abstract void storeNLinks(ArrayList<Links> linkList);
}
