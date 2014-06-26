package com.matthewlewis.sportscaster;

import org.json.JSONArray;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.TextView;

public class FavoritesActivity extends Activity{

	TextView title;
	TextView warning;
	ListView listView;
	Context baseContext;
	String fileName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.favorites);
		
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
		if (favorites != null) {
			
		} else {
			warning.setText(R.string.favorites_noStories);
			warning.setTextColor(Color.RED);
		}
	}
	
}
