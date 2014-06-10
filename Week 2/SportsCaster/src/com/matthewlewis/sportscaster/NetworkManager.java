/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File NetworkManager.java
 * 
 * Purpose The NetworkManager class simply checks for an internet connection using the Connectivity Service.  It returns a Boolean
 * to the caller with the result.
 * 
 */
package com.matthewlewis.sportscaster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkManager {
	public Boolean connectionStatus(Context context) {
        //create initial boolean to set true/false depending on network conditions
        Boolean connected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        //check to make sure we have a valid object
        if (networkInfo != null)
        {   //check the result to make sure it is actually connected and set boolean to true if so
            if (networkInfo.isConnected())
            {
                connected = true;
            }
        }
        return connected;
    }
}
