package com.fbytes.call03;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



public class Call4Help extends Activity implements LocationListener, GpsStatus.Listener, TextToSpeech.OnInitListener{

	private static String TAG="Call4Help";
	private static TextView CurrentLocAccuracy;
	private static TextView CurrentLocText;
	private LocationManager LocationManager=null;
	Location gpsLocation = null;
	Location networkLocation = null;	
	boolean downloadAGPS;
	private ImageView chooseAddress=null;

	//ArrayList<String> addressesTextList;
	ArrayList<AddressRec> addresses;
	private static Button makeCallButton;
	private static Button cancelHelpRequestButton;
	private static EditText actionsLog;

	private static int MAX_REVERSEGEO_ENTRIES=3;
	private static float LOC_AREA_RADIUS=0.0001f;

	Vibrator vibrator;

	ArrayAdapter<CharSequence> addressAdapter=null;
	static Type AddressRecType=new TypeToken<AddressRec>() {}.getType();

	private Handler mHandler;	
	// UI handler codes.
	public static final int UPDATE_ADDRESS = 1;
	public static final int UPDATE_LATLNG = 2;

	private static final int TEN_SECONDS = 10000;
	private static final int TEN_METERS = 10;
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private static int SMS_MESSAGE_LENGTH_LIMIT=50;

	private static int hookNum;
	private boolean isPlayingAudio;
	private boolean isCalling;
	MediaPlayer mediaPlay;

	int REQCODE_SELECTADDRESS=1;

	public static final String CONFIG_NAME = "Call03Config";
	public static SharedPreferences config;
	public static SharedPreferences.Editor configEditor;
	public static Gson gsonCompact;	

	static Type PhonesListType=new TypeToken<List<ContactRec>>() {}.getType();
	public static List<ContactRec> PhonesToSMS;
	static boolean LocOnly; // true until reverse geocoding performed
	static String HelpRequestMessage;
	private String audioFileName;
	int voiceCallPos;

	MailSender mailSender;

	private Location currentBestLocation;

	CallBroadcastReceiver callBroadcastReceiver;
	TelephonyManager telManager;
	PhoneListener callListener;

	public static TextToSpeech Tts=null;
	static boolean TTSInitialized=false;

	private void SendSMS(int pContactId){
		String phoneNumber=PhonesToSMS.get(pContactId).getPhone();

		String tHelpMessageFinal=GetFinalMessage();		
		Log.v("Message",tHelpMessageFinal);

		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> smsMessages=new ArrayList<String>();
		//ArrayList<PendingIntent> sentIntents=new ArrayList<PendingIntent>();
		//ArrayList<PendingIntent> deliveryIntents=new ArrayList<PendingIntent>();
		smsMessages=sms.divideMessage(tHelpMessageFinal);
		sms.sendMultipartTextMessage(phoneNumber, null, smsMessages, null, null);  
		AddToActionLog("SMS=>"+phoneNumber);
	}


	String GetFinalMessage(){
		String patternAddress = "@loc";
		String selectedAddress=CurrentLocText.getText().toString();
		String tHelpMessageFinal=HelpRequestMessage.replaceAll(patternAddress, selectedAddress);		
		String patternCoordinates = "@coord";
		String currentBestLocationStr=SubstBestLocation("lat=@latt,long=@long");
		tHelpMessageFinal=tHelpMessageFinal.replaceAll(patternCoordinates, currentBestLocationStr);
		return(tHelpMessageFinal);
	}

	private void MakeVoiceCalls(){

		callListener = new PhoneListener();

		telManager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

		voiceCallPos=0;
		MakeNextVoiceCall();
	}


	void MakeNextVoiceCall(){
		if (voiceCallPos>=PhonesToSMS.size()){
			isCalling=false;
			telManager.listen(callListener, PhoneStateListener.LISTEN_NONE);
			return;
		}

		for (int i=voiceCallPos; i<PhonesToSMS.size();i++){
			if (PhonesToSMS.get(i).getOption(0)){
				voiceCallPos=i;
				Log.d(TAG,"Calling to "+PhonesToSMS.get(voiceCallPos).getPhone());		

				String number = "tel:" + PhonesToSMS.get(voiceCallPos).getPhone();
				Intent callIntent = new Intent(Intent.ACTION_CALL , Uri.parse(number));
				//callIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, PhonesToSMS.get(pContactId).getPhone());
				AddToActionLog(getString(R.string.Call4Help_Actions_VoiceCall)+" "+PhonesToSMS.get(voiceCallPos).getPhone());
				startActivity(callIntent);		
				break;
			}
		}
	}


	private class PhoneListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state){
			case TelephonyManager.CALL_STATE_RINGING:
				Log.i(TAG, "RINGING, number: " + incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (!isCalling){
					isCalling=true;
				}
				hookNum++;

				if (!isPlayingAudio){
					AudioManager audioManager;
					audioManager=(AudioManager) getSystemService(AUDIO_SERVICE);
					audioManager.setSpeakerphoneOn(true);
					audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, 0);

					if (!audioFileName.equals("") && audioFileName!=null){
						try {							
							mediaPlay.setDataSource(audioFileName);
							mediaPlay.prepare();
							mediaPlay.start();
							mediaPlay.setOnCompletionListener(OnMediaPlayFinished);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
					}
					else{
						String tHelpMessageFinal=GetFinalMessage();	
						Say(tHelpMessageFinal,TextToSpeech.QUEUE_FLUSH);
					}
					isPlayingAudio=true;
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				hookNum=0;
				if (isCalling){
					voiceCallPos++;
					MakeNextVoiceCall();
				}
				StopAudio();
				break;
			}
		}
	}

	OnCompletionListener OnMediaPlayFinished=new OnCompletionListener(){

		@Override
		public void onCompletion(MediaPlayer arg0) {
			// TODO Auto-generated method stub

		}

	};


	private void SendEmail(int pContactId){

		String emailRecipient=PhonesToSMS.get(pContactId).getEmail();

		try {
			AddToActionLog("Email=>"+emailRecipient);

			String mailBody=GetFinalMessage();			
			if (!LocOnly){
				String selectedAddress=CurrentLocText.getText().toString();
				int addressID=GetAddressIdByText(selectedAddress);
				mailBody+="\n\n"+SubstBestLocation(addresses.get(addressID).getMapLink());				
			}

			mailSender.sendMail(mailBody,emailRecipient);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	LocationListener locListener=new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
Log.d(TAG,"LOCATION CHANGED Provider="+location.getProvider());
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.call_for_help);

		config = getSharedPreferences(CONFIG_NAME, 0);
		configEditor = config.edit();  
		gsonCompact = new Gson();
		Tts = new TextToSpeech(this,this);

		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
		 .permitNetwork()
	     .build();
		StrictMode.setThreadPolicy(policy);
		//StrictMode.enableDefaults();
		
		mailSender=new MailSender();

		LocOnly=true;

		hookNum=0;
		voiceCallPos=0;
		isPlayingAudio=false;
		isCalling=false;
		mediaPlay = new MediaPlayer();
		callBroadcastReceiver = new CallBroadcastReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(callBroadcastReceiver, filter);

		/*        
        Phone PhoneActive = PhoneFactory.getDefaultPhone();
        PhoneActive.registerForLineControlInfo(h, what, obj)
		 */        

		telManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);

		String PhonesInConfig=config.getString("PhoneToSMS","");
		PhonesToSMS=gsonCompact.fromJson(PhonesInConfig, PhonesListType);  
		HelpRequestMessage=config.getString("TextToSpeech", "");
		audioFileName=config.getString("AudioFile", "");

		addresses=new ArrayList<AddressRec>();

		// Start the vibration
		long[] pattern = {300,300,300,300,300,300,300,300,300,300};
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(pattern, -1);

		actionsLog=(EditText) findViewById(R.id.txtActionsLog);
		actionsLog.setFocusable(false);

		CurrentLocAccuracy=(TextView) findViewById(R.id.textLocAccValue);
		CurrentLocText=(TextView) findViewById(R.id.textLocText);
		CurrentLocText.setOnClickListener(ChooseAddressListener);
		chooseAddress=(ImageView) findViewById(R.id.chooseAddress);
		chooseAddress.setOnClickListener(ChooseAddressListener);
		
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_ADDRESS:
					String newAddressStr=(String) msg.obj;
					AddressRec newAddress=gsonCompact.fromJson(newAddressStr, AddressRec.class);  

					boolean tDuplicate=false;
					newAddress.setAddress(Tools.AddressToShortForm(newAddress.getAddress()));
					
					for (int tDupCheck=0; tDupCheck<addresses.size();tDupCheck++){
						if ( addresses.get(tDupCheck).getAddress().equals(newAddress.getAddress()) ){
							tDuplicate=true;
							break;
						}
					}
					if (!tDuplicate){
						addresses.add(newAddress);						
						LocOnly=false;
						Collections.sort(addresses, new Comparator<AddressRec>() {
							@Override
							public int compare(AddressRec s1, AddressRec s2) {
								if (s1.getAccuracy()>s2.getAccuracy())
									return(-1);
								else{
									if (s1.getAccuracy()==s2.getAccuracy()){
										if (s1.getAddress().length()>s2.getAddress().length())
											return(-1);
										else
											return(1);
									}
								}
								return(1);
							}
						});						
						CurrentLocText.setText(addresses.get(0).getAddress());
					}
					break;
				case UPDATE_LATLNG:
					//mLatLng.setText((String) msg.obj);
					break;
				}
			}
		};

/*		
	    LocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy( Criteria.ACCURACY_COARSE );
	    String providerCoarse = LocationManager.getBestProvider( criteria, true );
	    criteria.setAccuracy( Criteria.ACCURACY_FINE );
	    String providerFine = LocationManager.getBestProvider( criteria, true );

	    if ( providerCoarse==null || providerFine==null) {
	        Log.e( TAG, "No location provider found!" );
	        return;
	    }
	    
	    LocationManager.getLastKnownLocation(providerCoarse);	    
	    LocationManager.requestLocationUpdates(providerCoarse,500,3,locListener);
	    LocationManager.requestLocationUpdates(providerFine,500,3,locListener);
	    LocationManager.getLastKnownLocation(providerFine);
*/	    
		
		
		
		
		
		
		
		LocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		LocationManager.addGpsStatusListener(this);        
		//LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0.01f, this);  
		gpsLocation = requestUpdatesFromProvider(
				LocationManager.GPS_PROVIDER, R.string.not_support_gps);
		networkLocation = requestUpdatesFromProvider(
				LocationManager.NETWORK_PROVIDER, R.string.not_support_network);
				

		//downloadAGPS=Call03Activity.config.getBoolean("DownloadAGPS", true);

		cancelHelpRequestButton=(Button)findViewById(R.id.cancelHelpRequest);
		cancelHelpRequestButton.setOnClickListener(cancelHelpRequestListener);

		makeCallButton=(Button)findViewById(R.id.makeCall);
		makeCallButton.setOnClickListener(makeCallListener);


		AppWidgetManager appWidgetManager= AppWidgetManager.getInstance(getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(), Call03WidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.d(TAG,"Widgets installed: "+allWidgetIds.length);

	}

	OnClickListener makeCallListener=new OnClickListener(){
		@Override
		public void onClick(View v) {
			if (PhonesToSMS.size()==0)
				return;
			for (int i=0; i<PhonesToSMS.size();i++){
				if (PhonesToSMS.get(i).getOption(1))
					SendSMS(i);								
			}
			for (int i=0; i<PhonesToSMS.size();i++){
				if (PhonesToSMS.get(i).getOption(2))
					SendEmail(i);
			}
			MakeVoiceCalls();				
		}			
	};	

	OnClickListener cancelHelpRequestListener=new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			Call4Help.this.finish();
		}			
	};

	OnClickListener ChooseAddressListener=new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if (addresses.isEmpty())
				return;
			Intent intent = new Intent(Call4Help.this, Addresses.class);

			String[] addressesTexts= new String[addresses.size()];
			for (int i=0; i<addresses.size();i++){
				addressesTexts[i]=addresses.get(i).getAddress();
			}

			Bundle bunleAddresses = new Bundle();
			bunleAddresses.putStringArray("addresses", addressesTexts);
			intent.putExtras(bunleAddresses); 
			startActivityForResult(intent,REQCODE_SELECTADDRESS);
		}
	};

	@Override
	protected void onDestroy(){
		LocationManager.removeUpdates(this);
		StopAudio();
		unregisterReceiver(callBroadcastReceiver);	

		if (Tts != null) {
			Tts.stop();
			Tts.shutdown();
		}

		super.onDestroy();
	}


	void StopAudio(){
		if (isPlayingAudio){
			mediaPlay.stop();
			isPlayingAudio=false;
		}		
	}

	@Override
	protected void onStop() {				
		super.onStop();
	}	


	public static void Say(String pText,int pMode){
		Tts.speak(pText, pMode, null);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode==REQCODE_SELECTADDRESS && data!=null){
			String tSelectedAddress = data.getStringExtra("ADDRESS");
			CurrentLocText.setText(tSelectedAddress);
		}
	};	


	private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
		Location location = null;
		if (LocationManager.isProviderEnabled(provider)) {
			LocationManager.requestLocationUpdates(provider, TEN_SECONDS, TEN_METERS, this);
			location = LocationManager.getLastKnownLocation(provider);
		} else {
			Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
		}
		return location;
	}



	@Override
	public void onLocationChanged(Location pLoc) {

		if (isBetterLocation(pLoc)){
			currentBestLocation=pLoc;
		}
		else{
			return;
		}

		String tVal=String.format("%d", (int) pLoc.getAccuracy());		
		CurrentLocAccuracy.setText(tVal);

		String tNewLogStr="Prov:"+pLoc.getProvider();
		tNewLogStr=tNewLogStr+String.format(" Acc:%d", (int) pLoc.getAccuracy());		
		AddToActionLog(tNewLogStr);

		String tLocStr=String.format("lat=%f,long=%f", pLoc.getLatitude(),pLoc.getLongitude());
		if (LocOnly){
			CurrentLocText.setText(tLocStr);
		}
		Log.d(TAG,"Location data: "+tLocStr+" "+tNewLogStr);

		doReverseGeocoding(pLoc);		
	} 	

	private void doReverseGeocoding(Location pLoc) {
		// Since the geocoding API is synchronous and may take a while.  You don't want to lock
		// up the UI thread.  Invoking reverse geocoding in an AsyncTask.


		//(new ReverseGeocodingTask(this)).execute(new Location[] {pLoc});		
		(new GeoCoderGoogleAndroid(this,mHandler,UPDATE_ADDRESS)).execute(new Location[] {pLoc});

		(new GeoCoderYandex(this,mHandler,UPDATE_ADDRESS)).execute(new Location[] {pLoc});

		float deltaMeters = pLoc.getAccuracy()/10; // 100 meters 
		long equator_circumference = 6371000; //meters
		long polar_circumference = 6356800; //meters
		double rad_lat = (pLoc.getLatitude() * Math.PI / 180);
		double m_per_deg_long = 360.0f / polar_circumference;
		double m_per_deg_lat = 360.0f / (Math.cos(rad_lat) * equator_circumference);
		double deg_diff_long = deltaMeters * m_per_deg_long;  //Number of degrees latitude as you move north/south along the line of longitude
		double deg_diff_lat = deltaMeters * m_per_deg_lat;

		//String tLocStr=String.format("lat=%f,long=%f", pLoc.getLatitude()-deg_diff_lat,pLoc.getLongitude()-deg_diff_long);		
		//Log.d(TAG,"NEW: "+tLocStr);

		Location diffLoc=new Location(pLoc);
		diffLoc.setLatitude(pLoc.getLatitude()-deg_diff_lat);
		diffLoc.setLongitude(pLoc.getLongitude()-deg_diff_long);
		(new GeoCoderGoogleAndroid(this,mHandler,UPDATE_ADDRESS)).execute(new Location[] {diffLoc});

		diffLoc.setLatitude(pLoc.getLatitude()+deg_diff_lat);
		diffLoc.setLongitude(pLoc.getLongitude()+deg_diff_long);
		(new GeoCoderGoogleAndroid(this,mHandler,UPDATE_ADDRESS)).execute(new Location[] {diffLoc});

		diffLoc.setLatitude(pLoc.getLatitude()-deg_diff_lat);
		diffLoc.setLongitude(pLoc.getLongitude()+deg_diff_long);
		(new GeoCoderGoogleAndroid(this,mHandler,UPDATE_ADDRESS)).execute(new Location[] {diffLoc});

		diffLoc.setLatitude(pLoc.getLatitude()+deg_diff_lat);
		diffLoc.setLongitude(pLoc.getLongitude()-deg_diff_long);
		(new GeoCoderGoogleAndroid(this,mHandler,UPDATE_ADDRESS)).execute(new Location[] {diffLoc});	

		//(new GeoCoderGoogle(this,mHandler,UPDATE_ADDRESS)).execute(new Location[] {pLoc});
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


	public boolean isNetworkOnline() {
		boolean tStatus=false;
		try{
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
				tStatus= true;
			}else {
				netInfo = cm.getNetworkInfo(1);
				if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
					tStatus= true;
			}
		}catch(Exception e){
			e.printStackTrace();  
			return false;
		}
		return tStatus;
	}  	


	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}	

	protected boolean isBetterLocation(Location newLocation) {
		if (currentBestLocation == null) {
			return true;
		}

		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isNewer = timeDelta > 0;

		int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),currentBestLocation.getProvider());

		if (isMoreAccurate) {
			return true;
		} 
		else{
			return false;
		}
	}	

	void AddToActionLog(String pNewLine){
		if (actionsLog.getVisibility()==View.GONE)
			actionsLog.setVisibility(View.VISIBLE);
		actionsLog.setText(actionsLog.getText().toString()+pNewLine+"\n");
	}

	// replace @latt to lattitude and @long to longitude
	String SubstBestLocation(String pMask){
		if (currentBestLocation==null)
			return "UNKNOWN";
		String tLatt=new String();
		tLatt=tLatt.valueOf(currentBestLocation.getLatitude());
		String tLong=new String();
		tLong=tLong.valueOf(currentBestLocation.getLongitude());
		String tResult=pMask.replaceAll("@latt",tLatt);
		tResult=tResult.replaceAll("@long", tLong);
		return(tResult);
	}

	int GetAddressIdByText(String pAddressText){
		for (int i=0; i<addresses.size();i++){
			if (addresses.get(i).getAddress().equals(pAddressText))
				return i;
		}
		return -1;
	}


	@Override
	public void onInit(int status) {
		Log.d(TAG,"TTS_onInit  status="+status);
		if (TTSInitialized)
			return;
		if (status == TextToSpeech.SUCCESS) {
			int result = Tts.setLanguage(Locale.getDefault());
			//int result = Tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA ||
					result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e(TAG, "Language is not available.");
			} else {
				Log.d(TAG,"TTS Initialized with result="+String.valueOf(result));
				TTSInitialized=true;
				Tts.setOnUtteranceCompletedListener(OnSpeechSynthFinished);
			}
		} else {
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	OnUtteranceCompletedListener OnSpeechSynthFinished=new OnUtteranceCompletedListener(){

		@Override
		public void onUtteranceCompleted(String arg0) {
			return;
		}

	};	

	

}
