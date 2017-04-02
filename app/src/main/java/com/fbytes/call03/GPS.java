package com.fbytes.call03;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

public class GPS extends FragmentActivity {

	static Spinner spinnerLocMinAccuracy;
	static ArrayAdapter spinnerLocMinAccuracyAdapter;
	static CheckBox downloadAGPS;


	static Spinner spinnerLocMapProvider;	
	static ArrayAdapter spinnerLocMapProviderAdapter;
	static boolean ViewCreated;

	public static class GPSFragment extends Fragment {

		String TAG="GPSFragment";


		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);


		}

		/**
		 * The Fragment's UI is just a simple text view showing its
		 * instance number.
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.gps, container, false);

			ViewCreated=true;

			spinnerLocMinAccuracy = (Spinner) v.findViewById(R.id.spinMinAccuracy);
			spinnerLocMinAccuracyAdapter=(ArrayAdapter) spinnerLocMinAccuracy.getAdapter();
			String LocMinAccuracyStr=Call03Activity.config.getString("LocMinAccuracy","");        
			spinnerLocMinAccuracy.setSelection(spinnerLocMinAccuracyAdapter.getPosition(LocMinAccuracyStr));
			spinnerLocMinAccuracy.setOnItemSelectedListener(AccuracyChange);

			downloadAGPS=(CheckBox) v.findViewById(R.id.checkDownloadAGPS);
			boolean tDownloadAGPS=Call03Activity.config.getBoolean("DownloadAGPS", true);
			downloadAGPS.setChecked(tDownloadAGPS);
			downloadAGPS.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean pCheckStatus) {

				}        	
			});


			spinnerLocMapProvider=(Spinner) v.findViewById(R.id.spinMapProvider);
			spinnerLocMapProviderAdapter=(ArrayAdapter) spinnerLocMapProvider.getAdapter();
			String LocMapProviderStr=Call03Activity.config.getString("LocMapProvider",""); 
			spinnerLocMapProvider.setSelection(spinnerLocMapProviderAdapter.getPosition(LocMapProviderStr));
			spinnerLocMapProvider.setOnItemSelectedListener(ProviderChange);            
			return v;
		}		

		@Override
		public void onDestroyView(){
			super.onDestroy();
			ViewCreated=false;
		}
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.gps);
		ViewCreated=false;
	};

	public static OnItemSelectedListener AccuracyChange=new OnItemSelectedListener(){

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pPosition,
				long arg3) {
			//Call03Activity.OnChanges();
			Call03Activity.configEditor.putString("LocMinAccuracy", spinnerLocMinAccuracy.getSelectedItem().toString());
			Call03Activity.configEditor.commit();  			

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};

	public static OnItemSelectedListener ProviderChange=new OnItemSelectedListener(){

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pPosition,
				long arg3) {
			//Call03Activity.OnChanges();
			Call03Activity.configEditor.putString("LocMapProvider", spinnerLocMapProvider.getSelectedItem().toString());
			Call03Activity.configEditor.commit();  			

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};	


	public static void SaveConfig(){
		try{
			Call03Activity.configEditor.putString("LocMinAccuracy", spinnerLocMinAccuracy.getSelectedItem().toString());
			Call03Activity.configEditor.putString("LocMapProvider", spinnerLocMapProvider.getSelectedItem().toString());
			Call03Activity.configEditor.putBoolean("DownloadAGPS",downloadAGPS.isChecked());
		}
		catch(Exception e){
			// View is not created
		}

	}
}
