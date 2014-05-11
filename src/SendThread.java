import java.net.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class SendThread extends Thread {

	private int tIndex;
	private long totalNum;

	ArrayList<String> rtArray;
	ArrayList<String> urlArray;
	ArrayList<String> headerArray;
	Hashtable<Integer, Integer> resCode = new Hashtable<Integer, Integer>();
	Hashtable<String, Integer> threadExcep = new Hashtable<String, Integer>();

	private double rt = 0;
	private double qps = 0;
	private long response = 0;
	private double diffs = 0;
	private int random = 0;
	private int urlSize = 0;
	private int headerSize = 0;
	private boolean printHeader = false;

	public SendThread(int threadIndex, long total, ArrayList<String> array,
			ArrayList<String> urlarray, int random,
			ArrayList<String> headerArray, boolean printHeader) {
		this.tIndex = threadIndex;
		this.totalNum = total;
		rtArray = array;
		this.urlArray = urlarray;
		this.random = random;
		this.urlSize = urlArray.size();
		this.headerArray = headerArray;
		this.headerSize = headerArray.size();
		this.printHeader = printHeader;

	}

	public void run() {

		java.text.DecimalFormat df = new java.text.DecimalFormat("#.###");
		Date start = new Date();
		Integer tmpValue = 0;
		int urlArrayIndex = 0;
		int iRequest = 0;
		URL url = null;
		String tmpHeader = null;
		String tmpExcep = null;
		Integer tmpExcepValue = 1;
		String urlTemp = "";

		Random randomElement = new Random();
		for (iRequest = 0; iRequest < this.totalNum; iRequest++) {
			HttpURLConnection httpURLConnection = null;
			try {
				if (this.random == 1) {
					urlArrayIndex = randomElement.nextInt(this.urlSize);
				} else {
					urlArrayIndex = iRequest % this.urlSize;
				}
				urlTemp = this.urlArray.get(urlArrayIndex);
				url = new URL(urlTemp);
				
				httpURLConnection = (HttpURLConnection) url.openConnection();
				
				httpURLConnection.setConnectTimeout(6000);
				httpURLConnection.setReadTimeout(6000);
				
				for (int h = 0; h < headerSize; h++) {
					tmpHeader = headerArray.get(h);
					int firstIndex = tmpHeader.indexOf(":");
					String key = tmpHeader.substring(0, firstIndex);
					String value = tmpHeader.substring(firstIndex + 1);
					httpURLConnection.setRequestProperty(key, value);
				}
				
				
				httpURLConnection.connect();
				
				Integer code = httpURLConnection.getResponseCode();
				// Read the content of this connection and throw it
				// immediately
				httpURLConnection.getContent();
				InputStream in = httpURLConnection.getInputStream();
				
				byte[] bytes = new byte[100000];
				
				int actualLength = 0;
				actualLength = in.read(bytes);
//				while(in.read() != -1){
//					actualLength++;
//				}
//				
//				in.close();
				
				//System.out.println(String.format("Obj length:%1$1d", actualLength));
				
				
				if (printHeader == true) {
					printResponseHeader(httpURLConnection, urlTemp);
				}
				if (resCode.containsKey(code)) {
					tmpValue = resCode.get(code) + 1;
					resCode.put(code, tmpValue);
				} else {
					resCode.put(code, 1);
				}
				response += 1;

			} catch (Exception ep) {
				tmpExcep = String.format("%1$1s(%2$1s)", ep.getMessage(), ep
						.getClass().getCanonicalName());
				if (threadExcep.containsKey(tmpExcep)) {
					tmpExcepValue = threadExcep.get(tmpExcep) + 1;
					threadExcep.put(tmpExcep, tmpExcepValue);
				} else {
					threadExcep.put(tmpExcep, tmpExcepValue);
				}
				
			} finally {
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
					
				}
			}
		}
		Date end = new Date();

		long diffMs = end.getTime() - start.getTime();

		double rt = ((double) diffMs) / totalNum;

		double diffs = (double) diffMs / 1000;

		double qps = ((double) response) / diffs;

		this.rt = rt;
		this.qps = qps;
		this.diffs = diffs;
	}

	public double getRT() {
		return this.rt;
	}

	public double getQPS() {
		return this.qps;
	}

	public Hashtable<Integer, Integer> getResCode() {
		return resCode;
	}

	public double getCostTime() {
		return this.diffs;
	}

	public Hashtable<String, Integer> getThreadExcep() {
		return this.threadExcep;
	}

	public long getResponse() {
		return this.response;
	}

	private void printResponseHeader(HttpURLConnection httpURLConnection,
			String tempUrl) {
		try {
			Map<String, List<String>> responseHeader = httpURLConnection
					.getHeaderFields();
			Iterator<String> it = responseHeader.keySet().iterator();
			String tmpHeader = "";
			String header = "";
			while (it.hasNext()) {
				tmpHeader = it.next();
				String tmpValue = responseHeader.get(tmpHeader).toString();
				int valueSize = tmpValue.length() - 1;
				header = header + tmpHeader + ":"
						+ tmpValue.substring(1, valueSize) + "\n";
			}
			System.out.println(String.format("%1$s\n %2$s", tempUrl, header));
			System.out.println();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
