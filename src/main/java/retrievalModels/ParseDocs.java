package retrievalModels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import util.Constants;
import webCrawling.util.ElasticConnection;

/**
 * Parse given files to create index in ElasticSearch
 * 
 * @author Tirthraj
 * 
 *
 */
public class ParseDocs {

	private static Client client = ElasticConnection.getElasticSearchClient();

	/**
	 * Adds required features of documents from each file in 
	 * the folder to an ElasticSearch index
	 * @author Tirthraj
	 * @param folder
	 * a folder containing data files
	 */
	public static void addDocsFromFolder(File folder) {

		File[] files = folder.listFiles();
		
		BulkRequestBuilder bulkBuilder = client.prepareBulk();
		
		int bulkBuilderLength = 0;
		int id=1;
		
		for (File f : files) {

			try {

				FileReader fReader = new FileReader(f);
				BufferedReader bReader = new BufferedReader(fReader);
					
				String line = "";
				String docNo = "";
				StringBuilder body = new StringBuilder();
				
				boolean inText = false;
				
				while ((line = bReader.readLine()) != null) {
					if (line.startsWith("</TEXT>")) {
						inText = false;
					} else if (inText) {
						body.append(line).append(" ");
					} else if (line.startsWith("<DOCNO>")) {
						docNo = line.substring(8, line.length() - 9);
					} else if (line.startsWith("<TEXT>")) {
						inText = true;
						body.append(line.substring(6)).append(" ");
					} else if (line.startsWith("</DOC>")) {

						XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
								.startObject()
								.field("docno", docNo)
								.field("text", body)
								.endObject();
						
						bulkBuilder.add(client.prepareIndex
								(Constants.indexName, Constants.indexType, "" + id)
								.setSource(xContentBuilder));
						System.out.println(id);
						id++;
						bulkBuilderLength++;
						
						if(bulkBuilderLength % 1000 == 0) {
							BulkResponse bulkResponse = bulkBuilder.execute().actionGet();
							bulkBuilder = client.prepareBulk();
						}
						
						/*IndexResponse indexResponse = client
								.prepareIndex(Constants.indexName, Constants.indexType, "" + docNo)
								.setSource(builder).execute().actionGet();*/

						body = new StringBuilder();
					}
				}
				
				bReader.close();
				fReader.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(bulkBuilder.numberOfActions() > 0){
			BulkResponse bulkResponse = bulkBuilder.get();
		}
	}
	
	/**
	 * Parses all files at predefined file path to create an index
	 * @author Tirthraj
	 * @param args
	 */
	public static void main(String[] args) {

		File folder = new File(Constants.filePath);
		
		long startTime = System.currentTimeMillis();
		
		addDocsFromFolder(folder);

		long endTime = System.currentTimeMillis();
		
		System.out.println("Execution Time: "+(endTime-startTime)/60000);
		
		client.close();
	}
}
