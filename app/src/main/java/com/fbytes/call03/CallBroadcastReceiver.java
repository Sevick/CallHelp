package com.fbytes.call03;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallBroadcastReceiver extends BroadcastReceiver {
	
	String TAG="CallBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		
Log.d(TAG,"OnReceive");
Log.d(TAG,"Action="+intent.getAction());
		if (!intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL"))
			return;
		
/*		
Log.d(TAG,"OnReceive - passed int check");		
		Bundle bundle = intent.getExtras();
		if(null == bundle)
			return;
		String state = bundle.getString(TelephonyManager.EXTRA_STATE);


		if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
				TelephonyManager.EXTRA_STATE_RINGING)) {

			// Phone number 
			String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

			// Ringing state
			// This code will execute when the phone has an incoming call
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
				TelephonyManager.EXTRA_STATE_IDLE)
				|| intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
						TelephonyManager.EXTRA_STATE_OFFHOOK)) {

			Log.d(TAG,"OFFHOOKSTATE");

			// This code will execute when the call is answered or disconnected
		}
*/

	}
}