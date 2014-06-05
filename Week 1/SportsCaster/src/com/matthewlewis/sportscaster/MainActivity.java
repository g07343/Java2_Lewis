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
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.matthewlewis.sportscaster.NetworkManager;

public class MainActivity extends Activity {

	private static String apiURL = "http://api.espn.com/v1/now/top?limit=5&apikey=q82zaw4uydmpw6ccfcgh8ze2";
	private static TextView statusField;
	public static Context context;
	public static String fileName = "Stories.txt";
	static FileManager fileManager;
	static ListView listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		statusField  = (TextView) findViewById(R.id.internet_warning);
		NetworkManager manager = new NetworkManager();
		Boolean connected = manager.connectionStatus(this);
		
		listview = (ListView) findViewById(R.id.list);
		View listHeader = this.getLayoutInflater().inflate(R.layout.header, null);
		listview.addHeaderView(listHeader);
		
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
				Boolean response;
				if (msg.arg1 == RESULT_OK && msg.obj != null)
				{
					response = (Boolean) msg.obj;
					Log.i("HANDLE_MESSAGE", "Boolean was " +response);
					if (response == true)
					{
						displayData();						
					} else {
						statusField.setText("Error retrieving data!");
					}				
				} else {
					statusField.setText("Error retrieving data!");
				}
			}
		}
	}
	
	public static void displayData() {
		fileManager = FileManager.GetInstance();
		String rawData = fileManager.readFile(context, fileName);
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		try {
			JSONObject rawJson = new JSONObject(rawData);			
			JSONArray stories = (JSONArray) rawJson.get("feed");
			System.out.println("NUMBER OF STORIES:  " + stories.length());
			JSONObject storyObject;
			for (int i = 0; i < stories.length(); i++)
			{
				storyObject = stories.getJSONObject(i);
				String date = storyObject.getString("lastModified");
				String headline = storyObject.getString("headline");
				String description = storyObject.getString("description");
				System.out.println("Date was:  " + date);
				System.out.println("Title was:  " + headline);
				System.out.println("Description was:  " + description);
				
				HashMap<String, String> dataMap = new HashMap<String, String>();
				dataMap.put("date", date);
				dataMap.put("headline",headline);
				dataMap.put("description",description);
				
				list.add(dataMap);
			}
			
			SimpleAdapter adapter = new SimpleAdapter(context, list, R.layout.row, new String[]{"headline", "date", "description"}, new int[]{R.id.title, R.id.date, R.id.description});
			listview.setAdapter(adapter);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("DISPLAY_DATA", "Error parsing JSON from string!");
		}
	}
}
