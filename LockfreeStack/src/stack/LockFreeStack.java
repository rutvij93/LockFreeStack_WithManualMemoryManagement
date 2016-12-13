package stack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeStack<T> {
	AtomicReference<Node> top = new AtomicReference<Node>(null);
	static final int MIN_DELAY = 10;
	static final int MAX_DELAY = 100;
	Backoff backoff = new Backoff(MIN_DELAY, MAX_DELAY);
	public boolean tryPush(Node node){
		Node oldTop = top.get();
		node.next = oldTop;
		return(top.compareAndSet(oldTop, node));
	}

	public void push(T value) {
		Node node = new Node(value);
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

	public class Node {
		public T value;
		public Node next;
		public Node(T value) {
			this.value = value;
			next = null;
		}
	}
	
	protected Node tryPop() throws EmptyException {
		Node oldTop = top.get();
		if (oldTop == null) {
			throw new EmptyException();
		}
		Node newTop = oldTop.next;
		if (top.compareAndSet(oldTop, newTop)) {
			return oldTop;
		} else {
			return null;
		}
	}
	
	public T pop() throws EmptyException {
		while (true) {
			Node returnNode = tryPop();
			if (returnNode != null) {
				return returnNode.value;
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
}

