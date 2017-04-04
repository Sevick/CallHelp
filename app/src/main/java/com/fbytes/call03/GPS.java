package com.fbytes.call03;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
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

import java.util.Locale;

public class GPS extends FragmentActivity {

    static Spinner spinnerLocMinAccuracy;
    static ArrayAdapter spinnerLocMinAccuracyAdapter;
    static CheckBox downloadAGPS;


    static Spinner spinnerLocMapProvider;
    static ArrayAdapter spinnerLocMapProviderAdapter;
    static boolean ViewCreated;

    static Spinner spinnerLanguage;
    static ArrayAdapter spinnerLanguageAdapter;
    private static Locale myLocale;

    private static String actualLang;

    public static class GPSFragment extends Fragment {


        String TAG = "GPSFragment";


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

            ViewCreated = true;

            spinnerLocMinAccuracy = (Spinner) v.findViewById(R.id.spinMinAccuracy);
            spinnerLocMinAccuracyAdapter = (ArrayAdapter) spinnerLocMinAccuracy.getAdapter();
            String LocMinAccuracyStr = Call03Activity.config.getString("LocMinAccuracy", "");
            spinnerLocMinAccuracy.setSelection(spinnerLocMinAccuracyAdapter.getPosition(LocMinAccuracyStr));
            spinnerLocMinAccuracy.setOnItemSelectedListener(AccuracyChange);

            downloadAGPS = (CheckBox) v.findViewById(R.id.checkDownloadAGPS);
            boolean tDownloadAGPS = Call03Activity.config.getBoolean("DownloadAGPS", true);
            downloadAGPS.setChecked(tDownloadAGPS);
            downloadAGPS.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean pCheckStatus) {

                }
            });


            spinnerLocMapProvider = (Spinner) v.findViewById(R.id.spinMapProvider);
            spinnerLocMapProviderAdapter = (ArrayAdapter) spinnerLocMapProvider.getAdapter();
            String LocMapProviderStr = Call03Activity.config.getString("LocMapProvider", "");
            spinnerLocMapProvider.setSelection(spinnerLocMapProviderAdapter.getPosition(LocMapProviderStr));
            spinnerLocMapProvider.setOnItemSelectedListener(ProviderChange);


            spinnerLanguage = (Spinner) v.findViewById(R.id.spinLanguage);
            spinnerLanguageAdapter = (ArrayAdapter) spinnerLanguage.getAdapter();

            Log.d("FISH", "  *** ru-ru position - " + String.valueOf(spinnerLanguageAdapter.getPosition("Русский")));
            Log.d("FISH", "  *** ru-en position - " + String.valueOf(spinnerLanguageAdapter.getPosition("Английский")));
            Log.d("FISH", "  *** en-ru position - " + String.valueOf(spinnerLanguageAdapter.getPosition("Russian")));
            Log.d("FISH", "  *** en-en position - " + String.valueOf(spinnerLanguageAdapter.getPosition("English")));
//            Log.d("FISH", "  *** " + spinnerLanguageAdapter.);


            actualLang = Call03Activity.config.getString("Language", "");
            Log.d("FISH", "   ***   GPS onCrView actualLang = " + actualLang);
            Log.d("FISH", "   ***   GPS onCrView actualLang # = " + spinnerLanguageAdapter.getPosition(actualLang));
            spinnerLanguage.setSelection(Call03Activity.APP_LANGUAGES.indexOf( actualLang));
            spinnerLanguage.setOnItemSelectedListener(LanguageChange);



            return v;
        }

        @Override
        public void onDestroyView() {
            super.onDestroy();
            ViewCreated = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.gps);
        ViewCreated = false;
    }

    ;

    public static OnItemSelectedListener AccuracyChange = new OnItemSelectedListener() {

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

    public static OnItemSelectedListener ProviderChange = new OnItemSelectedListener() {

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

    public static OnItemSelectedListener LanguageChange = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int pPosition, long arg3) {

            String lang = Call03Activity.APP_LANGUAGES.get(pPosition);
            Log.d("FISH", "  ***  ***  *** GPS lang = " + lang + "   pos = " + pPosition);
            Log.d("FISH", "  ***  ***  *** GPS acttualLang = " + actualLang);

            if (!actualLang.equals(lang)) {

                Call03Activity.configEditor.putString("Language", lang);
                Call03Activity.configEditor.commit();
                setLocale(arg1.getContext(), lang);
                Log.d("FISH", "  ***  ***  *** GPS in Listener lang 2 SharPr " + lang);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    };

    public static void SaveConfig() {
        try {
            Call03Activity.configEditor.putString("LocMinAccuracy", spinnerLocMinAccuracy.getSelectedItem().toString());
            Call03Activity.configEditor.putString("LocMapProvider", spinnerLocMapProvider.getSelectedItem().toString());

            Call03Activity.configEditor.putString("Language", Call03Activity.APP_LANGUAGES.get(spinnerLanguage.getSelectedItemPosition()));

            Call03Activity.configEditor.putBoolean("DownloadAGPS", downloadAGPS.isChecked());
            Call03Activity.configEditor.commit();
        } catch (Exception e) {
            // View is not created
        }

    }

    public void changeLang(String lang) {
//		if (lang.equalsIgnoreCase(""))	return;
        myLocale = new Locale(lang);
//		saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//		updateTexts();
    }

    public static void setLocale(Context context, String lang) {
        Locale myLocale = new Locale(lang);
        Log.d("FISH", "  ***  ***  *** SetLocale " + lang);

        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(context, Call03Activity.class);
        context.startActivity(refresh);
        ((Activity) context).finish();
    }
}
