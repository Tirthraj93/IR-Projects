package webCrawling;


import java.util.Scanner;

import org.elasticsearch.client.Client;

import webCrawling.controller.CrawlingController;
import webCrawling.controller.QueueMaintanenceController;
import webCrawling.controller.UIController;
import webCrawling.util.Constants;
import webCrawling.util.ElasticConnection;

/**
 * This class is the entry point of the application
 * @author Tirthraj
 *
 */
public class Executor {
	
	private static Scanner reader = new Scanner(System.in);
	private static String query;
	private static long pagesToParse;
	
	/**
	 * Perform focused crawl for a given query and store
	 * it in an index of ElasticSearch. 
	 * Crawl is restricted to approximately n documents as provided 
	 * by the user.
	 * @author Tirthraj
	 */
	public static void main(String[] args) {
		
		getQuery();
		getPagesToParse();
		
		if(pagesToParse < Constants.crawlingPoolsSize * Constants.queueFetchSize) {
			Constants.crawlingPoolsSize = 1;
			Constants.queueFetchSize = (int) pagesToParse;
		}
		
		long startTime = System.currentTimeMillis();
		
		Client queueClient = ElasticConnection.getElasticSearchClient();
		QueueMaintanenceController queueMaintanenceController = new  QueueMaintanenceController(queueClient);
		queueMaintanenceController.start();
		
		Client uiClient = ElasticConnection.getElasticSearchClient();
		UIController uiController = new  UIController(uiClient, System.currentTimeMillis());
		uiController.start();
		
		CrawlingController.setQuery(query);
		
		if(CrawlingController.executeCrawler(pagesToParse)) {
			queueMaintanenceController.interrupt();
			queueMaintanenceController.kill();
			uiController.interrupt();
			uiController.kill();
			queueClient.close();
			uiClient.close();
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("\n\nExecution Time: "+(endTime-startTime)/60000);
	}
	
	/**
	 * Get query string from user
	 * @author Tirthraj
	 */
	private static void getQuery() {
		System.out.println("Enter Query String: ");
		query = reader.nextLine(); //"great modernist artists"
	}
	
	/**
	 * Get total number of pages to parse from user
	 * @author Tirthraj
	 */
	private static void getPagesToParse() {
		System.out.println("Enter Number of Documents to crawl: ");
		pagesToParse = reader.nextLong();
	}
}
