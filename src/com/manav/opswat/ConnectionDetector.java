/*
 * @author manavsinghal
 * ConnectionDetector.java - This class detects the Internet connectivity for Android devices.
 */

package com.manav.opswat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {

	private Context _context;

	/* Constructor Definition */
	public ConnectionDetector(Context context){
		this._context = context;
	}

	/* Method defined for detecting Internet Connection
	 * Returns boolean value - True for Internet Connected and False for Disconnected */
	public boolean isConnectingToInternet(){
		ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) 
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) 
				for (int i = 0; i < info.length; i++) 
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}
		}
		return false;
	}
}
