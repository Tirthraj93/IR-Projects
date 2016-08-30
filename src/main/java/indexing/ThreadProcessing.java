package indexing;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadProcessing implements Runnable {

	public static ArrayList<File> indexFileList = new ArrayList<File>();
	private int count;
	
	public ThreadProcessing(int countArg) {
		this.count = countArg;
	}

	public static ArrayList<File> getIndexFileList() {
		return indexFileList;
	}

	public static void setIndexFileList(ArrayList<File> indexFileList) {
		ThreadProcessing.indexFileList = indexFileList;
	}

	public static void threadMerge() {
		int listSize = indexFileList.size();
		if(listSize == 1) {
			return;
		}
		System.out.println("Merging " + listSize + " files");
		ExecutorService executor = Executors.newFixedThreadPool(listSize/2);
		int counter = 0;
		for(int i=0; i<listSize/2; i++) {
			ThreadProcessing workerThread = new ThreadProcessing(counter);
			counter += 2;
			executor.execute(workerThread);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {   }  
		counter = 0;
		for(int i=0; i<listSize/2; i++) {
			indexFileList.remove(counter);
			counter++;
		}
		threadMerge();
	}

	public void run() {
		File newIndexFile = indexFileList.get(this.count);
		File indexFile = indexFileList.get(this.count+1);
		
		File newCatalogFile = IndexProcessing.getCatalogFile(newIndexFile);
		File catalogFile = IndexProcessing.getCatalogFile(indexFile);
		
		IndexProcessing.mergeFiles(newIndexFile, indexFile, newCatalogFile, catalogFile);
		
		newIndexFile.delete();
		newCatalogFile.delete();
	}

}
