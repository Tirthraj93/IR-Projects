package webCrawling.controller;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;

import crawlercommons.robots.BaseRobotRules;
import webCrawling.model.Frontier;
import webCrawling.model.WebPage;
import webCrawling.service.CrawlingServiceImpl;
import webCrawling.service.FlushingService;
import webCrawling.service.IndexingServiceImpl;
import webCrawling.service.QueueingServiceImpl;
import webCrawling.util.Constants;
import webCrawling.util.ElasticConnection;
import webCrawling.util.HTMLProcessing;

public class CrawlingController implements Runnable {
	
	public static ConcurrentHashMap<String,BaseRobotRules> domainRobotRulesMap= new ConcurrentHashMap<String,BaseRobotRules>();
	public static ConcurrentHashMap<String, Integer> urlLinkCountMap = new ConcurrentHashMap<String, Integer>();
	public static ConcurrentHashMap<String, Long> domainAccessTimeMap = new ConcurrentHashMap<String, Long>();
	
	public static String query;
	
	public static long parsedPages = 0;
	private static long pagesToParse;
	private static Client enqueueClient = ElasticConnection.getElasticSearchClient();
	private static BulkRequestBuilder enQueueBulkBuilder = enqueueClient.prepareBulk();
	
	private Client client;
	public BulkRequestBuilder bulkBuilder;
	
	public CrawlingController(Client client) {
		super();
		this.client = client;
		bulkBuilder = client.prepareBulk();
	}

	public static void setQuery(String queryArg) {
		query = queryArg;
	}
	
	public static boolean executeCrawler(long pagesToParseArg) {
			
		pagesToParse = pagesToParseArg;
		
		//execute each url batch in a separate thread
		ExecutorService executor = Executors.newFixedThreadPool(Constants.crawlingPoolsSize);
		
		for(int i = 0; i < Constants.crawlingPoolsSize; i ++) {
			Client client = ElasticConnection.getElasticSearchClient();
			CrawlingController workerThread = new CrawlingController(client);
			executor.execute(workerThread);
		}
		
		executor.shutdown();
		while(!executor.isTerminated()) { }
		
		enqueueClient.close();
		return true;
	}

	@Override
	public void run() {
		
		IndexingServiceImpl indexingServiceImpl = new IndexingServiceImpl(client, bulkBuilder);
		QueueingServiceImpl queueingServiceImpl = new QueueingServiceImpl(client, enQueueBulkBuilder);
		CrawlingServiceImpl crawlingServiceImpl = new CrawlingServiceImpl();
		
		ArrayList<Frontier> additionalFrontiers = new ArrayList<Frontier>();
		
		while(parsedPages < pagesToParse) {
			
			ArrayList<Frontier> frontierList = queueingServiceImpl.getFrontierList(Constants.queueFetchSize);
			System.out.println("Dequeued " + frontierList.size());
			frontierList.addAll(additionalFrontiers);
			
			for (Frontier frontier : frontierList) {
				
				if(HTMLProcessing.enforcePolitenessPolicy(frontier.getDomainName())) {
					//System.out.println("Enforced politeness for " + frontier.getDomainName());
					additionalFrontiers.add(frontier);
					continue;
				}
				
				WebPage webPage;
				
				//Crawling
				if((webPage = crawlingServiceImpl.getCrawledUrlPage(frontier)) == null) {
					continue;
				}
				
				//Store Index
				indexingServiceImpl.storeIndex(webPage);
				
				//Store Links
				indexingServiceImpl.storeLinks(webPage);
				
				//Enqueue
				queueingServiceImpl.enqueueOutLinks(webPage, query);
				
				incrementParsedPages();
			}
			
			if(enQueueBulkBuilder.numberOfActions() >= Constants.enqueueBulkSize) {
				flushEnqueueBulkBuilder();
			}
			
			if(bulkBuilder.numberOfActions() == Constants.bulkSize
					|| parsedPages >= pagesToParse) {
		
				FlushingService flushingService = new FlushingService(bulkBuilder);
				flushingService.implementFlush(bulkBuilder.numberOfActions());
				bulkBuilder = client.prepareBulk();
			} 
		}
		
		if(bulkBuilder.numberOfActions() > 0) {
			FlushingService flushingService = new FlushingService(bulkBuilder);
			flushingService.implementFlush(bulkBuilder.numberOfActions());
		}
		
		urlLinkCountMap = new ConcurrentHashMap<String, Integer>();
		
		client.close();
	}
	
	public static synchronized void incrementParsedPages() {
		parsedPages++;
	}
	
	public static synchronized void flushEnqueueBulkBuilder() {
		FlushingService flushingService = new FlushingService(enQueueBulkBuilder);
		flushingService.implementFlush(enQueueBulkBuilder.numberOfActions());
		enQueueBulkBuilder = enqueueClient.prepareBulk();
	}
	
}
