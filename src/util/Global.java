package util;

import http.SendThread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
/*
 * Define the global variables
 */
public class Global {

	public static int threadCount = 1;
	public static long totalEachThread = 1;
	public static long LastThreadRequest = 0;
	public static long totalRequest = 1;
	public static boolean printHeader = false;
	public static boolean verifyMd5 = false;
	public static String urlFile = null;
	public static String cUrl = null;
	public static int random = 0;
	public static int displayIdleInSeconds = 0;
	public static boolean changVip = false;
	public static String getVipLocalInfo = "";
	public static String protocolVersion = "";
	public static double avgObjSizeInKB = 0;
	public static ArrayList<String> headerArray = new ArrayList<String>();
	public static ArrayList<String> urlArray = new ArrayList<String>();
	public static Hashtable<Integer, Integer> totalResCode = new Hashtable<Integer, Integer>();
	public static Hashtable<String, Integer> totalExcep = new Hashtable<String, Integer>();
	
	public static String lineSeperator = System.getProperty("line.separator");
	/*Result related variables*/
	
	public static long totalResponse = 0;
	public static double averRT = 0;
	public static double totalQPS = 0;
	public static double totalCostTime = 0;
	public static Hashtable<Integer, Integer> tmpResCode = new Hashtable<Integer, Integer>();
	public static Hashtable<String, Integer> tmpThreadExcep = new Hashtable<String, Integer>(); 
	public static Integer tmpCode = 0;
	public static Integer tmpValue = 0;
	public static String tmpExcep = null;

	private static void printHelp(){
		System.out.println("Usage: HttpSender [options] http://hostname[:port]/path\n" +
		  "Options:\n" +
		  "    --help    Help message\n" +
		  "    -c <number>  Concurrent threads\n" +
		  "    -n <number>  Number of total requests\n" +
		  "    -h <\"Key:Value\">  Header of http request\n" +
		  "    -f <file>  File to read url lists\n" +
		  "    -r <1 or 0>  1 random, 0 iterative\n" +
		  "    -v <1.0 or 1.1>  set protocol version. By default is 1.1\n" +
		  "    -d <seconds>  set display interval: By default is 10 seconds. 0 means never display\n" +
		  "    -p    Print response header\n" +
		  "    -md5  verify the body's md5 is correct\n");
	}
	
	public static boolean parseCmdLine(String[] args) throws Exception{
		if(args.length == 1){
			if(args[0].contains("http") == true)
			cUrl = args[0];
			else{
				printHelp();
				return false;
			}		
		}	
		else if (args.length == 2 || args.length == 0) {
			printHelp();
				return false;
		}
		else {
			int n = 0;
			while ( n < args.length ){
				if (args[n].compareToIgnoreCase("-c")==0 && args[n+1] != null){
					threadCount = Integer.valueOf(args[n+1]);
					n = n + 2;
				}
				else if (args[n].compareToIgnoreCase("-f")==0 && args[n+1] != null){
					urlFile = args[n+1];
					n = n + 2;
				}
				else if (args[n].compareToIgnoreCase("-n")==0 && args[n+1] != null){
					totalRequest = Long.valueOf(args[n+1]);
					n = n + 2;
				}
				else if (args[n].compareToIgnoreCase("-h")==0 && args[n+1] != null){
					headerArray.add(args[n+1]);
					n = n + 2;
				}
				else if (args[n].compareToIgnoreCase("-r")==0 && args[n+1] != null){
					random = Integer.valueOf(args[n+1]);
					n = n + 2;
				}
				else if (args[n].compareToIgnoreCase("-v")==0 && args[n+1] != null){
					protocolVersion = args[n+1];
					n = n + 2;
				}
				else if (args[n].compareToIgnoreCase("-d")==0 && args[n+1] != null){
					displayIdleInSeconds = Integer.valueOf(args[n+1]);
					n = n + 2;
				}
				else if (args[n].contains("http") == true){
					cUrl = args[n];
					n = n + 1;
				}
				else if (args[n].compareToIgnoreCase("-p")==0){
					printHeader = true;
					n = n + 1;		
				}
				else if (args[n].compareToIgnoreCase("-md5")==0){
					verifyMd5 = true;
					n = n + 1;		
				}
				else if (args[n].compareToIgnoreCase("-i")==0){
					changVip = true;
					n = n + 1;
				}
				else {
					printHelp();
					return false;
				}
			}
		}
		
		/*���������г�ͻ*/
		if (cUrl != null && urlFile != null){
			System.out.println("urlFile & url conflict");
			printHelp();
			return false;
		}
		
		
		if(cUrl != null && urlFile == null){
			urlArray = splitRegrexString(cUrl);
		}	
		totalEachThread = totalRequest / threadCount;
		
		// * ����ṩurlFile,����ļ����ȡURL������<url>����
		FileReader fr = null;
		BufferedReader br = null;
		try{
		if(urlFile != null && (!urlFile.isEmpty())){
			System.out.println("Start reading file");
			fr = new FileReader(urlFile); 
			br = new BufferedReader(fr); 
			String urlLine = br.readLine(); 
			while (urlLine != null) { 
				urlArray.add(urlLine);
				urlLine = br.readLine(); 
			} 
			System.out.println("Finish reading file");
		}
		}finally{
			if(br != null)
				{br.close();}
			if(fr != null)
				{fr.close();}
			
		}
		
		return true;
	}
	
	

	
	   /* 	   
	    *����URL�е�������ʽ
	    *[a-b]/[c-d]
	    *[a-b]/c
	    *a/[b-c]
	    *a/b  
	    */
	private static ArrayList<String> splitRegrexString(String regexString) {
	   ArrayList<String> elements = new ArrayList<String>();
	   
	   int need = regexString.indexOf("]");
	   int left = regexString.indexOf("/[");
	   int ab = regexString.indexOf("]/");
	   
	   int fIndex1 = regexString.indexOf("[");
	   int fIndex2 = regexString.indexOf("]",fIndex1);
	   int fIndex3 = regexString.indexOf('-',fIndex1);
	   int oIndex1 = regexString.indexOf('[', fIndex1 + 1);
	   int oIndex2 = regexString.indexOf(']', fIndex2 + 1);
	   int oIndex3 = regexString.indexOf('-', fIndex3 + 1);
	   
	   if( need == -1){
		   elements.add(regexString);
	   }
	   else if(left == -1){
		   elements.add(regexString);
	   }
	   else if(fIndex1 != -1 && fIndex2 != -1 && fIndex3 != -1 && oIndex1 != -1 && oIndex2 != -1 && oIndex3 != -1) {
		      String firstPart = regexString.substring(0, fIndex1);
		      String lastPart = regexString.substring(oIndex2 + 1);
		      int fFrom = Integer.valueOf(regexString.substring(fIndex1 + 1, fIndex3));
		      int fTo   = Integer.valueOf(regexString.substring(fIndex3 + 1, fIndex2));
		      int oFrom = Integer.valueOf(regexString.substring(oIndex1 + 1, oIndex3));
		      int oTo   = Integer.valueOf(regexString.substring(oIndex3 + 1, oIndex2));
		      for (int f = fFrom; f <= fTo; f ++){
		    	  for (int o = oFrom; o <= oTo; o ++){
		    		  elements.add(firstPart + f + '/' + o + lastPart);
		    	  }
		      }
	   }
	   else if(ab != -1 && fIndex1 != -1 && fIndex2 != -1 && fIndex3 != -1 && oIndex1 == -1) {
		   	  String firstPart = regexString.substring(0, fIndex1);
		      String lastPart = regexString.substring(fIndex2 + 1);
		      int fFrom = Integer.valueOf(regexString.substring(fIndex1 + 1, fIndex3));
		      int fTo   = Integer.valueOf(regexString.substring(fIndex3 + 1, fIndex2));
		      for (int f = fFrom; f <= fTo; f ++){
		    	  elements.add(firstPart + f + lastPart);
		      }
	   }
	   else if(ab == -1 && fIndex1 != -1 && fIndex2 != -1 && fIndex3 != -1){
		      String firstPart = regexString.substring(0, fIndex1);
		      String lastPart = regexString.substring(fIndex2 + 1);
		      int oFrom = Integer.valueOf(regexString.substring(fIndex1 + 1, fIndex3));
		      int oTo   = Integer.valueOf(regexString.substring(fIndex3 + 1, fIndex2));
	    	  for (int o = oFrom; o <= oTo; o ++){
	    		  elements.add(firstPart + o + lastPart);
	    	  }
	   }
	   else {
		   elements.add(regexString);
	   }
	   return elements;
	} 
	
	public static Result getResult(ArrayList<SendThread> threadArray, Date start, Date end){
		Result result = new Result();
		totalCostTime = (double) (end.getTime() - start.getTime())/1000;
		//thread process
		for(SendThread thread : threadArray) try {
			result.rtArray.add(thread.getRT());
			result.qpsArray.add(thread.getQPS());
			averRT += thread.getRT();
			totalQPS += thread.getQPS();
			avgObjSizeInKB += thread.getSumObjSizeInKB()/thread.getTotalNum();
			totalResponse += thread.getResponse();
			tmpResCode = thread.getResCode();
			Iterator<Integer> it = tmpResCode.keySet().iterator();
			while(it.hasNext()) {
				tmpCode = it.next();
				if(totalResCode.containsKey(tmpCode)){
					tmpValue = totalResCode.get(tmpCode) + tmpResCode.get(tmpCode);
					totalResCode.put(tmpCode, tmpValue);
				}
				else {
					tmpValue = tmpResCode.get(tmpCode);
					totalResCode.put(tmpCode, tmpValue);
				}
			}
			tmpThreadExcep = thread.getThreadExcep();
			Iterator<String> is = tmpThreadExcep.keySet().iterator();
			while(is.hasNext()){
				tmpExcep = is.next();
				if(totalExcep.containsKey(tmpExcep)) {
					tmpValue = totalExcep.get(tmpExcep) + tmpThreadExcep.get(tmpExcep);
					totalExcep.put(tmpExcep,tmpValue);
				}
				else {
					tmpValue = tmpThreadExcep.get(tmpExcep);
					totalExcep.put(tmpExcep,tmpValue);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(threadArray.size() > 0){
			averRT = averRT/threadArray.size();
			avgObjSizeInKB = avgObjSizeInKB/threadArray.size();
		}
		
		
		result.avgRT = averRT;
		result.qps = totalQPS;
		result.costTime = totalCostTime;
		result.totalResCode = totalResCode;
		result.totalExcep = totalExcep;
		result.avgObjSizeInKB = avgObjSizeInKB;
		result.threadCount = threadArray.size();
		return result;
	}
}
