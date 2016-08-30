package webCrawling.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;

import webCrawling.dao.IndexDAOImpl;
import webCrawling.dao.LinksDAOImpl;
import webCrawling.model.Index;
import webCrawling.model.Links;
import webCrawling.model.WebPage;

public class IndexingServiceImpl implements IndexingService {

	private Client client;
	private BulkRequestBuilder bulkBuilder;
	
	public IndexingServiceImpl(Client client) {
		this.client = client;
	}
	
	public IndexingServiceImpl(Client client, BulkRequestBuilder bulkBuilder) {
		this.client = client;
		this.bulkBuilder = bulkBuilder;
	}
	
	@Override
	public void storeIndex(WebPage webPage) {
		
		String id = webPage.getUrl();
		String html = webPage.getHtml();
		String htmlHeaders = webPage.getHeaders();
		String lastUpdated = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ").format(new Date());
		String text = webPage.getText();
		String title = webPage.getTitle();
			
		Index index = new Index(id, html, htmlHeaders, lastUpdated, text, title);
		
		IndexDAOImpl indexDAOImpl = new IndexDAOImpl(client, bulkBuilder);
		indexDAOImpl.storeIndex(index);
	}

	@Override
	public void storeLinks(WebPage webPage) {
		
		ArrayList<Links> linkList = new ArrayList<Links>();
		
		String srcLinks = webPage.getUrl();
		for (String dstLinks : webPage.getOutLinks()) {
			String id = srcLinks + "#" + dstLinks;
			Links links = new Links(id, srcLinks, dstLinks);
			linkList.add(links);
		}
		
		LinksDAOImpl linksDAOImpl = new LinksDAOImpl(client, bulkBuilder);
		linksDAOImpl.storeNLinks(linkList);
	}

	@Override
	public long getIndexedDocumentsCount() {
		IndexDAOImpl indexDAOImpl = new IndexDAOImpl(client);
		return indexDAOImpl.getIndexedDocumentsCount();
	}

}
