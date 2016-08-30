package indexing;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import util.CommonUtil;
import util.Constants;

public class InvertedIndex {
	private static Map<String,String> catalogMap = CommonUtil.getMapFromFile(new File(Constants.catalogFile));
	private static Map<String,String> docLengthMap = CommonUtil.getMapFromFile(new File(Constants.docLengthMapFile));
	private static Map<String,String> termIdMap = CommonUtil.getMapFromFile(new File(Constants.termIdMapFile));
	private String term;
	private ArrayList<IndexValue> indexValues = new ArrayList<IndexValue>();
	
	public InvertedIndex(String term) {
		super();
		this.term = term;
		String termId = termIdMap.get(term);
		String termString = "";
		try {
			RandomAccessFile indexFileAccess = new RandomAccessFile(new File(Constants.invertedIndexFile), "r");
			termString = IndexProcessing.getLine(indexFileAccess, catalogMap.get(termId));
			indexFileAccess.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.indexValues = IndexProcessing.getIndexValues(termString.trim());
	}
	
	public static Map<String, String> getDocLengthMap() {
		return docLengthMap;
	}

	public int getTf(String docId) {
		int docIdInt = Integer.parseInt(docId);
		for (IndexValue indexValue : indexValues) {
			if(indexValue.getDocId() == docIdInt) {
				return indexValue.getTf();
			}
		}
		return 0;
	}
	
	public long getTtf() {
		long ttf = 0;
		for (IndexValue indexValue : indexValues) {
			ttf += indexValue.getTf();
		}
		return ttf;
	}
	
	public int getDf() {
		return indexValues.size();
	}
	
	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}
	
	public ArrayList<IndexValue> getIndexValues() {
		return indexValues;
	}

	public static long getDocLength(String docId) {
		return Long.parseLong(docLengthMap.get(docId));
	}

	public List<String> getDocIdListHavingTerm() {
		List<String> docIdhListavingTerm = new ArrayList<String>();
		for (IndexValue indexValue : indexValues) {
			docIdhListavingTerm.add(indexValue.getDocId()+"");
		}
		return docIdhListavingTerm;
	}

	public static Map<String, ArrayList<Integer>> getTermPositionsMap(String docId, Map<String, Map<String, ArrayList<Integer>>> termDocPositionsMap) {
		Map<String, ArrayList<Integer>> termPositionsMap = new HashMap<String, ArrayList<Integer>>();
		boolean foundDoc = false;
		for (Entry<String, Map<String, ArrayList<Integer>>> entry : termDocPositionsMap.entrySet()) {
			Map<String, ArrayList<Integer>> docPositionMap = entry.getValue();
			if(docPositionMap.containsKey(docId)) {
				foundDoc = true;
				termPositionsMap.put(entry.getKey(), docPositionMap.get(docId));
			}
		}
		if(foundDoc) {
			return termPositionsMap;
		}
		return null;
	}

	public Map<String, ArrayList<Integer>> getDocPositionsMap() {
		Map<String, ArrayList<Integer>> docPositionsMap = new HashMap<String, ArrayList<Integer>>();
		for (IndexValue indexValue : indexValues) {
			ArrayList<Integer> positions = new ArrayList<Integer>();
			int prevPosition = 0;
			for(int position : indexValue.getPositions()) {
				int actualPosition = prevPosition + position;
				positions.add(actualPosition);
				prevPosition = actualPosition;
			}
			docPositionsMap.put(indexValue.getDocId()+"", positions);
		}
		return docPositionsMap;
	}
}