package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommonUtil {
	/**
	 * Dump data from given map to given file
	 * @param fileName name of the file
	 * @param map Map to dump data of
	 * @author Tirthraj
	 */
	public static <K, V> void dumpMap(String fileName,Map<K,V> map) {
		
		File file = new File(fileName);
		
		try {
			FileWriter fWriter = new FileWriter(file);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			StringBuilder sb = new StringBuilder();
			for (Entry<K, V> entry : map.entrySet()) {
				sb.append(entry.getKey()).append("\t").append(entry.getValue()).append("\n");
			}
			String out = sb.subSequence(0, sb.length()-1).toString();
			bWriter.append(out);
			bWriter.close();
			fWriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(map.size());
	}
	
	/**
	 * Read data (first two columns) from a file into a Map
	 * @param file file to read from
	 * @return map of strings created by reading first two columns of file
	 * @author Tirthraj
	 */
	public static Map<String,String> getMapFromFile(File file) {
		Map<String,String> map = new HashMap<String,String>();
		try {
			FileReader fReader = new FileReader(file);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String line = "";
			while ((line = bReader.readLine()) != null) {
				String[] sArray = line.split("\\s+");
				map.put(sArray[0], sArray[1]);
			}
			bReader.close();
			fReader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return map;
	}
}
