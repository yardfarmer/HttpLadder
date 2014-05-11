import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.String;

public class HttpSender {

	private static String lineSeperator = System.getProperty("line.separator");
	/**
	 * @param args0: total  args1 thread count
	 */
	public static void main(String[] args) throws Exception{
		
		String help = "Usage: HttpSender [options] http://hostname[:port]/path\n" +
					  "Options:\n" +
					  "    --help    Help message\n" +
					  "    -c    Concurrent threads\n" +
					  "    -n    Number of total requests\n" +
					  "    -h    Header of http request\n" +
					  "    -f    File to read url lists\n" +
					  "    -r    [1/0]:1 random, 0 iterative\n" +
					  "    -i    dynamic to get vip from dns\n" +
					  "    -p    Print response header\n";
		
		//some Parameters to read from arg:
		int threadCount = 1;
		long totalEachThread = 1;
		long LastThreadRequest = 0;
		long totalRequest = 1;
		boolean printHeader = false;
		String urlFile = null;
		String cUrl = null;
		int random = 0;
		boolean changVip = false;
		String getVipLocalInfo = "";
		
		ArrayList<String> headerArray = new ArrayList<String>();
		ArrayList<String> urlArray = new ArrayList<String>();
		Hashtable<Integer, Integer> totalResCode = new Hashtable<Integer, Integer>();
		Hashtable<String, Integer> totalExcep = new Hashtable<String, Integer>();
		
		// process args：
		if(args.length == 1){
			if(args[0].contains("http") == true)
			cUrl = args[0];
			else{
				System.out.println(help);
				return;
			}		
		}	
		else if (args.length == 2 || args.length == 0) {
				System.out.println(help);
				return;
		}
		else {
			int n = 0;
			while ( n < args.length ){
				if (args[n].contentEquals("-c") && args[n+1] != null){
					threadCount = Integer.valueOf(args[n+1]);
					n = n + 2;
				}
				else if (args[n].contentEquals("-f") && args[n+1] != null){
					urlFile = args[n+1];
					n = n + 2;
				}
				else if (args[n].contentEquals("-n") && args[n+1] != null){
					totalRequest = Long.valueOf(args[n+1]);
					n = n + 2;
				}
				else if (args[n].contentEquals("-h") && args[n+1] != null){
					headerArray.add(args[n+1]);
					n = n + 2;
				}
				else if (args[n].contentEquals("-r") && args[n+1] != null){
					random = Integer.valueOf(args[n+1]);
					n = n + 2;
				}
				else if (args[n].contains("http") == true){
					cUrl = args[n];
					n = n + 1;
				}
				else if (args[n].contentEquals("-p")){
					printHeader = true;
					n = n + 1;		
				}
				else if (args[n].contentEquals("-i")){
					changVip = true;
					n = n + 1;
				}
				else {
					System.out.println(help);
					return;
				}
			}
		}
		
		System.out.println("...Start...\n");
		
		/*处理命令行接收的cUrl*/
		if (cUrl != null && urlFile != null){
			System.out.println("urlFile & url conflict");
			System.out.println(help);
			return;
		}
		
		/*添加获取vip处理*/
		if(changVip == true){
			getVipLocalInfo = readLocalNetInfo();
			System.out.println(getVipLocalInfo);
			System.out.println(getLocalVip(getVipLocalInfo));
		} 
		
		if(cUrl != null && urlFile == null){
			urlArray = splitRegrexString(cUrl);
		}	
		totalEachThread = totalRequest / threadCount;
		
		// * 如果提供urlFile,则从文件里读取URL，忽略<url>参数
		if(urlFile != null && (!urlFile.isEmpty())){
			FileReader fr = new FileReader(urlFile); 
			BufferedReader br = new BufferedReader(fr); 
			String urlLine = br.readLine(); 
			while (urlLine != null) { 
				urlArray.add(urlLine);
				urlLine = br.readLine(); 
			} 
			br.close();
			fr.close();
		}
		/*文件处理完毕*/
		
		ArrayList<String> rtArray = new ArrayList<String>();
		ArrayList<SendThread> threadArray = new ArrayList<SendThread>();
		SendThread t = null;
		
		Date threadStart = new Date();
		for(int tc = 0; tc < (threadCount - 1); tc ++){
			t = new SendThread(tc, totalEachThread, rtArray,urlArray,random, headerArray, printHeader);
			t.start();
			threadArray.add(t);
		}
		
		//for the last thread when 100%3=1
		LastThreadRequest = totalEachThread + totalRequest % threadCount;
		t = new SendThread(threadCount, LastThreadRequest, rtArray,urlArray,random, headerArray, printHeader);
		t.start();
		threadArray.add(t);
		
		//Wait for all thread died
		for(int j=0; j<threadArray.size(); j++){
			threadArray.get(j).join();
		}
		Date threadEnd = new Date();
		
		long totalResponse = 0;
		double averRT = 0;
		double totalQPS = 0;
		double totalCostTime = (double) (threadEnd.getTime() - threadStart.getTime())/1000;
		Hashtable<Integer, Integer> tmpResCode = new Hashtable<Integer, Integer>();
		Hashtable<String, Integer> tmpThreadExcep = new Hashtable<String, Integer>(); 
		Integer tmpCode = 0;
		Integer tmpValue = 0;
		String tmpExcep = null;
		
		//thread process
		for(int j=0; j<threadArray.size(); j++) try {
			averRT += threadArray.get(j).getRT();
			totalQPS += threadArray.get(j).getQPS();
			totalResponse += threadArray.get(j).getResponse();
			tmpResCode = threadArray.get(j).getResCode();
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
			tmpThreadExcep = threadArray.get(j).getThreadExcep();
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
		}
		
		java.text.DecimalFormat rtDF=new java.text.DecimalFormat("#.###");
		java.text.DecimalFormat qpsDF=new java.text.DecimalFormat("#");
		
		System.out.println(String.format("Avg    RT: %1$1sms\nTotal QPS: %2$1s\nCost Time: %3$1ss\n",
				rtDF.format(averRT),qpsDF.format(totalQPS),rtDF.format(totalCostTime)));
		//print response code
		Iterator<Integer> it = totalResCode.keySet().iterator();
		System.out.println("Response Code:");
		while(it.hasNext()) {
			tmpCode = it.next();
			System.out.println(String.format("%1$-6d %2$-6d",tmpCode, totalResCode.get(tmpCode)));
		}
		
		//print exception
		System.out.println("\nException:");
		Iterator<String> is = totalExcep.keySet().iterator();
		while(is.hasNext()){
			tmpExcep = is.next();
			System.out.println(String.format("%1$-16s: %2$-5d", tmpExcep, totalExcep.get(tmpExcep)));
		}
		System.out.println("\n...Finish...");
		return;
	}

	   /* 	   
	    *处理URL中的正则表达式
	    *[a-b]/[c-d]
	    *[a-b]/c
	    *a/[b-c]
	    *a/b  
	    */
	public static ArrayList<String> splitRegrexString(String regexString) {
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
	} //

	public static String getLocalVip(String localInfo) {
		String localVip = null;
		URL iurl = null;
		try {
			iurl = new URL(localInfo);
			HttpURLConnection httpURLConnection = (HttpURLConnection) iurl.openConnection();
			httpURLConnection.setConnectTimeout(2000);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setReadTimeout(3000);
			httpURLConnection.getResponseCode();
			
			InputStream inStream = httpURLConnection.getInputStream();
			InputStreamReader inReader = new InputStreamReader(inStream);
			BufferedReader inBuffer = new BufferedReader(inReader);
			String tmp = null;
			while((tmp = inBuffer.readLine()) != null ){
				if (tmp.contains("vip:"))
					localVip = tmp.substring(5);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception: Failed to Get Dynamic Vip!");
			System.out.println(e.getMessage());
		}	
		return localVip;
	}
	
	public static String readLocalNetInfo() {
		String localNetInfo = "";
		String readline = "";
		BufferedReader br = null;
		Integer time = 0;
		
		try {
			br= new BufferedReader(new InputStreamReader(new FileInputStream("dns.conf"))); 
		} catch (FileNotFoundException fn)
		{
			System.out.println("dnsFile not exist");
		}
		try {
			readline = br.readLine();
			while( readline != null ){
				if(readline.contains("host=")){
					localNetInfo += "http://";
					localNetInfo += readline.substring(5);
				}
				else if(readline.contains("port=")){
					localNetInfo += ":";
					localNetInfo += readline.substring(5);
					localNetInfo += "/gtm.cgi?";
				}
				else if(readline.contains("pool=")){
					localNetInfo +=readline;
				}
				else if(readline.contains("wideip=")){
					localNetInfo += "&";
					localNetInfo += readline;
				}
				else if(readline.contains("ldns")){
					localNetInfo += "&";
					localNetInfo += readline;
				}
				else if(readline.contains("interval=")){
					time = Integer.valueOf(readline.substring(9));
				}
				readline = br.readLine();
			}
		}catch (Exception ioe){
			ioe.printStackTrace();
		}
		//if(localNetInfo != "" )
		System.out.println(localNetInfo);
		if(time != 0 )
			System.out.println(time);
		return localNetInfo;
	}
	
	public static ArrayList<String> newArrayList(ArrayList<String> urlList, String vip){
		int start;
		int end;
		String tmpHost = null;
		String tmpUrl = "";
		
		for(int n=0; n < urlList.size(); n++) {
			tmpUrl = urlList.get(n);
			start = tmpUrl.indexOf("//");
			end = tmpUrl.indexOf("/", start+2);
			tmpHost = tmpUrl.substring(start + 2, end);
			System.out.println(tmpUrl.replace(tmpHost, vip));
			urlList.set(n, tmpUrl.replace(tmpHost, vip));
			}
		System.out.println(urlList.toString());
		return urlList;
	}
}
