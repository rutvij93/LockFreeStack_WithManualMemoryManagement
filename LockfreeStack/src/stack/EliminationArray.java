package stack;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EliminationArray<T> {
	public static int duration = 100;
	LockFreeExchanger<T>[] exchanger;
	Random random;
	@SuppressWarnings("unchecked")
	public EliminationArray(int capacity) {
		exchanger = (LockFreeExchanger<T>[]) new LockFreeExchanger[capacity+100];
		for (int i = 0; i < capacity+100; i++) {
			exchanger[i] = new LockFreeExchanger<T>();
		}
		random = new Random();
	}
	public T visit(T value, int range) throws TimeoutException {
		int slot;
		if (range <= 1){
			slot = 0;
		}
		else{
			slot = ThreadLocalRandom.current().nextInt(0,range);
		}
		return (exchanger[slot].exchange(value, duration,TimeUnit.MICROSECONDS));
	}
}