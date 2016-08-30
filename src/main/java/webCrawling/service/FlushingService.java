package webCrawling.service;

import java.io.Flushable;
import java.io.IOException;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;

public class FlushingService implements Flushable {

	private BulkRequestBuilder bulkBuilder;
	
	public FlushingService(BulkRequestBuilder bulkBuilder) {
		this.bulkBuilder = bulkBuilder;
	}

	@Override
	public void flush() throws IOException {
		BulkResponse response = bulkBuilder.get();
	}
	
	public void implementFlush(int builderSize) {
		try {
			System.out.println("Flushing... " + builderSize);
			if(builderSize > 0) {
				flush();
			}
			System.out.println("Flushed.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoNodeAvailableException e) {
			e.printStackTrace();
		}
	}
}
