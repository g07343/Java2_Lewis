/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File MainActivity.java
 * 
 * Purpose MainActivity contains the base functionality for the program.  After launch, it checks for an Internet connection, 
 * and if found, pulls data for display.  If one is not found, it attempts to load previously saved data.
 * 
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewlewis.sportscaster.NetworkManager;

public class MainActivity extends Activity {

	//declare class variables
	private static String apiURL = "http://api.espn.com/v1/now/popular?limit=10&apikey=q82zaw4uydmpw6ccfcgh8ze2";
	private static TextView statusField;
	public static Context context;
	public static String fileName = "Stories.txt";
	private static FileManager fileManager;
	private static ListView listview;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		statusField = (TextView) findViewById(R.id.internet_warning);
		final NetworkManager manager = new NetworkManager();
		final Button reloadBtn = (Button) findViewById(R.id.reload_btn);
		Boolean connected = manager.connectionStatus(this);
		reloadBtn.setVisibility(View.GONE);

		listview = (ListView) findViewById(R.id.list);
		View listHeader = this.getLayoutInflater().inflate(R.layout.header,
				null);
		listview.addHeaderView(listHeader);

		if (connected) {
			Log.i("CONNECTION_CHECK", "Good connection");
			startRetrieval();
		} else {
			Log.i("CONNECTION_CHECK", "No connection");
			fileManager = FileManager.GetInstance();
			String savedData = fileManager.readFile(context, fileName);

			if (savedData != null && !savedData.isEmpty()) {
				statusField.setVisibility(View.GONE);
				Toast.makeText(getApplicationContext(),
						"No Internet Connection.  Displaying old stories.",
						Toast.LENGTH_LONG).show();
				displayData();
			} else {
				statusField
						.setText("No internet! Please check your internet connection.");
				reloadBtn.setVisibility(View.VISIBLE);
				reloadBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// Re check our Internet connection
						Boolean connected = manager.connectionStatus(context);
						if (connected) {
							reloadBtn.setVisibility(View.GONE);
							//now that we have internet, get data
							startRetrieval();
						}
					}
				});
			}

		}
	}

	/**
	 * Begin the retrieval of remote JSON data now that we know we have an Internet connection.
	 */
	public void startRetrieval() {
		final ApiHandler apiHandler = new ApiHandler(this);
		Messenger apiMessenger = new Messenger(apiHandler);
		Intent startApiService = new Intent(context, APIService.class);
		startApiService.putExtra(APIService.MESSENGER_KEY, apiMessenger);
		startApiService.putExtra(APIService.API_KEY, apiURL);
		startService(startApiService);
		statusField.setText("Getting data...");
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

	/**
	 * Create a custom Handler class extending the base class and use a weak reference
	 *  to the MainActivity to avoid memory leaks
	 */
	private static class ApiHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		/**
		 * Instantiates a new api handler.
		 *
		 * @param activity the activity
		 */
		public ApiHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		/* 
		 * override the base class's handleMessage method and check if we were able to read the saved data or not.
		 * If so, go ahead and display it using the below displayData method.
		 */
		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mActivity.get();
			if (activity != null) {
				Boolean response;
				if (msg.arg1 == RESULT_OK && msg.obj != null) {
					response = (Boolean) msg.obj;
					Log.i("HANDLE_MESSAGE", "Boolean was " + response);
					if (response == true) {
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

	/**
	 * This method is called once we have determined that data was successfully saved to the device's storage.
	 * Get this data, and parse it for the information we want before displaying it to the user.
	 */
	public static void displayData() {
		statusField.setVisibility(View.GONE);
		fileManager = FileManager.GetInstance();
		String rawData = fileManager.readFile(context, fileName);
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		try {
			JSONObject rawJson = new JSONObject(rawData);
			JSONArray stories = (JSONArray) rawJson.get("feed");
			System.out.println("NUMBER OF STORIES:  " + stories.length());
			JSONObject storyObject;
			for (int i = 0; i < stories.length(); i++) {
				storyObject = stories.getJSONObject(i);

				String date;
				try {
					date = storyObject.getString("lastModified");
				} catch (Exception e) {
					Log.i("DISPLAY_DATA"," Error retrieving date for story");
					date = "No date provided";
					e.printStackTrace();
				}

				String headline;
				try {
					headline = storyObject.getString("headline");
				} catch (Exception e) {
					Log.i("DISPLAY_DATA"," Error retrieving title for story");
					headline = "No title given...";
					e.printStackTrace();
				}

				String description;
				try {
					description = storyObject.getString("description");
				} catch (Exception e) {
					Log.i("DISPLAY_DATA"," Error retrieving description for story");
					description = "No description provided for this story.";
					e.printStackTrace();
				}

				int sportIcon;

				try {
					JSONArray detailsArray = storyObject
							.getJSONArray("categories");
					JSONObject firstItem = detailsArray.getJSONObject(0);
					String sportId = firstItem.getString("sportId");
					int sportNum = Integer.parseInt(sportId);
					switch (sportNum) {
					case 28:
						sportIcon = R.drawable.football;
						break;
					case 90:
						sportIcon = R.drawable.hockey;
						break;
					case 46:
						sportIcon = R.drawable.basketball;
						break;
					case 10:
						sportIcon = R.drawable.baseball;
						break;
					default:
						sportIcon = R.drawable.generic;
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();
					Log.i("DISPLAY_DATA", "error accessing sportID");
					sportIcon = R.drawable.generic;
				}

				HashMap<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("date", date);
				dataMap.put("headline", headline);
				dataMap.put("description", description);
				dataMap.put("icon", Integer.toString(sportIcon));

				list.add(dataMap);
			}

			SimpleAdapter adapter = new SimpleAdapter(context, list,
					R.layout.row, new String[] { (String) "headline",
							(String) "date", "description", "icon" },
					new int[] { R.id.title, R.id.date, R.id.description,
							R.id.sport_icon });
			listview.setAdapter(adapter);

		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("DISPLAY_DATA", "Error parsing JSON from saved data!");
		}
	}
}
