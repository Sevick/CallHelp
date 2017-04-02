package com.fbytes.call03;

import java.io.IOException;
import java.util.Locale;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SMS extends FragmentActivity  {

	static String TAG="SMS";
	static String MsgToSpeech;
	static EditText EditTextToSpeech;
	static CheckBox CheckAddLocToVoiceMessage;
	static Spinner spinnerVoiceCallType;
	static ArrayAdapter spinnerVoiceCallTypeAdapter;
	static LinearLayout LayoutAudioFileOptions;
	static EditText EditSMTPSenderEmail;
	static EditText EditSMTPServer;
	static EditText EditSMTPUser;
	static EditText EditSMTPPassword;
	static EditText EditSMTPEmailSubject;
	static CheckBox CheckSMTPUseSSL;
	static String SMTPSenderEmail;
	static String SMTPServer;
	static String SMTPUser;
	static String SMTPPass;
	static boolean SMTPUseSSL;
	static String SMTPEmailSubject;
	static ImageButton pickAudio;
	static ImageButton playAudio;
	static EditText audioFile;
	static MediaPlayer mediaPlay;
	static boolean isPlayingVoiceMessage;
	static boolean isSayingLocText;
	static String audioFileName;
	static boolean addLocToVoiceMessage;

	public static TextToSpeech Tts=null;
	static boolean TTSInitialized=false;
	static Context TtsContext;


	static int REQCODE_SELECTAUDIO=1;

	public static class SMSFragment extends Fragment implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener{
		static View v;
		IntentFilter SpeechEndFilter;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			SpeechEndFilter = new IntentFilter(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
			getActivity().getApplicationContext().registerReceiver(SpeechEndReceiver, SpeechEndFilter);
		}

		@Override
		public void onDestroy(){
			getActivity().getApplicationContext().unregisterReceiver(SpeechEndReceiver);

			super.onDestroy();
		}

		BroadcastReceiver SpeechEndReceiver = new BroadcastReceiver(){
			public void onReceive(Context p1, Intent p2){
				if (p2.getAction().equals(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED) && Tts != null){
					isSayingLocText=false;
					isPlayingVoiceMessage=false;	
					playAudio.setImageDrawable(getResources().getDrawable(R.drawable.av_play));		
					Log.d(TAG,"Speech completed");
				}
			}
		};		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			v = inflater.inflate(R.layout.sms, container, false);

			isPlayingVoiceMessage=false;
			isSayingLocText=false;
			mediaPlay = new MediaPlayer();

			//Tts = new TextToSpeech(this.getParent(),this);

			Tts = new TextToSpeech(getActivity().getApplicationContext(),this);
			
			
			LayoutAudioFileOptions=(LinearLayout) v.findViewById(R.id.layoutAudioFileOptions);			
			
			spinnerVoiceCallType = (Spinner) v.findViewById(R.id.spinVoiceCallType);
			spinnerVoiceCallTypeAdapter=(ArrayAdapter) spinnerVoiceCallType.getAdapter();
			int voiceCallType=Call03Activity.config.getInt("VoiceCallType",0);   
			spinnerVoiceCallType.setOnItemSelectedListener(VoiceCallTypeChangeListener);
			spinnerVoiceCallType.setSelection(voiceCallType);

			audioFile=(EditText)v.findViewById(R.id.editAudioFile);
			audioFileName=Call03Activity.config.getString("AudioFile", "");    
			audioFile.setText(audioFileName);
			//audioFile.setOnFocusChangeListener(FocusChangeListener);
			audioFile.addTextChangedListener(OnTextChangeListener);

			pickAudio=(ImageButton) v.findViewById(R.id.btnPickAudio);
			pickAudio.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setType("audio/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent,"Select Audio "), REQCODE_SELECTAUDIO);
				};
			});


			playAudio=(ImageButton) v.findViewById(R.id.btnPlayAudio);
			playAudio.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if (!isPlayingVoiceMessage){

						if (audioFileName!=null & !audioFileName.equals("")){
							try {
								Log.d(TAG,"Playing "+audioFileName);
								mediaPlay.setDataSource(audioFileName);
								mediaPlay.prepare();
								mediaPlay.setOnCompletionListener(OnMediaPlayFinished);
								mediaPlay.start();
								isPlayingVoiceMessage=true;
								isSayingLocText=false;
								playAudio.setImageDrawable(getResources().getDrawable(R.drawable.av_stop));							
								// TODO Auto-generated catch block	
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
						else{
							isPlayingVoiceMessage=true;
							isSayingLocText=true;
							SayFullText();
							SayLocText();
							playAudio.setImageDrawable(getResources().getDrawable(R.drawable.av_stop));
						}
					}
					else{
						if (isSayingLocText){
							if (Tts.isSpeaking()){
								Tts.stop();
							}
						}
						else{
							mediaPlay.stop();
						}
						isSayingLocText=false;
						isPlayingVoiceMessage=false;
						playAudio.setImageDrawable(getResources().getDrawable(R.drawable.av_play));					
					}
				}

			});


			MsgToSpeech=Call03Activity.config.getString("TextToSpeech", "");    
			if (MsgToSpeech==null || MsgToSpeech.equals("")){
				MsgToSpeech=getString(R.string.defaultMessageText);
			}

			EditTextToSpeech=(EditText) v.findViewById(R.id.editTextToSpeech);
			EditTextToSpeech.setText(MsgToSpeech);
			EditTextToSpeech.addTextChangedListener(OnTextChangeListener);

			CheckAddLocToVoiceMessage=(CheckBox)v.findViewById(R.id.checkAddLocToVoiceMessage);
			addLocToVoiceMessage=Call03Activity.config.getBoolean("AddLocToVoiceMessage", true);
			CheckAddLocToVoiceMessage.setChecked(addLocToVoiceMessage);
			CheckAddLocToVoiceMessage.setOnCheckedChangeListener(checkStateListener);

			//		String MessageText=Call03Activity.config.getString("MessageText","");

			EditSMTPSenderEmail=(EditText)v.findViewById(R.id.editSMTPSenderEmail);			
			SMTPSenderEmail=Call03Activity.config.getString("SMTPSenderEmail", "");   
			EditSMTPSenderEmail.setText(SMTPSenderEmail);
			EditSMTPSenderEmail.addTextChangedListener(OnTextChangeListener);

			EditSMTPServer=(EditText)v.findViewById(R.id.editSMTPServer);
			SMTPServer=Call03Activity.config.getString("SMTPServer","");
			EditSMTPServer.setText(SMTPServer);
			EditSMTPServer.addTextChangedListener(OnTextChangeListener);

			EditSMTPUser=(EditText)v.findViewById(R.id.editSMTPUsername);
			SMTPUser=Call03Activity.config.getString("SMTPUser","");
			EditSMTPUser.setText(SMTPUser);
			EditSMTPUser.addTextChangedListener(OnTextChangeListener);

			EditSMTPPassword=(EditText)v.findViewById(R.id.editSMTPPassword);
			SMTPPass=Call03Activity.config.getString("SMTPPass","");
			EditSMTPPassword.setText(SMTPPass);
			EditSMTPPassword.addTextChangedListener(OnTextChangeListener);

			CheckSMTPUseSSL=(CheckBox)v.findViewById(R.id.checkSMTPUseSSL);
			SMTPUseSSL=Call03Activity.config.getBoolean("SMTPUseSSL", false);
			CheckSMTPUseSSL.setChecked(SMTPUseSSL);
			CheckSMTPUseSSL.setOnCheckedChangeListener(checkStateListener);

			EditSMTPEmailSubject=(EditText)v.findViewById(R.id.editSMTPEmailSubject);
			SMTPEmailSubject=Call03Activity.config.getString("SMTPEmailSubject",getString(R.string.MessageTab_SMTPEmailSubject_DefaultText));
			EditSMTPEmailSubject.setText(SMTPEmailSubject);
			EditSMTPEmailSubject.addTextChangedListener(OnTextChangeListener);      

			return v;
		}	

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (resultCode == RESULT_OK && requestCode==REQCODE_SELECTAUDIO){
				Call03Activity.OnChanges();
				Uri uri = data.getData();
				audioFileName = uri.getPath();
				audioFile.setText(audioFileName);			

			}
		};	

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
					Log.v(TAG,"TTS Initialized with result="+String.valueOf(result));
					TTSInitialized=true;
					//Tts.setOnUtteranceProgressListener(OnSpeechProgress);
					Tts.setOnUtteranceCompletedListener(this);
				}
			} else {
				Log.e(TAG, "Could not initialize TextToSpeech.");
			}
		}

		@Override
		public void onUtteranceCompleted(String arg0) {
			isSayingLocText=false;
			isPlayingVoiceMessage=false;	
			playAudio.setImageDrawable(getResources().getDrawable(R.drawable.av_play));		
			Log.d(TAG,"Speech completed");
		}



		OnCompletionListener OnMediaPlayFinished=new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer arg0) {
				SayLocText();
			}

		};    	
	}	
	/*	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sms, container, false);
        return v;
    }
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.sms);

	};




	static View.OnFocusChangeListener FocusChangeListener=new View.OnFocusChangeListener(){
		@Override
		public void onFocusChange(View v, boolean hasFocus)
		{
			if (!hasFocus){
				SaveConfig();
			}
		}
	};


	static void SaveConfig(){
		try{
			MsgToSpeech=EditTextToSpeech.getText().toString();
			Call03Activity.configEditor.putString("TextToSpeech", MsgToSpeech);		
			addLocToVoiceMessage=CheckAddLocToVoiceMessage.isChecked();
			Call03Activity.configEditor.putBoolean("AddLocToVoiceMessage",addLocToVoiceMessage);

			SMTPSenderEmail=EditSMTPSenderEmail.getText().toString();
			Call03Activity.configEditor.putString("SMTPSenderEmail", SMTPSenderEmail);
			SMTPServer=EditSMTPServer.getText().toString();
			Call03Activity.configEditor.putString("SMTPServer", SMTPServer);
			SMTPUser=EditSMTPUser.getText().toString();
			Call03Activity.configEditor.putString("SMTPUser", SMTPUser);
			SMTPPass=EditSMTPPassword.getText().toString();
			Call03Activity.configEditor.putString("SMTPPass", SMTPPass);
			SMTPUseSSL=CheckSMTPUseSSL.isChecked();
			Call03Activity.configEditor.putBoolean("SMTPUseSSL", SMTPUseSSL);
			SMTPEmailSubject=EditSMTPEmailSubject.getText().toString();
			Call03Activity.configEditor.putString("SMTPEmailSubject",SMTPEmailSubject);

			audioFileName=audioFile.getText().toString();		
			Call03Activity.configEditor.putString("AudioFile", audioFileName);
			Call03Activity.configEditor.commit();
		}
		catch(Exception e){
			// View not created
		}

	}

	public static void Say(String pText,int pMode){
		if (TTSInitialized)
			Tts.speak(pText, pMode, null);
	}


	//TextToSpeech.OnInitListener TtsInitListener=new TextToSpeech.OnInitListener{


	//};

	static String GetFinalMessage(){
		String patternAddress = "@loc";
		String selectedAddress="";
		String HelpRequestMessage=EditTextToSpeech.getText().toString();
		String tHelpMessageFinal=HelpRequestMessage.replaceAll(patternAddress, selectedAddress);		
		String patternCoordinates = "@coord";
		String currentBestLocationStr=SubstBestLocation("lat=@latt,long=@long");
		tHelpMessageFinal=tHelpMessageFinal.replaceAll(patternCoordinates, currentBestLocationStr);
		return(tHelpMessageFinal);
	}

	static String SubstBestLocation(String pMask){
		Location tFakeLoc=new Location("fake");
		tFakeLoc.setLongitude(55.550456f);
		tFakeLoc.setLatitude(37.3345523);

		String tLatt=new String();
		tLatt=tLatt.valueOf(tFakeLoc.getLatitude());
		String tLong=new String();
		tLong=tLong.valueOf(tFakeLoc.getLongitude());
		String tResult=pMask.replaceAll("@latt",tLatt);
		tResult=tResult.replaceAll("@long", tLong);
		return(tResult);
	}

	static void SayLocText(){
		/*		
		isSayingLocText=true;
		String tTestMsg=GetFinalMessage();
		Say(tTestMsg,TextToSpeech.QUEUE_FLUSH);
		 */		
	}

	static void SayFullText(){
		isSayingLocText=true;
		String tTestMsg=GetFinalMessage();
		Say(tTestMsg,TextToSpeech.QUEUE_FLUSH);		
	}

	static OnCheckedChangeListener checkStateListener=new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			//Call03Activity.OnChanges();
			SaveConfig();
		}		
	};
	
	public static TextWatcher OnTextChangeListener=new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {
			SaveConfig();
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
			
		}
		
	};	
	
	public static OnItemSelectedListener VoiceCallTypeChangeListener=new OnItemSelectedListener(){

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pPosition,
				long arg3) {
			//Call03Activity.OnChanges();
			int curSelection=spinnerVoiceCallType.getSelectedItemPosition();
			Call03Activity.configEditor.putInt("VoiceCallType",curSelection);
			Call03Activity.configEditor.commit();  		
			if (curSelection==0){
				LayoutAudioFileOptions.setVisibility(View.GONE);
			}
			else{
				LayoutAudioFileOptions.setVisibility(View.VISIBLE);	
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};	
}
