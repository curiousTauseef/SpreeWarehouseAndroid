package org.genshin.gsa;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.Credentials;
import android.os.Bundle;
import android.util.Log;

public class RESTConnector extends Activity {
	private Boolean initialized;
	private DefaultHttpClient client; 
	
	private UsernamePasswordCredentials credentials;
	private AuthScope scope;
	private String server;
	private int port;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialized = false;
	}

	public void setup(String server, long port, String user, String password) {
		this.server = server;
		this.port = (int)port;
		
		this.client = new DefaultHttpClient();
		
		if (user != null) {
			this.credentials = new UsernamePasswordCredentials(user, password);
			this.scope = new AuthScope(server, (int)port);
			this.client.getCredentialsProvider().setCredentials(scope, credentials);
		}
		
		this.initialized = true;
	}
	
	public String test() {
		int statusCode = 0;
		String combinedStatus = "unsent";
		Log.d("RESTDEBUG", combinedStatus);
		
		if (!initialized) {
			Log.d("RESTDEBUG", "Uninitialized");
			return "Uninitialized";
		}
		
		try {
			HttpGet getter = new HttpGet("http://" + this.server + ":" + this.port);
			combinedStatus = "Attempting GET to: " + getter.getURI();
			Log.d("RESTDEBUG", combinedStatus);
			HttpResponse response = this.client.execute(getter);
			combinedStatus = "Executed GET";
			Log.d("RESTDEBUG", combinedStatus);
			StatusLine statusLine = response.getStatusLine();
			statusCode = statusLine.getStatusCode();
			combinedStatus = "GET Result: " + String.valueOf(statusCode);
			Log.d("RESTDEBUG", combinedStatus);
		} catch (ClientProtocolException e) {
			combinedStatus = "ClientProtocolException: " + e.getMessage();
			Log.d("RESTDEBUG", combinedStatus);
		} catch (IOException e) {
			combinedStatus = "IOException: " + e.toString();
			Log.d("RESTDEBUG", combinedStatus);
		}
		
		return combinedStatus;
	}
	
	private Object responseToObject(HttpResponse response, Class<?> cls) {
		//Gson gson = new Gson();
		/*HttpEntity entity = response.getEntity();
		InputStream content = entity.getContent();
		Reader reader = new InputStreamReader(content);*/
		Reader reader;
		try {
			reader = new InputStreamReader(response.getEntity().getContent());
			//return gson.fromJson(reader, cls);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Object getObject(String targetURL, Class<?> containerClass) {
		Object data = null;
		
		try {
			HttpGet getter = new HttpGet("http://" + this.server); //+ targetURL);
			HttpResponse response = client.execute(getter);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				data = responseToObject(response, containerClass);
			} else {
				//Log.e(ParseJSON.class.toString(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data;
	}
	
	public JSONObject getJSON(String targetURL) {
		JSONObject data = null;
							
		try {
			HttpGet getter = new HttpGet("http://" + this.server + ":" + this.port + "/" + targetURL);
			//Set headers manually because Android doesn't seem to care to
			getter.addHeader("Authorization", "Basic " + this.credentials.getUserName() + ":" + this.credentials.getPassword());
			HttpResponse response = client.execute(getter);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				Log.d("PARSE", content);
				data = new JSONObject(content);
			} else {
				Log.d("PARSE", "RESPONSE: " + statusCode);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return data;
	}
}

