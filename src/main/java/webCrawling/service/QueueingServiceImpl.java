package webCrawling.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;

import indexing.Term;
import util.Constants;
import util.Stemmer;
import webCrawling.controller.CrawlingController;
import webCrawling.dao.FrontierDAOImpl;
import webCrawling.model.Frontier;
import webCrawling.model.WebPage;
import webCrawling.util.HTMLProcessing;

public class QueueingServiceImpl implements QueueingService {

	private Client client;
	private BulkRequestBuilder bulkBuilder;
	
	public QueueingServiceImpl(Client client) {
		this.client = client;
	}

	public QueueingServiceImpl(Client client, BulkRequestBuilder bulkBuilder) {
		this.client = client;
		this.bulkBuilder = bulkBuilder;
	}

	@Override
	public ArrayList<Frontier> getFrontierList(int queuefetchsize) {
		FrontierDAOImpl frontierDAOImpl = new FrontierDAOImpl(client);
		return frontierDAOImpl.getAndDeleteNFrontiers(queuefetchsize);
	}

	@Override
	public void enqueueOutLinks(WebPage webPage, String query) {
		
		//Create frontier list for queuing
		ArrayList<Frontier> frontierList = new ArrayList<Frontier>();
		
		float parentScore = getScore(query,webPage.getText());
		
		for (String link : webPage.getOutLinks()) {
			
			String id = link;
			int discoveryWaveNo = webPage.getWaveNo() + 1;
			String domainName = HTMLProcessing.getDomain(link);
			int inLinkCount = 1;
			
			if(CrawlingController.urlLinkCountMap.containsKey(link)) {
				inLinkCount = CrawlingController.urlLinkCountMap.get(link);
			}
			
			boolean visited = false; 
			String visitedDomainName = domainName;
				
			Frontier frontier = new Frontier(id, discoveryWaveNo, domainName, inLinkCount, parentScore, visited, visitedDomainName);
			frontierList.add(frontier);
		}
			
		//store entire frontier list to elasticsearch
		FrontierDAOImpl frontierDAOImpl = new FrontierDAOImpl(client, bulkBuilder);
		frontierDAOImpl.storeNFrontiers(frontierList);
	}

	@Override
	public void removeExtraUrls(int maxQueueSize) {
		FrontierDAOImpl frontierDAOImpl = new FrontierDAOImpl(client);
		frontierDAOImpl.removeExtraFrontierEntries(maxQueueSize);
	}
	
	private float getScore(String query, String text) {
		
		float score = 0f;
		
		ArrayList<String> termList = Term.createTerms(text);
		Map<String, Long> tfMap = new HashMap<String, Long>();
		Stemmer s = new Stemmer();
		
		float docLength = termList.size();
		
		for (String term : termList) {
			long newtTf = 1L;
			if(tfMap.containsKey(term))
				newtTf = tfMap.get(term) + 1;
			tfMap.put(term, newtTf);
		}
		
		for (String queryTerm : query.split("\\s")) {
			if (!Constants.stopWordsSet.contains(queryTerm)) {
				queryTerm = s.stem(queryTerm.trim());
			}
			else {
				continue;
			}
			float tf = 0f;
			if(tfMap.containsKey(queryTerm)) {
				tf = (float) tfMap.get(queryTerm);
			}
			score += tf / (tf + 0.5 + (1.5 * (docLength/webCrawling.util.Constants.avgDocLengthCrawling)));;
		}
		
		return score;
	}

}
