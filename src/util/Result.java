package util;

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;

public class Result {

	public int threadCount = 0;
	public double avgRT = 0;
	public double qps = 0;
	public double costTime = 0;
	public double avgObjSizeInKB = 0;
	public Hashtable<Integer, Integer> totalResCode = null;
	public Hashtable<String, Integer> totalExcep = null;
	
	//ÿ���̵߳�RT
	public java.util.ArrayList<Double> rtArray = new java.util.ArrayList<Double>();
		
	//ÿ���̵߳�QPS
	public java.util.ArrayList<Double> qpsArray = new java.util.ArrayList<Double>();
	
	public void print(){

		java.text.DecimalFormat rtDF=new java.text.DecimalFormat("#.###");
		java.text.DecimalFormat qpsDF=new java.text.DecimalFormat("#");
		SimpleDateFormat dateFormat = new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss");
		java.util.Date now = new java.util.Date();
		
		System.out.println("\n======<Summary Result>======");
		
		
		System.out.println(String.format("Time: %1$1s", dateFormat.format(now)));
		System.out.println(String.format("%1$1s %2$1d", "Concurrency Level:",threadCount));
		
		System.out.println(String.format("Avg    RT: %1$1sms\nTotal QPS: %2$1s\nCost Time: %3$1ss\nAvg  Size: %4$1sKB\n",
				rtDF.format(avgRT),qpsDF.format(qps),rtDF.format(costTime),rtDF.format(avgObjSizeInKB)));
		//print response code
		Iterator<Integer> it = totalResCode.keySet().iterator();
		System.out.println("Response Code:");
		int tmpCode;
		while(it.hasNext()) {
			tmpCode = it.next();
			System.out.println(String.format("[code]%1$-6d %2$-6d",tmpCode, totalResCode.get(tmpCode)));
		}
		
		//print exception
		System.out.println("\nException:");
		Iterator<String> is = totalExcep.keySet().iterator();
		String tmpExcep;
		while(is.hasNext()){
			tmpExcep = is.next();
			System.out.println(String.format("[exp]%1$-16s: %2$-5d", tmpExcep, totalExcep.get(tmpExcep)));
		}
		
//		System.out.println("\nQPS for each thread:");
//		for(double q : this.qpsArray){
//			System.out.println(rtDF.format(q));
//		}
//		
//		System.out.println("\nRT for each thread:");
//		for(double r : this.rtArray){
//			System.out.println(r);
//		}
		
		System.out.println("======</Summary Result>======");
	}
}
