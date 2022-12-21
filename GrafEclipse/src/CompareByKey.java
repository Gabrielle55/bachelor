import java.util.Comparator;
public class CompareByKey implements Comparator<Elem> {
	
	public int compare(Elem a, Elem b) {
		return Integer.compare(a.key, b.key);
	}
	

}
