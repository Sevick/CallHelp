package com.fbytes.call03;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Call03Activity extends FragmentActivity{
	//extends TabActivity {

    private static String TAG="Call03Activity";
	public static final String CONFIG_NAME = "Call03Config";
	public static SharedPreferences config;
	public static SharedPreferences.Editor configEditor;
	public static Gson gsonCompact;
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	private static boolean HasChanges=false;
	private static Button SaveConfigButton;
	private static Button CancelConfigButton;


	public static void OnChanges(){
		HasChanges=true;
		SaveConfigButton.setEnabled(true);
	}
	
	public void SaveConfig(){
		
		//Intent intent=new Intent();
		//intent.setAction("com.fbytes.call03.SaveConfig");
		//sendBroadcast(intent);
		
		ContactsCL.SaveConfig();
		SMS.SaveConfig();

		GPS.SaveConfig();
		configEditor.commit();
		
		HasChanges=false;
		SaveConfigButton.setEnabled(false);		
	}

	public void CancelConfig(){
		Call03Activity.this.finish();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		config = getSharedPreferences(CONFIG_NAME, 0);
		configEditor = config.edit();  
		gsonCompact = new Gson();

		SaveConfigButton=(Button)findViewById(R.id.btnSaveConfig);
		SaveConfigButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				SaveConfig();
			}			
		});
		CancelConfigButton=(Button)findViewById(R.id.btnConfigCancel);
		CancelConfigButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				CancelConfig();
			}			
		});
		
		final TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();

		mViewPager = (ViewPager)findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, tabHost, mViewPager);

		mTabsAdapter.addTab(tabHost.newTabSpec("tab1").setIndicator(getResources().getString(R.string.Tabs_Contacts),getResources().getDrawable(R.drawable.contacts)),
				ContactsCL.ContactsFragment.class, null);
		
		mTabsAdapter.addTab(tabHost.newTabSpec("tab2").setIndicator(getResources().getString(R.string.Tabs_Message),getResources().getDrawable(R.drawable.message)),
				SMS.SMSFragment.class, null);
		
		mTabsAdapter.addTab(tabHost.newTabSpec("tab3").setIndicator(getResources().getString(R.string.Tabs_GPS),getResources().getDrawable(R.drawable.email)),
				GPS.GPSFragment.class, null);

		AppWidgetManager appWidgetManager= AppWidgetManager.getInstance(getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(), Call03WidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.d(TAG,"Widgets installed: "+allWidgetIds.length);



	}


	private OnClickListener SaveConfigListener = new OnClickListener() {
		public void onClick(View v) {
			/*
        	TextToSpeech=EditTextToSpeech.getText().toString();
            configEditor.putString("TextToSpeech", TextToSpeech);
            configEditor.commit();   
            setResult(RESULT_OK);
            finish();
			 */            
		}
	};


	public static class TabsAdapter extends FragmentPagerAdapter
	implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(), info.args);
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			// Unfortunately when TabHost changes the current tab, it kindly
			// also takes care of putting focus on it when not in touch mode.
			// The jerk.
			// This hack tries to prevent this from pulling focus out of our
			// ViewPager.
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
	}


	
}