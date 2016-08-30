package indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import util.Constants;

public class Executor {
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		QueryExecution.executeQueries();
		long endTime = System.currentTimeMillis();
		System.out.println("\n\nExecution Time: "+(endTime-startTime)/60000);
	}
	public static void indexCreation() {
		long startTime = System.currentTimeMillis();
		ParseDocs.processDocuments(new File(Constants.filePath));
		long endTime = System.currentTimeMillis();
		long indexTime = (endTime-startTime)/60000;
		
		startTime = System.currentTimeMillis();
		IndexProcessing.mergeIndexFiles();
		endTime = System.currentTimeMillis();
		
		IndexProcessing.renameMergedFile();
		
		System.out.println("\n\nIndex Time: " + indexTime);
		System.out.println("\n\nMerge Time: "+(endTime-startTime)/60000);
	}
	public static void printTotalDocLength() {
		long length = 0;
		for(int i=1;i<84679;i++) {
			length += InvertedIndex.getDocLength(i+"");
		}
		System.out.println(length);
	}
	public static void generateTermTestOutput() {
		File inputFile = new File(Constants.termsFileInput);
		File outputFile = new File(Constants.termsFileOutput);
		try {
			FileReader fReader = new FileReader(inputFile);
			BufferedReader bReader = new BufferedReader(fReader);

			String line = "";
			StringBuffer sbOut = new StringBuffer();
			while ((line = bReader.readLine()) != null) {
				String term = Term.createTerms(line).get(0);
				System.out.println("Term: "+term);
				InvertedIndex invertedIndex = new InvertedIndex(term);
				int df = invertedIndex.getDf();
				long ttf = invertedIndex.getTtf();
				sbOut.append(term+" "+df+" "+ttf+"\n");
			}
			bReader.close();
			fReader.close();
			
			String outString = sbOut.subSequence(0, sbOut.length()-1).toString();
			
			FileWriter fWriter = new FileWriter(outputFile);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			bWriter.append(outString);
			bWriter.close();
			fWriter.close();
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
