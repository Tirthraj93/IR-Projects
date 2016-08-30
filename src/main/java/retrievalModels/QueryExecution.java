package retrievalModels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.elasticsearch.client.Client;

import util.Constants;
import webCrawling.util.ElasticConnection;

public class QueryExecution {

	private static Client client = ElasticConnection.getElasticSearchClient();
	
	public static void main(String[] args) throws IOException {
		if(Constants.languageModel) {
			executeLanguageModel();
		}
		else {
			executeTermVectorModel();
		}
	}
	
	static void executeTermVectorModel() {
		long startTime = System.currentTimeMillis();
		try {
			FileReader fReader = new FileReader(Constants.queryFile);
			BufferedReader bReader = new BufferedReader(fReader);
			
			StringBuilder outputString = new StringBuilder();
			String line = "";
			String qNo = "";
			int no = 0;
			
			// Using Set<String> to get unique terms in the query,
			// because duplicate terms need not be evaluated again
			Set<String> terms = new HashSet<String>();

			while ((line = bReader.readLine()) != null) {
				System.out.println("Processiong Q"+no);
				String[] words = line.split("\\s+");
				
				qNo = words[0];

				for (int i = 1; i < words.length; i++) {
					terms.add(words[i]);
				}
				
				Map<String,Double> otfdqMap = new HashMap<String, Double>();
				for (String term : terms) {
					
					System.out.println("\tTerm: "+term);
					List<String> allDocNoOfDocHavingTerm = 
							ModelHelper.getDocNoOfDocHavingTerm(term);
					long dfw = allDocNoOfDocHavingTerm.size();

					for (String docno : allDocNoOfDocHavingTerm) {
						long tf = ModelHelper.getTermFrequency(term, "docno", docno);
						double score = Models.bm25(tf, docno, dfw);
						if (otfdqMap.containsKey(docno)) {
							score = otfdqMap.get(docno)+score;
						}
						otfdqMap.put(docno, score);
					}
				}
				
				String queryInfo = qNo + " " + "Q"+no +" ";
				Map<String,Double> otfdqMapSorted = ModelHelper.getTopN(otfdqMap, Constants.resultSize);
				int rank=1;
				for (Entry<String, Double> entry : otfdqMapSorted.entrySet()) {
					outputString.append(queryInfo+entry.getKey()+" "+rank+" "+entry.getValue()+" "+"Tirth"+"\n");
					rank++;
				}
				
				no++;
				terms = new HashSet<String>();
			}
			FileWriter fWriter = new FileWriter(Constants.bM25ResultsFile);
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
		
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time: "+(endTime-startTime)/60000);

		client.close();
	}
	
	static void executeLanguageModel() {
		long startTime = System.currentTimeMillis();
		
		Map<String,Long> docLengthMap = ModelHelper.getDocLengthMapFromFile();
		
		try {
			FileReader fReader = new FileReader(Constants.queryFile);
			BufferedReader bReader = new BufferedReader(fReader);
			
			StringBuilder outputString = new StringBuilder();
			String line = "";
			String qNo = "";
			int no = 0;
			
			// Using Set<String> to get unique terms in the query,
			// because duplicate terms need not be evaluated again
			Set<String> terms = new HashSet<String>();
			
			while ((line = bReader.readLine()) != null) {
				System.out.println("Processiong Q"+no);
				String[] words = line.split("\\s+");
				
				qNo = words[0];

				for (int i = 1; i < words.length; i++) {
					terms.add(words[i]);
				}
				
				Map<String,Double> otfdqMap = new HashMap<String, Double>();
				for (String term : terms) {
					
					System.out.println("\tTerm: "+term);
					
					double ttf = ModelHelper.getTotalTermFrequency(term);

					List<String> allDocNoOfDocHavingTerm = 
							ModelHelper.getDocNoOfDocHavingTerm(term);
					
					for (String docno : docLengthMap.keySet()) {
						if(!allDocNoOfDocHavingTerm.contains(docno)) {
							double score = Models.lm_jm(0, ttf, docno);
							if (otfdqMap.containsKey(docno)) {
								score = otfdqMap.get(docno)+score;
							}
							otfdqMap.put(docno, score);
						}
						else {
							long tf = ModelHelper.getTermFrequency(term, "docno", docno);

							double score = Models.lm_jm(tf, ttf, docno);
							
							if (otfdqMap.containsKey(docno)) {
								score = otfdqMap.get(docno)+score;
							}
							otfdqMap.put(docno, score); 
						}
					}
				}
				
				String queryInfo = qNo + " " + "Q"+no +" ";
				Map<String,Double> otfdqMapSorted = ModelHelper.getTopN(otfdqMap, Constants.resultSize);
				int rank=1;
				for (Entry<String, Double> entry : otfdqMapSorted.entrySet()) {
					outputString.append(queryInfo+entry.getKey()+" "+rank+" "+entry.getValue()+" "+"Tirth"+"\n");
					rank++;
				}
				
				no++;
				terms = new HashSet<String>();
			}
			FileWriter fWriter = new FileWriter(Constants.JMUniLMResultsFile);
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
		
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time: "+(endTime-startTime)/60000);

		client.close();
	}
	
}
