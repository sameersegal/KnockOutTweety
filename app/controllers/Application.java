package controllers;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import play.mvc.Controller;
import play.mvc.WebSocketController;

/**
 * The main controller for the KnockOutTweety web app. 
 *
 * Here is a sample tweet output: 
 2077
  {
   "geo":null,
   "text":"#ThingsInMyLifeThatArePriceless family",
   "id_str":"193534661505466368",
   "retweet_count":0,
   "in_reply_to_status_id":null,
   "retweeted":false,
   "created_at":"Sat Apr 21 03:00:33 +0000 2012",
   "truncated":false,
   "coordinates":null,
   "source":"\u003Ca href=\"http:\/\/blackberry.com\/twitter\" rel=\"nofollow\"\u003ETwitter for BlackBerry\u00ae\u003C\/a\u003E",
   "in_reply_to_status_id_str":null,
   "in_reply_to_user_id_str":null,
   "entities":{
      "urls":[

      ],
      "user_mentions":[

      ],
      "hashtags":[
         {
            "text":"ThingsInMyLifeThatArePriceless",
            "indices":[
               0,
               31
            ]
         }
      ]
   },
   "favorited":false,
   "contributors":null,
   "place":null,
   "user":{
      "statuses_count":5361,
      "favourites_count":9,
      "profile_background_color":"ACDED6",
      "id_str":"490728480",
      "follow_request_sent":null,
      "profile_background_tile":true,
      "profile_background_image_url_https":"https:\/\/si0.twimg.com\/images\/themes\/theme18\/bg.gif",
      "created_at":"Sun Feb 12 21:09:56 +0000 2012",
      "profile_sidebar_fill_color":"DDEEF6",
      "url":"http:\/\/www.facebook.com\/TrizzyT.Montana",
      "description":" #TeamLakers  #TeamKansas #TeamAlabama  #TeamCapricorn #TeamRedWings #TeamKobeBryant #TeamThomasRobinson #TrentRichardson",
      "listed_count":0,
      "time_zone":"Hawaii",
      "profile_sidebar_border_color":"C0DEED",
      "verified":false,
      "notifications":null,
      "default_profile":false,
      "profile_use_background_image":true,
      "location":"Never-Do-Right City",
      "is_translator":false,
      "contributors_enabled":false,
      "lang":"en",
      "geo_enabled":false,
      "profile_text_color":"333333",
      "protected":false,
      "profile_image_url_https":"https:\/\/si0.twimg.com\/profile_images\/2080699947\/Picture_20016_1__normal",
      "screen_name":"TrizzyTroof_CA",
      "profile_background_image_url":"http:\/\/a0.twimg.com\/images\/themes\/theme18\/bg.gif",
      "followers_count":98,
      "profile_image_url":"http:\/\/a0.twimg.com\/profile_images\/2080699947\/Picture_20016_1__normal",
      "name":"Trizzy T.",
      "profile_link_color":"038544",
      "id":490728480,
      "default_profile_image":false,
      "show_all_inline_media":false,
      "following":null,
      "utc_offset":-36000,
      "friends_count":129
   },
   "id":193534661505466368,
   "in_reply_to_screen_name":null,
   "in_reply_to_user_id":null
 }
 * 
 * @author Sameer
 * 
 */
public class Application extends Controller {

	/**
	 * Renders app/views/Application/index.html on '/' request
	 */
	public static void index() {
		render();
	}

	/**
	 * Controller to handle socket events
	 * 
	 */
	public static class Socket extends WebSocketController {

		/**
		 * Connects to twitter and provides a stream of tweets. The twitter
		 * stream is bounded by a geography
		 * 
		 * @param username
		 *            twitter username
		 * @param password
		 *            twitter password
		 * @throws EOFException
		 * @throws IOException
		 */
		public static void feed(String username, String password)
				throws EOFException, IOException {

			// While the websocket is open
			while (inbound.isOpen()) {

				// Create a connection to twitter. Check documentation for
				// streaming api here:
				// https://dev.twitter.com/docs/streaming-api
				HttpClient httpclient = new DefaultHttpClient();

				// Basic http authentication
				((DefaultHttpClient) httpclient).getCredentialsProvider()
						.setCredentials(
								new AuthScope("stream.twitter.com", 443),
								new UsernamePasswordCredentials(username,
										password));

				// Post request. We ask Twitter to provide us a stream of tweets
				// delimited by the length
				HttpPost postreq = new HttpPost(
						"https://stream.twitter.com/1/statuses/filter.json?delimited=length");

				// Post parameters
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						1);
				// Bay Area region coordinates -- SW & NE for the bounding box
				nameValuePairs.add(new BasicNameValuePair("locations",
						"-122.75,36.8,-121.75,37.8"));
				postreq.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Making a call to twitter
				org.apache.http.HttpResponse stream = httpclient
						.execute(postreq);
				
				// Since this is streaming api, we continuously get a response
				// from twitter and need to process it chuck by chuck
				InputStream is = stream.getEntity().getContent();

				byte[] b = new byte[10];
				
				// To process chunk by chunk, extract the length first by reading initial bytes
				// And then read the tweet
				while (readLine(is, b, 0, b.length) > 0) {
					String count = new String(b).trim();
					
					// In case twitter sends us a new line feed 
					// to keep the connection open 
					if("".equals(count)){
						continue;
					}
					
					// Reading the next <count> bytes to extract the tweet
					byte[] buffer = new byte[Integer.parseInt(count)];
					is.read(buffer);
					
					// Store as string
					String tweet = new String(buffer);

					// If the socket is still open, send the tweet to the browser
					if (inbound.isOpen()) {
						outbound.send(tweet);
					} else {
						disconnect();
						return;
					}
					
					// re-init b to avoid strange results
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
