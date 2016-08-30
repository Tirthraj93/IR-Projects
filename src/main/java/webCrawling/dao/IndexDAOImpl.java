package webCrawling.dao;

import java.io.IOException;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import webCrawling.model.Index;

public class IndexDAOImpl implements IndexDAO {
	
	private Client client;
	private BulkRequestBuilder bulkBuilder;
	
	public IndexDAOImpl(Client client) {
		this.client = client;
	}
	
	public IndexDAOImpl(Client client, BulkRequestBuilder bulkBuilder) {
		super();
		this.client = client;
		this.bulkBuilder = bulkBuilder;
	}

	@Override
	public void storeIndex(Index index) {
		try {
				XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject()
						.field(Index.FIELD_HTML,index.getHtml())
						.field(Index.FIELD_HTTP_HEADERS,index.getHtmlHeaders())
						.field(Index.FIELD_LAST_UPDATED,index.getLastUpdated())
						.field(Index.FIELD_TEXT,index.getText())
						.field(Index.FIELD_TITLE,index.getTitle())
					.endObject();

				IndexRequestBuilder indexRequestBuilder = client.prepareIndex()
						.setIndex(Index.INDEX)
						.setType(Index.TYPE)
						.setId(index.getId())
						.setSource(builder);
				bulkBuilder.add(indexRequestBuilder);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public long getIndexedDocumentsCount() {
		SearchResponse searchResponse = client.prepareSearch(Index.INDEX)
				.setTypes(Index.TYPE)
				.setSize(0)
				.get();
		
		return searchResponse.getHits().getTotalHits();
	}
}
