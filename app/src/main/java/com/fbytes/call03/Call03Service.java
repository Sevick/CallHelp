package com.fbytes.call03;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEventListener;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;



public class Call03Service extends Service implements LocationListener, GpsStatus.Listener{
	
	public String TAG="Call03Service";
	
	public static final String MAKECALL = "MakeCall";
	public static final String CANCELCALL = "CancelCall";
	
	public Call03Service(){
		Log.i(TAG,"Service created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStart(intent, startId);
		
        Log.i(TAG, "onStartCommand");
        makeCall(intent);
		stopSelf(startId);
		return START_STICKY;
	}

	private void makeCall(Intent intent) {
		Log.i(TAG, "This is the intent " + intent);
		if (intent != null){
			String requestedAction = intent.getAction();
			Log.i(TAG, "This is the action " + requestedAction);

			if (requestedAction != null){
				int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
				if (requestedAction.equals(MAKECALL)){
					Log.i(TAG,"Service is calling to 03");
					Log.i(TAG, "This is the Call03 to widget " + widgetId);
					AppWidgetManager appWidgetMan = AppWidgetManager.getInstance(this);
					RemoteViews views = new RemoteViews(this.getPackageName(),R.layout.widgetlayout);          		
					//views.setTextViewText(R.id.widgetText,"Making call");
					appWidgetMan.updateAppWidget(widgetId, views);
					Log.i(TAG, "Call03 updated!");
				}

				if (requestedAction.equals(CANCELCALL)){
					AppWidgetManager appWidgetMan = AppWidgetManager.getInstance(this);
					RemoteViews views = new RemoteViews(this.getPackageName(),R.layout.widgetlayout);
					//views.setTextViewText(R.id.widgetText,"");
					appWidgetMan.updateAppWidget(widgetId, views);
				}
			}
		}
	}
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
    @Override
	public void onLocationChanged(Location pLoc) {
   	
	} 	
	
	private void GpsSignalLost(){
		
	    	    	
	}

	@Override
	public void onGpsStatusChanged(int pStatus) {
		switch (pStatus) {
			case LocationProvider.OUT_OF_SERVICE:
				Log.v(TAG, "GPS Status Changed: Out of Service");
				GpsSignalLost();			
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.v(TAG, "GPS Status Changed: Temporarily Unavailable");
				GpsSignalLost();			
				break;
			case LocationProvider.AVAILABLE:
				Log.v(TAG, "GPS Status Changed: Available");				
				break;
		}		
	}
	
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			Log.v(TAG, "Status Changed: Out of Service");
			GpsSignalLost();			
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.v(TAG, "Status Changed: Temporarily Unavailable");
			GpsSignalLost();			
			break;
		case LocationProvider.AVAILABLE:
			Log.v(TAG, "Status Changed: Available");				
			break;
		}
	}
	
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, TAG+": GPS disabled",Toast.LENGTH_SHORT).show(); 
		Log.v(TAG, "GPS disabled");
		/*
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
		*/		
	}
	

}
