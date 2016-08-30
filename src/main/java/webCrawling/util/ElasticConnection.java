package webCrawling.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import webCrawling.util.Constants;

public class ElasticConnection {
	public static Client getElasticSearchClient() {
		try {
			return TransportClient.builder()
					.settings(Constants.settings).build()
					.addTransportAddress
					(new InetSocketTransportAddress
							(InetAddress.getByName(Constants.elasticHost), 
									Constants.elasticPort));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
}
