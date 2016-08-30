package indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrievalModels.ModelHelper;
import retrievalModels.Models;

import java.util.Set;

import util.CommonUtil;
import util.Constants;

public class QueryExecution implements Runnable {

	private static Map<String,String> docIdNoMap = CommonUtil.getMapFromFile(new File(Constants.docIdNoMapFile));
	private static StringBuilder outputString = new StringBuilder();
	private static FileWriter fWriter;
	private String query;
	private int number;
	
	public QueryExecution(String queryLine, int number) {
		this.query = queryLine;
		this.number = number;
	}

	public static void executeQueries() {
		long startTime = System.currentTimeMillis();
		try {
			FileReader fReader = new FileReader(Constants.queryFile);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String line = "";
			int no = 0;
			
			ExecutorService executor = Executors.newFixedThreadPool(Constants.executionThreadPoolSize);
			
			while ((line = bReader.readLine()) != null) {
				QueryExecution newThread = new QueryExecution(line,no);
				no++;
				executor.execute(newThread);
			}
			
			executor.shutdown();
			while (!executor.isTerminated()) {   }  
			
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
	}
	
	public void run() {
		switch (Constants.executionModel) {
		case 1:
			executeTermVectorModel();
			break;
		case 2:
			executeLanguageModel();
			break;
		default:
			executeProximitySearchModel();
			break;
		}
	}
	
	private void executeTermVectorModel() {
		System.out.println("Starting Q" + number + "...");
		
			String[] words = query.split("\\s+");
			String qNo = words[0];
			// Using Set<String> to get unique terms in the query,
			// because duplicate terms need not be evaluated again
			Set<String> terms = new HashSet<String>();
			for (int i = 1; i < words.length; i++) {
				terms.add(words[i]);
			}
			
			Map<String,Double> otfdqMap = new HashMap<String, Double>();
			for (String term : terms) {

				InvertedIndex invertedIndex = new InvertedIndex(term);
				List<String> allDocIdHavingTerm = 
						invertedIndex.getDocIdListHavingTerm();
				long dfw = allDocIdHavingTerm.size();
					for (String docId : allDocIdHavingTerm) {
					long tf = invertedIndex.getTf(docId);
					double score = Models.bm25(tf, docId, dfw);
					String docno = docIdNoMap.get(docId);
					if (otfdqMap.containsKey(docno)) {
						score = otfdqMap.get(docno)+score;
					}
					otfdqMap.put(docno, score);
				}
			}
			
			String queryInfo = qNo + " " + "Q"+ number +" ";
			Map<String,Double> otfdqMapSorted = ModelHelper.getTopN(otfdqMap, Constants.resultSize);
			int rank=1;
			StringBuilder sbOut = new StringBuilder();
			for (Entry<String, Double> entry : otfdqMapSorted.entrySet()) {
				sbOut.append(queryInfo+entry.getKey()+" "+rank+" "+entry.getValue()+" "+"Tirth"+"\n");
				rank++;
			}
			outputString.append(sbOut.toString());
			try {
				fWriter = new FileWriter(Constants.bM25ResultsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Ended Q" + number + "...");
	}
	
	private void executeLanguageModel() {
		System.out.println("Starting Q" + number + "...");

			String[] words = query.split("\\s+");
			String qNo = words[0];
			// Using Set<String> to get unique terms in the query,
			// because duplicate terms need not be evaluated again
			Set<String> terms = new HashSet<String>();
			for (int i = 1; i < words.length; i++) {
				terms.add(words[i]);
			}
				
			Map<String,Double> otfdqMap = new HashMap<String, Double>();
			for (String term : terms) {
				
				InvertedIndex invertedIndex = new InvertedIndex(term);
				double ttf = invertedIndex.getTtf();

				List<String> allDocIdHavingTerm = 
						invertedIndex.getDocIdListHavingTerm();
					
				for (String docId : docIdNoMap.keySet()) {
					String docno = docIdNoMap.get(docId);
					if(!allDocIdHavingTerm.contains(docId)) {
						double score = Models.lm_laplace(0, docId);
						if (otfdqMap.containsKey(docno)) {
							score = otfdqMap.get(docno)+score;
						}
						otfdqMap.put(docno, score);
					}
					else {
						long tf = invertedIndex.getTf(docId);

						double score = Models.lm_laplace(tf, docId);
							
						if (otfdqMap.containsKey(docno)) {
							score = otfdqMap.get(docno)+score;
						}
						otfdqMap.put(docno, score); 
					}
				}
			}
				
			String queryInfo = qNo + " " + "Q"+ number +" ";
			Map<String,Double> otfdqMapSorted = ModelHelper.getTopN(otfdqMap, Constants.resultSize);
			int rank=1;
			StringBuilder sbOut = new StringBuilder();
			for (Entry<String, Double> entry : otfdqMapSorted.entrySet()) {
				sbOut.append(queryInfo+entry.getKey()+" "+rank+" "+entry.getValue()+" "+"Tirth"+"\n");
				rank++;
			}
			outputString.append(sbOut.toString());
			try {
				fWriter = new FileWriter(Constants.laplaceUniLMResultsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Ended Q" + number + "...");
	}
	
	private void executeProximitySearchModel() {
		System.out.println("Starting Q" + number + "...");

		String[] words = query.split("\\s+");
		String qNo = words[0];
		// Using Set<String> to get unique terms in the query,
		// because duplicate terms need not be evaluated again
		Map<String,Map<String,ArrayList<Integer>>> termDocPositionsMap = new HashMap<String, Map<String,ArrayList<Integer>>>();
		
		for (int i = 1; i < words.length; i++) {
			String term = words[i];
			InvertedIndex invertedIndex = new InvertedIndex(term);
			termDocPositionsMap.put(term,invertedIndex.getDocPositionsMap());
		}

		Map<String,Double> otfdqMap = new HashMap<String, Double>();
		for (String docId : docIdNoMap.keySet()) {
			String docno = docIdNoMap.get(docId);
			Map<String,ArrayList<Integer>> termPositionsMap = InvertedIndex.getTermPositionsMap(docId,termDocPositionsMap);
			double score = Models.proximitySearch(docId,termPositionsMap);
			otfdqMap.put(docno, score);
		}
		String queryInfo = qNo + " " + "Q"+ number +" ";
		Map<String,Double> otfdqMapSorted = ModelHelper.getTopN(otfdqMap, Constants.resultSize);
		int rank=1;
		StringBuilder sbOut = new StringBuilder();
		for (Entry<String, Double> entry : otfdqMapSorted.entrySet()) {
			sbOut.append(queryInfo+entry.getKey()+" "+rank+" "+entry.getValue()+" "+"Tirth"+"\n");
			rank++;
		}
		outputString.append(sbOut.toString());
		try {
			fWriter = new FileWriter(Constants.proximitySearchResultsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ended Q" + number + "...");
	}
}
