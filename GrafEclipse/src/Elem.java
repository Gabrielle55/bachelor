import java.util.*;

// Hvert element skal have en key (total distance), en bitarray (indeholder removed_edges) og en hyperpath (kant id'er)
public class Elem {

	public int key;
	public int[] hyperpath;
	public int[] removed_edges;
	
	
	public Elem(int key, int[] hyperpath, final int[] removed_edges) {
		this.key = key;
		this.hyperpath = hyperpath;
		this.removed_edges = removed_edges;
	}
	
	
	private int getKey() {
		return key;
	}
	private void setKey(int key) {
		this.key = key;
	}
	private int[] getRemoved_edges() {
		return removed_edges;
	}

	private void setRemoved_edges(int[] removed_edges) {
		this.removed_edges = removed_edges;
	}

	private int[] getHyperpath_edge() {
		return hyperpath;
	}
	private void setHyperpath(int[] hyperpath) {
		this.hyperpath = hyperpath;
	}



}

