/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File APIService.java
 * 
 * Purpose 
 * 
 */

package com.matthewlewis.sportscaster;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class APIService extends IntentService{

	public static final String MESSENGER_KEY = "messenger";
	public static final String API_KEY = "api";
	
	public APIService() {
		super("APIService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("ON_HANDLE_INTENT","service has started..");
		
		Bundle extras = intent.getExtras();
		Messenger messenger = (Messenger) extras.get(MESSENGER_KEY);
		String url = extras.getString(API_KEY);
		
		Message msg = Message.obtain();
		msg.arg1 = Activity.RESULT_OK;
		msg.obj = "Url sent was:  " + url;
		try {
			messenger.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("ON_HANDLE_INTENT", "error sending message");
		}
	}

}
