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
 * alerting the user to make sure they have an Internet connection.  As of week 3, it uses the MainActivityFragment to contain all of its
 * UI, and only handles the raw data of the interface.  The one exception to this is an alert dialog that displays the previous story title
 * and rating, since this data (and the alert dialog itself) is not connected to the fragment and wouldn't make sense to put it there.
 * 
 * 
 */
package com.matthewlewis.sportscaster;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewlewis.sportscaster.NetworkManager;

public class MainActivity extends Activity implements MainActivityFragment.mainFragmentInterface,
	DetailViewFragment.detailsFragmentInterface, SearchFragment.searchFragmentInterface {

	//declare class variables
	private static String apiURL = "http://api.espn.com/v1/now/popular?limit=10&apikey=q82zaw4uydmpw6ccfcgh8ze2";
	public static Context context;
	public static String fileName = "Stories.txt";
	public static String favFileName = "Favorites.txt";
	static ArrayList<HashMap<String, Object>> list;
	private View ratingAlert;
	private String alertTitle;
	private String savedImageUrl;
	private String savedDescription;
	private String savedTitle;
	private String savedDate;
	private Integer savedRating;
	private String savedStoryUrl;
	private int alertInt;
	private AlertDialog ratingDialog;
	static MainActivityFragment mainFragment;
	DetailViewFragment detailFragment;
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.fragment_main);
		//set our public variable context, so outside classes can access it if needed
		context = this;
		
		//check out the status of our sharedPreferences, and depending on the result, do something
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getString("userName", "").toString().isEmpty())
		{  //we haven't established our users identity yet, so launch our dialogFragment to prompt user
			PreferencesFragment prefFrag = PreferencesFragment.newInstance();
			prefFrag.show(getFragmentManager(), "preferences_dialog");
		}
		
		//set up our reference to the MainActivityFragment so we can use it to call methods when needed
		mainFragment = (MainActivityFragment) getFragmentManager().findFragmentById(R.id.main_fragment);
		
		//set up a reference to the DetailViewFragment if we need it
		detailFragment = (DetailViewFragment) getFragmentManager().findFragmentById(R.id.detail_fragment);
		
		//create instance of NetworkManager class to check Internet connection
		final NetworkManager manager = new NetworkManager();
		
		
		//use our instance of Network Manager to determine our current connectivity
		Boolean connected = manager.connectionStatus(this);
		
			
		//check for a saved instance and get data if we have one
		if (savedInstanceState != null )
		{	
			list = (ArrayList<HashMap<String, Object>>) savedInstanceState.getSerializable("saved");
		
			//set our adapter within our fragment via interface
			applyAdapter(context, list);
			
			//check to see if the user had left a search "open" before rotation, and if so, restore it
			String previousSearch = savedInstanceState.getString("searched");
			if (previousSearch != null)
			{
				applyFilter(previousSearch);
			}
			
			//if our alert was showing before the view was destroyed, recreate it
        	String oldTitle = savedInstanceState.getString("alertTitle");
        	Integer oldRating = (int) savedInstanceState.getInt("alertInt");
        	
        	//grab the description so we can check if there is saved data for display in landscape
        	savedDescription = savedInstanceState.getString("description");
        	
        	//check to see if we had an alert displaying when device was rotated
        	if (oldTitle != null && !(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE))
        	{
        		//create our alert again from our saved data
        		createAlert(oldTitle, oldRating);   		
        	}
        	if (savedDescription != null)
        	{
        		savedImageUrl = savedInstanceState.getString("imageLink");
        		savedDate = savedInstanceState.getString("date");
        		savedStoryUrl = savedInstanceState.getString("link");
        		savedTitle = savedInstanceState.getString("title");
        		savedRating = (int) savedInstanceState.getInt("rating");
        		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        		{
        			detailFragment.clearImage();
        			detailFragment.populateData(savedTitle, savedDate, savedDescription, savedImageUrl, savedStoryUrl, savedRating);
        			detailFragment.rating = savedRating;
        			detailFragment.setRating(savedRating);
        		}
        		
        	} else {
        		//since the second view hasn't been created yet, pass the first story so we show a formatted interface
        		itemSelected(1, null);
        	}
			
		} else {		
			//if we have network connection
			if (connected) {
				Log.i("CONNECTION_CHECK", "Good connection");
				//call method to begin retrieval of remote data
				startRetrieval();
			} else {
				//no network connection, see if we have local data saved
				Log.i("CONNECTION_CHECK", "No connection");
				
				//retrieve savedData to a string using FileManager singleton
				String savedData = FileManager.GetInstance().readFile(context, fileName);
				
				//check if savedData is valid, meaning we had a saved file from a previous session
				if (savedData != null && !savedData.isEmpty()) {
					
					//let user know they are viewing old data due to lack of Internet connection
					Toast.makeText(getApplicationContext(),
							"No Internet Connection.  Displaying old stories.",
							Toast.LENGTH_LONG).show();
					
					//now that we have verified old data exists, call method to display it to the user
					displayData(null);
				} else {
					//we have no internet connection and no local storage, so alert the fragment via interface method
					updateStatus(null);
				}

			}
		}
		
	}

	/**
	 * Begin the retrieval of remote JSON data now that we know we have an Internet connection.
	 */
	public void startRetrieval() {
		//create a Handler object based on the subclass below
		final ApiHandler apiHandler = new ApiHandler(context);
		
		//create a messenger object to communicate back and forth
		Messenger apiMessenger = new Messenger(apiHandler);
		
		//create an intent so we can actually start the APIService activity
		Intent startApiService = new Intent(context, APIService.class);
		
		//Add our messenger and URL string to the intent using the keys provided within the service itself
		startApiService.putExtra(APIService.MESSENGER_KEY, apiMessenger);
		startApiService.putExtra(APIService.API_KEY, apiURL);
		
		//Start the service, passing the intent
		context.startService(startApiService);
		
		//let the user know that data is being retrieved
		updateStatus("Getting data...");
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		return true;
	}
	
	//we use this function to dynamically hide the "favorites" contextual menu in portrait, since it only needs to serve one function there
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		System.out.println("Prepare menu runs!");
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			SubMenu sub = menu.getItem(1).getSubMenu();
			sub.clear();
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id) {
		
		case R.id.menu_info:
			//user selected the "info" option within the action bar.  Start the "floating window" activity
			Intent infoIntent = new Intent(context, InfoFragment.class);
			startActivityForResult(infoIntent, 0);
			
			break;
			case R.id.menu_search:
				SearchFragment searchFrag = SearchFragment.newInstance();
				searchFrag.show(getFragmentManager(), "search_dialog");
				break;
			
			case R.id.menu_preferences:
				PreferencesFragment prefFrag = PreferencesFragment.newInstance();
				prefFrag.show(getFragmentManager(), "preferences_dialog");
				break;
			case R.id.menu_favorites:
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					//detect if we're in portrait, and if so, load Favorites Activity.  We only do this in portrait,
					//because in landscape there is a contextual menu that dynamically displays the below 2 options
					Intent showFavorites = new Intent(context, FavoritesActivity.class);
			    	startActivityForResult(showFavorites, 0);
				}
				break;
			case R.id.contextual_addFav:
				//user tapped the 'add favorite' option from contextual menu in landscape
				System.out.println("ADD FAV");
				String title = detailFragment.titleView.getText().toString();
				String date = detailFragment.dateView.getText().toString();
				String imageUrl = detailFragment.imageUrl;
				String storyUrl = detailFragment.storyUrl;
				String description = detailFragment.descriptionView.getText().toString();
				int rating = (int) detailFragment.ratingBar.getRating();
				JSONObject storyObject = new JSONObject();
				try {
					storyObject.put("title", title);
					storyObject.put("date", date);
					storyObject.put("imageUrl", imageUrl);
					storyObject.put("storyUrl", storyUrl);
					storyObject.put("description", description);
					storyObject.put("rating", rating);
					
					//send the JSON formatted story for saving and check the returned String for success
					FileManager fileManager = FileManager.GetInstance();
					String wasSaved = fileManager.writeFavorites(context, favFileName, storyObject);
					if (wasSaved != null) {
						if (wasSaved.equals("Story is already a favorite")) {
							Toast.makeText(getApplicationContext(),
									"This story was already a favorite!",
									Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(getApplicationContext(),
									"Story favorited!",
									Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(getApplicationContext(),
								"Story favorited!",
								Toast.LENGTH_LONG).show();
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case R.id.contextual_goToFav:
				//user tapped the 'go to favorites' option from contextual menu in landscape
				System.out.println("GOTO FAV");
				Intent showFavorites = new Intent(context, FavoritesActivity.class);
		    	startActivityForResult(showFavorites, 0);
				break;
			default:
				break;
		}
		return true;
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
		 * @param context the activity
		 */
		public ApiHandler(Context context) {
			mActivity = new WeakReference<MainActivity>((MainActivity) context);
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
						activity.displayData(null);
					} else {
						//boolean was false, there was an error saving the data.  Alert user
						activity.updateStatus("Error retrieving data!");
					}
				} else {
					//boolean was false, there was an error saving the data.  Alert user
					activity.updateStatus("Error retrieving data!");
				}
			}
		}
	}

	/**
	 * This method is called once we have determined that data was successfully saved to the device's storage.
	 * Get this data, and parse it for the information we want before displaying it to the user.  This method needs 
	 * try/catch blocks around nearly every JSON operation due to ESPN's inconsistency with their responses
	 */
	public void displayData(ArrayList<HashMap<String, Object>> data) {
		
		//retrieve stored data using FileManager singleton
		String rawData = FileManager.GetInstance().readFile(context, fileName);
		
		if (data != null)
		{
			list = data;
			//create a SimpleAdapter in conjunction with the above created data
			applyAdapter(context, list);
		} else {
			//convert to an arrayList of HashMaps, which contain objects
			list = new ArrayList<HashMap<String, Object>>();
		}	
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
				
				String url;
				try {
					//try to get the url as a string so we can let the user access the ESPN webpage in the detailView
					JSONObject allLinks = storyObject.getJSONObject("links");
					JSONObject web = allLinks.getJSONObject("web");
					url = web.getString("href");
					//url = allLinks.getString("web");
				} catch (Exception e) {
					Log.i("DISPLAY_DATA"," Error retrieving URL for story");
					url = "No link provided for this story.";
				}
				
				String imageURL;	
				//try to get a link to an image related to the story for viewing within the detailsView
				try {
					JSONArray images = storyObject.getJSONArray("images");
					JSONObject imageObject = images.getJSONObject(0);
					imageURL = imageObject.getString("url");
					System.out.println("Link to image was:  " + imageURL);
				} catch (Exception e) {
					Log.i("DISPLAY_DATA", "Error getting url for image");
					imageURL = null;
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
						//hockey - set to hockey icon
						sportIcon = R.drawable.hockey;
						break;
					case 46:
						//basketball - set to basketball icon
						sportIcon = R.drawable.basketball;
						break;
					case 10:
						//baseball - set to baseball icon
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
				
				//add our link
				dataMap.put("url", url);
				
				//add our image
				dataMap.put("imageLink", imageURL);
				
				//finally, add the above HashMap to our ArrayList
				list.add(dataMap);
			}
			
			applyAdapter(context, list);

		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("DISPLAY_DATA", "Error parsing JSON from saved data!");
		}
	}
	//make sure to grab our retrieved data in the event our activity is destroyed, so we don't have to redownload it all again
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		//save the tableView's data so we don't use pull down redundant data again
		System.out.println("VIEW DESTROYED!!!");
		savedInstanceState.putSerializable("saved", (Serializable) list);
		
		//make sure to dismiss our alert dialog if it's not null and grab our values
		//there is strange behavior that after rotating the device twice, these values are lost for some reason, 
		//so we grab them here to ensure we can save them
		if (ratingDialog != null)
		{
			TextView alertTitleView = (TextView) ratingDialog.findViewById(R.id.ratingAlert_title);
			if (alertTitleView != null)
			{
				if (!alertTitleView.getText().equals(""))
				{
					alertTitle = (String) alertTitleView.getText();
					RatingBar oldRating = (RatingBar) ratingDialog.findViewById(R.id.ratingAlert_rating);
					float oldFloat = oldRating.getRating();
					alertInt = (int)oldFloat;
				}
			}
			ratingDialog.dismiss();
		}
			
		
		//check to see if we have a saved value for description and if so, save it
		if (savedDescription != null) {
			savedInstanceState.putString("description", savedDescription);
			savedInstanceState.putString("imageLink", savedImageUrl);
			savedInstanceState.putString("date", savedDate);
			savedInstanceState.putString("link", savedStoryUrl);
			savedInstanceState.putString("title", savedTitle);
			if (savedRating != null)
			{
				savedInstanceState.putInt("rating", savedRating);
			} else {
				savedInstanceState.putInt("rating", 0);
			}		
		}
		
		//also, check to see if our ratingAlert is on-screen at this moment and if so, capture its data to restore
		if (alertTitle != null)
		{
			System.out.println("AlertTitle was:  " + alertTitle);
			savedInstanceState.putString("alertTitle", alertTitle);
			savedInstanceState.putInt("alertInt", alertInt);
			alertTitle = null;
			alertInt = -1;
			
		}
		
		if (mainFragment.searchedString != null)
		{
			savedInstanceState.putString("searched", mainFragment.searchedString);
		}
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	//get returned data when the detailView is destroyed
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//check to make sure our detailView exited successfully, and then get the returned data
		if (resultCode == RESULT_OK && requestCode == 0)
		{
			Bundle result = data.getExtras();
			alertInt = (Integer) result.get("rating");
			alertTitle = (String) result.get("title");
			
			//send our values to be created into an alertDialog (external method so we can reuse it within onCreate if necessary)
			createAlert(alertTitle, alertInt);
			
			//check to see if we were returned an imageLink and if so, that means the user rotated the device while it was still in
			//portrait, so we need to grab all of the required data to repopulate the detailsview that is now onscreen
			savedImageUrl = (String) result.get("imageLink");
			if (savedImageUrl != null)
			{
				//grab all of our data from the intent returned from the activity so that we can save it to 
				//the second view if the user rotates to landscape
				savedDescription = (String) result.get("description");
				savedDate = (String) result.get("date");
				savedTitle = (String) result.get("title");
				savedStoryUrl = (String) result.get("link");
				savedRating = (Integer) result.getInt("rating");
				
				//if the user rotated the device when the second activity was still open in portrait, handle it here
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && detailFragment.isInLayout())
				{
					//reset our image manually so that the device retrieves the correct one for the story
					detailFragment.clearImage();
					
					//use our newly retrieved data to display in the detail fragment, which is now on the right side of the screen
					detailFragment.populateData(alertTitle, savedDate, savedDescription, savedImageUrl, savedStoryUrl, savedRating);
					
					//set our rating
					detailFragment.rating = savedRating;
					detailFragment.setRating(savedRating);
				}	
			}
		}
		
	}
	//the below method creates our alertDialog dynamically using a string for the title and an int for the rating
	public void createAlert(String title, Integer rating) {
		//create our alert dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		LayoutInflater inflater = this.getLayoutInflater();
		
		//get our alertView as a view object first so we can alter it's data
		ratingAlert = inflater.inflate(R.layout.ratingalert, null);
		
		//grab our rating view contained in it and set it to whatever was previously entered in DetailView
		RatingBar previousRating = (RatingBar) ratingAlert.findViewById(R.id.ratingAlert_rating);
		previousRating.setRating(rating);
		
		//grab our title textview and set it to the title of the story
		TextView titleView = (TextView) ratingAlert.findViewById(R.id.ratingAlert_title);
		titleView.setText(title);
		builder.setView(ratingAlert);
		
		
		//create a button to dismiss the alert and tell it what to do when tapped
		builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				
				//need to set the dialog and title to null here manually to avoid its values being saved in the onSaveInstanceState method
				ratingDialog = null;
				alertTitle = null;
			}
		});
		//build the alert
		ratingDialog = builder.create();
		
		//show the alert to the user
		ratingDialog.show();
	}
	
	//this is the interface method from the MainActivityFragment, which passes MainActivity an int to communicate
	//which row was selected in its listview.  Since we already have the data here, no need to pass it back.
	@Override
	public void itemSelected(int position, String title) {
		//convert our position to the correct one chosen by the user
		int actualSelected = position -=1;
		
		//grab the hashmap with our selected story's data
		HashMap<String, Object> dataMap = null;
		
		//get an instance of the detailsViewFragment so we can check if it is valid
		detailFragment = (DetailViewFragment) getFragmentManager().findFragmentById(R.id.detail_fragment);
		
		if (title != null)
		{
			System.out.println("Selected story was:  " + title);
			
			for (int i = 0; i < list.size(); i++)
			{
				HashMap<String, Object> storyData = list.get(i);
				String savedTitle = (String) storyData.get("headline");
				if (savedTitle.equals(title))
				{
					dataMap = storyData;
					break;
				}
			}
		} else {
			dataMap = list.get(actualSelected);
		}
		
		//check to make sure we have our fragment and it is currently in the view (landscape)
		if (detailFragment != null && detailFragment.isInLayout())
		{   //grab our data for the selected story 	
	    	savedTitle = (String) dataMap.get("headline");
	    	savedDate = (String) dataMap.get("date");
	    	savedDescription = (String) dataMap.get("description");
	    	savedImageUrl = (String) dataMap.get("imageLink");
	    	savedStoryUrl = (String) dataMap.get("url");
	    	
	    	//reset the image of the DetailFragment just in case, otherwise it won't display the correct one
	    	detailFragment.clearImage();
	    	
	    	//set our new data to the detailFragment
	    	detailFragment.populateData(savedTitle, savedDate, savedDescription, savedImageUrl, savedStoryUrl, savedRating);
		} else {
			//send the data to the DetailsActivity, since our second fragment hasn't been initialized
	    	Intent showDetail = new Intent(context, DetailView.class);
	    	showDetail.putExtra("data", dataMap);
	    	startActivityForResult(showDetail, 0);
		}
		
	}
	
//using this method, we can send the data for our listview to our fragment to apply
	@Override
	public void applyAdapter(Context context,
			ArrayList<HashMap<String, Object>> list) {
		mainFragment.setData(context, list);
	}

	//this method lets us communicate to the MainActivityFragment that we don't have internet.
	//The correct interface is then displayed from within the fragment, rather than applying it from here.
	//if we send this method a string, it applies it to the "StatusText" to update the user, otherwise, it's no internet
	@Override
	public void updateStatus(String status) {
		if (status != null)
		{//if we received an actual string as an argument, pass to fragment to display in the StatusText field
			mainFragment.toggleStatusUI(status);
		} else {
			//otherwise, we didn't receive a valid string, meaning we have no internet connection
			mainFragment.toggleStatusUI(null);
		}
	}

	//this method is only here because it is needed within the Detail activity to communicate to its fragment
	@Override
	public void displayDetails(String storyTitle, String storyDate,
			String storyDescription, String imageLink, String storyLink) {
		// TODO Auto-generated method stub
		
	}

	//this method lets us apply our saved rating to the detailFragment dynamically
	@Override
	public void setRating(int number) {
		savedRating = number;
		
	}

	//this let's our detailFragment grab the saved rating from here if necessary
	@Override
	public Integer getRating() {
		// TODO Auto-generated method stub
		return savedRating;
	}

	@Override
	public void applyFilter(CharSequence s) {
		System.out.println("Filter data called!");
		mainFragment.filterData(s);
	}

	public static void updateFragment() {
		// TODO Auto-generated method stub
		mainFragment.updateName();
	}
	
}
