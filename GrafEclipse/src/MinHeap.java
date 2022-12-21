import java.math.*;

public class MinHeap {
	private static int[] heap; 
	private static int heap_size;
	private int max_size;
	
	public MinHeap(int max_size) {
		this.max_size = max_size;
		this.heap_size = 0;
		heap = new int[this.max_size];
	}
	
	private static void buildMinHeap(int[] heap) {
		setHeap_size(heap.length);
		for (int i = Math.floorDiv(heap_size, 2); i >= 0; i--) {
			heapify(heap, heap_size, i);
		}
	}
	
	
	// Denne del skal omskrives, således at der i listen med keys og knude id's bliver opdateret.
	private static void heapify(int arr[], int n, int i)
    {
        int smallest = i;  // Initialize smallest as root
        int l = 2*i + 1;  // left = 2*i + 1
        int r = 2*i + 2;  // right = 2*i + 2
  
        // If left child is larger than root
        if (l < n && arr[l] < arr[smallest])
        	smallest = l;
  
        // If right child is larger than largest so far
        if (r < n && arr[r] < arr[smallest])
        	smallest = r;
  
        // If largest is not root
        if (smallest != i)
        {
        	// Her skal der laves en ændring, da vi også skal opdatere vores heap
            int swap = arr[i];
            arr[i] = arr[smallest];
            arr[smallest] = swap;
  
            // Recursively heapify the affected sub-tree
            heapify(arr, n, smallest);
        }
    }
	
	public static int extractMin() {
		if (heap.length < 1) {
			System.out.println("Error: heap underflow");
		}
		int min = heap[0];
		heap[0] = heap[heap_size - 1];
		setHeap_size( heap_size - 1);
		heapify(heap, heap_size, 0);
		return min;
	}
	
	private static void increaseKey(int[] heap, int i, int key ) {
		if (key > heap[i]) {
			System.out.println("Error: New key is larger than current key");
		}
		heap[i] = key;
		while (i > 0 && heap[parent(i)] > heap[i]) {
			swap(i, parent(i));
			i = parent(i);
		}
	}
	
	public static void insertKey(int[] heap, int key) {
		setHeap_size(heap_size + 1);
		int[] new_heap = new int[heap_size];
		for (int i = 0; i < heap.length; i++) {
			new_heap[i] = heap[i];
		}
		setHeap(new_heap);
		new_heap[heap_size - 1] = Integer.MAX_VALUE;
		increaseKey(new_heap, heap_size - 1, key);
	}
	
	private static void swap(int i, int j) {
		int temp = heap[i];
		heap[i] = heap[j];
		heap[j] = temp;	
	}
	
	
	private int root() {
		return heap[0];
	}
	
	private static int parent(int i) {
		int par = i / 2;
		if (par % 2 == 0) {
			return par - 2;
		} else {
			return par - 1;
		}

	}

	
	private int left(int i) {
		return (2*i)+1;
	}
	
	private int right(int i) {
		return (2*i)+2;
	}
	
	public static int[] getHeap() {
		return heap;
	}

	public static void setHeap(int[] heap) {
		MinHeap.heap = heap;
	}

	public static int getHeap_size() {
		return heap_size;
	}

	public static void setHeap_size(int heap_size) {
		MinHeap.heap_size = heap_size;
	}

}
