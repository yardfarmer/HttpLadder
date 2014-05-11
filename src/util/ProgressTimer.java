package util;

import http.SendThread;

import java.util.ArrayList;

public class ProgressTimer extends java.util.TimerTask  {
	
	private ArrayList<SendThread> threadArray;
	public ProgressTimer(ArrayList<SendThread> threadArray ){
		this.threadArray = threadArray;
	}
	
	public void run(){
		int currentCount = 0;
		for(SendThread thread : threadArray){
			currentCount += thread.getCurrentCount();
		}
		
		System.out.println(String.format("Finished requests: %1$-10d", currentCount));
	}
}
