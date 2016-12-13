package stack;

import java.util.concurrent.TimeoutException;

public class EliminationBackoffStack<T> extends LockFreeStackRecycle<T> {
	
	public static int capacity = 0;
	public static int pop = 0;
	public static int push = 0;
	public static int count = 0;
	public static ThreadLocal<Integer> freesize = new ThreadLocal<Integer>() {
		protected synchronized Integer initialValue() {
			return new Integer(0);
		}
	};
	EliminationArray<T> eliminationArray = new EliminationArray<T>(capacity);
	
	public void push(T value) {
		Node node = allocate(value);						// recycle existing node. Use with 50% probability. Open this method.
		while (true) {
			if (tryPush(node)) {
				return;
			} else if(capacity >=  1)try {
				T otherValue = eliminationArray.visit(value, capacity);
				if (otherValue == null) {
					return; 
				}
			} catch (TimeoutException ex) {
			}
			else{
			}
		}
	}
	
	public T pop() throws EmptyException {
		while (true) {
			Node returnNode = tryPop();
			if (returnNode != null) {
				T value = returnNode.value;
				if (freesize.get() <= 10){
					free(returnNode);
					freesize.set(freesize.get()+1);
				}
				return value;
			} else try {
				T otherValue = eliminationArray.visit(null, capacity);
				if (otherValue != null) {
					if (freesize.get() <= 10){
						free(returnNode);
						freesize.set(freesize.get()+1);
					}
					return otherValue;
				}
			} catch (TimeoutException ex) {
				try {
					backoff.backoff();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
