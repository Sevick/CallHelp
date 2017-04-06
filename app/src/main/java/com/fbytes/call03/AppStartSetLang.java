package com.fbytes.call03;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.fbytes.call03.Call03Activity.CONFIG_NAME;

public class AppStartSetLang extends Application {

    public static final List<String> APP_LANGUAGES = Arrays.asList(new String[]{"en", "ru"});

    public static SharedPreferences config;
    public static SharedPreferences.Editor configEditor;

    @Override
    public void onCreate() {
        super.onCreate();

        config = getSharedPreferences(CONFIG_NAME, 0);
        configEditor = config.edit();
        String actLang = config.getString("Language", "");
        String sysLang = Locale.getDefault().getLanguage();

        if (actLang == null || actLang.equals("")) {
            configEditor.putString("Language", sysLang);
            actLang = sysLang;
        }

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(actLang);
        res.updateConfiguration(conf, dm);
    }

    public static void setLocale(Context context, String lang){
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(lang);
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(context, Call03Activity.class);
        context.startActivity(refresh);
        ((Activity) context).finish();
    }
}
