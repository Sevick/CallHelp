package com.fbytes.call03;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;


public class AsyncGeoCoder extends AsyncTask<Location, Void, Void>{

	private String TAG="AsyncGeoCoder";
	private Handler msgHandler;
	private int msgID;
	Context mContext;
	protected String Name;
	public static Gson gsonCompact;	
	String MapLink;

	public AsyncGeoCoder(Context pContext,Handler pHandler,int pMessage) {
		super();
		mContext = pContext;
		msgHandler=pHandler;
		msgID=pMessage;
		gsonCompact = new Gson();
		MapLink="";
	}
	
	@Override
	protected Void doInBackground(Location... params) {
		Location loc = params[0];
		return null;
	}

	void StartReverseCoding(Location pLoc){};
	
	void PostAddress(String pAddress,Location pLoc){
		AddressRec newAddress=new AddressRec();
		newAddress.setAddress(pAddress);
		newAddress.setGeocodeProvider(Name);
		newAddress.setLoc(pLoc);
		newAddress.setMapLink(MapLink);

		String AddressStr=gsonCompact.toJson(newAddress);  	
		Message.obtain(msgHandler, msgID, AddressStr).sendToTarget();
	}

}
