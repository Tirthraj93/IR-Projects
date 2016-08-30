package webCrawling.dao;

import java.io.IOException;
import java.util.ArrayList;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;

import webCrawling.model.Frontier;
import webCrawling.util.Constants;

public class FrontierDAOImpl implements FrontierDAO {
	
	private static XContentBuilder queryUpdateBuilder;
	
	private Client client;
	private BulkRequestBuilder bulkBuilder;
	
	static {
		try {
			queryUpdateBuilder = XContentFactory.jsonBuilder()
					.startObject()
						.field(Frontier.FIELD_VISITED_DOMAIN_NAME, "")
						.field(Frontier.FIELD_VISITED, true)
					.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public FrontierDAOImpl(Client client) {
		super();
		this.client = client;
	}
	
	public FrontierDAOImpl(Client client, BulkRequestBuilder bulkBuilder) {
		super();
		this.client = client;
		this.bulkBuilder = bulkBuilder;
	}

	@Override
	public ArrayList<Frontier> getAndDeleteNFrontiers(int n) {
		
		ArrayList<Frontier> frontierList = new ArrayList<Frontier>();
		
		Script script = new Script(Constants.scriptDequeue,ScriptType.INDEXED,"groovy",null);
		ScoreFunctionBuilder sfBuilder = ScoreFunctionBuilders.scriptFunction(script);
		
		QueryBuilder customScoreQuery = QueryBuilders.functionScoreQuery(sfBuilder)
				.boostMode("replace");
		
		QueryBuilder dequeueQuery = QueryBuilders.boolQuery()
				.must(customScoreQuery)
				.filter(QueryBuilders.termQuery(Frontier.FIELD_VISITED, false));	
		
		SearchResponse searchResponse = client.prepareSearch(Frontier.INDEX)
				.setTypes(Frontier.TYPE)
				.setSize(n)
				.setQuery(dequeueQuery)
				.addField(Frontier.FIELD_DISCOVERY_WAVE_NO)
				.addField(Frontier.FIELD_DOMAIN_NAME)
				.addField(Frontier.FIELD_IN_LINK_CNT)
				.addField(Frontier.FIELD_PARENT_SCORE)
				.addField(Frontier.FIELD_VISITED)
				.addField(Frontier.FIELD_VISITED_DOMAIN_NAME)
		        .execute()
		        .actionGet();
		
		SearchHit[] frontierHits = searchResponse.getHits().getHits();
		//ArrayList<SearchHit> frontiersToRemove = new ArrayList<SearchHit>();
		
		for (SearchHit hit : frontierHits) {
			
			String domainName = hit.field(Frontier.FIELD_DOMAIN_NAME).getValue().toString();
			
			/*if(enforcePoliteness(domainName)) {
				continue;
			}
			
			frontiersToRemove.add(hit);*/
			
			String id = hit.getId();
			int discoveryWaveNo = Integer.parseInt(hit.field(Frontier.FIELD_DISCOVERY_WAVE_NO).getValue().toString());
			int inLinkCount = Integer.parseInt(hit.field(Frontier.FIELD_IN_LINK_CNT).getValue().toString());
			float parentScore = Float.parseFloat(hit.field(Frontier.FIELD_PARENT_SCORE).getValue().toString());
			boolean visited = (boolean) hit.field(Frontier.FIELD_VISITED).getValue();
			String visitedDomainName = hit.field(Frontier.FIELD_VISITED_DOMAIN_NAME).getValue().toString();
			
			Frontier frontier = new Frontier(id,discoveryWaveNo, domainName, inLinkCount, parentScore, visited, visitedDomainName);
			frontierList.add(frontier);
		}

		deleteFrontiers(frontierHits);
		
		return frontierList;
	}
	
	/*private static boolean enforcePoliteness(String domain) {
		if(CrawlingController.domainAccessTimeMap.containsKey(domain)) {
			long prevTime = CrawlingController.domainAccessTimeMap.get(domain);
			long currTime = System.currentTimeMillis();
			long timeDiff = currTime - prevTime;
			if(timeDiff <= 1000) {
				return true;
			}
		}
		else {
			CrawlingController.domainAccessTimeMap.put(domain, System.currentTimeMillis());
		}
		return false;
	}*/
	
	@Override
	public void storeNFrontiers(ArrayList<Frontier> frontierList) {
		
		for (Frontier frontier : frontierList) {
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject()
						.field(Frontier.FIELD_DISCOVERY_WAVE_NO, frontier.getDiscoveryWaveNo())
						.field(Frontier.FIELD_DOMAIN_NAME,frontier.getDomainName())
						.field(Frontier.FIELD_IN_LINK_CNT,frontier.getInLinkCount())
						.field(Frontier.FIELD_PARENT_SCORE,frontier.getParentScore())
						.field(Frontier.FIELD_VISITED,frontier.isVisited())
						.field(Frontier.FIELD_VISITED_DOMAIN_NAME,frontier.getVisitedDomainName())
					.endObject();
				
				IndexRequestBuilder indexRequestBuilder = client.prepareIndex()
						.setIndex(Frontier.INDEX)
						.setType(Frontier.TYPE)
						.setId(frontier.getId())
						.setSource(builder);
				
				Script script = new Script(Constants.scriptDequeue,ScriptType.INDEXED,"groovy",null);
				
				UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate()
						.setIndex(Frontier.INDEX)
						.setType(Frontier.TYPE)
						.setId(frontier.getId())
						.setUpsert(indexRequestBuilder.request())
						.setScript(script);
				
				bulkBuilder.add(updateRequestBuilder);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void deleteFrontiers(SearchHit[] frontierHits) {
		
		if(frontierHits.length == 0) {
			return;
		}
		
		BulkRequestBuilder bulkBuilder = client.prepareBulk();
		
		for (SearchHit searchHit : frontierHits) {
			
			String id = searchHit.getId();
			String domainName = searchHit.getFields().get(Frontier.FIELD_DOMAIN_NAME).value().toString();
			
			XContentBuilder builder;
			try {
				builder = XContentFactory.jsonBuilder()
				.startObject()
					.field(Frontier.FIELD_DOMAIN_NAME, domainName)
					.field(Frontier.FIELD_VISITED, true)
				.endObject();
				
				UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate()
						.setIndex(Frontier.INDEX)
						.setType(Frontier.TYPE)
						.setId(id)
						.setDoc(builder);
				
				bulkBuilder.add(updateRequestBuilder);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		BulkResponse response = bulkBuilder.execute().actionGet();
	}

	@Override
	public void removeExtraFrontierEntries(int maxQueueSize) {
		
		Script script = new Script(Constants.scriptDequeue,ScriptType.INDEXED,"groovy",null);
		ScoreFunctionBuilder sfBuilder = ScoreFunctionBuilders.scriptFunction(script);
		
		QueryBuilder customScoreQuery = QueryBuilders.functionScoreQuery(sfBuilder)
				.boostMode("replace");
		
		QueryBuilder dequeueQuery = QueryBuilders.boolQuery()
				.must(customScoreQuery)
				.filter(QueryBuilders.termQuery(Frontier.FIELD_VISITED, false));	
		
		SearchResponse scrollResp = client.prepareSearch(Frontier.INDEX)
				.setTypes(Frontier.TYPE)
				.setScroll(new TimeValue(60000))
				.setQuery(dequeueQuery)
				.addField(Frontier.FIELD_VISITED_DOMAIN_NAME)
				.setSize(maxQueueSize) // maxQueueSize frontier tuples will be returned for each scroll
		        .execute()
		        .actionGet();
		
		BulkRequestBuilder bulkBuilder = client.prepareBulk();
		
		while(true) {
			
			//skipped first maxQueueSize tuples of Frontier as only those need not be updated
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
		    		.setScroll(new TimeValue(60000))
		    		.execute()
		    		.actionGet();
			
			//Break condition: No hits are returned
		    if (scrollResp.getHits().getHits().length == 0) {
		        break;
		    }
		    
			for(SearchHit hit : scrollResp.getHits().getHits()) {
				
				UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate()
					.setIndex(Frontier.INDEX)
					.setType(Frontier.TYPE)
					.setId(hit.getId())
					.setDoc(queryUpdateBuilder);
				
				bulkBuilder.add(updateRequestBuilder);
			}
		}
		
		if(bulkBuilder.numberOfActions() > 0) {
			BulkResponse response = bulkBuilder.get();
		}
	}
}
