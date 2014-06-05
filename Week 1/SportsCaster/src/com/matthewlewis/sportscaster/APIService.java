/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File APIService.java
 * 
 * Purpose The ApiService is used to pull down remote data from the API on it's own thread.  Once it has data, it then 
 * passes it to the FileManager class to save to the device.  Once this is complete, it uses a message object to 
 * alert the caller of the results.  In this case, it returns a boolean, indicating if the write operation
 * was successful.
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
	private Boolean writeSuccess;
	
	FileManager fileManager;
	
	/**
	 * Instantiates a new API service.
	 */
	public APIService() {
		super("APIService");
		// TODO Auto-generated constructor stub
	}

	/* Override the onHandleIntent method to make use of the passed Intent object, and the data it carries.
	 * 
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		
		Log.i("ON_HANDLE_INTENT","service has started..");
		
		//get our extra data the intent held
		Bundle extras = intent.getExtras();
		//grab our messenger object
		Messenger messenger = (Messenger) extras.get(MESSENGER_KEY);
		//get the API URL, passed as a String
		String urlString = extras.getString(API_KEY);
		String response = "";
		try {
			//convert our string to a valid URL
			URL url = new URL(urlString);
			//send our URL to the getResponse method and set the result to a String
			response = getResponse(url);
			
			//check our returned string to make sure we received valid data
			if (!(response.equals("Error retrieving remote data")))
			{
				//get instance of our FileManager singleton
				fileManager = FileManager.GetInstance();
				//set our boolean to the result of FileManager's writeFile method, so we can alert the main activity
				writeSuccess = fileManager.WriteFile(MainActivity.context, MainActivity.fileName, response);
			}
		} catch (MalformedURLException e1) {

			e1.printStackTrace();
			Log.i("ON_HANDLE_INTENT", "Error converting URL");
		}
		//get a message object from the pool to send data with
		Message msg = Message.obtain();
		//set the "RESULT_OK" to the first arg so we can alert MainActivity the service is complete
		msg.arg1 = Activity.RESULT_OK;
		//set the boolean as the obj, so that MainActivity will know if there were any problems
		msg.obj = writeSuccess;
		try {
			//attempt to send the message object
			messenger.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("ON_HANDLE_INTENT", "error sending message");
		}
	}
	
	/**
	 * Gets the response from the API as a String object.  It then passes back this string to the onHandleIntent method
	 * alerting it of failure, or supplying it the valid data.
	 */
	public String getResponse(URL url) {
		String response;
		//try to connect to the API using the passed URL object
		try {
            URLConnection connection = url.openConnection();
            //use a buffered input stream to get remote data
            BufferedInputStream buffer = new BufferedInputStream(connection.getInputStream());
            //create byte array to hold the data
            byte[] contextByte = new byte[1024];
            int byteRead;
            //create a string builder to build the response in its entirety
            StringBuilder responseBuffer = new StringBuilder();

            //while connection is open and receiving data, continue to append data to StringBuilder object
            while((byteRead = buffer.read(contextByte)) != -1)
            {
                response = new String(contextByte, 0, byteRead);
                responseBuffer.append(response);
            }
            //set final returned data to a String
            response = responseBuffer.toString();
            
        } catch (IOException e) {
            e.printStackTrace();
            response = "Error retrieving remote data";
            Log.e("GET_RESPONSE", e.toString());
        }
		//Return response string, which could either contain a string representing an error, or
		//the valid data.
        return response;
	}
}
