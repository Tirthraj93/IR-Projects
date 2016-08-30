package webCrawling.controller;


import org.elasticsearch.client.Client;

import webCrawling.service.QueueingServiceImpl;
import webCrawling.util.Constants;

public class QueueMaintanenceController extends Thread {

	private Client client;
	private boolean isRunning = true;
	
	public QueueMaintanenceController(Client client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		QueueingServiceImpl queueingServiceImpl = new QueueingServiceImpl(client);
		while(isRunning) {
			//System.out.println("Refining Queue...");
			CrawlingController.flushEnqueueBulkBuilder();
			queueingServiceImpl.removeExtraUrls(Constants.maxQueueSize);
			//System.out.println("Refined Queue.");
			try {
				Thread.sleep(Constants.timeToNextQueueTruncation);
			} catch (InterruptedException e) {
				//e.printStackTrace();
				isRunning = false;
			}
		}
	}
	
	public void kill() {
		isRunning = false;
	}
	
}
