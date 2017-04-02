package com.fbytes.call03;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;


public class Addresses extends ListActivity {

	public static String TAG="Addresses";
		
	public static ListView AddressesListView;
	ArrayList<AddressRec> addresses;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addresses);

		AddressesListView = getListView();        
		AddressesListView.setClickable(true);
		
		Bundle bunleAddresses = getIntent().getExtras();
		String[] addressesTexts=bunleAddresses.getStringArray("addresses");       
        AddressesListView.setAdapter(new ArrayAdapter<String>(this,R.layout.address_line, addressesTexts));
	
		AddressesListView.setOnItemLongClickListener(onAddressLongClick);  
		AddressesListView.setOnItemClickListener(onAddressClick);
	};
	
	@Override
	public void onStart(){
		super.onStart();
	}

	private OnItemClickListener onAddressClick = new OnItemClickListener() {
		public void onItemClick(AdapterView parentView, View childView, int position, long id) {
			String tSelectedAddr=(String)AddressesListView.getItemAtPosition(position);
			Intent resultIntent = new Intent();
			resultIntent.putExtra("ADDRESS", tSelectedAddr);
			setResult(Activity.RESULT_OK,resultIntent);
			finish();
		}
	};  

	private OnItemLongClickListener onAddressLongClick = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
			String tSelectedAddr=(String)AddressesListView.getItemAtPosition(position);
			Intent resultIntent = new Intent();
			resultIntent.putExtra("ADDRESS", tSelectedAddr);
			setResult(Activity.RESULT_OK,resultIntent);
			finish();
			return true;
		}
	};
}

	
