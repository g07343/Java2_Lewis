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
 * and if found, pulls data for display.  If one is not found, it attempts to load previously saved data, before finally
 * alerting the user to make sure they have an Internet connection
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
		//set our public variable context, so outside classes can access it if needed
		context = this;
		//grab our text field in charge of communicating to the user
		statusField = (TextView) findViewById(R.id.internet_warning);
		
		//create instance of NetworkManager class to check Internet connection
		final NetworkManager manager = new NetworkManager();
		
		final Button reloadBtn = (Button) findViewById(R.id.reload_btn);
		
		//use our instance of Network Manager to determine our current connectivity
		Boolean connected = manager.connectionStatus(this);
		//set our button (only used for Re checking Internet) to "GONE" by default
		reloadBtn.setVisibility(View.GONE);
		
		//set up our list view and inflate our header view and then add it
		listview = (ListView) findViewById(R.id.list);
		View listHeader = this.getLayoutInflater().inflate(R.layout.header,
				null);
		listview.addHeaderView(listHeader);
		
		//if we have network connection
		if (connected) {
			Log.i("CONNECTION_CHECK", "Good connection");
			//call method to begin retrieval of remote data
			startRetrieval();
		} else {
			//no network connection, see if we have local data saved
			Log.i("CONNECTION_CHECK", "No connection");
			//get instance of FileManager singleton
			fileManager = FileManager.GetInstance();
			
			//retrieve savedData to a string
			String savedData = fileManager.readFile(context, fileName);
			
			//check if savedData is valid, meaning we had a saved file from a previous session
			if (savedData != null && !savedData.isEmpty()) {
				
				//hide our statusField since we no longer need it
				statusField.setVisibility(View.GONE);
				
				//let user know they are viewing old data due to lack of Internet connection
				Toast.makeText(getApplicationContext(),
						"No Internet Connection.  Displaying old stories.",
						Toast.LENGTH_LONG).show();
				
				//now that we have verified old data exists, call method to display it to the user
				displayData();
			} else {
				//no Internet or saved data, alert user they need Internet!
				statusField.setText("No internet! Please check your internet connection.");
				
				//display our button, which rechecks Internet
				reloadBtn.setVisibility(View.VISIBLE);
				
				//set onClick listener to check for a change in Internet connectivity
				reloadBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// Re check our Internet connection
						Boolean connected = manager.connectionStatus(context);
						if (connected) {
							//hide our button now, since we no longer need it
							reloadBtn.setVisibility(View.GONE);
							//now that we have Internet, get data
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
		//create a Handler object based on the subclass below
		final ApiHandler apiHandler = new ApiHandler(this);
		
		//create a messenger object to communicate back and forth
		Messenger apiMessenger = new Messenger(apiHandler);
		
		//create an intent so we can actually start the APIService activity
		Intent startApiService = new Intent(context, APIService.class);
		
		//Add our messenger and URL string to the intent using the keys provided within the service itself
		startApiService.putExtra(APIService.MESSENGER_KEY, apiMessenger);
		startApiService.putExtra(APIService.API_KEY, apiURL);
		
		//Start the service, passing the intent
		startService(startApiService);
		
		//let the user know that data is being retrieved
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
				//check our response to make sure we have a valid object and the Service finished successfully
				if (msg.arg1 == RESULT_OK && msg.obj != null) {
					//convert our object to a boolean, letting us know if the data was written 
					response = (Boolean) msg.obj;
					Log.i("HANDLE_MESSAGE", "Boolean was " + response);
					
					//if the boolean is true, data was successfully saved and we can use it
					if (response == true) {
						//call the displayData function, which reads, formats, and displays the data to the user
						displayData();
					} else {
						//boolean was false, there was an error saving the data.  Alert user
						statusField.setText("Error retrieving data!");
					}
				} else {
					//boolean was false, there was an error saving the data.  Alert user
					statusField.setText("Error retrieving data!");
				}
			}
		}
	}

	/**
	 * This method is called once we have determined that data was successfully saved to the device's storage.
	 * Get this data, and parse it for the information we want before displaying it to the user.  This method needs 
	 * try/catch blocks around nearly every JSON operation due to ESPN's inconsistency with their responses
	 */
	public static void displayData() {
		//hide our statusField now that we are showing data 
		statusField.setVisibility(View.GONE);
		
		//get instance of FileManger singleton
		fileManager = FileManager.GetInstance();
		
		//retrieve stored data using FileManager
		String rawData = fileManager.readFile(context, fileName);
		
		//convert to an arrayList of HashMaps, which contain objects
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		try {
			//convert our raw string to a JSON object
			JSONObject rawJson = new JSONObject(rawData);
			
			//get JSONArray named "feed" from base JSONObject
			JSONArray stories = (JSONArray) rawJson.get("feed");
			System.out.println("NUMBER OF STORIES:  " + stories.length());
			
			//create a JSONObject to contain each news story contained in the "feed" array
			JSONObject storyObject;
			
			//for the number of stories returned, create a list item (brace for tons of try/catch!)
			for (int i = 0; i < stories.length(); i++) {
				storyObject = stories.getJSONObject(i);

				String date;
				try {
					//try to get the date string for the current story (if it's included)
					date = storyObject.getString("lastModified");
				} catch (Exception e) {
					Log.i("DISPLAY_DATA"," Error retrieving date for story");
					date = "No date provided";
					e.printStackTrace();
				}

				String headline;
				try {
					//try to get the title string for the current story (if it's included)
					headline = storyObject.getString("headline");
				} catch (Exception e) {
					Log.i("DISPLAY_DATA"," Error retrieving title for story");
					headline = "No title given...";
					e.printStackTrace();
				}

				String description;
				try {
					//try to get the description for the current story (if it's included)
					description = storyObject.getString("description");
				} catch (Exception e) {
					Log.i("DISPLAY_DATA"," Error retrieving description for story");
					description = "No description provided for this story.";
					e.printStackTrace();
				}
				
				//create an integer object to reference the different sport icons in Drawable
				int sportIcon;
				
				//the below attempts to identify which sport the story pertains to.  This is done with a unique "id"
				//that is sometimes returned within the story.
				try {
					//try to get the "categories" JSONArray, if it exists 
					JSONArray detailsArray = storyObject
							.getJSONArray("categories");
					
					//get the first child object of the "categories" array, which is always
					//where the sports id lives
					JSONObject firstItem = detailsArray.getJSONObject(0);
					
					//grab the string representing which sport is featured in the story
					String sportId = firstItem.getString("sportId");
					//convert string to integer so we can figure out which sport
					int sportNum = Integer.parseInt(sportId);
					
					//check the sportNumber integer, and hopefully determine which sport the story is about
					//and then apply an icon from drawables to the "sportIcon" integer
					switch (sportNum) {
					case 28:
						//football - set to football icon
						sportIcon = R.drawable.football;
						break;
					case 90:
						//football - set to hockey icon
						sportIcon = R.drawable.hockey;
						break;
					case 46:
						//football - set to basketball icon
						sportIcon = R.drawable.basketball;
						break;
					case 10:
						//baseball
						sportIcon = R.drawable.baseball;
						break;
					default:
						//unknown sport found - apply generic icon
						sportIcon = R.drawable.generic;
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();
					Log.i("DISPLAY_DATA", "error accessing sportID");
					sportIcon = R.drawable.generic;
				}
				//create a new HashMap to hold all of the retrieved data
				HashMap<String, Object> dataMap = new HashMap<String, Object>();
				
				//add our date String
				dataMap.put("date", date);
				
				//add our headline String
				dataMap.put("headline", headline);
				
				//add our description String
				dataMap.put("description", description);
				
				//add our icon
				dataMap.put("icon", Integer.toString(sportIcon));
				
				//finally, add the above HashMap to our ArrayList
				list.add(dataMap);
			}
			//create a SimpleAdapter in conjunction with the above created data
			SimpleAdapter adapter = new SimpleAdapter(context, list,
					R.layout.row, new String[] { (String) "headline",
							(String) "date", "description", "icon" },
					new int[] { R.id.title, R.id.date, R.id.description,
							R.id.sport_icon });
			
			//set our adapter
			listview.setAdapter(adapter);

		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("DISPLAY_DATA", "Error parsing JSON from saved data!");
		}
	}
}
