package webCrawling.controller;

import org.elasticsearch.client.Client;

import webCrawling.service.IndexingServiceImpl;
import webCrawling.util.Constants;

public class UIController extends Thread {
	
	private Client client;
	private long startTime;
	private boolean isRunning = true;

	public UIController(Client client, long time) {
		super();
		this.client = client;
		this.startTime = time;
	}
	
	@Override
	public void run() {
		while(isRunning) {
			
			try {
				Thread.sleep(Constants.indexedDocumentCountPrintInterval);
			} catch (InterruptedException e) {
				isRunning = false;
			}
			
			IndexingServiceImpl indexingServiceImpl = new IndexingServiceImpl(client);
			long indexedDocCount = indexingServiceImpl.getIndexedDocumentsCount();
			
			long endTime = System.currentTimeMillis();
			long timeTakenInMinutes = (endTime - startTime) / 60000;
			
			System.out.println("Indexed " + indexedDocCount + " pages in " + timeTakenInMinutes + " minute(s).");
			System.out.println("Parsed " + CrawlingController.parsedPages + " pages.");
		}
	}
	
	public void kill() {
		isRunning = false;
	}
}
