package com.fbytes.call03;

import android.location.Location;

public class AddressRec  {
	String GeocodeProvider;
	String Address; 
	double Latt;
	double Long;
	float LocAccuracy;
	String LocProvider;
	String MapLink;
	
	void AddressRec(){
		GeocodeProvider="";
		Address="";
		Latt=0.0f; Long=0.0f;
		LocAccuracy=9999;
		LocProvider="";
		MapLink="";		
	}
	
	String getAddress(){return Address;};
	String getGeocodeProvider(){return GeocodeProvider;};
	float getAccuracy(){return LocAccuracy;};
	String getMapLink(){return MapLink;};
	
	void setAddress(String pAddress){Address=pAddress;};
	void setLoc(Location pLoc){Latt=pLoc.getLatitude();Long=pLoc.getLongitude();LocAccuracy=pLoc.getAccuracy();LocProvider=pLoc.getProvider();};
	void setGeocodeProvider(String pGeocodeProvider){GeocodeProvider=pGeocodeProvider;};
	void setMapLink(String pMapLink){MapLink=pMapLink;};
}
