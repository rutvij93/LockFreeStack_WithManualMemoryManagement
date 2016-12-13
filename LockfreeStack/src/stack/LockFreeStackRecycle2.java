package stack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicStampedReference;

public class LockFreeStackRecycle2<T> {
	AtomicStampedReference<Node> top = new AtomicStampedReference<Node>(null, 0);
	static final int MIN_DELAY = 10;
	public static int MAX_DELAY = 100;
	public static int count = 0;
	Backoff backoff = new Backoff(MIN_DELAY, MAX_DELAY);
	ThreadLocal<Node> freeList = new ThreadLocal<Node>() {
	    protected Node initialValue() { return null; };
	};
	
	public Node allocate(T value) {
	    int[] stamp = new int[1];
	    Node node = freeList.get();
	    if (node == null) { 									// nothing to recycle
	    	node = new Node(value);
	    } else {            									// recycle existing node. Use with 50% probability.
	    	int a = ThreadLocalRandom.current().nextInt(0,2);
	    	if (a == 0){
	    		freeList.set(node.next.get(stamp));
		    	EliminationBackoffStack.freesize.set(EliminationBackoffStack.freesize.get() - 1);
		    	count++;
	    	}
	    	else{
	    		node = new Node(value);
	    	}
	    }
	    // initialize
	    node.value = value;
	    return node;
	}

	public void free(Node node) {
	    Node free = freeList.get();
	    node.next = new AtomicStampedReference<Node>(free, 0);
	    freeList.set(node);
	}
	public boolean tryPush(Node node){
		int[] oldStamp = new int[1];
		Node oldTop = top.get(oldStamp);
		node.next.set(oldTop, oldStamp[0]);
		return(top.compareAndSet(oldTop, node, oldStamp[0], oldStamp[0] + 1));
	}

	public void push(T value) {
		Node node = allocate(value);
		while (true) {
			if (tryPush(node)) {
				return;
			} else {
				try {
					backoff.backoff();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	protected Node tryPop() throws EmptyException {
		int[] oldStamp = new int[1];
		int[] newStamp = new int[1];
		Node oldTop = top.get(oldStamp);
		if (oldTop == null) {
			throw new EmptyException();
		}
		Node newTop = oldTop.next.get(newStamp);
		if (top.compareAndSet(oldTop, newTop, oldStamp[0], newStamp[0])) {
			return oldTop;
		} else {
			return null;
		}
	}
	
	public T pop() throws EmptyException {
		while (true) {
			Node returnNode = tryPop();
			if (returnNode != null) {
				T value = returnNode.value;
				free(returnNode);
				return value;
			} else {
				try {
					backoff.backoff();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public class Backoff {
		final int minDelay, maxDelay;
		int limit;
		public Backoff(int min, int max) {
			this.minDelay = min;
			this.maxDelay = max;
			this.limit = minDelay;
		}
		public void backoff() throws InterruptedException {
			int delay = ThreadLocalRandom.current().nextInt(minDelay,maxDelay);
			limit = Math.min(maxDelay, 2 * limit);
			Thread.sleep(delay);
		}
	}
	
	public class Node {
		public T value;
		public AtomicStampedReference<Node> next;
		public Node(T value) {
			this.value = value;
			this.next  = new AtomicStampedReference<Node>(null, 0);
		}
	}
}

