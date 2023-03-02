/* Copyright 2019-2022 Universidad Politécnica de Madrid (UPM).
 *
 * Authors:
 *    Mario San Emeterio de la Parte
 *    Vicente Hernández Díaz
 *
 * This software is distributed under a dual-license scheme:
 *
 * - For academic uses: Licensed under GNU Affero General Public License as
 *                      published by the Free Software Foundation, either
 *                      version 3 of the License, or (at your option) any
 *                      later version.
 *
 * - For any other use: Licensed under the Apache License, Version 2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * You can get a copy of the license terms in licenses/LICENSE.
 *
 */

package demeter.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GetObservationsFromAFarCloud {

	private static final Logger log = Logger.getLogger(GetObservationsFromAFarCloud.class);
	
	/**	   Method for querying the DataBase and obtaining the resource list **/
	public static JsonObject getResources(String query) throws IOException, SocketTimeoutException {
		try {

			String name = "datatransformer";
			String password = "GRyS2022";
			String authString = name + ":" + password;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);

			trustEveryone();
			log.debug("Requesting data from repositories.. ");
			URL uri = new URL(query);
			HttpsURLConnection conn = (HttpsURLConnection) uri.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			conn.setRequestProperty("Content-Type", "application/json");
		
			//Timeout de 1 min
			conn.setConnectTimeout(60000);

			int code=conn.getResponseCode();
			if (code != HttpsURLConnection.HTTP_OK) {
				throw new RuntimeException(Integer.toString(code));
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String response = br.lines().collect(Collectors.joining());
			JsonParser parser = new JsonParser();
			JsonElement responseJSON = parser.parse(response);
			JsonObject queryResponse = responseJSON.getAsJsonObject();
			
			while ((response = br.readLine()) != null) {

			}	        
			conn.disconnect();
			//	    Store object in cache
			return queryResponse;
		}
//	     	This catch is implemented because otherwise WebApplicationExceptions are treated as RuntimeExceptions,
//			and processed as such.
		catch (WebApplicationException e) {
			throw new WebApplicationException(Response.status(500).entity(e.getMessage()).build());
		}
		catch(SocketTimeoutException e) {
			log.error("Could not connect to DAM: Timeout Exception");
			throw new SocketTimeoutException("Timeout");
		}
		catch (MalformedURLException e) {
			log.error("Could not connect to DAM: "+e.getMessage());
			throw new MalformedURLException("MalformedURL");
		}
		catch (RuntimeException e) {
			log.error("Could not connect to DAM: "+e.getMessage());
			throw new WebApplicationException(Response.status(500).entity("ERROR: Could not connect to DAM").build());
		}
	}  
	
	private static void trustEveryone() {		
		try { 
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){ 
				@Override
				public boolean verify(String hostname, SSLSession session) { 
					return true; 
				}
			}); 
			SSLContext context = SSLContext.getInstance("TLS"); 
			context.init(null, new X509TrustManager[]{new X509TrustManager(){ 
				@Override
				public void checkClientTrusted(X509Certificate[] chain, 
						String authType) throws CertificateException {} 
				@Override
				public void checkServerTrusted(X509Certificate[] chain, 
						String authType) throws CertificateException {} 
				@Override
				public X509Certificate[] getAcceptedIssuers() { 
					return new X509Certificate[0]; 
				}
			}}, new SecureRandom()); 
			HttpsURLConnection.setDefaultSSLSocketFactory( 
					context.getSocketFactory()); 
		} catch (Exception e) { // should never happen 
			e.printStackTrace(); 
		} 
	}
}
