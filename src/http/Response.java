package http;
/*
 * This class contains the HTTP response information
 */
public class Response {

	private int responseCode = -1;
	private long contentFullSizeInByte = -1;
	private long headerSizeInByte = -1;
	private String md5HexString = "";
		
	
	public int getResponseCode(){
		return this.responseCode;
	}
	
	public long getContentFullSizeInByte(){
		return this.contentFullSizeInByte;
	}
	
	public void setResponseCode(int value){
		this.responseCode = value;
	}
	
	public void setContentFullSizeInByte(long value){
		this.contentFullSizeInByte = value;
	}
	
	public String getMd5HexString(){
		return this.md5HexString;
	}
	
	public void setMd5HexString(String value){
		this.md5HexString = value;
	}
	
	public long getHeaderLenInByte(){
		return this.headerSizeInByte;
	}
	
	public void setHeaderLenInByte(long value){
		this.headerSizeInByte = value;
	}
}
