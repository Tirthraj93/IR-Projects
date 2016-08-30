package webCrawling.dao;

import java.io.IOException;
import java.util.ArrayList;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import webCrawling.model.Links;

public class LinksDAOImpl implements LinksDAO {
	
	private Client client;
	private BulkRequestBuilder bulkBuilder;
	
	public LinksDAOImpl(Client client, BulkRequestBuilder bulkBuilder) {
		this.client = client;
		this.bulkBuilder = bulkBuilder;
	}

	@Override
	public void storeNLinks(ArrayList<Links> linkList) {
		try {
			for (Links link : linkList) {
				
				XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject()
						.field(Links.FIELD_SRC_LINKS,link.getSrcLinks())
						.field(Links.FIELD_DST_LINKS,link.getDstLinks())
					.endObject();

				IndexRequestBuilder indexRequestBuilder = client.prepareIndex()
						.setIndex(Links.INDEX)
						.setType(Links.TYPE)
						.setId(link.getId())
						.setSource(builder);

				bulkBuilder.add(indexRequestBuilder);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
