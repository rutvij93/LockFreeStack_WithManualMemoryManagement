
import java.util.concurrent.ThreadLocalRandom;

import stack.*;

public class EliminationStack {
	public static int THREADS = 128;
	public static int delay = 1;
	public static int work = 1000;
	public static int timeout = 0;
	public static int size = 0;
	
	static EliminationBackoffStack<Integer> instance = new EliminationBackoffStack<Integer>();
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, InterruptedException {
		long elapsed = 0; 
		if (args.length == 5){
			THREADS = Integer.parseInt(args[0]);
			delay = Integer.parseInt(args[1]);
			work = Integer.parseInt(args[2]);
			EliminationArray.duration = Integer.parseInt(args[3]);
			EliminationBackoffStack.capacity = Integer.parseInt(args[4]);
		}
		else{
			System.out.println("Incorrect Argument Count. Format --> java EliminationStack p d n t e");
			return;
		}
//		if (EliminationBackoffStack.capacity == 0){
//			EliminationBackoffStack.capacity = 1;
//		}
		System.out.println("Concurrent Stack");
	    Thread[] myThreads = new Thread[THREADS];
	    for (int i = 0; i < THREADS; i++) {
	    	myThreads[i] = new PushPopThread();
	    }
	    long start = System.currentTimeMillis();
	    for (int i = 0; i < THREADS; i ++) {
	    	myThreads[i].start();
	    }
	    for (int i = 0; i < THREADS; i ++) {
	    	myThreads[i].join();
	    }
	    elapsed = System.currentTimeMillis() - start;
	    System.out.println("Timetaken by EliminationStack	" + elapsed + "ms");
	    System.out.println("Push: " + EliminationBackoffStack.push + ", Successful Pop: " + EliminationBackoffStack.pop);
	    int remaining = 0;
	    while(true){
	    	try {
				instance.pop();
				remaining++; 
			} catch (EmptyException e) {
				System.out.println("Remaining Elements in stack: " + remaining);
				break;
			}
	    }
	}
	
	// Push and Pop
	
	static class PushPopThread extends Thread {
		private long elapsed = 0; 
	    @SuppressWarnings({"static-access" })
		public void run() {
			for(int i = 0; i < work ; i++){
				int a = ThreadLocalRandom.current().nextInt(0,2);						//Push pop with 50% probability
				int random = ThreadLocalRandom.current().nextInt(0,delay+1);			//Add random delay after each operation 
		    	if (a == 0){
		    		instance.push(1);
		    		try {
						Thread.currentThread().sleep(random);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    	else{
		        	try {
						instance.pop();
					} catch (stack.EmptyException e) {
					}
		        	try {
						Thread.currentThread().sleep(random);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    }
    	}
	    public long getElapsed() {
			return elapsed;
		}
	}
}
