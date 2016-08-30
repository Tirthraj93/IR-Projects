package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.elasticsearch.common.settings.Settings;

public class Constants {

	//******************************
	// Global Constants
	//******************************
	public static final String filePath = "D:\\NEU\\Sem 2 - Summer\\IR\\IR_data\\AP89_DATA\\AP_DATA\\ap89_collection\\";
	public static final String queryFilePath = "D:\\NEU\\Sem 2 - Summer\\IR\\IR_data\\AP89_DATA\\AP_DATA\\";

	//******************************
	// Manual Indexing Constants
	//******************************

	// Index Type
	//	1 : Index as it is
	//	2 : Remove stop words
	//	3 : Stem
	// 	4 : Remove stop words and stem
	public static final int manualIndexType = 4;
	public static final String indexFilePath = queryFilePath +"index\\";
	public static final String invertedIndexFile;
	public static final String catalogFile;
	public static final String termIdMapFile;
	public static final String docLengthMapFile;
	public static final double sumOfAllDocLength;
	public static final double vocabSize;
	public static final String termsFileOutput;
	static {
		switch (manualIndexType) {
		case 1:
			termsFileOutput = indexFilePath + "in_0_50_no_stop_no_stem_output.txt";
			invertedIndexFile = indexFilePath + "invertedIndexType1";
			catalogFile = indexFilePath + "indexCatalogType1";
			termIdMapFile = indexFilePath + "term_id_map_type1";
			docLengthMapFile = indexFilePath + "doc_length_map_type1";
			sumOfAllDocLength = 38391689;
			vocabSize = 198588;
			break;
		case 2:
			termsFileOutput = indexFilePath + "in_0_50_stop_output.txt";
			invertedIndexFile = indexFilePath + "invertedIndexType2";
			catalogFile = indexFilePath + "indexCatalogType2";
			termIdMapFile = indexFilePath + "term_id_map_type2";
			docLengthMapFile = indexFilePath + "doc_length_map_type2";
			sumOfAllDocLength = 21528970;
			vocabSize = 198196;
			break;
		case 3:
			termsFileOutput = indexFilePath + "in_0_50_stem_output.txt";
			invertedIndexFile = indexFilePath + "invertedIndexType3";
			catalogFile = indexFilePath + "indexCatalogType3";
			termIdMapFile = indexFilePath + "term_id_map_type3";
			docLengthMapFile = indexFilePath + "doc_length_map_type3";
			sumOfAllDocLength = 38391689;
			vocabSize = 158053;
			break;
		default:
			termsFileOutput = indexFilePath + "in_0_50_stop_stem_output.txt";
			invertedIndexFile = indexFilePath + "invertedIndexType4";
			catalogFile = indexFilePath + "indexCatalogType4";
			termIdMapFile = indexFilePath + "term_id_map_type4";
			docLengthMapFile = indexFilePath + "doc_length_map_type4";
			sumOfAllDocLength = 21528970;
			vocabSize = 157862;
			break;
		}
	}
	public static final double totalNoOfDocs = 84678;
	public static final double avgDocLength = sumOfAllDocLength / totalNoOfDocs;
	public static final Set<String> stopWordsSet = getStopWordsSet();
	public static final String docIdNoMapFile = indexFilePath + "doc_id_no_map";
	public static final String termsFileInput = indexFilePath + "in_0_50.txt";
	
	// Execution Model
	// 1 : Vector Model
	// 2 : Language Model
	// 3 : Proximity Search Model
	public static final int executionModel = 3;
	public static final int executionThreadPoolSize = 25;
	//******************************
	// Elastic Search Constants
	//******************************
	public static final Settings settings = Settings.settingsBuilder().put("client.transport.sniff", true)
			.put("cluster.name", Constants.clusterName).build();
	public static final String clusterName = "dead-pool";
	public static final String elasticHost = "localhost";
	public static final int elasticPort = 9300;
	public static final float avgDocLengthCrawling = 5000;
	//******************************
	// Constants for Crawling
	//******************************
	public static final String scriptDequeue = "MAGIC_DEQUEUE";
	public static final String scriptEnqueue = "MAGIC_ENQUEUE";
	public static int crawlingPoolsSize = 8;
	public static int queueFetchSize = 30;
	public static final int bulkSize = 30;
	public static final int maxQueueSize = 5000;
	public static final long timeToNextQueueTruncation = 30 * 1000; //in milliseconds
	public static final long indexedDocumentCountPrintInterval = 1 * 60 * 1000; //in milliseconds
	public static final int crawlingThreadTimeoutMinutes = 60;
	public static final int robotConnectionTimeout = 500;
	public static final int urlConnectionTimeout = 4000;
	//******************************
	// Constants for Document Parsing
	//******************************
	public static final String indexName = "ap_dataset";
	public static final String indexType = "document";
	
	//******************************
	// Constants for Querying
	//******************************
	public static final String idDocNoFile = queryFilePath + "id_doc_no";
	public static final String docLengthFile = queryFilePath + "docLength";
	public static final String queryFile = queryFilePath + "query_desc.51-100.short_updated.txt";
	public static final String okapiResultsFile = queryFilePath + "okapi_tf_results";
	public static final String tfIdfResultsFile = queryFilePath + "tf_idf_results";
	public static final String bM25ResultsFile = queryFilePath + "bm25_results";
	public static final String laplaceUniLMResultsFile = queryFilePath + "laplace_uni_LM_results";
	public static final String JMUniLMResultsFile = queryFilePath + "jm_uni_lm_results";
	public static final String proximitySearchResultsFile = queryFilePath + "proximity_search_results";
	public static final String stopWordsFile = queryFilePath + "stoplist.txt";
	public static final String indexFieldToAnalyze = "text";
	public static final int resultSize = 1000;
	
	//******************************
	// Constants for Models
	//******************************
	//public static final double sumOfAllDocLength = 20984156; //20983860
	//public static final double totalNoOfDocs = 84678; 
	//public static final double avgDocLength = 247.811190628; //247.807695033
	public static final double bm25Part1Numberator = totalNoOfDocs + 0.5;
//	public static final double vocabSize = 178050;
	public static final boolean languageModel = true;
	public static final double lambda = 0.6;
	public static final double proximityLambda = 0.8;
	public static final double proximityC = 1500;
	public static final double backgCoef = 1 - lambda;
	public static final ArrayList<String> removalTermsList = new ArrayList<String>() {{
			add("document");
			add("will");
			add("must");
			add("discuss");
			add("describ");
			add("type");
			add("event");
			add("predict");
			add("countri");
			add("identifi");
	}};
	
	//******************************
	// Constant Helpers
	//******************************
	private static Set<String> getStopWordsSet() {
		
		Set<String> stopWordsSet = new HashSet<String>();
		
		try {
			FileReader fReader = new FileReader(Constants.stopWordsFile);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String line = "";
			
			while ((line = bReader.readLine()) != null) {
				stopWordsSet.add(line);
			}
			
			bReader.close();
			fReader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return stopWordsSet;
	}

}
