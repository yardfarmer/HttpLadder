/*
 * ����md5�����㷨
 */

package util;

public class MD5 {

	public static String getMd5HexString(byte[] input) throws Exception{
		java.security.MessageDigest  md = java.security.MessageDigest.getInstance("MD5");
		 md.update(input);
		 byte b[] = md.digest();
		 
		 return byteToHexString(b);
	}
	
	private static String byteToHexString(byte[] b)
	 {
	  StringBuffer sb=new StringBuffer();
	  String temp="";
	  for(int i=0;i<b.length;i++)
	  {
	   temp=Integer.toHexString(b[i]&0Xff);
	   if(temp.length()==1)
	    temp="0"+temp;
	   sb.append(temp);
	  }
	  return sb.toString();

	 }
}
