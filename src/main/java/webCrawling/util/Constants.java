/**
 * 
 */
package webCrawling.util;

import org.elasticsearch.common.settings.Settings;

/**
 * @author Tirthraj
 *
 */
public class Constants {
	
	//******************************
	// Elastic Search Constants
	//******************************
	public static final Settings settings = Settings.settingsBuilder().put("client.transport.sniff", true)
			.put("cluster.name", Constants.clusterName).build();
	public static final String clusterName = "dead-pool";
	public static final String elasticHost = "localhost";
	public static final int elasticPort = 9300;
	public static final float avgDocLengthCrawling = 5000;
	public static final String scriptDequeue = "MAGIC_DEQUEUE";
	public static final String scriptEnqueue = "MAGIC_ENQUEUE";
	
	//******************************
	// Constants for Crawling
	//******************************
	public static int crawlingPoolsSize = 4;
	public static int queueFetchSize = 30;
	public static final int bulkSize = 200;
	public static final int enqueueBulkSize = 200;
	public static final int maxQueueSize = 5000;
	public static final long timeToNextQueueTruncation = 20 * 1000; //in milliseconds
	public static final long indexedDocumentCountPrintInterval = 1 * 60 * 1000; //in milliseconds
	public static final int crawlingThreadTimeoutMinutes = 60;
	public static final int robotConnectionTimeout = 500;
	public static final int urlConnectionTimeout = 4000;	
}
