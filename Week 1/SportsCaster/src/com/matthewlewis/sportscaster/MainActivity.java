/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File MainActivity.java
 * 
 * Purpose 
 * 
 */
package com.matthewlewis.sportscaster;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.matthewlewis.sportscaster.NetworkManager;

public class MainActivity extends Activity {

	private static String apiURL = "http://api.espn.com/v1/now";
	private static TextView statusField;
	private static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		statusField  = (TextView) findViewById(R.id.internet_warning);
		NetworkManager manager = new NetworkManager();
		Boolean connected = manager.connectionStatus(this);

		if (connected) {
			Log.i("CONNECTION_CHECK", "Good connection");
			statusField.setText("Internet Connected!");

			final ApiHandler apiHandler = new ApiHandler(this);

			Messenger apiMessenger = new Messenger(apiHandler);
			Intent startApiService = new Intent(context, APIService.class);
			startApiService.putExtra(APIService.MESSENGER_KEY, apiMessenger);
			startApiService.putExtra(APIService.API_KEY, apiURL);
			startService(startApiService);
			statusField.setText("Getting data...");

		} else {
			Log.i("CONNECTION_CHECK", "No connection");
			statusField.setText("No internet!");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static class ApiHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public ApiHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mActivity.get();
			if (activity != null) {
				String response;
				if (msg.arg1 == RESULT_OK && msg.obj != null)
				{
					response = (String) msg.obj;
					Log.i("HANDLE_MESSAGE", response);
				}
			}
		}
	}

}
