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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		thisActivity = this;
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
		System.out.println("Array of stories was:  " + favorites);
		//create and apply an adapter for our listview
		if (favorites != null && favorites.length() > 0) {
			JSONObject storyObject = new JSONObject();
			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < favorites.length(); i++) {
				try {
					storyObject = favorites.getJSONObject(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HashMap<String, Object> storyMap = new HashMap<String, Object>();
				try {
					storyMap.put("title", storyObject.get("title"));
					storyMap.put("date", storyObject.get("date"));
					storyMap.put("imageUrl", storyObject.get("imageUrl"));
					storyMap.put("storyUrl", storyObject.get("storyUrl"));
					storyMap.put("description", storyObject.get("description"));
					storyMap.put("rating", storyObject.get("rating"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				list.add(storyMap);
			}
			//now that we have all of our data, create an adapter to populate our customized listView rows
			SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), list,
					R.layout.favorites_row, new String[] { (String) "title",
							(String) "date", "description"},
					new int[] { R.id.favRow_title, R.id.favRow_date, R.id.favRow_description,
							});
			System.out.println("SetData method called on Fragment side!");
			//set our adapter
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (editing == true)
					{ //user is in editing mode, so allow them to remove a story from favorites
						TextView selectedStory = (TextView) view.findViewById(R.id.favRow_title);
		        		final String selectedTitle = selectedStory.getText().toString();
		        		System.out.println("Story to be deleted:  " + selectedTitle);
		        		
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
					}
					
				}
				
			});
			
		} else {
			warning.setText(R.string.favorites_noStories);
			warning.setTextColor(Color.RED);
		}
	}
	//we can reuse our action bar from the DetailView activity since it is identical for our purposes
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//inflate our custom menu for this activity so that the user can save stories as favorites
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detail_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_detail_favorite) {
			if (listView.getCount() > 0 && listView.getVisibility() == View.VISIBLE)
			{
				if (editing == true) {
					editing = false;
					warning.setText(R.string.favorites_warning);
					warning.setTextColor(Color.WHITE);
				} else {
					editing = true;
					warning.setText("Tap a story to remove it from your favorites.  Tap the favorites icon again to stop.");
					warning.setTextColor(Color.RED);
				}
			}
		} else {
			super.finish();
		}	
		return true;
	}
	
	//this method just lets us manually reload our listview
	public void reloadList() {
		// get our saved stories from the FileManager
		FileManager fileManager = FileManager.GetInstance();
		JSONArray favorites = fileManager.readFavorites(baseContext, fileName);

		// create and apply an adapter for our listview
		if (favorites != null && favorites.length() > 0) {
			JSONObject storyObject = new JSONObject();
			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < favorites.length(); i++) {
				try {
					storyObject = favorites.getJSONObject(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HashMap<String, Object> storyMap = new HashMap<String, Object>();
				try {
					storyMap.put("title", storyObject.get("title"));
					storyMap.put("date", storyObject.get("date"));
					storyMap.put("imageUrl", storyObject.get("imageUrl"));
					storyMap.put("storyUrl", storyObject.get("storyUrl"));
					storyMap.put("description", storyObject.get("description"));
					storyMap.put("rating", storyObject.get("rating"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				list.add(storyMap);
			}
			// now that we have all of our data, create an adapter to populate
			// our customized listView rows
			SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), list,
					R.layout.favorites_row, new String[] { (String) "title",
							(String) "date", "description" }, new int[] {
							R.id.favRow_title, R.id.favRow_date,
							R.id.favRow_description, });
			System.out.println("SetData method called on Fragment side!");
			// set our adapter
			listView.setAdapter(adapter);
		} else {
			listView.setVisibility(View.GONE);
			listView.invalidate();
			warning.setText(R.string.favorites_noStories);
			warning.setTextColor(Color.RED);
		}
	}
}
