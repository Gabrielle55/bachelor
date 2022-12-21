import java.util.*;

public class Runner {

	public static void main(String[] args) {
//		GrafEclipse.createHyperGraph(args[0]);
//		GrafEclipse.createSimpleGraphAndRunDijkstra(args[1], 0);
//		GrafEclipse.testSBT(args[2]);
		
		int WORD_SIZE = 32;
	    int size_of_bit_array = (int) Math.ceil(9/ 8);
	    int[] removed_edges = new int[size_of_bit_array / WORD_SIZE + (size_of_bit_array % WORD_SIZE == 0 ? 0 : 1)];
	    
	    for (int i = 0; i < 9; i++) {
	    	System.out.println("removed edges " + getBit(removed_edges, i));
	    }
	    System.out.println("break");
	    removed_edges = setBit(removed_edges, 3, true);
	    for (int i = 0; i < 9; i++) {
	    	System.out.println("removed edges " + getBit(removed_edges, i));
	    }
	    
	}
	
	// Inspired by the code at https://stackoverflow.com/questions/15736626/java-how-to-create-and-manipulate-a-bit-array-with-length-of-10-million-bits
    private static boolean getBit(int[] bits, int pos) {
    	// 32 is the word size
        return (bits[pos / 32] & (1 << (pos % 32))) != 0;
    }
    

    // Inspired by the code at https://stackoverflow.com/questions/15736626/java-how-to-create-and-manipulate-a-bit-array-with-length-of-10-million-bits
    private static int[] setBit(int[] bits, int pos, boolean b) {
    	
        int word = bits[pos / 32];
        int posBit = 1 << (pos % 32);
        if (b) {
            word |= posBit;
        } else {
            word &= (0xFFFFFFFF - posBit);
        }
        bits[pos / 32] = word;
        return bits;
    }

}
