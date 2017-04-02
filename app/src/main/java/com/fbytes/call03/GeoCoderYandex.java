package com.fbytes.call03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import ru.yandex.yandexmapkit.*;
import ru.yandex.yandexmapkit.map.MapEvent;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

public class GeoCoderYandex extends AsyncGeoCoder {
	private static String TAG="GeoCoderYandex";
	
	public GeoCoderYandex(Context context,Handler pHandler,int pMessage) {
		super(context,pHandler,pMessage);
		
		Name="Yandex";
		MapLink="http://maps.yandex.ru/?ll=@long,@latt&pt=@long,@latt&z=16&l=map";
	}	
	
	@Override
	protected Void doInBackground(Location... params) {
		Location loc = params[0];
		
		HttpClient client = new DefaultHttpClient();
		String tLocStr=String.valueOf(loc.getLongitude())+","+String.valueOf(loc.getLatitude());
		String tUrlStr="http://geocode-maps.yandex.ru/1.x/?spn=0.0017,0.0017&kind=house&format=json&geocode="+tLocStr;
		//Log.d(TAG,"Loc:"+tLocStr);		
		//Log.d(TAG,"URL:"+tUrlStr);
		HttpGet httpGet = new HttpGet(tUrlStr);
	    try {
	      HttpResponse response = client.execute(httpGet);      
	      StatusLine statusLine = response.getStatusLine();
	      int statusCode = statusLine.getStatusCode();
	      if (statusCode == 200) {
	    	//Log.d(TAG,"Got 200 response. Parsing...");	    	  
	        HttpEntity entity = response.getEntity();
	        String jsonRespStr=new String("");
	        InputStream content = entity.getContent();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	        String line;
	        while ((line = reader.readLine()) != null) {
	        	jsonRespStr+=line;
	        }
	        try {
				JSONObject jsonObject = new JSONObject(jsonRespStr);
				JSONObject responseObject=jsonObject.getJSONObject("response");
				JSONObject geoObjectCollection=responseObject.getJSONObject("GeoObjectCollection");
				JSONArray featureMembersArray=geoObjectCollection.getJSONArray("featureMember");
				for (int i=0; i<featureMembersArray.length(); i++){
					
					JSONObject featureMember = featureMembersArray.getJSONObject(i);
					JSONObject geoObject = featureMember.getJSONObject("GeoObject");
					JSONObject metaDataProperty = geoObject.getJSONObject("metaDataProperty");
					JSONObject geocoderMetaData = metaDataProperty.getJSONObject("GeocoderMetaData");
					JSONObject addressDetails = geocoderMetaData.getJSONObject("AddressDetails");
					JSONObject addressCountry = addressDetails.getJSONObject("Country");
					JSONObject addressLocality = addressCountry.getJSONObject("Locality");
					JSONObject addressThoroughfare = addressLocality.getJSONObject("Thoroughfare");
					JSONObject addressPremise = addressThoroughfare.getJSONObject("Premise");
					
					String tAddrText=addressThoroughfare.getString("ThoroughfareName")+",";
					tAddrText+=addressPremise.getString("PremiseNumber");
					PostAddress(tAddrText,loc);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      } else {
	        Log.e(TAG,"Yandex return errorcode - "+statusCode);
	      }
	    }   
	    catch (ClientProtocolException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
		return null;
 
	}
}
