package com.fbytes.call03;

// http://www.edumobile.org/android/android-development/gecoding-example/comment-page-1/#comment-321


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

public class GeoCoderGoogle extends AsyncGeoCoder {

	public static String TAG="GeoCoderGoogle";

	public GeoCoderGoogle(Context context,Handler pHandler,int pMessage) {
		super(context,pHandler,pMessage);

		Name="Google";
		MapLink="http://maps.google.com/maps?q=@long,@latt&ll=@long,@latt&z=17";
	}	

	@Override
	protected Void doInBackground(Location... params) {
		Location loc = params[0];
		String localityName = "";

		HttpClient client = new DefaultHttpClient();
		String tLocStr=String.valueOf(loc.getLatitude())+","+String.valueOf(loc.getLongitude());
		String tUrlStr="http://maps.googleapis.com/maps/api/geocode/json?language=ru_RU&latlng="+tLocStr+"&sensor=true";
		Log.d(TAG,tUrlStr);
		HttpGet httpGet = new HttpGet(tUrlStr);
		try {
			HttpResponse response = client.execute(httpGet);      
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode!=200){
				Log.e(TAG,"Google reverse geocoder return errorcode - "+statusCode);
				return null;
			}
			//Log.d(TAG,"Got 200 response. Parsing...");	    	  
			HttpEntity entity = response.getEntity();
			String jsonRespStr=new String("");
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = reader.readLine()) != null) {
				jsonRespStr+=line;
			}
			//Log.d(TAG,jsonRespStr);
			try {
				JSONObject mainJsonObject = new JSONObject(jsonRespStr);
				JSONArray resultsArray=mainJsonObject.getJSONArray("results");
				String resultsStatus=mainJsonObject.getString("status");
				if (!resultsStatus.equals("OK")){
					Log.e(TAG,"Google reverse geocoder return bad status -"+resultsStatus);
					return null;
				}
				for (int i=0; i<resultsArray.length(); i++){
					JSONObject result=resultsArray.getJSONObject(i);
					String revAddress=result.getString("formatted_address");
					Log.d(TAG,"Address="+revAddress);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}   
		catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}


	public static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}



