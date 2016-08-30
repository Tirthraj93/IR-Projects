package retrievalModels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;

import util.Constants;
import util.Stemmer;
import webCrawling.util.ElasticConnection;

public class ModelHelper {

	private static Client client = ElasticConnection.getElasticSearchClient();
	
	public static void main(String[] args) {
		storeDocLengthMap();
	}
	
	static void removeNoiseFromQueries() {
		Set<String> stopWordMap = Constants.stopWordsSet;
		Stemmer s = new Stemmer();

		try {
			FileReader fReader = new FileReader(Constants.queryFile);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String line = "";
			int no=1;
			
			StringBuilder outputString = new StringBuilder();
			
			while ((line = bReader.readLine()) != null) {
				System.out.println("Processiong Q"+no);

				line = line.replace('.', ' ');
				line = line.replace('-', ' ');
				line = line.replace(',', ' ');
				line = line.replace('(', ' ');
				line = line.replace(')', ' ');
				line = line.replace('"', ' ');
				
				if(line.contains("U S")) {
					line = line.replace("U S", "u.s.");
				}
				
				String[] words = line.split("\\s+");
				
				outputString.append(words[0]+" ");
				
				for (int i = 1; i < words.length; i++) {
					
					String word = words[i].toLowerCase();
					String stemmedWord = s.stem(word);
					
					if (stopWordMap.contains(stemmedWord) || Constants.removalTermsList.contains(stemmedWord)) {
						System.out.println("\tRemoved: "+stemmedWord);
						continue;
					}
					
					System.out.println("\t"+words[i] + " -> "+stemmedWord);
					outputString.append(stemmedWord+" ");
				}
				
				outputString.append("\n");
				no++;
			}
			FileWriter fWriter = new FileWriter(Constants.queryFile);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			bWriter.append(outputString);
			
			bWriter.close();
			fWriter.close();
			bReader.close();
			fReader.close();		
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve docno field for all documents in the index
	 * containing given term
	 * @param term to be searched in all documents
	 * @return A List containing docno field of all documents
	 * containing given term
	 * @author Tirthraj
	 */
	static List<String> getDocNoOfDocHavingTerm(String term) {
		
		List<String> allIdOfDocHavingTerm = new ArrayList<String>();
		
		QueryBuilder qBuilder = QueryBuilders.termQuery(Constants.indexFieldToAnalyze,term);
		
		SearchResponse scrollResp = client.prepareSearch(Constants.indexName)
				.setTypes(Constants.indexType)
		        .setScroll(new TimeValue(60000))
		        .setQuery(qBuilder)
		        .addField("docno")
		        .setSize(10000)	//10000 hits per shard will be returned for each scroll
		        .get();
		
		//Scroll until no hits are returned
		while (true) {

		    for (SearchHit hit : scrollResp.getHits().getHits()) {
		        //Handle the hit...
		    	allIdOfDocHavingTerm.add(hit.field("docno").getValue().toString());
		    }
		    
		    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
		    		.setScroll(new TimeValue(60000))
		    		.get();
		    
		    //Break condition: No hits are returned
		    if (scrollResp.getHits().getHits().length == 0) {
		        break;
		    }
		}
		
		return allIdOfDocHavingTerm;
	}
	
	static Map<String,Long> getDocLengthMapFromFile() {
		
		Map<String,Long> docLengthMap = new HashMap<String, Long>();
		
		try {
			FileReader fReader = new FileReader(Constants.docLengthFile);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String line = "";
			
			while((line = bReader.readLine()) != null) {
				
				String[] valuePair = line.split("\\s+");
				
				docLengthMap.put(valuePair[0], Long.parseLong(valuePair[1]));
			}
			
			bReader.close();
			fReader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return docLengthMap;
	}
	
	static void storeDocLengthMap() {
		
		long startTime = System.currentTimeMillis();
		
		Map<String,Long> docLengthMap = getDocLengthMap();
		
		System.out.println("Map Size: "+docLengthMap.size());
		long endTime = System.currentTimeMillis();
		System.out.println("Map Execution Time: "+(endTime-startTime)/60000);
		
		startTime = System.currentTimeMillis();
		
		FileWriter fWriter = null;
		BufferedWriter bWriter = null;
		StringBuilder data = new StringBuilder();
		
		double sum = 0;
		
		try {
			fWriter = new FileWriter(Constants.docLengthFile);
			bWriter = new BufferedWriter(fWriter);
	
			for (Entry<String,Long> entry : docLengthMap.entrySet()) {
				data.append(entry.getKey()+"\t"+entry.getValue()+"\n");
				sum+=entry.getValue();
			}
			
			data.append("Total"+"\t"+sum+"\n");
			bWriter.append(data.toString());
			
			bWriter.close();
			fWriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Total: "+sum);
		
		endTime = System.currentTimeMillis();
		System.out.println("File Storage Execution Time: "+(endTime-startTime)/60000);
	}

	static Map<String,Long> getDocLengthMap() {
		Map<String,Long> docLengthMap = new HashMap<String,Long>();

		Script script = new Script("docLength",ScriptType.FILE,"groovy",null);
		
		SearchResponse scrollResp = client.prepareSearch(Constants.indexName)
				.setTypes(Constants.indexType)
				.setScroll(new TimeValue(60000))
				.addField("docno")
				.addScriptField("doc_length", script)
				.setSize(1000)	//1000 hits per shard will be returned for each scroll
		        .get();	
		
		// Scroll until no hits are returned
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				// Handle the hit...
				String docId = hit.field("docno").getValue().toString();
				Long docLength = Long.parseLong(hit.field("doc_length").getValue().toString());
				
				System.out.println(docId+"-"+docLength);
				docLengthMap.put(docId, docLength);
			}

			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(60000))
					.get();

			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
		
		client.close();
		
		return docLengthMap;
	}
	
	static long getTermFrequency(String term, String field, String value) {

		HashMap<String,String> paramMap = new HashMap<String,String>();
		paramMap.put("term", term);
		paramMap.put("field", "text");

		Script script = new Script("getTF",ScriptType.FILE,"groovy",paramMap);
		ScoreFunctionBuilder sfBuilder = ScoreFunctionBuilders.scriptFunction(script);
		
		QueryBuilder fsQuery = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery(field, value))
				.must(QueryBuilders.termQuery("text", term));	
		
		QueryBuilder customScoreQuery = QueryBuilders.functionScoreQuery(fsQuery,sfBuilder)
				.boostMode("replace");
		
		SearchResponse searchResp = client.prepareSearch(Constants.indexName)
				.setTypes(Constants.indexType)
				.setQuery(customScoreQuery)
				.addField("stream_id")
		        .get();
		
		return (long) searchResp.getHits().getMaxScore();
		
	}
	
	static double getTotalTermFrequency(String term) {
		
		HashMap<String,String> paramMap = new HashMap<String,String>();
		paramMap.put("term", term);
		paramMap.put("field", "text");
		
		String scriptFieldName = "ttf";

		Script script = new Script("getTTF",ScriptType.FILE,"groovy",paramMap);
		
		SearchResponse searchResp = client.prepareSearch(Constants.indexName)
				.setTypes(Constants.indexType)
				.setSize(1)
				.addScriptField(scriptFieldName, script)
				.get();
		
		SearchHit[] hit = searchResp.getHits().getHits();
		
		return Double.parseDouble(hit[0].field(scriptFieldName).getValue().toString());
	}

	public static LinkedHashMap<String, Double> getTopN(Map<String, Double> otfdqMap, int n) {
		
		LinkedHashMap<String, Double> descendingValuesMap = sortByValue(otfdqMap);
		LinkedHashMap<String, Double> topN = new LinkedHashMap<String, Double>();
		
		int i = 0;
		for (Entry<String, Double> entry : descendingValuesMap.entrySet()) {
			if(i<n) {
				topN.put(entry.getKey(), entry.getValue());
				i++;
			}
			else {
				break;
			}
		}
		
		return topN;
	}
	
	static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue( Map<K, V> map ) {
	    
		LinkedList<Map.Entry<K, V>> list =
	        new LinkedList<Map.Entry<K, V>>( map.entrySet() );
	   
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	    {
	        public int compare(Entry<K, V> o1, Entry<K, V> o2 )
	        {
	            return ( o1.getValue() ).compareTo( o2.getValue() ) * -1;
	        }
	    } );
	
		LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
	    for (Map.Entry<K, V> entry : list)
	    {
	        result.put( entry.getKey(), entry.getValue());
	    }
	    return result;
	}
}
