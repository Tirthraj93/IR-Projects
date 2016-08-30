package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PersistanceHelper {
	/**
	 * Serialize given object into given file
	 * @param <T> Object type
	 * @param t object to serialize
	 * @param fileString Output file string for serialization
	 */
	public static <T> void serializeObject(T t, String fileString) {
		try {
			FileOutputStream fileOut = new FileOutputStream(fileString);
			ObjectOutputStream objOut =  new ObjectOutputStream(fileOut);
			objOut.writeObject(t);
			objOut.close();
			fileOut.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Deserialize given file into given Object
	 * @param fileString file string to deserialize
	 * @param <T> Object type
	 * @param t output object type for deserialization
	 * @return The output object type with deserialized data
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deSerializeObject(String fileString, T t) {
		T tOut = null;
		try {
			FileInputStream fileIn = new FileInputStream(fileString);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			tOut = (T) objIn.readObject();
			objIn.close();
			fileIn.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		return tOut;
	}
}
