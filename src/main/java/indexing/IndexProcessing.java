package indexing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import util.CommonUtil;
import util.Constants;

/**
 * A class for creating and processing manual index of Documents
 * @author Tirthraj
 */
public class IndexProcessing {
	/**
	 * Counter to keep track of file number
	 */
	private static int counter =  1;
	private static Map<String,String> docLengthMap = new HashMap<String, String>();
	
	
	public static Map<String, String> getDocLengthMap() {
		return docLengthMap;
	}

	/**
	 * Create index of given list of Documents
	 * @param documentList list of Documents to be indexed
	 * @author Tirthraj
	 */
	public static void indexDocuments(ArrayList<Document> documentList) {
		Map<Integer,ArrayList<IndexValue>> invertedIndexMap = getInvertedIndex(documentList);
		storeInvertedIndex(invertedIndexMap);
	}

	/**
	 * Merge all files up to the counter 
	 * @author Tirthraj
	 */
	public static void mergeIndexFiles() {
		ArrayList<File> indexFileList = new ArrayList<File>();
		for(int i=0;i<counter-1;i++) {
			indexFileList.add(new File(Constants.invertedIndexFile+"_"+(i+1)));
		}
		ThreadProcessing.setIndexFileList(indexFileList);
		ThreadProcessing.threadMerge();
	}
	
	/**
	 * Merges two index files into one
	 * @param left index file 1
	 * @param right index file 2
	 */
	public static File mergeFiles(File newIndexFile, File indexFile, File newCatalogFile, File catalogFile) {

		String mergedFileName = indexFile.getName();
		System.out.println("Merging... "+newIndexFile.getName() + " - " + mergedFileName);
		
		if(!indexFile.exists()) {
			newIndexFile.renameTo(indexFile);
			newCatalogFile.renameTo(catalogFile);
			return indexFile;
		}
		
		Map<String,String> newCatalogMap = CommonUtil.getMapFromFile(newCatalogFile);
		Map<String,String> catalogMap = CommonUtil.getMapFromFile(catalogFile);
		
		File tempIndexFile = new File("temp"+mergedFileName);
		File tempCatalogFile = new File("temp"+catalogFile.getName());
		
		int lineNo = 1;
		try {
			RandomAccessFile tempIndexFileAccess = new RandomAccessFile(tempIndexFile, "rw");
			RandomAccessFile tempCatalogFileAccess = new RandomAccessFile(tempCatalogFile, "rw");
			RandomAccessFile newIndexFileAccess = new RandomAccessFile(newIndexFile, "r");
			RandomAccessFile indexFileAccess = new RandomAccessFile(indexFile, "r");
			for (Entry<String, String> entry : catalogMap.entrySet()) {
				String term = entry.getKey();
				String offset = entry.getValue();
				String indexTerm = getLine(indexFileAccess, offset);
				if(newCatalogMap.containsKey(term)) {
					String newIndexTerm = getLine(newIndexFileAccess,newCatalogMap.get(term));
					String mergedTerm = mergeTerms(newIndexTerm.trim(),indexTerm.trim());
					long beginOffset = tempIndexFileAccess.length();
					tempIndexFileAccess.seek(beginOffset);
					tempIndexFileAccess.write(mergedTerm.getBytes());
					long endOffset = tempIndexFileAccess.getFilePointer()-1;
					String catalogString = term+" "+beginOffset+" "+endOffset+" "+lineNo+"\n";
					lineNo++;
					tempCatalogFileAccess.seek(tempCatalogFileAccess.length());
					tempCatalogFileAccess.write(catalogString.getBytes());
					newCatalogMap.remove(term);
				}
				else {
					long beginOffset = tempIndexFileAccess.length();
					tempIndexFileAccess.seek(beginOffset);
					tempIndexFileAccess.write(indexTerm.getBytes());
					long endOffset = tempIndexFileAccess.getFilePointer()-1;
					String catalogString = term+" "+beginOffset+" "+endOffset+" "+lineNo+"\n";
					lineNo++;
					tempCatalogFileAccess.seek(tempCatalogFileAccess.length());
					tempCatalogFileAccess.write(catalogString.getBytes());
				}
			}
			for (Entry<String, String> entry : newCatalogMap.entrySet()) {
				String newIndexTerm = getLine(newIndexFileAccess,entry.getValue());
				long beginOffset = tempIndexFileAccess.length();
				tempIndexFileAccess.seek(beginOffset);
				tempIndexFileAccess.write(newIndexTerm.getBytes());
				long endOffset = tempIndexFileAccess.getFilePointer()-1;
				String catalogString = entry.getKey()+" "+beginOffset+" "+endOffset+" "+lineNo+"\n";
				lineNo++;
				tempCatalogFileAccess.seek(tempCatalogFileAccess.length());
				tempCatalogFileAccess.write(catalogString.getBytes());
			}
			tempCatalogFileAccess.setLength(tempCatalogFileAccess.length()-1);
			tempIndexFileAccess.close();
			tempCatalogFileAccess.close();
			newIndexFileAccess.close();
			indexFileAccess.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		tempIndexFile.renameTo(indexFile);
		tempCatalogFile.renameTo(catalogFile);
		
		return tempIndexFile;
	}

	public static Map<String, String> getTermDataMap(File indexFile, Map<String, String> catalogMap) {
		Map<String, String> termDataMap = new HashMap<String, String>();
		try {
			RandomAccessFile fileAccess = new RandomAccessFile(indexFile, "r");
			for (Entry<String, String> entry : catalogMap.entrySet()) {
				fileAccess.seek(Long.parseLong(entry.getValue()));
				String data = fileAccess.readLine() + "\n";
				termDataMap.put(entry.getKey(), data);
			}
			fileAccess.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return termDataMap;
	}

	public static String mergeTerms(String term1, String term2) {

		ArrayList<IndexValue> leftIndexValues = getIndexValues(term1);
		ArrayList<IndexValue> rightIndexValues = getIndexValues(term2);
		
		ArrayList<IndexValue> indexValues = mergeIndexValues(leftIndexValues, rightIndexValues);
		StringBuffer sb = new StringBuffer();
		for (IndexValue indexValue : indexValues) {
			sb.append(indexValue.toString()+"#");
		}
		return sb.toString() + "\n";
	}
	
	public static ArrayList<IndexValue> mergeIndexValues(ArrayList<IndexValue> leftList, ArrayList<IndexValue> rightList) {
		ArrayList<IndexValue> mergedIndexValues = new ArrayList<IndexValue>();
		while((leftList.size() > 0) && (rightList.size() > 0)) {
			IndexValue leftListObject = leftList.get(0);
			IndexValue rightListObject = rightList.get(0);
			if(leftListObject.getTf() > rightListObject.getTf()) {
				mergedIndexValues.add(leftListObject);
				leftList.remove(0);
			}
			else {
				mergedIndexValues.add(rightListObject);
				rightList.remove(0);
			}
		}
		while(leftList.size() > 0) {
			mergedIndexValues.add(leftList.get(0));
			leftList.remove(0);
		}
		while(rightList.size() > 0) {
			mergedIndexValues.add(rightList.get(0));
			rightList.remove(0);
		}
		return mergedIndexValues;
	}
	
	public static String getLine(RandomAccessFile fileAccess, String offset) {
		String line = "";
		try {
			fileAccess.seek(Long.parseLong(offset));
			line = fileAccess.readLine() + "\n";
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}

	public static ArrayList<IndexValue> getIndexValues(String termString) {
		ArrayList<IndexValue> indexValues = new ArrayList<IndexValue>();
		String[] indexValueStrings = termString.split("\\#");
		for (String string : indexValueStrings) {
			String[] indexValueString = string.split("\\s+");
			String docId = indexValueString[0];
			String[] positionsArray = Arrays.copyOfRange(indexValueString, 1, indexValueString.length);
			int tf = positionsArray.length;
			ArrayList<Integer> positions = new ArrayList<Integer>();
			for (String p : positionsArray) {
				positions.add(Integer.parseInt(p));
			}
			IndexValue i = new IndexValue(Integer.parseInt(docId), tf, positions);
			indexValues.add(i);
		}
		return indexValues;
	}
	
	/**
	 * Returns a file pointer of a catalog file based on original file
	 * @param file original file
	 * @return catalog file based on file
	 */
	public static File getCatalogFile(File file) {
		String[] name = file.getName().split("_");
			return new File(Constants.catalogFile+"_"+name[1]);
	}

	/**
	 * Creates a map of inverted index from given list of Documents
	 * @param documentList list of Documents to be indexed
	 * @return a map of inverted index
	 * @author Tirthraj
	 */
	public static Map<Integer,ArrayList<IndexValue>> getInvertedIndex(ArrayList<Document> documentList) {
		Map<Integer,ArrayList<IndexValue>> invertedIndexMap = new HashMap<Integer, ArrayList<IndexValue>>();
		for (Document document : documentList) {
			System.out.println(document.toString());
			int docId = document.getId();
			long docLength = 0;
			for (Term t : document.getTerms()) {
				
				int term = t.getId();
				ArrayList<Integer> positions = t.getPositions();
				int tf = positions.size();
				docLength += tf;
				IndexValue indexValue = new IndexValue(docId, tf, positions);
				
				if(invertedIndexMap.containsKey(term)) {
					ArrayList<IndexValue> temp = new ArrayList<IndexValue>();
					temp.add(indexValue);
					invertedIndexMap.put(term, mergeIndexValues(invertedIndexMap.get(term), temp));
				}
				else {
					ArrayList<IndexValue> indexValues = new ArrayList<IndexValue>();
					indexValues.add(indexValue);
					invertedIndexMap.put(term, indexValues);
				}
			}
			docLengthMap.put(docId+"", docLength+"");
		}
		return invertedIndexMap;
	}
	
	/**
	 * stores inverted index given in a map to an index file 
	 * along with a catalog file to read index file efficiently
	 * @param invertedIndexMap a map of inverted index to be stored
	 * @author Tirthraj
	 */
	public static void storeInvertedIndex(Map<Integer, ArrayList<IndexValue>> invertedIndexMap) {
		Map<String,String> sOut = getIndexFileContent(invertedIndexMap);
		File invertedIndexFile = new File(Constants.invertedIndexFile+"_"+counter);
		File catalogFile = new File(Constants.catalogFile+"_"+counter);
		writeIndexFileContent(sOut, invertedIndexFile, catalogFile);
		counter++;
	}
	
	public static Map<String,String> getIndexFileContent(Map<Integer, ArrayList<IndexValue>> invertedIndexMap) {
		Map<String,String> indexFileContent = new HashMap<String, String>();
		for (Entry<Integer, ArrayList<IndexValue>> entry : invertedIndexMap.entrySet()) {
			StringBuffer sbIndex = new StringBuffer();
			for (IndexValue i : entry.getValue()) {
				sbIndex.append(i.toString()+"#");
			}
			indexFileContent.put(entry.getKey().toString(), sbIndex.toString() + "\n");
		}
		return indexFileContent;
	}
	
	public static File writeIndexFileContent(Map<String, String> sOut, File outputIndexFile, File outputCatalogFile) {
		
		int lineNo = 1;
		
		try {
			RandomAccessFile indexFileAccess = new RandomAccessFile(outputIndexFile, "rw");
			FileWriter catalogWriter = new FileWriter(outputCatalogFile);
			BufferedWriter catalogBufferedWriter = new BufferedWriter(catalogWriter);
			StringBuilder sbCatalog = new StringBuilder();
			
			for (Entry<String,String> entry : sOut.entrySet()) {
				
				String termId = entry.getKey();
				
				long beginOffset = indexFileAccess.getFilePointer();
				byte[] indexLineBytes = (entry.getValue()).getBytes();
				indexFileAccess.write(indexLineBytes);
				long endOffset = indexFileAccess.getFilePointer()-1;
				
				sbCatalog.append(termId+" "+beginOffset+" "+endOffset+" "+lineNo+"\n");
				lineNo++;
			}
			
			String cOut = sbCatalog.subSequence(0, sbCatalog.length()-1).toString();
			catalogBufferedWriter.append(cOut);
			
			indexFileAccess.close();
			catalogBufferedWriter.close();
			catalogWriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return outputIndexFile;
	}

	public static void renameMergedFile() {
		File folder = new File(Constants.indexFilePath);
		File[] files = folder.listFiles();
		for (File file : files) {
			if(file.getName().startsWith(Constants.invertedIndexFile)) {
				file.renameTo(new File(Constants.invertedIndexFile));
			}
			else if(file.getName().startsWith(Constants.catalogFile)) {
				file.renameTo(new File(Constants.catalogFile));
			}
		}
	}
}
