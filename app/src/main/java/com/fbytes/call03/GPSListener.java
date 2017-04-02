package com.fbytes.call03;

import android.location.Location;

public interface GPSListener {
	public void onLocationChanged(Location pLoc);
	public void onSattsCountChanges(int pSats,int pSatsInFix);
}
