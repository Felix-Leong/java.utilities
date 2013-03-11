package de.ebf.utils;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.log4j.Logger;

public class HttpUtil {
	
	private static final Logger log = Logger.getLogger(HttpUtil.class);
	
	/* DEFAULT HTTP CLIENT VALUES */
	private static final Long DEFAULT_CONNECTION_TIMEOUT 			= 30000L;
	private static final boolean DEFAULT_FOLLOW_REDIRECTS 			= true;
	
	public static boolean isAvailable(String url, String user, String pass) throws Exception{
		HttpResponse httpResponse = HttpUtil.get(url, user, pass);
		if (httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
        	return true;
        }
		return false;
	}

	public static HttpResponse get(String url, String user, String pass) throws Exception{
		URI uri = new URI(url);
		if (uri.getHost()==null || uri.getScheme()==null){
			throw new IllegalArgumentException("Incorrect URL "+url);
		}
		HttpGet httpGet2 = new HttpGet(uri);

		DefaultHttpClient httpClient = getNewHttpClient();
		
	    try {
	        httpClient.getCredentialsProvider().setCredentials(
	                new AuthScope(httpGet2.getURI().getHost(), httpGet2.getURI().getPort()),
	                new UsernamePasswordCredentials(user, pass));
	
	        // Create AuthCache instance
	        AuthCache authCache = new BasicAuthCache();
	        // Generate BASIC scheme object and add it to the local auth cache
	        BasicScheme basicAuth = new BasicScheme();
	        authCache.put(new HttpHost(httpGet2.getURI().getHost()), basicAuth);
	
	        // Add AuthCache to the execution context
	        BasicHttpContext localcontext = new BasicHttpContext();
	        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
	
        	HttpResponse response = httpClient.execute(httpGet2, localcontext);
            
        	if (response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){
        		log.warn("Got invalid HTTP response code for URL "+url+" "+response.getStatusLine().getStatusCode());
        	}
    		return response;
            //HttpEntity entity = response.getEntity();
            //EntityUtils.consume(entity);
	    } finally {
	        // When HttpClient instance is no longer needed,
	        // shut down the connection manager to ensure
	        // immediate deallocation of all system resources
	        //httpClient.getConnectionManager().shutdown();
	    }
	}
	
	private static DefaultHttpClient getNewHttpClient() {
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance("SSL");
			// set up a TrustManager that trusts everything
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}
				
				public void checkServerTrusted(X509Certificate[] certs,	String authType) {
				}
			} }, new SecureRandom());
			
			SSLSocketFactory sf = new SSLSocketFactory(sslContext);
			Scheme httpsScheme = new Scheme("https", 443, sf);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(httpsScheme);
			
			ClientConnectionManager cm = new BasicClientConnectionManager(schemeRegistry);
			DefaultHttpClient httpClient = new DefaultHttpClient(cm);
			
			final HttpParams params = new BasicHttpParams();
			HttpClientParams.setRedirecting(params, DEFAULT_FOLLOW_REDIRECTS);
			HttpClientParams.setConnectionManagerTimeout(params, DEFAULT_CONNECTION_TIMEOUT);
			HttpClientParams.setCookiePolicy(params, CookiePolicy.IGNORE_COOKIES);
			httpClient.setParams(params);
			return httpClient;
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
