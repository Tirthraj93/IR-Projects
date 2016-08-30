package retrievalModels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import indexing.IndexProcessing;
import indexing.InvertedIndex;
import util.Constants;

public class Models {
	
	private static Map<String, String> docLengthMap = InvertedIndex.getDocLengthMap();
	
	public static double okapiTF(long tf, String docno) {
		return tf / (tf + 0.5 + (1.5 * (Long.parseLong(docLengthMap.get(docno))/Constants.avgDocLength)));
	}
	
	public static double tfIDF(long tf, String docno, long dfw) {
		return okapiTF(tf, docno) * Math.log(Constants.totalNoOfDocs / dfw);
	}
	
	public static double bm25(long tf, String docno, long dfw) {
		// k1 = 1.2
	    // 0 !k2 ! 1000
		// b = 0.75
		
		double k1 = 1.2;
		double k2 = 500;
		double b = 0.75;
		double docLengthFactor = Long.parseLong(docLengthMap.get(docno))/Constants.avgDocLength;
		
		double part1Denominator = dfw + 0.5;
		double part1 = Math.log(Constants.bm25Part1Numberator / part1Denominator);

		double part2Numerator = tf + (k1 * tf);
		double part2Denominator = tf + (k1 * ((1 - b) + (b *docLengthFactor)));
		double part2 = part2Numerator/part2Denominator;
		
		double part3Numerator = tf + (k2 * tf);
		double part3 = part3Numerator / (tf + k2);
		
		return part1 * part2;
		
	}
	
	public static double lm_laplace(long tf, String docno) {
		double pLaplace = (tf + 1.0) / (double)(Long.parseLong(docLengthMap.get(docno)) + Constants.vocabSize);
		return Math.log(pLaplace);
	}
	
	public static double lm_jm(long tf, double ttf, String docno) {
		
		double docLength = Long.parseLong(docLengthMap.get(docno));
		double part1;
		
		if (docLength == 0){
			part1 = 0;
		}
		else {
			part1 = Constants.lambda * (tf / docLength);
		}
		
		double part2Num = ttf - tf;
		double part2Den = Constants.sumOfAllDocLength - docLength;
		
		double part2 = Constants.backgCoef * (part2Num / part2Den);
		
		return Math.log(part1 + part2);
	}

	public static double proximitySearch(String docId, Map<String, ArrayList<Integer>> termPositionsMap) {
		if(termPositionsMap == null) {
			return 0;
		}
		double docLength = Double.parseDouble(docLengthMap.get(docId));
		int nGramLength = termPositionsMap.size();
		int minSpan = Integer.MAX_VALUE;
		ArrayList<ArrayList<Integer>> positionsList = new ArrayList<ArrayList<Integer>>();
		for (Entry<String, ArrayList<Integer>> entry : termPositionsMap.entrySet()) {
			positionsList.add(entry.getValue());
		}
		int positionsRemaining;
		do {
			int[] windowArray = new int[nGramLength];
			int minValue = Integer.MAX_VALUE;
			int minIndex = 0;
			for (int i=0; i<nGramLength; i++) {
				ArrayList<Integer> temp = positionsList.get(i);
				int value = temp.get(0);
				windowArray[i] = value;
				if(value < minValue && temp.size() != 1) {
					minValue = value;
					minIndex = i;
				}
			}
			int span = getSpan(windowArray);
			if(span < minSpan) {
				minSpan = span;
			}
			positionsRemaining = 0;
			for (ArrayList<Integer> positions : positionsList) {
				positionsRemaining += positions.size();
			}
			ArrayList<Integer> temp = positionsList.get(minIndex);
			if(temp.size() != 1) {
				positionsList.get(minIndex).remove(0);
			}
		}while(positionsRemaining != nGramLength);
		
		double numerator = (Constants.proximityC - minSpan) * nGramLength;
		double denominator = docLength + Constants.vocabSize;
		
		return numerator / denominator;
	}

	private static int getSpan(int[] windowArray) {
		Arrays.sort(windowArray);
		return  windowArray[windowArray.length-1] - windowArray[0];
	}
	public static void main(String[] args) {
		Map<String, ArrayList<Integer>> termPositionsMap = new HashMap<String, ArrayList<Integer>>();
		ArrayList<Integer> list1 = new ArrayList<Integer>() {{
			add(1);
			add(12);
			add(8);
		}};
		ArrayList<Integer> list2 = new ArrayList<Integer>() {{
			add(2);
			add(6);
			add(9);
		}};
		ArrayList<Integer> list3 = new ArrayList<Integer>() {{
			add(4);
			add(7);
			add(10);
		}};
		termPositionsMap.put("a", list1);
		termPositionsMap.put("b", list2);
		termPositionsMap.put("c", list3);
		proximitySearch("dummy",termPositionsMap);
	}
}
