package http;

import java.net.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.*;
public class SendThread extends Thread {
	
	private int tIndex;
	private long totalNum;
	private double sumObjSizeInKB = 0;
	
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
	private long currentCount = 0;
	
	private HttpClient client = null;

	public SendThread(int threadIndex, long total, 
			ArrayList<String> urlarray, int random,
			ArrayList<String> headerArray, boolean printHeader) {
		this.tIndex = threadIndex;
		this.totalNum = total;
		this.urlArray = urlarray;
		this.random = random;
		this.urlSize = urlArray.size();
		this.headerArray = headerArray;
		this.printHeader = printHeader;

		this.client = new DefaultHttpClient();
		
		//set client request version, by default is http/1.1 
		if(util.Global.protocolVersion.equals("1.0")){
			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, org.apache.http.HttpVersion.HTTP_1_0);
		}
		
		// Set client to reuse TIME_WAIT socket
		// client.getParams().setParameter(CoreConnectionPNames.SO_REUSEADDR,true);
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
			try {
				if (this.random == 1) {
					urlArrayIndex = randomElement.nextInt(this.urlSize);
				} else {
					urlArrayIndex = iRequest % this.urlSize;
				}
				urlTemp = this.urlArray.get(urlArrayIndex);
				Response resp = HttpRequest.send(client, urlTemp);

				Integer code = resp.getResponseCode();

				Integer value = resCode.get(code);
				tmpValue = value != null ? value + 1 : 1;
				resCode.put(code, tmpValue);
				
				currentCount = iRequest + 1;
				//��������object��С
				this.sumObjSizeInKB += ((double)resp.getContentFullSizeInByte())/1024;
				
				if(util.Global.verifyMd5){
					checkMd5(resp, urlTemp);
				}
				
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
				response += 1;
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

	public double getSumObjSizeInKB(){
		return this.sumObjSizeInKB;
	}
	
	public long getTotalNum(){
		return this.totalNum;
	}
	
	public long getCurrentCount(){
		return this.currentCount;
	}

	protected void finalize() {
		// release the tcp connection when this thread object is released.
		if (this.client != null) {
			this.client.getConnectionManager().shutdown();
		}
	}
	
	/*
	 * ��֤���������Ӧ��200������֤url���ļ�����(��������׺����)��body��md5ֵ��һ���ģ������һ��ֱ�Ӵ�ӡ���
	 */
	private void checkMd5(Response resp, String url) throws Exception{
		if(200 == resp.getResponseCode()){
			//��URL��ȡ�ļ�����
			String fileName = "";
			URL urlObj = new URL(url);
			String path = urlObj.getFile();
			
			int left = path.lastIndexOf("/");
			int right = path.lastIndexOf(".");
			
			if(left == -1){
				fileName = "";
			}else{
				if(right == -1){
					right = path.length();
				}
				
				if(right > (left+1)){
					fileName = path.substring(left+1, right);
				}else{
					fileName = "";
				}
			}
			
			//��ȡresponse body��md5
			String md5 = resp.getMd5HexString();
			
			//System.out.println(String.format("expected md5:%1$1s actual md5:%2$1s", fileName,md5));
			if((resp.getContentFullSizeInByte() >= resp.getHeaderLenInByte()) && (false == md5.contentEquals(fileName))){
				System.out.println(String.format("[error]expected md5:%1$1s actual md5:%2$1s resCode:%3$1d contentLen:%4$1d headerLen:%5$1d", fileName,md5,resp.getResponseCode(),resp.getContentFullSizeInByte(),resp.getHeaderLenInByte()));
			}
		}
	}
}
