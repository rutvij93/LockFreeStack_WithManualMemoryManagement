
import java.util.concurrent.ThreadLocalRandom;

import stack.*;

public class StackTest {
	public static int number = 0;
	public static boolean running = true;
	public static String implementation;
	public static int THREADS = 128;
	public static int delay = 1;
	public static int work = 1000;
	public static int timeout = 0;
	public static int size = 0;
	
	static EliminationBackoffStack<Integer> instance = new EliminationBackoffStack<Integer>();
	static LockFreeStackRecycle<Integer> instance1 = new LockFreeStackRecycle<Integer>();

	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, InterruptedException {
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
		System.out.println("Concurrent Stack");
		long start = System.currentTimeMillis();
	    Thread[] myThreads = new Thread[THREADS];
	    
	    //LockFreeRecycle
	    
	    number = 0;
	    running = true;
	    implementation = "LockFreeRecycle";
	    for (int i = 0; i < THREADS; i++) {
	    	myThreads[i] = new PushPopThread();
	    }
	    start = System.currentTimeMillis();
	    for (int i = 0; i < THREADS; i ++) {
	    	myThreads[i].start();
	    }
	    while (System.currentTimeMillis() <= (start + 1000));		    
	    running = false;
	    for (int i = 0; i < THREADS; i ++) {
	    	myThreads[i].join();
	    }
	    System.out.println("Operations for LockFreeRecycle	  	" + number);

	}
	
	// Push and Pop
	
	static class PushPopThread extends Thread {
	    int value;
		@SuppressWarnings("static-access")
		public void run() {
    		while (running){
    			int a = ThreadLocalRandom.current().nextInt(0,2);
		    	if (a == 0){
		    		instance1.push(1);
		    		try {
						Thread.currentThread().sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    	else{
		        	try {
						instance1.pop();
					} catch (stack.EmptyException e) {
					}
		        	try {
						Thread.currentThread().sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		        synchronized(this)
		        {
					number = number + 2;
				}
			}
	    }
	}
}
