package indexing;

import java.util.ArrayList;
import java.util.Comparator;

public class IndexValue {
	private int docId;
	private int tf;
	private ArrayList<Integer> positions = new ArrayList<Integer>();
	
	public IndexValue(int docId, int tf, ArrayList<Integer> positions) {
		super();
		this.docId = docId;
		this.tf = tf;
		this.positions = positions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Integer integer : positions) {
			sb.append(integer).append(" ");
		}
		String positionsString = sb.toString();
		return docId + " " + positionsString.subSequence(0, positionsString.length()-1);
	}
	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public int getTf() {
		return tf;
	}

	public void setTf(int tf) {
		this.tf = tf;
	}

	public ArrayList<Integer> getPositions() {
		return positions;
	}

	public void setPositions(ArrayList<Integer> positions) {
		this.positions = positions;
	}
	
}

class IndexValueComparator implements Comparator<IndexValue> {

	public int compare(IndexValue o1, IndexValue o2) {
		if(o1.getTf() >= o2.getTf()) {
			return -1;
		}
		else {
			return 1;
		}
	}
	
}