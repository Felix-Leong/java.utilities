package de.ebf.utils;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

public class HttpUtil {
	
	public static boolean isAvailable(String url, String user, String pass) throws Exception{
		URI uri = new URI(url);
		if (uri.getHost()==null || uri.getScheme()==null){
			throw new IllegalArgumentException("Incorrect URL");
		}
		HttpGet httpGet2 = new HttpGet(uri);
	
	    DefaultHttpClient httpclient = new DefaultHttpClient();
	    try {
	        httpclient.getCredentialsProvider().setCredentials(
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
	
        	HttpResponse response = httpclient.execute(httpGet2, localcontext);
            if (response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
            	return true;
            }
            //HttpEntity entity = response.getEntity();
            //EntityUtils.consume(entity);
	    } finally {
	        // When HttpClient instance is no longer needed,
	        // shut down the connection manager to ensure
	        // immediate deallocation of all system resources
	        httpclient.getConnectionManager().shutdown();
	    }
	    return false;
	}

}
