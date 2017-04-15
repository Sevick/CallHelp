package com.fbytes.call03;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;



public class ContactsActivity extends FragmentActivity  {

	private static String TAG="ContactsActivity";
	private static ListView PhonesToSMSListView;
	private static FloatingActionButton addContactButton;
	private static LinearLayout ContactsLayout;
	private static LinearLayout ProgressLayout;
	//private static ProgressBar ProgressCircle;
	static Type PhonesListType=new TypeToken<List<ContactRec>>() {}.getType();
	public static List<ContactRec> PhonesToSMS;
	public static String PhonesInConfig;


	public static int REQCODE_GETCONTACT=1;

	public static View v;

	public static class ContactsFragment extends Fragment  {
		public static EfficientAdapter contactsToCall;

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
			ContactsActivity.v = inflater.inflate(R.layout.contacts, container, false);

			ContactsActivity.PhonesToSMSListView = (ListView) ContactsActivity.v.findViewById(R.id.listContacts);
			ContactsActivity.PhonesToSMSListView.setClickable(true);

			ContactsLayout = (LinearLayout) ContactsActivity.v.findViewById(R.id.layoutContacts);
			ProgressLayout = (LinearLayout) ContactsActivity.v.findViewById(R.id.progressLayout);


/*
			ContactsActivity.UnderlayingView=(TextView) ContactsActivity.v.findViewById(R.id.textUnderContacts);
			ContactsActivity.UnderlayingView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {			
					GetNewContact();  
				}

			});
*/

			ContactsActivity.addContactButton=(FloatingActionButton) ContactsActivity.v.findViewById(R.id.addContact);
			ContactsActivity.addContactButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					GetNewContact();
				}

			});



			PhonesInConfig=Call03Activity.config.getString("PhoneToSMS","");
			Log.d(TAG,"Phones in config="+ ContactsActivity.PhonesInConfig);

			if (!ContactsActivity.PhonesInConfig.equals("")){
				ContactsActivity.PhonesToSMS=Call03Activity.gsonCompact.fromJson(PhonesInConfig, PhonesListType);
			}
			else{
				ContactsActivity.PhonesToSMS=new ArrayList<ContactRec>();
			}

			Log.d(TAG,"PhonesToSMS="+ ContactsActivity.PhonesToSMS);


			//setContentView(R.layout.contacts);
			contactsToCall=new EfficientAdapter(v.getContext());
			ContactsActivity.PhonesToSMSListView.setAdapter(contactsToCall);

			ContactsActivity.PhonesToSMSListView.setOnItemLongClickListener(RemovePhoneFromSMS);
			ContactsActivity.PhonesToSMSListView.setOnItemClickListener(AddPhoneToSMS);

			return ContactsActivity.v;
		}	



		public OnItemClickListener AddPhoneToSMS = new OnItemClickListener() {
			public void onItemClick(AdapterView parentView, View childView, int position, long id) {
				GetNewContact();

			}
		};  

		public OnItemLongClickListener RemovePhoneFromSMS = new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
				ContactsActivity.PhonesToSMS.remove(ContactsActivity.PhonesToSMSListView.getItemAtPosition(position));
				contactsToCall.notifyDataSetChanged();
				//ContactsActivity.PhonesToSMSListView.invalidateViews();
				//Call03Activity.OnChanges();
				SaveConfig();
				return(true);
			}
		};	

		void GetNewContact(){
			ContactsLayout.setVisibility(View.GONE);
			ProgressLayout.setVisibility(View.VISIBLE);

			Intent intGetContact = new Intent(Intent.ACTION_PICK,Contacts.CONTENT_URI);
			startActivityForResult(intGetContact, ContactsActivity.REQCODE_GETCONTACT);
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode== ContactsActivity.REQCODE_GETCONTACT){
				ContactsLayout.setVisibility(View.VISIBLE);
				ProgressLayout.setVisibility(View.GONE);
				if (data != null) {
					Uri uri = data.getData();
					if (uri != null) {
						Cursor c = null;
						try {
							c = v.getContext().getContentResolver().query(uri, new String[] { 
									PhoneLookup.DISPLAY_NAME,BaseColumns._ID },null, null, null);
							if (c != null && c.moveToFirst()) {
								int id = c.getInt(1);
								String name=c.getString(0);
								String strId=new String();
								strId=strId.valueOf(id);

								Log.d(TAG,uri + "\nid: " + id+" Name:" +name);

								String phoneNumStr="";
								Cursor phoneNumCur = v.getContext().getContentResolver().query(
										ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+ strId, 
										null,null);             	

								if (phoneNumCur != null && phoneNumCur.moveToFirst()) {
									phoneNumStr=phoneNumCur.getString(phoneNumCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
								}   
								else{
									Log.d(TAG,"Cant get phone number");
								}
								phoneNumCur.close();

								String[] projection=new String[]{
										Email.DATA,
										Email.TYPE
								};

								String emailStr="";
								Cursor emailCur = v.getContext().getContentResolver().query(
										ContactsContract.CommonDataKinds.Email.CONTENT_URI,
										//ContactsContract.CommonDataKinds.Email.CONTENT_URI,
										null, 
										ContactsContract.CommonDataKinds.Email.CONTACT_ID+"=?", 
										new String[]{strId},null);

								if (emailCur != null && emailCur.moveToFirst()) {
									emailStr=emailCur.getString(emailCur.getColumnIndex(Email.DATA));
									Log.d(TAG,"Email="+emailStr);
								}
								else{
									Log.d(TAG,"Cant get email");
								}
								emailCur.close();

								ContactRec tAddContact=new ContactRec(id,name,phoneNumStr,emailStr);							
								PhonesToSMS.add(tAddContact);
								Log.d(TAG,"OnActivityResut:id="+tAddContact.conId)  ;                      
								PhonesToSMSListView.invalidateViews();
								Call03Activity.OnChanges();
								//SaveConfig();

							}
						} finally {
							if (c != null) {
								c.close();
							}
						}
					}
				}
			}
		}  		

		public static class EfficientAdapter extends BaseAdapter {
			private LayoutInflater mInflater;
			private Bitmap[] IconsOn=new Bitmap[3];
			private Bitmap[] IconsOff=new Bitmap[3];
			//private Bitmap mIcon1;
			//private Bitmap mIcon2;

			public EfficientAdapter(Context context) {
				// Cache the LayoutInflate to avoid asking for a new one each time.
				mInflater = LayoutInflater.from(context);

				// Icons bound to the rows.
				//mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon48x48_1);
				//mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon48x48_2);

				IconsOn[0]=BitmapFactory.decodeResource(context.getResources(), R.drawable.call_on);
				IconsOn[1]=BitmapFactory.decodeResource(context.getResources(), R.drawable.sms_on);
				IconsOn[2]=BitmapFactory.decodeResource(context.getResources(), R.drawable.email_on);

				IconsOff[0]=BitmapFactory.decodeResource(context.getResources(), R.drawable.call_off);
				IconsOff[1]=BitmapFactory.decodeResource(context.getResources(), R.drawable.sms_off);
				IconsOff[2]=BitmapFactory.decodeResource(context.getResources(), R.drawable.email_off);

			}

			public int getCount() {
				return PhonesToSMS.size();
			}

			public Object getItem(int position) {
				return PhonesToSMS.get(position);
			}

			public long getItemId(int position) {
				return PhonesToSMS.get(position).conId;
			}

			public View getView(int position, View convertView, ViewGroup parent) {

				// A ViewHolder keeps references to children views to avoid unneccessary calls
				// to findViewById() on each row.
				final ViewHolder holder;

				// When convertView is not null, we can reuse it directly, there is no need
				// to reinflate it. We only inflate a new View when the convertView supplied
				// by ListView is null.
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.list_item_icon_text, null);

					// Creates a ViewHolder and store references to the two children views
					// we want to bind data to.
					holder = new ViewHolder();
					holder.textName = (TextView) convertView.findViewById(R.id.textName);
					holder.textPhone = (TextView) convertView.findViewById(R.id.textPhone);
					holder.textEmail = (TextView) convertView.findViewById(R.id.textEmail);
					holder.icon1 = (ImageView) convertView.findViewById(R.id.icon1);
					holder.icon2 = (ImageView) convertView.findViewById(R.id.icon2);
					holder.icon3 = (ImageView) convertView.findViewById(R.id.icon3);

					convertView.setTag(holder);
				} else {
					// Get the ViewHolder back to get fast access to the TextView
					// and the ImageView.
					holder = (ViewHolder) convertView.getTag();
				}

				// Bind the data efficiently with the holder.
				ContactRec thisItem=PhonesToSMS.get(position);         
				String tName=thisItem.getName();
				String tPhone=thisItem.getPhone();
				String tEmail=thisItem.getEmail();
				holder.textName.setText(tName);
				holder.textPhone.setText(tPhone);
				holder.textEmail.setText(tEmail);
				holder.icon1.setImageBitmap(thisItem.getOption(0) ? IconsOn[0] : IconsOff[0]);
				holder.icon2.setImageBitmap(thisItem.getOption(1) ? IconsOn[1] : IconsOff[1]);
				holder.icon3.setImageBitmap(thisItem.getOption(2) ? IconsOn[2] : IconsOff[2]);

				holder.icon1.setTag(thisItem);
				holder.icon1.setOnClickListener(onPhoneIconClickListener);

				holder.icon2.setTag(thisItem);
				holder.icon2.setOnClickListener(onSmsIconClickListener);

				holder.icon3.setTag(thisItem);
				holder.icon3.setOnClickListener(onEmailIconClickListener);

				return convertView;
			}


			static class ViewHolder {
				TextView textName;
				TextView textPhone;
				TextView textEmail;
				ImageView icon1;
				ImageView icon2;
				ImageView icon3;
			}

			OnClickListener onPhoneIconClickListener =new OnClickListener(){
				@Override
				public void onClick(View v) {
					ContactRec tThisRec=(ContactRec) v.getTag();

					if (tThisRec.getPhone()==null || tThisRec.getPhone().equals("")){
						Context context = v.getContext();
						Resources res = context.getResources();
						String text = res.getString(R.string.USRMSG_ContactHasNoPhone);
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
					else {
						if (tThisRec.getOption(0)) {
							tThisRec.setOption(0, false);
						} else {
							tThisRec.setOption(0, true);
						}
						PhonesToSMSListView.invalidateViews();
						//Call03Activity.OnChanges();
						SaveConfig();
					}
				}
			};	

			OnClickListener onSmsIconClickListener =new OnClickListener(){
				@Override
				public void onClick(View v) {
					ContactRec tThisRec = (ContactRec) v.getTag();
					if (tThisRec.getPhone()==null || tThisRec.getPhone().equals("")){
						Context context = v.getContext();
						Resources res = context.getResources();
						String text = res.getString(R.string.USRMSG_ContactHasNoPhone);
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
					else {
						if (tThisRec.getOption(1)) {
							tThisRec.setOption(1, false);
						} else {
							tThisRec.setOption(1, true);
						}
						PhonesToSMSListView.invalidateViews();
						//Call03Activity.OnChanges();
						SaveConfig();
					}
				}
			};

			OnClickListener onEmailIconClickListener =new OnClickListener(){
				@Override
				public void onClick(View v) {
					ContactRec tThisRec=(ContactRec) v.getTag();
					if (tThisRec.getEmail()==null || tThisRec.getEmail().equals("")){
						Context context = v.getContext();
						Resources res = context.getResources();
						String text = res.getString(R.string.USRMSG_ContactHasNoEmail);
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
					else {
						if (tThisRec.getOption(2)) {
							tThisRec.setOption(2, false);
						} else {
							tThisRec.setOption(2, true);
						}
						PhonesToSMSListView.invalidateViews();
						//Call03Activity.OnChanges();
						SaveConfig();
					}
				}
			};
		}



	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		//PhonesToSMSListView = getListView();        

	};




	public static void SaveConfig(){
		try{
			PhonesInConfig=Call03Activity.gsonCompact.toJson(PhonesToSMS, PhonesListType);    	
			Log.d(TAG,"Saving in config:"+PhonesInConfig);		
			Call03Activity.configEditor.putString("PhoneToSMS", PhonesInConfig);
			Call03Activity.configEditor.commit();
		}
		catch(Exception e){
			// View not created
		}
	}







}
