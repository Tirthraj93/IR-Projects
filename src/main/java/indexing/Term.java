package indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Constants;
import util.Stemmer;

/**
 * A class containing a unique term id, term string and its positions in a document.
 * @author Tirthraj
 *
 */
public class Term {
	
	/**
	 * term id counter
	 */
	private static int counter = 1;
	
	/**
	 * Map of term and term id
	 */
	private static TreeMap<String,Integer> termMap = new TreeMap<String, Integer>();
	
	/**
	 * A unique term id
	 */
	private int id;
	
	/**
	 * the actual term
	 */
	private String term;
	
	private int basePosition;

	/**
	 * the term positions ArrayList
	 */
	private ArrayList<Integer> positions = new ArrayList<Integer>();
	
	/**
	 * If term has occurred before, then use same term id; otherwise create new term id.
	 * Set term as termArg. Append position to positions.
	 * @param termArg term string
	 * @param position position of the term in the document
	 */
	public Term(String termArg, int position) {
		if(termMap.containsKey(termArg)) {
			this.id = termMap.get(termArg);
			this.term = termArg;
			this.basePosition = position;
			this.positions.add(position);
		}
		else {
			termMap.put(termArg, counter);
			this.id = counter;
			this.term = termArg;
			this.basePosition = position;
			this.positions.add(position);
			counter++;
		}
	}

	
	public static TreeMap<String, Integer> getTermMap() {
		return termMap;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public ArrayList<Integer> getPositions() {
		return positions;
	}

	public void addPosition(int position) {
		this.positions.add(position);
	}

	@Override
	public String toString() {
		return id + " " + term + " " + positions.toString();
	}

	/**
	 * Create ArrayList of Term for a Document
	 * @param text The text of the Document
	 * @return The ArrayList of Term based on given text
	 */
	public static ArrayList<Term> getTerms(String text) {
		ArrayList<Term> termsList = new ArrayList<Term>();
		ArrayList<String> terms = createTerms(text);
		int position = 1;
		HashMap<String,Term> termMapLocal = new HashMap<String,Term>();
		for (String term : terms) {
			// If term is occurring first time then create new Term object
			// and put it in termMap
			if(!termMapLocal.containsKey(term)) {
				Term t = new Term(term,position);
				termMapLocal.put(term, t);
				position++;
			}
			// For recurrence of the term, append its position to an existing
			// Term object and put it in termMap
			else {
				Term t = termMapLocal.get(term);
				ArrayList<Integer> prevPositions = t.getPositions();
				int prevPositionsSize = prevPositions.size();
				int newPosition = position - t.basePosition;
				if(prevPositionsSize > 1) {
					newPosition -= prevPositions.get(prevPositionsSize-1);
				}
				t.addPosition(newPosition);
				termMapLocal.put(term, t);
				position++;
			}
		}
		//return the list of Term for given text stored in termMap
		for (Entry<String, Term> entry : termMapLocal.entrySet()) {
			termsList.add(entry.getValue());
		}
		return termsList;
	}
	 
	/**
	 * Tokenize the document text for indexing
	 * @param text The text of the document
	 * @return The ArrayList of tokens/terms
	 * 	stemmed or non-stemmed based on stemming constant
	 * @author Tirthraj
	 */
	public static ArrayList<String> createTerms(String text) {
		ArrayList<String> termsList = new ArrayList<String>();
		Stemmer s = new Stemmer();
		if (text.length() != 0) {
			//text = text.replaceAll("(?!\\.)\\W|_", " ");
			//String splitRegEx = "\\s+|\\.\\s+|.$"; //Split the string to match (\\w+(\\.?\\w+)*)
			//String splitRegEx = "\\s+";
			//String[] terms = text.trim().split(splitRegEx);
			
			String regex = "[0-9a-z]+(\\.?[0-9a-z]+)*";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(text.toLowerCase().trim());
			
			switch (Constants.manualIndexType) {
			case 1:
				// Index as it is
				while(matcher.find()) {
					String term = matcher.group();
					termsList.add(term.trim());
				}
				break;
			case 2:
				// Remove stop words
				while(matcher.find()) {
					String term = matcher.group();
					if (!Constants.stopWordsSet.contains(matcher.group())) {
						termsList.add(term.trim());
					}
				}
				break;
			case 3:
				// Stem
				while(matcher.find()) {
					String term = matcher.group();
					termsList.add(s.stem(term.trim()));
				}
				break;
			case 4:
				// Remove stop words and stem
				while(matcher.find()) {
					String term = matcher.group();
					if (!Constants.stopWordsSet.contains(matcher.group())) {
						termsList.add(s.stem(term.trim()));
					}
				}
				break;
			default:
				break;
			}
		}
		return termsList;
	}
}
