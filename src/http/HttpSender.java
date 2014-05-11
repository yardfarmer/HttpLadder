package http;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.String;

import util.*;
public class HttpSender {
	
	/**
	 * @param args0: total  args1 thread count
	 */
	
	public static void main(String[] args) throws Exception{
		//Parse command line, read url from file
		if(!util.Global.parseCmdLine(args))
			return;
		
		//Execute http request in multiple threads.
		ArrayList<SendThread> threadArray = new ArrayList<SendThread>();
		SendThread t = null;
		
		//set timer for displaying inprogress
		ProgressTimer progressTimer = null;
		Timer timer = null;
		if(Global.displayIdleInSeconds > 0){
			timer = new Timer();
			progressTimer = new ProgressTimer(threadArray);
			timer.schedule(progressTimer , 0, Global.displayIdleInSeconds*1000);
		}
		
		
		//Start thread
		Date threadStart = new Date();
		for(int tc = 0; tc < (Global.threadCount - 1); tc ++){
			t = new SendThread(tc, Global.totalEachThread, Global.urlArray,Global.random, Global.headerArray, Global.printHeader);
			t.start();
			threadArray.add(t);
		}
		
		//for the last thread when 100%3=1
		long LastThreadRequest = Global.totalEachThread + Global.totalRequest % Global.threadCount;
		t = new SendThread(Global.threadCount-1, LastThreadRequest, Global.urlArray,Global.random, Global.headerArray, Global.printHeader);
		t.start();
		threadArray.add(t);
		
		//Wait for all thread died
		for(int j=0; j<threadArray.size(); j++){
			threadArray.get(j).join();
		}
		
		//End timer
		if(timer != null)
			timer.cancel();
		
		//Print the finall progress info
		if(progressTimer != null)
			progressTimer.run();
		
		Date threadEnd = new Date();
		//get result after executing
		Result result = Global.getResult(threadArray, threadStart, threadEnd);
		
		//print result
		result.print();
		
	}
}
