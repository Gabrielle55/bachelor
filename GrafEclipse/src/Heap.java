import java.math.*;
public class Heap {
	private static int[] heap;
	private static int heap_size;
	private int max_size;
	
	public Heap(int max_size) {
		this.max_size = max_size;
		this.heap_size = 0;
		heap = new int[this.max_size];
	}
	
	public static void buildMaxHeap(int[] heap) {
		setHeap_size(heap.length);
		for (int i = Math.floorDiv(heap_size, 2); i >= 0; i--) {
			heapify(heap, heap_size, i);
		}
	}
	
	private static void heapify(int arr[], int n, int i)
    {
        int largest = i;  // Initialize largest as root
        int l = 2*i + 1;  // left = 2*i + 1
        int r = 2*i + 2;  // right = 2*i + 2
  
        // If left child is larger than root
        if (l < n && arr[l] > arr[largest])
            largest = l;
  
        // If right child is larger than largest so far
        if (r < n && arr[r] > arr[largest])
            largest = r;
  
        // If largest is not root
        if (largest != i)
        {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;
  
            // Recursively heapify the affected sub-tree
            heapify(arr, n, largest);
        }
    }
	
	public static int extractMax() {
		if (heap.length < 1) {
			System.out.println("Error: heap underflow");
		}
		int max = heap[0];
		heap[0] = heap[heap_size - 1];
		setHeap_size( heap_size - 1);
		heapify(heap, heap_size, 0);
		return max;
	}
	
	private static void increaseKey(int[] heap, int i, int key ) {
		if (key < heap[i]) {
			System.out.println("Error: New key is smaller than current key");
		}
		heap[i] = key;
		// while (i > 1 && heap[parent(i)] < heap[i]) {
		while (i > 0 && heap[parent(i)] < heap[i]) {
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
		new_heap[heap_size - 1] = Integer.MIN_VALUE;
		increaseKey(new_heap, heap_size - 1, key);
	}
	
	private static void swap(int i, int j) {
		int temp = heap[i];
		heap[i] = heap[j];
		heap[j] = temp;	
	}

	public int getSize(int[] heap) {
		int size = heap.length;
		this.heap_size = size;
		return size;
	}
	
	private int root() {
		return heap[0];
	}
	
	private static int parent(int i) {
		return Math.floorDiv(i - 1,2);
	}
	
	private static int left(int i) {
		return (2*i)+1;
	}
	
	private static int right(int i) {
		return (2*i)+2;
	}
	
	public static int[] getHeap() {
		return heap;
	}

	public static void setHeap(int[] heap) {
		Heap.heap = heap;
	}

	public static int getHeap_size() {
		return heap_size;
	}

	public static void setHeap_size(int heap_size) {
		Heap.heap_size = heap_size;
	}

}
