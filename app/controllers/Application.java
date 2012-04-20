package controllers;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import play.mvc.Controller;
import play.mvc.WebSocketController;

public class Application extends Controller {

	public static void index() {
		render();
	}

	public static class Socket extends WebSocketController {

		public static void feed(String username, String password)
				throws EOFException, IOException {

			while (inbound.isOpen()) {

				org.apache.http.client.HttpClient httpclient = new DefaultHttpClient();
				HttpPost postreq = new HttpPost(
						"https://stream.twitter.com/1/statuses/filter.json?delimited=length");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						1);
				nameValuePairs.add(new BasicNameValuePair("locations",
				// "5.11,71.35,38.35,97.72"));
						"-122.75,36.8,-121.75,37.8,-74,40,-73,41"));
				// "12.77,77.37,13.14,77.77"));

				postreq.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				((DefaultHttpClient) httpclient).getCredentialsProvider()
						.setCredentials(
								new AuthScope("stream.twitter.com", 443),
								new UsernamePasswordCredentials(username,
										password));

				org.apache.http.HttpResponse stream = httpclient
						.execute(postreq);
				InputStream is = stream.getEntity().getContent();

				byte[] b = new byte[10];
				int off = 0;
				int count = 0;

				// String s = EntityUtils.toString(stream.getEntity());
				while ((count = readLine(is, b, 0, b.length)) > 0) {
					byte[] buffer = new byte[Integer.parseInt(new String(b)
							.trim())];
					off = off + is.read(buffer);
					String tweet = new String(buffer);

					if (inbound.isOpen()) {
						outbound.send(tweet);
					} else {
						disconnect();
						return;
					}
					b = new byte[10];
				}
			}
		}

		/*
		 * Originally found in apache-tomcat-6.0.26
		 * org.apache.tomcat.util.net.TcpConnection Licensed under the Apache
		 * License, Version 2.0
		 */
		private static int readLine(InputStream in, byte[] b, int off, int len)
				throws IOException {
			if (len <= 0) {
				return 0;
			}
			int count = 0, c;
			while ((c = in.read()) != -1) {
				b[off++] = (byte) c;
				count++;
				if (c == '\n' || count == len) {
					break;
				}
			}
			return count > 0 ? count : -1;
		}
	}
}
