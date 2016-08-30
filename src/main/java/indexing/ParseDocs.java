package indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import util.CommonUtil;
import util.Constants;

/**
 * A class to parse given files for indexing
 * @author Tirthraj
 */
public class ParseDocs {

	/**
	 * Processes all documents in the data to index
	 * @param folder
	 *            The path to the folder containing all files
	 * @return an ArrayList of retrieved Documents
	 * @author Tirthraj
	 */
	public static void processDocuments(File folder) {
		
		File[] files = folder.listFiles();
		ArrayList<Document> documentList = new ArrayList<Document>();
		
		int bulkBuilderLength = 0;
		
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
						Document document = new Document(docNo, body.toString());
						documentList.add(document);
						bulkBuilderLength++;
						
						if(bulkBuilderLength % 1000 == 0 || bulkBuilderLength == 84678) {
							IndexProcessing.indexDocuments(documentList);
							documentList = new ArrayList<Document>();
						}
						
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
		
		CommonUtil.dumpMap(Constants.termIdMapFile,Term.getTermMap());
		CommonUtil.dumpMap(Constants.docIdNoMapFile,Document.getDocIdNoMap());
		CommonUtil.dumpMap(Constants.docLengthMapFile, IndexProcessing.getDocLengthMap());;
	}
}
