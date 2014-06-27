/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File FavoritesActivity.java
 * 
 * Purpose The FavoritesActivity is in charge of displaying all old/Favorited stories that have been saved to the device.  It loads these within a listview, with each
 * story's rating containing the rating saved.  The user can also select each story in the list and load it into the DetailView activity as well as remove favorites 
 * by tapping the "Favorite" icon in the Action Bar, which sets the listview onClickListener into an "edit" mode.
 * 
 */
package com.matthewlewis.sportscaster;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FavoritesActivity extends Activity{

	TextView title;
	TextView warning;
	ListView listView;
	Context baseContext;
	String fileName;
	boolean editing;
	private Activity thisActivity;
	ArrayList<HashMap<String, Object>> list;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//grab a reference to this activity
		thisActivity = this;
		
		//set our content view
		setContentView(R.layout.favorites);
		
		//set our editing boolean as 'false', as it is enabled when the user wants to delete a favorite
		editing = false;
		
		//grab our baseContext for use throughout activity
		baseContext = MainActivity.context;
		
		//grab our fileName for use throughout activity
		fileName = MainActivity.favFileName;
		
		//set up our UI elements for use
		title = (TextView) findViewById(R.id.favorites_title);
		listView = (ListView) findViewById(R.id.favorites_listView);
		warning = (TextView) findViewById(R.id.favorites_warning);
		
		//give our users a back button in the Action Bar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		//grab our shared preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(baseContext);
		
		//set up our title to match userName from sharedPreferences
		if(!(prefs.getString("userName", "").toString().isEmpty())) {
			String userName = prefs.getString("userName", "").toString();
			title.setText(userName + "'" +"s " + "Favorites:");
		}
		
		
		//get our saved stories from the FileManager
		FileManager fileManager = FileManager.GetInstance();
		JSONArray favorites = fileManager.readFavorites(baseContext, fileName);
		
		//create and apply an adapter for our listview
		if (favorites != null && favorites.length() > 0) {
			JSONObject storyObject = new JSONObject();
			list = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < favorites.length(); i++) {
				try {
					storyObject = favorites.getJSONObject(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//create a new hashmap to hold all data for each story
				HashMap<String, Object> storyMap = new HashMap<String, Object>();
				try {
					//put all of our data needed for each story into the hashmap
					storyMap.put("headline", storyObject.get("title"));
					storyMap.put("date", storyObject.get("date"));
					storyMap.put("imageLink", storyObject.get("imageUrl"));
					storyMap.put("url", storyObject.get("storyUrl"));
					storyMap.put("description", storyObject.get("description"));
					storyMap.put("rating", storyObject.get("rating"));
					
					//need to check what rating was saved so we can properly set the related image in the listView items
					int iconId;
					int savedRating = storyObject.getInt("rating");
					switch (savedRating) {
						//zero stars
						case 0:
							iconId = R.drawable.zerostar;
							break;
						//one star
						case 1:
							iconId = R.drawable.onestar;
							break;
						//two stars
						case 2:
							iconId = R.drawable.twostar;
							break;
						//three stars
						case 3:
							iconId = R.drawable.threestar;
							break;
						//four stars	
						case 4:
							iconId = R.drawable.fourstar;
							break;
						//five stars	
						case 5:
							iconId = R.drawable.fivestar;
							break;
						//default, set to 0 just as a fallback	
						default:
							iconId = R.drawable.zerostar;	
							break;
					}
					
					//add which ever icon (int) to the hashmap, so we can show the correct number of saved stars
					storyMap.put("ratingIcon", iconId);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//add our hashmap to our arrayList
				list.add(storyMap);
			}
			//now that we have all of our data, create an adapter to populate our customized listView rows
			SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), list,
					R.layout.favorites_row, new String[] { (String) "headline",
							(String) "date", "description", "ratingIcon"},
					new int[] { R.id.favRow_title, R.id.favRow_date, R.id.favRow_description,
							R.id.favRow_rating});
			System.out.println("SetData method called on Fragment side!");
			//set our adapter
			listView.setAdapter(adapter);
			
			//set up our listView's onItemClickListener
			listView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					//check our editing boolean, which determines what action the user wants to take
					if (editing == true)
					{ //user is in editing mode, so allow them to remove a story from favorites
						TextView selectedStory = (TextView) view.findViewById(R.id.favRow_title);
						
						//grab the title of the story that the user selected
		        		final String selectedTitle = selectedStory.getText().toString();
		        		
		        		//show an alert dialog to warn the user of what they are doing
		        		new AlertDialog.Builder(thisActivity)
		        		.setTitle("Confirm removal")
		        		.setMessage("Are you sure you want to remove " + "\"" + selectedTitle + "\"" + "?")
		        		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// user selected yes, so delete the favorited story and reload the listView
								FileManager fileManager = FileManager.GetInstance();
								fileManager.deleteFavorite(baseContext, MainActivity.favFileName, selectedTitle);
								reloadList();
							}
						})
						
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// don't need to do anything as 'cancel' is handled for us
								
							}
						})
		        		.show();
					} else {
						//editing is currently off, so launch the DetailView using this story's data (may not have time to implement this)
						HashMap<String, Object> dataMap = list.get(position);
						System.out.println("Selected position in listview was:  " + position);
						System.out.println("Story hashmap being sent is:  "  + dataMap);
						Intent showDetail = new Intent(getBaseContext(), DetailView.class);
				    	showDetail.putExtra("data", dataMap);
				    	startActivityForResult(showDetail, 0);
					}
					
				}
				
			});
			
		} else {
			//there are no stories to display to the user (they haven't favorited anything yet), so alert them why 
			//activity appears empty
			warning.setText(R.string.favorites_noStories);
			warning.setTextColor(Color.RED);
		}
	}
	//we can reuse our action bar from the DetailView activity since it is identical for our purposes
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//inflate our custom menu for this activity so that the user can remove stories if they want
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detail_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	//handle whatever action is necessary when the user selects something within the Action Bar
	public boolean onOptionsItemSelected(MenuItem item) {
		
		//grab the id of the item selected
		int id = item.getItemId();
		
		//check if it's equal to the "favorites" icon, otherwise it was the back button
		if (id == R.id.menu_detail_favorite) {
			
			//only allow the button to do something if our listview contains data
			if (listView.getCount() > 0 && listView.getVisibility() == View.VISIBLE)
			{
				//if editing is currently true, set to false and disable "edit" mode/reset our textview's text and appearance
				if (editing == true) {
					editing = false;
					warning.setText(R.string.favorites_warning);
					warning.setTextColor(Color.WHITE);
				} else {
					//editing boolean was false, so enable "edit" mode and set our textView to instruct the user
					editing = true;
					warning.setText("Tap a story to remove it from your favorites.  Tap the favorites icon again to stop.");
					warning.setTextColor(Color.RED);
				}
			}
		} else {
			//back button was tapped, finish activity
			super.finish();
		}	
		return true;
	}
	
	//this method just lets us manually reload our listview
	//this is used when a user has deleted a favorite from here, or when they traverse back to this activity after
	//loading and deleting a favorite from within the DetailActivity
	public void reloadList() {
		// get our saved stories from the FileManager
		FileManager fileManager = FileManager.GetInstance();
		
		//create a JSONArray and set to the return value from FileManger's readFavorites method
		JSONArray favorites = fileManager.readFavorites(baseContext, fileName);

		// create and apply an adapter for our listview
		if (favorites != null && favorites.length() > 0) {
			JSONObject storyObject = new JSONObject();
			
			//overwrite our arrayList with the most recent/accurate data
			list = new ArrayList<HashMap<String, Object>>();
			
			//for the number of object contained in arraylist, create JSONObjects for each
			for (int i = 0; i < favorites.length(); i++) {
				try {
					storyObject = favorites.getJSONObject(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//create a hashmap object to hold the data for each JSONObject
				HashMap<String, Object> storyMap = new HashMap<String, Object>();
				try {
					//add each piece of data to the hashmap
					storyMap.put("headline", storyObject.get("title"));
					storyMap.put("date", storyObject.get("date"));
					storyMap.put("imageLink", storyObject.get("imageUrl"));
					storyMap.put("url", storyObject.get("storyUrl"));
					storyMap.put("description", storyObject.get("description"));
					storyMap.put("rating", storyObject.get("rating"));
					
					//need to check what rating was saved so we can properly set the related image in the listView items
					int iconId;
					int savedRating = storyObject.getInt("rating");
					
					//like in above method, check the id against the rating, so we can apply the right icon
					switch (savedRating) {
						// 0 stars
						case 0:
							iconId = R.drawable.zerostar;
							break;
						//one star
						case 1:
							iconId = R.drawable.onestar;
							break;
						//two stars
						case 2:
							iconId = R.drawable.twostar;
							break;
						//three stars	
						case 3:
							iconId = R.drawable.threestar;
							break;
						//four stars	
						case 4:
							iconId = R.drawable.fourstar;
							break;
						//five stars	
						case 5:
							iconId = R.drawable.fivestar;
							break;
						//default - apply 0 just in case
						default:
							iconId = R.drawable.zerostar;	
							break;
					} 
					
					//add our iconId (int) to the hashmap
					storyMap.put("ratingIcon", iconId);
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//add each hashmap to our arraylist
				list.add(storyMap);
			}
			// now that we have all of our data, create an adapter to populate
			// our customized listView rows
			SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), list,
					R.layout.favorites_row, new String[] { (String) "headline",
							(String) "date", "description", "ratingIcon" }, new int[] {
							R.id.favRow_title, R.id.favRow_date,
							R.id.favRow_description, R.id.favRow_rating});
			
			// set our adapter
			listView.setAdapter(adapter);
		} else {
			//no stories to display within our listview, so alert the user and hide the listview
			listView.setVisibility(View.GONE);
			listView.invalidate();
			warning.setText(R.string.favorites_noStories);
			warning.setTextColor(Color.RED);
		}
	}
	
	//this just checks for a boolean that is returned from DetailsActivity, which indicates whether or not
	//the user modified their favorites from within that activity.  If so, we need to refresh the favorites list
	//so its current and doesn't confuse the user into thinking they may not have actually deleted something
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//check to make sure our detailView exited successfully, and then get the returned data
		if (resultCode == RESULT_OK && requestCode == 0)
		{
			Bundle result = data.getExtras();
			boolean wasChanged = result.getBoolean("changedFav");
			//data may have been changed, reload our listview
			if (wasChanged == true)
			{
				reloadList();
			}
		}
	}
}
