package com.fbytes.call03;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


//------------------------------------------------------------------
// AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
// we do not want to invoke it from the UI thread.
public class GeoCoderGoogleAndroid extends AsyncGeoCoder {
	
	private String TAG="GeoCoderGoogleAndroid";
	Context mContext;
	private int MAX_REVERSEGEO_ENTRIES=3;	
	List<Address> addressesList;

	public GeoCoderGoogleAndroid(Context context,Handler pHandler,int pMessage) {
		super(context,pHandler,pMessage);
		mContext = context;
		Name="GoogleAndroid";
		MapLink="http://maps.google.com/maps?q=loc:@latt,@long&z=17";
	}

	@Override
	protected Void doInBackground(Location... params) {
		Location loc = params[0];

		//Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
		Geocoder defaultGeocoder = new Geocoder(mContext, new Locale("ru","RU"));

		int tRetriesCount=0;

		while (tRetriesCount<3){
			try {
				// Call the synchronous getFromLocation() method by passing in the lat/long values.
				addressesList = defaultGeocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), MAX_REVERSEGEO_ENTRIES);
				break;
			} catch (IOException e) {
				e.printStackTrace();
				tRetriesCount++;
			}
		}
		if (addressesList != null && addressesList.size() > 0) {
			//Log.d(TAG,"Got "+addressesList.size()+" addresses from reverse geocoding");	        	
			Address tAddress;
			String tAddressText;
			for (int i=0; i<addressesList.size();i++){
				tAddress = addressesList.get(i);
				if (tAddress.getLocality()!=null){
					tAddressText = String.format("%s",tAddress.getMaxAddressLineIndex() > 0 ? tAddress.getAddressLine(0) : "");
					AddressRec newAddress=new AddressRec();
					newAddress.setAddress(tAddressText);
					newAddress.setGeocodeProvider("Google");
					newAddress.setLoc(loc);
					PostAddress(tAddressText,loc);
					

				}
			}
		}
		return null;
	}

}
