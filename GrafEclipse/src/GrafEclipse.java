import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import java.lang.System;

public class GrafEclipse {

	public static void main(String[] args) {
//		testDijkstra(args[1], 0);
//		testSBT(args[1]);
		createHypergraphAndRunKShortest(args[3]);
	}
	

	private static void createHypergraphAndRunKShortest(String argument) {
		int[] edges_hyper = new int[0];
		int[] edge_ids;
		int[] lengths; // Bruges til at lave listen "vertices_hyper"
		int[] vertices_hyper;
		int[] FS;
		int[] BS;
		int number_of_nodes_hyper = 0;
		int number_of_edges_hyper = 0;

		
		try {
			File hyper_file = new File(argument);
			Scanner scanner = new Scanner(hyper_file);
			number_of_nodes_hyper = scanner.nextInt();
			number_of_edges_hyper = scanner.nextInt();
			
			edges_hyper = readFile(scanner, number_of_nodes_hyper, number_of_edges_hyper);
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		edge_ids = new int[number_of_edges_hyper];

		// "lengths" er en list over længden af de individuelle knuders FS og BS
		lengths = new int[number_of_nodes_hyper * 2];
		lengths = computeLengths(edges_hyper);

		// Nu skal listem med kanter køres igennem for at få længderne på FS og BS listerne

		// Den kumulatative sum af FSes og BSes længder - 1 er nu i listen "vertices_hyper"
		vertices_hyper = computeVerticesSums(lengths, number_of_nodes_hyper);
		

		
		int FSlength = vertices_hyper[vertices_hyper.length -2] + 1;
		int BSlength = vertices_hyper[vertices_hyper.length -1] + 1;

		FS = new int[FSlength];
		BS = new int[BSlength];
		
		int number_until_next_length = 0;
		int index = -1;
		for (int i = 0; i < edges_hyper.length; i++) {	
			if (number_until_next_length > 2) {
				// FS
				vertices_hyper[edges_hyper[i]] -= 1;
				FS[vertices_hyper[edges_hyper[i]]] = index;
			}
			else if (number_until_next_length == 2) {
				// BS
				vertices_hyper[edges_hyper[i] + 1] -= 1;
				BS[vertices_hyper[edges_hyper[i] + 1]] = index;
			} else if(number_until_next_length == 0){
				index++;
				edge_ids[index] = i;
				
				number_until_next_length = edges_hyper[i] + 1;
			}
			
			number_until_next_length--;
		}

		
//		int random_start_node =  (int)(Math.random()*(((vertices_hyper.length - 1) / 2)-0+1)+0) * 2; 
//		System.out.println("random " + random_start_node);
//		int random_end_node = (int)(Math.random()*(((vertices_hyper.length - 1) / 2)-0+1)+0) * 2;
//		System.out.println("random " + random_end_node);
//		long average_time = 0;
//		
//		for (int i = 0; i < 5; i++) {
//			String filename = "experiment1_20000Result";
//			long time_1 = System.nanoTime();
//			String cost_and_paths = KShortest(vertices_hyper, edges_hyper, edge_ids, FS, BS, 36320, 13452, 100);
//			long estimated_time = System.nanoTime() - time_1;
//			average_time += estimated_time;
//			cost_and_paths += estimated_time;
//			filename += (Integer.toString(i)) + ".txt";
//			createFileWithCostsAndPaths(cost_and_paths, filename);
//		}
//		System.out.println("Average time: " + (average_time/5));
		
		String cost_and_paths = KShortest(vertices_hyper, edges_hyper, edge_ids, FS, BS, 10866, 55584, 100);
		System.out.println(cost_and_paths);
		
		//System.out.println("Time used " + estimated_time + " in nanoseconds");
		//System.out.println("which is " +  TimeUnit.MICROSECONDS.convert(estimated_time, TimeUnit.NANOSECONDS) + " microseconds");
	}


	private static int[] orderListOfUsedEdges(int[] hyperpath_nodes, int[] hyperpath_edges, int[] edges, int[] edge_ids) {
		int[] ordered_hyperpath_edges = new int[hyperpath_edges.length];
		int chosen_head;
		for (int i = 1; i < hyperpath_nodes.length; i++) {
			chosen_head = hyperpath_nodes[i];
			// Hvis hovedet af halen er hovedet af den søgte kant
			for (int j = 0; j < hyperpath_edges.length; j++) {
				if (edges[edge_ids[hyperpath_edges[j]] + edges[edge_ids[hyperpath_edges[j]]] - 1] == chosen_head) {
					ordered_hyperpath_edges[i - 1] = hyperpath_edges[j];
					break;
				}
			}
		}
		return ordered_hyperpath_edges;
	}


	private static int[] findHyperpath( int[] dist_pred_edge, int[] edges_hyper, int[] edge_ids, int[] removed_edges, int source, int end_node) {
		int[] hyperpath_edges = findListOfUsedEdges(dist_pred_edge, edges_hyper, edge_ids, removed_edges, source, end_node);
		//System.out.println("number of hyperpath edges " + hyperpath_edges.length);
		int[] new_edges = reverseAndSplitUpListOfEdges(hyperpath_edges, edge_ids, edges_hyper, null);
		int[] used_vertices = findListOfUsedVertices(new_edges);
		int[] sorted_path = DFS(new_edges, used_vertices, source, end_node);
		int[] ordered_hyperpath_edges = orderListOfUsedEdges(sorted_path, hyperpath_edges, edges_hyper, edge_ids);

		return ordered_hyperpath_edges;
	}
	
	private static int[] findListOfUsedVertices(int[] new_edges) {
		ArrayList<Integer> encountered_vertices = new ArrayList<>();
		
		for (int i = 0; i < new_edges.length; i++) {
			if (!encountered_vertices.contains(new_edges[i])) {
				encountered_vertices.add(new_edges[i]);
			}
		}
		int[] used_vertices = new int[encountered_vertices.size()];
		for (int i = 0; i < used_vertices.length; i++) {
			used_vertices[i] = encountered_vertices.get(i);
		}
		
		return used_vertices;
	}
	
	/* 
	 * Tager en hypergraf og en hypervej som input.
	 * Retunerer en opdateret prioritetskø.
	 */
	private static PriorityQueue<Elem> backBranch(int[] vertices, int[] edges, int[] edge_ids, int[] FS, int[] BS, PriorityQueue<Elem> pq, int[] old_bits, int[] hyperpath, int source, int end_node) {
		int number_of_edges_in_backwards_star;
		int[] hyperpath_new;
		int head_of_edge;
		int WORD_SIZE = 32;
		int size_of_bit_array = (int) Math.ceil(edge_ids.length + 1 / 8);
		int[] removed_edges = new int[size_of_bit_array / WORD_SIZE + (size_of_bit_array % WORD_SIZE == 0 ? 0 : 1)];
		
		// q i pseudokoden BackBranch er hyperpath.length - 1
		for (int i = hyperpath.length - 1; i > 0; i--) {
			removed_edges = old_bits.clone();
			removed_edges = setBit(removed_edges, hyperpath[i], true);

			// Fiks BS for H(hypepath[j])
			// Dette skal være med undtagelse af den hyperkant, som der er valgt.
			for (int j = hyperpath.length - 1; j > i; j--) {
				
				
				head_of_edge = edges[edge_ids[hyperpath[j]] + edges[edge_ids[hyperpath[j]]] - 1 ];
				if (head_of_edge + 3 >= vertices.length) {
					number_of_edges_in_backwards_star = BS.length - 1 - vertices[head_of_edge + 1];
				} else {
					number_of_edges_in_backwards_star = vertices[head_of_edge + 3] - vertices[head_of_edge + 1];
				}

				for (int p = 0; p < number_of_edges_in_backwards_star; p++) {
					if (BS[vertices[head_of_edge + 1] + p] != hyperpath[j]) {
						removed_edges = setBit(removed_edges, BS[vertices[head_of_edge + 1] + p], true);
					}
				}

			}
			// Kør SBT med denne removed_edges
			int[] dist_prev = SBT(vertices, edges, edge_ids, FS, removed_edges, source);
			
			// Hvis der er en ny vej, da laves et nyt element som indsættes i pq
			if (dist_prev[end_node] != Integer.MAX_VALUE && dist_prev[end_node + 1] != -2){
				hyperpath_new = findHyperpath(dist_prev, edges, edge_ids, removed_edges, source, end_node);
				Elem element = new Elem(dist_prev[end_node], hyperpath_new, removed_edges);
				// Indsæt i pq
				pq.add(element);
				
			}
		}
		return pq;
	}
	
    // Inspireret af koden på: https://stackoverflow.com/questions/15736626/java-how-to-create-and-manipulate-a-bit-array-with-length-of-10-million-bits
    private static boolean getBit(int[] bits, int pos) {
    	// 32 er antallet af bits i en liste af int
        return (bits[pos / 32] & (1 << (pos % 32))) != 0;
    }
    

    // Inspireret af koden på: https://stackoverflow.com/questions/15736626/java-how-to-create-and-manipulate-a-bit-array-with-length-of-10-million-bits
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
	
	private static String KShortest(int[] vertices, int[] edges, int[] edge_ids, int[] FS, int[] BS, int source, int end_node, int k) {
		PriorityQueue<Elem> pq = new PriorityQueue<>(new CompareByKey()); // Deres L
		String cost_and_paths = "";
	    int WORD_SIZE = 32;
	    int size_of_bit_array = (int) Math.ceil(edge_ids.length + 1 / 8);
	    int[] removed_edges = new int[size_of_bit_array / WORD_SIZE + (size_of_bit_array % WORD_SIZE == 0 ? 0 : 1)];
	    int[] dist_prev = SBT(vertices, edges, edge_ids, FS, removed_edges, source);
	    if (dist_prev[end_node + 1] != -2) {
			// Indsæt den korteste vej samt hypergraf i pq
			int[] hyperpath = findHyperpath(dist_prev, edges, edge_ids, removed_edges, source, end_node);
			Elem shortest_path = new Elem( dist_prev[end_node], hyperpath, removed_edges);
			pq.add(shortest_path);

			
			for (int i = 1; i <= k; i++) {
				if (pq.size() == 0) {
					break;
				}
				Elem shortest = pq.poll();
	
				// Print vejen
				cost_and_paths += "Cost: " + Integer.toString(shortest.key) + "\nPath: ";

				for (int l = 0; l < shortest.hyperpath.length; l++) {
					cost_and_paths += Integer.toString(shortest.hyperpath[l]) + " ";
	
				}
				cost_and_paths += "\n";
			
				
				if (i == k) {
					break;
				}
				
				// Retunerer den opdaterede prioritetskø
				pq = backBranch(vertices, edges, edge_ids, FS, BS, pq, shortest.removed_edges, shortest.hyperpath, source, end_node);
		
			}
	    } 
		return cost_and_paths;
	}

	private static void createFileWithCostsAndPaths(String cost_and_paths, String filename) {
		createFile(filename);
		String path = System.getProperty("user.dir");
		String full_path = path + "\\Eksperimenter\\";
		writeToFile(full_path, filename, cost_and_paths);
	}


	private static int[] reverseAndSplitUpListOfEdges(int[] used_edge_ids, int[] edge_ids, int[] edges, int[] removed_edges) {
		// Hver kant skal splittes op i en ny liste, så kanten vendes og er en almindelig kant
		ArrayList<Integer> new_edges = new ArrayList<>();
		int[] new_edges_list;
		int number_of_nodes_in_tail;
		int index_in_edges;
		int former_head_of_edge;
		for (int i = 0; i < used_edge_ids.length; i++) {
			index_in_edges = edge_ids[used_edge_ids[i]];
			number_of_nodes_in_tail = edges[edge_ids[used_edge_ids[i]]] - 2;
			former_head_of_edge = edges[index_in_edges + number_of_nodes_in_tail + 1];
			
			for (int j = 1; j <= number_of_nodes_in_tail; j++) {
				new_edges.add(former_head_of_edge);
				new_edges.add(edges[index_in_edges + j]);
			}
		}
		new_edges_list = new int[new_edges.size()];
		for (int i = 0; i < new_edges_list.length; i++) {
			new_edges_list[i] = new_edges.get(i);
		}
		return new_edges_list;		
	}
	
	private static int[] findListOfUsedEdges(int[] dist_pred_edge, int[] edges_hyper, int[] edge_ids, int[] removed_edges, int source, int end_node) {
		ArrayList<Integer> path = new ArrayList<>();
		int[] used_edge_ids;

		path = recursiveFindListOfUsedEdges(dist_pred_edge, edges_hyper, edge_ids, removed_edges, source, end_node, path);
		
		
		used_edge_ids = new int[path.size()];
		for (int i = 0; i < path.size(); i++) {
			used_edge_ids[i] = path.get(i);
		}

		
		return used_edge_ids;	
	}
	
	private static ArrayList<Integer> recursiveFindListOfUsedEdges(int[] dist_pred_edge, int[] edges_hyper,
			int[] edge_ids, int[] removed_edges, int source, int current_node, ArrayList<Integer> path) {
		if (current_node == source) {
			return path;
		}
		int current_edge = dist_pred_edge[current_node + 1];
		if (!path.contains(current_edge) && getBit(removed_edges, current_edge) == false) {
			path.add(current_edge);
		} else {
			return path;
		}

		int number_of_nodes_in_tail = edges_hyper[edge_ids[current_edge]] - 1;
		//System.out.println("number of nodes " + (number_of_nodes_in_tail - 1));
		for (int i = 1; i < number_of_nodes_in_tail; i++) {
			path = recursiveFindListOfUsedEdges(dist_pred_edge, edges_hyper, edge_ids, removed_edges, source, edges_hyper[edge_ids[current_edge] + i], path);
		}
		return path;
	}
	
	// Denne metode er baseret på pseudokoden i "Introduction to Algorithms" skrevet af Cormen et al. Tredje version
	private static int[] DFS(int[] new_edges_list, int[] vertices_used, int source, int end_node){
		int[] color_finish_time = new int[vertices_used.length * 2 + 1];
		int[] sorted_path = new int[vertices_used.length];
		int index = 0;

		color_finish_time[color_finish_time.length - 1] = 0; // Tiden
		for (int i = 0; i < vertices_used.length; i++) {
			color_finish_time[i] = -1; // Hvis en knude ikke er besøgt er den "hvid", hvilket skrives som -1
			color_finish_time[vertices_used.length + i] = -1;

		}
		for (int i = 0; i < vertices_used.length; i++) {
			if(color_finish_time[i] == -1) { // Hvis knuden ikke tidligere er besøgt			
				color_finish_time = DFSVisit(new_edges_list, vertices_used, color_finish_time, i, index);
			}
		}
		for (int i = 0; i < vertices_used.length; i++) {
			sorted_path[i] = color_finish_time[vertices_used.length + i];
		}

		return sorted_path;
	}

	
	// Denne metode er baseret på pseudokoden i "Introduction to Algorithms" skrevet af Cormen et al. Tredje version
	private static int[] DFSVisit(int[] new_edges_list, int[] vertices_used, int[] color_finish_time, int i, int index) {
		color_finish_time[color_finish_time.length - 1]++;
		color_finish_time[i] = 0; // Grå skrives som 0
		
		// For hver kant der er nabo til i
		for (int j = 0; j < new_edges_list.length; j += 2) {
			if (new_edges_list[j] == vertices_used[i]) {
				for (int k = 0; k < vertices_used.length; k++) {
					if (new_edges_list[j + 1] == vertices_used[k]) {
						if (color_finish_time[k] == -1) {
							color_finish_time = DFSVisit(new_edges_list, vertices_used, color_finish_time, k, index);
						}
					}
				}
			}
		}
		color_finish_time[i] = 1; // Sort skrives som 1
		color_finish_time[color_finish_time.length - 1]++; // Inkrementér rime
		while (color_finish_time[vertices_used.length + index] != -1) {
			index++;
		}
		color_finish_time[vertices_used.length + index] = vertices_used[i];	
		return color_finish_time;
	}
	
	public static void createHyperGraph(String args) {
		int[] edges_hyper = new int[0];
		int[] edge_ids;
		int[] lengths; // Bruges til at lave listen "vertices_hyper"
		int[] vertices_hyper;
		int[] FS;
		int[] BS;
		int number_of_nodes_hyper = 0;
		int number_of_edges_hyper = 0;

		
		try {
			File hyper_file = new File(args);
			Scanner scanner = new Scanner(hyper_file);
			number_of_nodes_hyper = scanner.nextInt();
			number_of_edges_hyper = scanner.nextInt();
			
			edges_hyper = readFile(scanner, number_of_nodes_hyper, number_of_edges_hyper);
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		edge_ids = new int[number_of_edges_hyper];

		// "lengths" er en list over længden af de individuelle knuders FS og BS
		lengths = new int[number_of_nodes_hyper * 2];
		lengths = computeLengths(edges_hyper);

		// Nu skal listem med kanter køres igennem for at få længderne på FS og BS listerne

		// Den kumulatative sum af FSes og BSes længder - 1 er nu i listen "vertices_hyper"
		vertices_hyper = computeVerticesSums(lengths, number_of_nodes_hyper);
		

		
		int FSlength = vertices_hyper[vertices_hyper.length -2] + 1;
		int BSlength = vertices_hyper[vertices_hyper.length -1] + 1;

		FS = new int[FSlength];
		BS = new int[BSlength];
		
		int number_until_next_length = 0;
		int index = -1;
		for (int i = 0; i < edges_hyper.length; i++) {	
			if (number_until_next_length > 2) {
				// FS
				vertices_hyper[edges_hyper[i]] -= 1;
				FS[vertices_hyper[edges_hyper[i]]] = index;
			}
			else if (number_until_next_length == 2) {
				// BS
				vertices_hyper[edges_hyper[i] + 1] -= 1;
				BS[vertices_hyper[edges_hyper[i] + 1]] = index;
			} else if(number_until_next_length == 0){
				index++;
				edge_ids[index] = i;
				
				number_until_next_length = edges_hyper[i] + 1;
			}
			
			number_until_next_length--;
		}

		
	}
	
	public static void testDijkstra(String args, int source_node) {

		int[] m_edges_vertices = newParsing(args);
		int number_of_nodes = m_edges_vertices[0];
		
		int[] edges_simple = new int[m_edges_vertices.length - number_of_nodes - 1];

		int[] vertices_simple = new int[number_of_nodes];
		
		for (int i = 0; i < m_edges_vertices.length - number_of_nodes - 1; i++) {
			edges_simple[i] = m_edges_vertices[i+1];
		}

		for (int i = 0; i < number_of_nodes; i++) {
			vertices_simple[i] = m_edges_vertices[1 + edges_simple.length + i];
		}
				
		int[] dist_prev;

		dist_prev = dijkstra(edges_simple, vertices_simple, source_node);
		
		createFileWithResult(dist_prev, args, source_node);
	}
	
	public static void testSBT(String args) {
		// Denne del bruger parsing til at læse data på formatet for en almindelig graf
		// Vægtene der læses vil blive indsat men ikke brugt.
		int[] m_edges_vertices_SBT = newParsing(args);
		int number_of_nodes_SBT = m_edges_vertices_SBT[0];
		int[] edges_simple_SBT = new int[m_edges_vertices_SBT.length - number_of_nodes_SBT - 1];
		
		int[] vertices_simple_SBT = new int[number_of_nodes_SBT];
		
		for (int i = 0; i < m_edges_vertices_SBT.length - number_of_nodes_SBT - 1; i++) {
			
			edges_simple_SBT[i] = m_edges_vertices_SBT[i+1];
		}
		
		for (int i = 0; i < number_of_nodes_SBT; i++) {
			vertices_simple_SBT[i] = m_edges_vertices_SBT[1 + edges_simple_SBT.length + i];
		}


		createNewGraphsAndTest(edges_simple_SBT, vertices_simple_SBT, number_of_nodes_SBT);
	}
	
	private static void createNewGraphsAndTest(int[] edges_simple, int[] vertices_simple, int number_of_nodes) {
		Random random = new Random();
		int[] edges_hyper;
		int[] vertices_hyper;
		int[] FS;
		int[] BS;
		int[] edges_id;
		int[] size_of_tail;
		int[] weights;
		int[] lengths;
		int number_of_new_nodes;
		int counter_for_tail_size = 0;
		for (int i = 0; i < edges_simple.length; i += 2) {
			number_of_new_nodes = random.nextInt(5000,50000);
			vertices_hyper = new int[(number_of_new_nodes + 2) * 2];

			size_of_tail = new int[number_of_new_nodes + 1];
			weights = new int[number_of_new_nodes + 1];

			counter_for_tail_size = 2;
			for (int j = 0; j < number_of_new_nodes + 1; j++) {
				// Laver en ny knude og giver det et nyt ID
				size_of_tail[j] = random.nextInt(1, counter_for_tail_size);
				weights[j] = random.nextInt(1,21); 
				if (counter_for_tail_size < 6) {
					counter_for_tail_size++;
				}
			}
						
			edges_id =  new int[size_of_tail.length];

			edges_hyper = computeEdgesHyperFramework(size_of_tail, edges_id, number_of_new_nodes + 2, random);
			edges_id = computeEdgesId(edges_hyper, edges_id);
			edges_hyper = computeHyperEdgeList(edges_hyper, weights, edges_id, random);

			// Dette er listen med længderne af de individuelle knuders FS og BS
			lengths = new int[(number_of_new_nodes + 2) * 2];
			lengths = computeLengths(edges_hyper);
			vertices_hyper = computeVerticesSums(lengths, number_of_new_nodes + 2);
			
			FS = new int[vertices_hyper[vertices_hyper.length -2] + 1];
			BS = new int[vertices_hyper[vertices_hyper.length -1] + 1];
			
			int number_until_next_length = 0;
			int index = -1;
			for (int l = 0; l < edges_hyper.length; l++) {	
				if (number_until_next_length > 2) {
					// FS
					vertices_hyper[edges_hyper[l]] -= 1;
					FS[vertices_hyper[edges_hyper[l]]] = index;
				}
				else if (number_until_next_length == 2) {
					// BS
					vertices_hyper[edges_hyper[l] + 1] -= 1;
					BS[vertices_hyper[edges_hyper[l] + 1]] = index;
				} else if(number_until_next_length == 0){
					index++;
					number_until_next_length = edges_hyper[l] + 1;
				}			
				number_until_next_length--;
			}

			
			int[] dist_pred_edge = SBT(vertices_hyper, edges_hyper, edges_id, FS, null, 0);
			printNodeAndDistanceSBT(dist_pred_edge, i);
			
			simpleGraphWithDijkstraInSBTTest(vertices_hyper, edges_hyper, edges_id, FS, size_of_tail, i);
			
		}
		

	}
	
	
	private static void printNodeAndDistanceDijkstra(int[] dist_prev, int k) {
		String path_to_dir = System.getProperty("user.dir");
		String path = path_to_dir + "\\Dijkresults\\";
		String name = "Dijkstraresult" + Integer.toString(k) + ".txt";
		String filename = path + name;
		File file = new File(filename);
		try {
			FileWriter writer = new FileWriter(filename);
			for (int i = 0; i < dist_prev.length; i += 2) {
				writer.write(Integer.toString(i / 2) + " " + dist_prev[i] + "\n");			
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void printNodeAndDistanceSBT(int[] dist_pred_edge, int k) {
		String path_to_dir = System.getProperty("user.dir");
		String path = path_to_dir + "\\SBTresults\\";
		String name = "SBTresult" + Integer.toString(k) + ".txt";
		String filename = path + name;
		File file = new File(filename);
		try {
			FileWriter writer = new FileWriter(filename);
			for (int i = 0; i < dist_pred_edge.length; i += 2) {
				writer.write(Integer.toString(i / 2) + " " + dist_pred_edge[i] + "\n");			
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void simpleGraphWithDijkstraInSBTTest(int[] vertices_hyper, int[] edges_hyper, int[] edges_id, int[] FS, int[] size_of_tail, int i) {
		int writing_index = 0;
		int number_of_edges_from_id;
		int temp;
		int vertices_index = 0;
		int[] dist_prev;
		int[] vertices_simple;
		int[] edges_simple = new int[sumOfList(size_of_tail) * 2];;
		vertices_simple = new int[vertices_hyper.length / 2];
		vertices_simple[0] = 0;
		for (int j = 0; j < vertices_hyper.length - 2; j += 2) {
			if (j == vertices_hyper.length - 1) {
				number_of_edges_from_id = FS.length - vertices_hyper[j] - 1;
			} else {
				number_of_edges_from_id = vertices_hyper[j+2] - vertices_hyper[j];
			}
	
			for (int p = 0; p < number_of_edges_from_id; p++) {

				temp = edges_hyper[edges_id[FS[vertices_hyper[j] + p]]] - 1;

				edges_simple[writing_index] = edges_hyper[edges_id[FS[vertices_hyper[j] + p]] + temp] / 2; // head of tail

				edges_simple[writing_index + 1] = edges_hyper[edges_id[FS[vertices_hyper[j] + p]] + temp + 1]; //weight of edge;

				writing_index += 2;
			}
			vertices_index++;
			vertices_simple[vertices_index] = writing_index;
		}

		int source_node = 0;
		dist_prev = dijkstra(edges_simple, vertices_simple, source_node);
		printNodeAndDistanceDijkstra(dist_prev, i);
	}
			


	private static int[] computeEdgesId(int[] edges_hyper, int[] edges_id) {
		int index = edges_hyper[0] + 1;
		int i = 1;
		edges_id[0] = 0;
		while (i < edges_id.length) {
			edges_id[i] = index;
			index += edges_hyper[index] + 1;
			i++;
		}
		return edges_id;
	}



	private static int[] computeHyperEdgeList(int[] edges_hyper, int[] weights, int[] edge_ids, Random random) {
		int index = 0;
		int index_in_hyper;
		int head_of_edge;

		while (index < edge_ids.length - 1) {
			index_in_hyper = edge_ids[index] + 1;
			head_of_edge = edges_hyper[edge_ids[index + 1] - 2];
			for (int i = 0; i < edges_hyper[edge_ids[index]] - 2; i++) {
				edges_hyper[ index_in_hyper + i] = getTailId(head_of_edge);
				head_of_edge -= 2;
			}
			edges_hyper[edge_ids[index + 1] - 1] = weights[index];
			index++;

		}
		// Indsætter korrekt den sidste kant for at sikre, at den sidste kant har den oprindelige slutknude som dens slutknude
		index_in_hyper = edge_ids[index] + 1;
		head_of_edge = edges_hyper[edges_hyper.length - 2];
		for (int i = 0; i < edges_hyper[edge_ids[index]] - 2; i++) {
			edges_hyper[ index_in_hyper + i] = getTailId(head_of_edge);
			head_of_edge -= 2;
		}
		edges_hyper[ edges_hyper.length - 1] = weights[index];
		
		return edges_hyper;	
	}
	
	private static int getTailId(int head_id) {
		return head_id - 2;
	}



	private static void computeEdgeLists(int[] edges_hyper, int[] edges_simple, int[] weights) {
		int index_simple_edges = 0;
		int number_of_nodes_in_tail = 0;
		int index_in_edges_hyper = edges_hyper[0] + 1;
		int weight_index = 0;
		int head_of_edge = index_in_edges_hyper - 2;
		edges_hyper[1] = 0;
		edges_simple[0] = head_of_edge / 2;
		edges_simple[1] = weights[weight_index];	
		while ( index_in_edges_hyper < edges_hyper.length) {
			for (int i = 0; i < number_of_nodes_in_tail; i++) {
				edges_simple[index_simple_edges] = head_of_edge / 2;
				edges_simple[index_simple_edges + 1] = weights[weight_index];
				index_simple_edges += 2;
			}
			edges_hyper[index_in_edges_hyper - 1] = weights[weight_index];
			index_in_edges_hyper += edges_hyper[index_in_edges_hyper] + 1;
			head_of_edge = index_in_edges_hyper - 2;
			weight_index++;
			
		}
	}

	private static int computeLengthOfEdges(int[] list) {
		int length = 0;
		for (int i = 0; i < list.length; i++) {
			length += list[i] + 3; 
		}
		return length;
	}
	
	private static int[] computeEdgesHyperFramework(int[] list, int[] edges_id, int number_of_nodes, Random random) {
		int[] edges = new int[computeLengthOfEdges(list)];
		int index = 0;
		int i = 0;
		while (i < list.length - 1) {
			// Sætter tallet der indikerer hvornår næste kant starter
			edges[index] = list[i] + 2;
			edges[index + list[i] + 1] = i * 2 + 2;
			index += list[i] + 3;
			i++;
		}
		// Brugt til at sikre at der er mindst én vej til den sidste knude
		edges[index] = list[i] + 2;
		edges[index + list[i] + 1] = number_of_nodes * 2 - 2;
		return edges;		
	}
	
	private static int sumOfList(int[] list) {
		int sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i];
		}
		return sum;
	}
	
		
	private static void path(int[] dist_pred_edge, int[] vertices, int[] edges, int[] edge_ids, int[] FS, int source) {
		String hyperpaths = "";
		String hyperpath;
		int number_of_neighbors;
		int number_of_edges_in_tail;
		int[] distances;
		int index_of_min;
		int id_of_min_node;
		int node_id;
		int index_in_edges;
		int cost;
		for (int i = 0; i < vertices.length - 1; i += 2) {
			if( i + 2 >= vertices.length) {
				number_of_neighbors = FS.length - 1 - vertices[i];
			} else {
				number_of_neighbors = vertices[i + 2] - vertices[i];
			}
			
			if ( number_of_neighbors == 0) { // Hvis der er ingen kanter i FS, da kan det være endeknuden af en hypervej
				hyperpath = "";
				if( dist_pred_edge[i + 1] >= 0) { // Hvis der er en vej til knuden
					cost = dist_pred_edge[i];
					hyperpath += "Distance: " + Integer.toString(cost) + "\n";
					hyperpath += Integer.toString(i);
					node_id = i;
					
					while (node_id != source) {
						index_in_edges = edge_ids[dist_pred_edge[node_id + 1]];
						hyperpath += " -> ";
						number_of_edges_in_tail = edges[index_in_edges] - 2;
						distances = new int[number_of_edges_in_tail];
						for (int j = 0; j < number_of_edges_in_tail; j++) {							
							distances[j] = dist_pred_edge[edges[index_in_edges + j + 1]];
						}
						index_of_min = findIndexOfMinInList(distances);
						id_of_min_node = edges[index_in_edges + 1 + index_of_min];	
						hyperpath += Integer.toString(id_of_min_node);
						node_id = id_of_min_node;
					}
					
				}
				hyperpaths += hyperpath + "\n";
			}
		}
		
		System.out.println(hyperpaths);
		
	}
	
	private static int findIndexOfMinInList(int[] list) {
		int min = list[0];
		int index = 0;
		for (int i = 1; i < list.length; i++) {
			if (list[i] < min) {
				min = list[i];
				index = i;
			}
		}
		return index;
	}

	// Dette er baseret på pseudokoden fra:
	// https://www.geeksforgeeks.org/dijkstras-algorithm-for-adjacency-list-representation-greedy-algo-8/
	private static int[] dijkstra(int[] edges, int[] vertices, int source_id) {
		// dist_prev er på formatet; [knude_0, pris_0, knude_1, pris_1, ... , knude_m, pris_m]
		// pq er på formatet: [pris_id_0, id_0, ... , pris_id_m, id_m]
		// Altså er pq ikke ordnet, således at et knude_id stemmer overens med indekset.
		int[] pq = new int[0];
		int[] index_list_for_pq = new int[vertices.length];
		int[] dist_prev = new int[vertices.length * 2];
		int alt;
		int number_of_neighbors;
		int index_from_vertices;
		int v_id;
		int u_dist;
		int u_id;

		
		for (int i = 0; i < index_list_for_pq.length; i++) {
			index_list_for_pq[i] = -1;
		}
		for (int i = 0; i < vertices.length; i++) {
			dist_prev[i*2] = Integer.MAX_VALUE;
			dist_prev[i*2+1] = -2;
			pq = insertKey(pq, index_list_for_pq, i, Integer.MAX_VALUE);
			
		}
		pq = decreaseKey(pq, index_list_for_pq, source_id, 0);

		dist_prev[source_id * 2] = 0;
		dist_prev[source_id * 2 + 1] = -1;


		while (pq.length != 0) {
			
			// Udtrækker den mindste pris og det tilhørende id
			u_dist = pq[0];
			u_id = pq[1];
			pq = heapExtractMin(pq, index_list_for_pq);
	
			// Bruges til at finde antallet af naboer til u
			if (u_id == vertices.length - 1){
				number_of_neighbors = (edges.length - vertices[u_id]) / 2;
			} else {
				number_of_neighbors = (vertices[u_id + 1] - vertices[u_id]) / 2;
			}


			index_from_vertices = vertices[u_id];

			// For hver af u's naboer, der stadig er i pq, da opdateres prisen og dens tidligere knude i køen
			for (int i = 0; i < number_of_neighbors; i++) {			

				v_id = edges[index_from_vertices + 2*i];
				if( index_list_for_pq[v_id] != -1) {
					if (u_dist == Integer.MAX_VALUE) {
						alt = Integer.MAX_VALUE;
					}  else {
						alt = u_dist + edges[index_from_vertices + 2*i + 1];
					}

					if (dist_prev[v_id * 2] > alt) {
						// Opdaterer prisen for knude v
						dist_prev[v_id *2] = alt;
						// Opdaterer den tidligere knude i vejen for knude v
						dist_prev[v_id * 2 + 1] = u_id;

						pq = decreaseKey(pq, index_list_for_pq, v_id, alt);

					}
				}

			}
		}
		
		return dist_prev;
	}
	
	private static boolean pq_contains(int[] pq, int id) {
        for(int i = 1; i < pq.length; i += 2) {
            if(pq[i] == id) {
                return true;
            }
        }
		return false;
	}
	
	/*
	 * Denne metode bruges til at skrive resultatet af Dijkstra i en fil, der kan sammenholdes
	 * med NetworkX'es Dijkstra implementation.
	 * Filen kommer ud på formatet:
	 * knude_id_0 næst_sidste_knude_id_0 pris_0 \n
	 * knude_id_1 næst_sidste_knude_id_1 pris_1 \n
	 * ...
	 * knude_id_n-1 næst_sidste_knude_id_n-1 pris_n-1 \n
	 */
	private static void createFileWithResult(int[] dist_prev, String graphname, int source_node) {
		// Lav fil
		int index_of_dot = graphname.indexOf('.');
		String paths;
		String filename = graphname.substring(0, index_of_dot) + "result.txt";
		try {
			File result_file = new File(filename );
			if (result_file.createNewFile()) {
				System.out.println("File created");
			} else {
				System.out.println("File already exists");
			}
			
		} catch (IOException e) {
			System.out.println("An error occured");
			e.printStackTrace();
		}
		try {
			FileWriter writer = new FileWriter(filename);
			writer.write( "0 " + dist_prev[1] + " " + dist_prev[0] + "\n");
			for (int i = 2; i < dist_prev.length; i = i + 2) {
				if ( dist_prev[i+1] == -2 || dist_prev[i] < 0) {
					writer.write(i/2 + " None None\n");
				} else {
					writer.write( i/2 + " " + dist_prev[i+1] + " " + dist_prev[i] + "\n");
				}
			}
			//paths = generatePathsAndDistancePrints(dist_prev, source_node);
			//System.out.println(paths);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static String generatePathsAndDistancePrints(int[] dist_prev, int source) {
		String lines = "";
		String path_line = "";
		ArrayList<Integer> path = new ArrayList<>();
		int predecessor;
		for (int i = 0; i < dist_prev.length / 2; i++) {
			if (dist_prev[2*i + 1] != -2) {
				path = new ArrayList<>();
				path.add(i);
				if (i != source) {					
					predecessor = dist_prev[2*i + 1];
					while (predecessor != source) {
						path.add(predecessor);
						predecessor = dist_prev[predecessor * 2 + 1];				
					}
	
					path.add(source);
	
					lines = lines + "Node " + Integer.toString(i) + ":\n";
					lines = lines + "    distance: " + Integer.toString(dist_prev[2*i]) + "\n";
					lines = lines + "    path: ";
	
					path_line = Integer.toString(path.get(path.size() - 1));
					for (int j = path.size() - 2; j >= 0; j--) {
						path_line = path_line + "->";
						path_line = path_line + Integer.toString(path.get(j));
					}
					lines = lines + path_line + "\n";
					path_line = "";
				} else {
					lines = lines + "Node " + Integer.toString(i) + ":\n";
					lines = lines + "    distance: " + Integer.toString(dist_prev[2*i]) + "\n";
					lines = lines + "    path: " + Integer.toString(source) + "\n";
				}
			}
		}
		return lines;
	}

	// Minimums heap
	private static void heapify(int[] pq, int[] index_list_for_pq, int i) {
		int smallest;
		int l = left(i);
		int r = right(i);

		if( l <= pq.length - 2 && pq[l] <= pq[i]) {
			smallest = l;
		} else {
			smallest = i;
		}
		if (r <= pq.length - 2&& pq[r] <= pq[smallest]) {
			smallest = r;
		}
		if( smallest != i) {
			swap(pq, index_list_for_pq, i, smallest);
			heapify(pq, index_list_for_pq, smallest);
		}
	}
	
	private static int left(int i) {
		return (2*i) + 2;
	}
	
	private static int right(int i) {
		return (2 * i) + 4;
	}

	private static int parent(int i) {
		if ( i == 0) {
			return 0;
		}
		int par = i / 2;
		if (par % 2 == 0) {
			return par - 2;
		} else {
			return par - 1;
		}
	}

	
	
	private static void swap(int[] pq, int[] index_list_for_pq, int i, int j) {
		// Opdatering af index_list_for_pq
		index_list_for_pq[pq[i+1]] = j;
		index_list_for_pq[pq[j+1]] = i;
				
		// Swap i pq
		int temp_key = pq[i];
		int temp_id = pq[i+1];
		pq[i] = pq[j];
		pq[i+1] = pq[j+1];
		pq[j] = temp_key;
		pq[j+1] = temp_id;		
	}
	
	// Denne metode retunerer ikke minimumsværdien, men den opdaterede pq

    private static int[] heapExtractMin(int[] pq, int[] index_list_for_pq) {
        if (pq.length < 2) {
            System.out.println("Error: heap underflow");
            return null;
        }

        //int min = pq[0];		
        index_list_for_pq[pq[1]] = -1;
        pq[0] = pq[pq.length - 2];
        pq[1] = pq[pq.length - 1];

        index_list_for_pq[pq[1]] = 0;
        int[] new_pq = new int[pq.length - 2];
        for (int i = 0; i < pq.length - 2; i++) {
            new_pq[i] = pq[i];
        }

        pq = new_pq;

        heapify(pq, index_list_for_pq, 0);
        return pq;
    }

    // Ændrer værdien af et element i prioritetskøen og holder heapordnen
	private static int[] decreaseKey(int[] pq, int[] index_list_for_pq, int id, int key) {
		int i = index_list_for_pq[id];
		
		if (key > pq[i]) {
			System.out.println("Error: New key is larger than current key");
		}
		pq[i] = key;
		while( pq[parent(i)] > pq[i]) {
			// Opdatering af index_list_for_pq sker i swap
			swap(pq, index_list_for_pq, i, parent(i));
			i = parent(i);
		}
		return pq;
	}
	
	// Indsætter en nøgle i prioritetskøen pq og opdateree index_list_for_pq.
	private static int[] insertKey(int[] pq, int[] index_list_for_pq , int node, int key) {
		int[] new_pq = new int[pq.length + 2];
		for (int i = 0; i < pq.length; i++) {
			new_pq[i] = pq[i];
		}
		
		pq = new_pq;
		pq[pq.length - 2] = Integer.MAX_VALUE;
		pq[pq.length - 1] = node;
		index_list_for_pq[node] = pq.length -2;	
		return decreaseKey(pq, index_list_for_pq, node, key);
	}
	

	
	private static int[] SBT(int[] vertices, int[] edges, int[] edge_ids, int[] FS, int[] removed_edges, int source) {
		// Prioritetskøen pq er på formatet:
		// [vægt_0, id_0, vægt_1, id_1, ..., vægt_m, id_m] 
		int[] pq = new int[0];
		int[] index_list_for_pq = new int[vertices.length];
		int[] k_j = new int[edge_ids.length]; // each element is automatically set to 0
		int[] dist_prev = new int[vertices.length ];
		int u_id;
		int number_of_forward_edges;
		int temp_dist;
		int chosen_edge_j;
		int weight_of_edge;
		int head_of_edge;
		int number_of_nodes_in_tail;


		// Bruges i KShortest
		if (removed_edges == null) {
			int WORD_SIZE = 32;
		    int size_of_bit_array = (int) Math.ceil(edge_ids.length + 1 / 8);
		    removed_edges = new int[size_of_bit_array / WORD_SIZE + (size_of_bit_array % WORD_SIZE == 0 ? 0 : 1)];
		} 


		// For hver knude, sæt distancen til uendelig (Integer.MAX_VALUE)
		for (int i = 0; i < dist_prev.length / 2; i++) {
			dist_prev[i*2] = Integer.MAX_VALUE;
			dist_prev[i*2+1] = -2; // Tidigere kant sættes til -2 for at markere som ukendt
		}
		
		for (int i = 0; i < index_list_for_pq.length; i++) {
			index_list_for_pq[i] = -1;
		}

		// Prioritetskøen består udelukkende af start knuden
		pq = insertKey(pq, index_list_for_pq, source / 2, 0);

		// Sæt distancen og den sidst brugte kan for start knuden
		dist_prev[source] = 0;
		dist_prev[source + 1] = -1;

		
		
		while (pq.length != 0) {
			
			// Udtræk knuden med den mindste distance
			u_id = pq[1] * 2;
			pq = heapExtractMin(pq, index_list_for_pq);

			// Find antallet af kanter i FS(u)
			if (vertices.length > u_id + 2) {
				number_of_forward_edges = vertices[u_id + 2] - vertices[u_id];
			} else {
				number_of_forward_edges = FS.length - 1 - vertices[u_id];
			}
						
			// For hver kant i FS(u)
			for (int i = 0; i < number_of_forward_edges; i++) {
				// Den valgte kant, som er i FS(u)
				chosen_edge_j = FS[vertices[u_id]  + i];
				
				// Tjek for om kanten er i hypergrafen
				if (getBit(removed_edges, chosen_edge_j) == false){

					// Inkrementer tælleren af kanten
					k_j[chosen_edge_j]++;
					
					// Find antaller af knuder i halen, så det kan tjekkes om alle kanter med hale i den valgte knude er brugt
					number_of_nodes_in_tail = edges[edge_ids[chosen_edge_j]] - 2;				
					
					// Hvis k_j[chosen_edge_j] == |T(E_j)|			
					if( k_j[chosen_edge_j] == number_of_nodes_in_tail) {
						
						// Beregn vægtfunktionen					
						temp_dist = weightFunction(dist_prev, edges, edge_ids, chosen_edge_j);
						
						head_of_edge = edges[edge_ids[chosen_edge_j] + edges[edge_ids[chosen_edge_j]] - 1];
						weight_of_edge = edges[edge_ids[chosen_edge_j] + edges[edge_ids[chosen_edge_j]]];
						
						//if (dist_prev[head_of_edge] > temp_dist + weight_of_edge && temp_dist != Integer.MAX_VALUE) {
						if (dist_prev[head_of_edge] > temp_dist + weight_of_edge) {

							// Hvis hovedet af kanten ikke er i pq
							if (index_list_for_pq[head_of_edge / 2] == -1) {	
								pq = insertKey(pq, index_list_for_pq, head_of_edge / 2,  temp_dist + weight_of_edge);
								
							}
	 						

							dist_prev[head_of_edge] = temp_dist + weight_of_edge;
							dist_prev[head_of_edge + 1] = chosen_edge_j;
							
							// Indsæt decrease key
							pq = decreaseKey(pq, index_list_for_pq, head_of_edge / 2, temp_dist + weight_of_edge);
						}
					}
				}
			}

		}

		return dist_prev;
	}
	
	/*
	 * Bruges i SBT til at finde minimumsvægten af alle inadgående kanter i den valgte knude.
	 * Der gøres brug af denne, når man tester SBT ved at sammenholde resultater fundet af Dijkstra og SBT
	 */
	private static int minWeightFunction(int[] dist_prev, int[] edges, int[] edge_ids, int chosen_edge) {
		int min = dist_prev[edges[edge_ids[chosen_edge] + 1]];
		int number_of_nodes_in_tail;
		
		number_of_nodes_in_tail = edges[edge_ids[chosen_edge]] - 2;
		for(int i = 2; i <= number_of_nodes_in_tail + 1; i++) {
			if (dist_prev[edges[edge_ids[chosen_edge] + i]] < min) {
				min = dist_prev[edges[edge_ids[chosen_edge] + i]];
			}
		}
		return min;
	}
	
	/*
	 * Denne metode er en additiv vægtfunktion, som kan bruges i SBT.
	 */
	private static int weightFunction(int[] dist_prev, int[] edges, int[] edge_ids, int chosen_edge) {
		int sum = 0;
		int number_of_nodes_in_tail;	
		number_of_nodes_in_tail = edges[edge_ids[chosen_edge]] - 2;
		for(int i = 1; i <= number_of_nodes_in_tail; i++) {
//			if ( dist_prev[edges[edge_ids[chosen_edge] + i]] == Integer.MAX_VALUE) {
//				return Integer.MAX_VALUE;
//			}
			sum += dist_prev[edges[edge_ids[chosen_edge] + i]];
		}
		return sum;
	}
	
	
	/* Bruges til hypergrafer
	 * Tager et filnavn fra stdin og læser filen, mens den laver listen "edges" baseret på dataen.
	 * De IDer der er brugt i filen erstattes med nye IDer, der er lige tal.
	 * Input filen skal være på formatet:
	 * Antallet af knuder \n
	 * Antallet af kanter \n
	 * Kant_1 \n
	 * Kant_2 \n
	 * ...
	 * Kant_n
	 * Hver kant er på formatet: Antal knuder i halen, knude_id_1, knude_id_2, ... , knude_id_i, vægt
	 * i <= m, hvor m er det højeste knude_id 
	 */
	private static int[] readFile(Scanner scanner, int number_of_nodes, int number_of_edges) {
		ArrayList<Integer> temp_edges = new ArrayList<>();
		int[] already_encountered_ids;
		int[] edges = new int[number_of_edges * 2];
		int number_of_nodes_in_backwards_star = 0;
		int comma;
		int new_comma;
		int temp_id;
		String line;
		already_encountered_ids = new int[number_of_nodes];
		// Instantiér listen med -1
		for (int k = 0; k <already_encountered_ids.length; k++) {
			already_encountered_ids[k] = -1;
		}

		line = scanner.nextLine(); // Der er en tom linje, der skal springes over efter
								   // tidligere at have brugt scanner.NextInt()
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			
			comma = line.indexOf(',');

			// Det første tal der skal skrives i listen "edges"
			number_of_nodes_in_backwards_star = Integer.parseInt(line.substring(0, comma));
			temp_edges.add(number_of_nodes_in_backwards_star + 2);
			
			
			for (int i = 0; i < number_of_nodes_in_backwards_star + 1; i++) {
				new_comma = line.indexOf(',', comma + 1);
				temp_id = Integer.parseInt(line.substring(comma + 1, new_comma));
				// Det ændrede ID er tilføjet til ArrayList
				temp_edges.add(checkIfAlreadyEncountered(temp_id, already_encountered_ids));
				comma = new_comma;
			}
			// Vægten er ikke ændret men blot tilføjet til ArrayList
			temp_edges.add(Integer.parseInt(line.substring(line.lastIndexOf(',') + 1)));
							
		}
		edges = new int[temp_edges.size()];
		for (int i = 0; i < temp_edges.size(); i++) {
			edges[i] = temp_edges.get(i);
		}

		return edges;
	}
		

	
	/*
	 * Bruges til at tælle antallet af kommaer i en streng
	 * Dette hjælper programmet med at afgøre længden af "edges"
	 */
	public static int countOfOccurrences(String s, char c) {
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}
	
	/*
	 * Tager den formatterede streng fra filen og sætter dataen ind i "edges"
	 */
	public static int[] parseEdges(String s) {
		int comma; // Indeks af næste komma
		String temp;
		int lengthOfEdges = countOfOccurrences(s, ',');
		int[] edges = new int[lengthOfEdges];
		int j = 0; // Denne bruges til at holde styr på, hvor der skal skrives i edges
		int number_of_indexes_until_next_length = 0;
		int id;
		int[] already_encountered_ids = new int[edges.length - 2];
		// initialize array with -1.
		for (int k = 0; k <already_encountered_ids.length; k++) {
			already_encountered_ids[k] = -1;
		}
		

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != ',') {
				comma = s.indexOf(",", i); // Finder indeks på næste komma
				temp = s.substring(i, comma);
				try {
					if (number_of_indexes_until_next_length > 1) { // Dette er et ID
						id = checkIfAlreadyEncountered(Integer.parseInt(temp), already_encountered_ids);
						edges[j] = id;
						j++;
					} else if (number_of_indexes_until_next_length == 1) { // vægt
						edges[j] = Integer.parseInt(temp);
						j++;
					} else { // Dette er en ny længde, derfor skal strengen ikke modificeres
						edges[j] = Integer.parseInt(temp);
						number_of_indexes_until_next_length = Integer.parseInt(temp) + 1;
						j++;

					}
				} catch (NumberFormatException e) {
					System.out.println("Something went wrong");
				}
				i++;
			}
			number_of_indexes_until_next_length--;
			
		}
		return edges;
	}

	/*
	 * Holder styr på hviler IDer, der er fundet i input filen og konverterer dem
	 * til IDerne brugt i resten af programmet.
	 */
	private static int checkIfAlreadyEncountered(int temp_id, int[] already_encountered) {
		int i = 0;
		while ( already_encountered[i] != -1 && i < already_encountered.length) {
			if (already_encountered[i] != temp_id) {
				i++;
			} else {
				return 2*i;
			}
		}
		// Dette ID er ikke tidligere læst
		if (already_encountered[i] == -1) {
			already_encountered[i] = temp_id;
		}
		
		return 2*i;
	}
	
	private static int[] computeLengths(int[] edges) {
		int[] lengths = new int[edges.length -1];
		int number_of_entries_until_next_length = 0;		
		for(int i = 0; i < edges.length; i++) {
			if (number_of_entries_until_next_length > 2) {
				lengths[edges[i]]++;
			}
			else if (number_of_entries_until_next_length == 2) {
				lengths[edges[i] + 1]++;
			}
			else if (number_of_entries_until_next_length == 0) {
				number_of_entries_until_next_length = edges[i] + 1;
			}
			number_of_entries_until_next_length--;
		}
		return lengths;
	}
	
	private static int[] computeVerticesSums(int[] lengths, int number_of_nodes_hyper) {
		// Dette er en liste med længderne af de individuelle knuders FS og BS
		int[] vertices = new int[number_of_nodes_hyper * 2];
		// Disse er brugt til at beregne de kumulatative summer
		int sumFS = 0;
		int sumBS = 0;
		for (int i = 0; i < vertices.length; i = i + 2) {

			sumFS += lengths[i];
			vertices[i] = sumFS;

			sumBS += lengths[i+1];
			vertices[i+1] = sumBS;
		}
		return vertices;
	}
	
	


	// Parsing brugt til almindelige grafer
	private static int[] newParsing(String filename) {
		int[] m_edges_vertices = null;
		int[] edges;
		int[] vertices;
		int[] temp_lengths;
		String line;
		int comma;
		int new_comma;
		int number_of_nodes;
		int number_of_edges;
		int from_id = 0;
		int to_id = 0;
		int weight = 0;
		int sum = 0;
		int counter = 0;
		try {
			File testFile = new File(filename);
			Scanner scanner = new Scanner(testFile);
			number_of_nodes = scanner.nextInt();
			vertices = new int[number_of_nodes];
			temp_lengths = new int[number_of_nodes];
			number_of_edges = scanner.nextInt();
			edges = new int[number_of_edges * 2];

			scanner.nextLine(); // Denne linje er tom, hvilket er et resultat af at have benyttet NextInt()
			
			
			while(scanner.hasNextLine()) {
				line = scanner.nextLine();
				comma = line.indexOf(',');
				from_id = Integer.parseInt(line.substring(0, comma));
				new_comma = line.indexOf(',', comma + 1);
				to_id = Integer.parseInt(line.substring(comma + 1, new_comma));

				comma = line.indexOf(',', new_comma);
				weight = Integer.parseInt(line.substring(new_comma + 1));	
				
				edges[counter] = to_id;
				edges[counter+1] = weight;
				counter = counter+2;
				// Hvor mange kanter der er udadgående fra from_id
				temp_lengths[from_id] += 2;
			}
			scanner.close();
			
			vertices[0] = 0;
			for (int i = 1; i < temp_lengths.length; i++) {
				sum = sum + temp_lengths[i-1]; 
				vertices[i] = sum; 
			}

			m_edges_vertices = new int[1 + edges.length + vertices.length];
			m_edges_vertices[0] = number_of_nodes;
			for (int i = 0; i < edges.length; i++) {
				m_edges_vertices[i+1] = edges[i];
			}
			for (int i = 0; i < vertices.length; i++) {
				m_edges_vertices[i + 1 + edges.length] = vertices[i];
			}
		
		}catch(FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();			
		}

		return m_edges_vertices;
		
	}
	
	
	
	/*
	 * The method creates a new file if it does not already exist.
	 */
	public static void createFile(String filename) {
		// Create file
		try {
			File file = new File(filename);
			if (file.createNewFile()) {
				System.out.println("File created: " + file);
			} else {
				System.out.println("File already exists");
			} 
		} catch (IOException e) {
			System.out.println("An error occured");
			e.printStackTrace();
		}
	}
	
	/*
	 * Input: Stien, til hvor filen ligger, filnavnet, samt dataen, der skal skrives til filen
	 * Output: Skriver den givne data i den angivne fil
	 */
	public static void writeToFile(String path, String filename, String data) {
		String complete_path = path + filename;
		try {
			FileWriter writer = new FileWriter(complete_path);
			writer.append(data);

			System.out.println("Succesfully wrote to file");
			writer.close();
		} catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		}
	}
	
	
}
