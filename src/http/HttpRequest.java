package http;

import org.apache.http.*;
import org.apache.http.client.*;
import java.io.InputStream;
import java.util.Date;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/*
 * Send http requests
 */
public class HttpRequest {
	public static Response send(HttpClient client, String url) throws Exception {
		Response response = new Response();

		InputStream instream = null;
		try {
			HttpGet httpget = new HttpGet(url);
			for (String tmpHeader : util.Global.headerArray) {
				int firstIndex = tmpHeader.indexOf(":");
				String key = tmpHeader.substring(0, firstIndex);
				key = key.trim();
				String value = tmpHeader.substring(firstIndex + 1);
				value = value.trim();
				httpget.setHeader(key, value);
			}
			HttpResponse httpResponse = client.execute(httpget);
			StatusLine statusLine = httpResponse.getStatusLine();
			response.setResponseCode(statusLine.getStatusCode());
			Header h = httpResponse.getFirstHeader("Content-Length");
			response.setHeaderLenInByte(h == null ? 0 : Long.valueOf(h.getValue()));
			HttpEntity entity = httpResponse.getEntity();

			// print header if needed

			if (util.Global.printHeader) {
				HeaderIterator headers = httpResponse.headerIterator();
				// such as HTTP/1.1 200 OK
				String printAll = httpResponse.getStatusLine().toString();
				while (headers.hasNext()) {
					printAll += util.Global.lineSeperator + headers.next();
				}
				System.out.print("\n" + url + "\n");
				System.out.println(printAll);
			}

			if (entity != null) {

				/*
				 * ��������ݶ�ȡҪ������������� 1. �������Ҫ�������������ݣ�buffer�������ó�һ�������ֵ���ظ�ʹ�� 2.
				 * �����Ҫ������ɵ����ݣ�����֤md5����buffer���뱣֤һ�ο�����������������
				 */
				instream = entity.getContent();

				if (true == util.Global.verifyMd5) {
					if (200 == response.getResponseCode()) {
						int l;
						byte[] dest = new byte[204800];
						byte[] tmp = new byte[10240];
						long contentSize = 0;
						String resTmp = "";
						int destPos = 0;
						while ((l = instream.read(tmp)) != -1) {
							contentSize += l;

							// copy thre content readed to dest array
							System.arraycopy(tmp, 0, dest, destPos, l);
							destPos += l;
						}
						
						response.setContentFullSizeInByte(contentSize);

						byte[] targetContent = new byte[(int) contentSize];
						System.arraycopy(dest, 0, targetContent, 0,
								(int) contentSize);

						resTmp = util.MD5.getMd5HexString(targetContent);
						response.setMd5HexString(resTmp);
					}

				} else {
					int l;
					byte[] tmp = new byte[10240];
					long contentSize = 0;
					while ((l = instream.read(tmp)) != -1) {
						contentSize += l;
					}
					response.setContentFullSizeInByte(contentSize);
					
				}
			} else {
				response.setContentFullSizeInByte(0);
			}

		} finally {
			if (instream != null ) {
				instream.close();
			}
		}
//		//���ڴ�ӡÿ��RT
//		Date end = new Date();
//		long diffMs = end.getTime() - start.getTime();
//
//		double rt = ((double) diffMs);
//		System.out.println(df.format(rt));
		
		return response;
	}

}
