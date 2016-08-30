package indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Document represents the document to index
 * @author Tirthraj
 */
public class Document {
	
	/**
	 * static document id counter
	 */
	private static int docCounter = 1;
	
	/**
	 * Map of doc id to doc no
	 */
	private static Map<Integer,String> docIdNoMap = new HashMap<Integer, String>();
	
	/**
	 * The unique id of the document given while parsing
	 */
	private int id;
	
	/**
	 * A unique document number from data
	 */
	private String docno;
	
	/**
	 * ArrayList of Terms in the document text
	 * containing term id, term string and its positions
	 */
	private ArrayList<Term> terms;

	/**
	 * Constructor to initialize values of class variables
	 * @param id
	 * @param docno
	 * @param text
	 */
	public Document(String docno, String text) {
		super();
		this.id = docCounter;
		this.docno = docno;
		this.terms = Term.getTerms(text);
		docIdNoMap.put(docCounter, docno);
		docCounter++;
	}
	
	public static Map<Integer, String> getDocIdNoMap() {
		return docIdNoMap;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDocno() {
		return docno;
	}
	public void setDocno(String docno) {
		this.docno = docno;
	}
	public List<Term> getTerms() {
		return terms;
	}
	public void setTerms(ArrayList<Term> terms) {
		this.terms = terms;
	}
	@Override
	public String toString() {
		return id + " " + docno + " " + terms.size();
	}
	
	/*public static void main(String[] args) {
		Document d1 = new Document("a1", "a");
		Document d2 = new Document("a2", "b");
		System.out.println(d1.getId()+"-"+d2.getId());
	}*/
}
