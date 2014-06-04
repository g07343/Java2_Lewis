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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
	FileManager fileManager;
	
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
		String urlString = extras.getString(API_KEY);
		String response = "";
		try {
			URL url = new URL(urlString);
			response = getResponse(url);
			Log.i("RESPONSE", response);
			if (!(response.equals("Error retrieving remote data")))
			{
				fileManager = FileManager.GetInstance();
				fileManager.WriteFile(MainActivity.context, MainActivity.fileName, response);
			}
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.i("ON_HANDLE_INTENT", "Error converting URL");
		}
		
		Message msg = Message.obtain();
		msg.arg1 = Activity.RESULT_OK;
		msg.obj = "Data returned was:  " + response;
		try {
			messenger.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("ON_HANDLE_INTENT", "error sending message");
		}
	}
	
	public String getResponse(URL url) {
		String response;
		
		try {
            URLConnection connection = url.openConnection();
            BufferedInputStream buffer = new BufferedInputStream(connection.getInputStream());
            byte[] contextByte = new byte[1024];
            int byteRead;
            StringBuilder responseBuffer = new StringBuilder();

            while((byteRead = buffer.read(contextByte)) != -1)
            {
                response = new String(contextByte, 0, byteRead);
                responseBuffer.append(response);
            }
            response = responseBuffer.toString();
            
        } catch (IOException e) {
            e.printStackTrace();
            response = "Error retrieving remote data";
            Log.e("GET_RESPONSE", e.toString());
        }
        return response;
	}
}
