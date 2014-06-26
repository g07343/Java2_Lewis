/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File DetailView.java
 * 
 * Purpose The DetailView class holds the detail activity, which is loaded whenever the user selects a sports story from 
 * MainActivity's listview.  It is passed all of the data it needs to display to the user, and returns the rating the user set.
 * 
 */
package com.matthewlewis.sportscaster;


import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class DetailView extends Activity implements DetailViewFragment.detailsFragmentInterface {
	
	private String imageUrl;
	private String title;
	private String storyUrl;
	DetailViewFragment detailFragment;
	String description;
	String date;
	Integer rating;
	Bitmap image;
	boolean alreadySaved;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		setContentView(R.layout.fragment_detail);
			
		//set up our boolean to help in toggling the saved favorite state
		alreadySaved = false;
		
		// set up our reference to the MainActivityFragment so we can use it to
		// call methods when needed
		detailFragment = (DetailViewFragment) getFragmentManager().findFragmentById(R.id.detail_fragment);
		
		
		// check orientation and if in landscape, end the activity
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			finish();
			return;
		}

		//set up a default rating number in case the user never sets a new one
		rating = 0;
		
		//add a nice back button using built-in functionality rather than trying to cram in more UI
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		//grab our intent and bundle for our info
		Bundle data = getIntent().getExtras();
		@SuppressWarnings("unchecked")
		
		//get our passed hashmap
		HashMap<String, Object> storyData = (HashMap<String, Object>) data.get("data");
		
		//grab our individual bits of data to apply to the UI
		title = (String) storyData.get("headline");
		String date = (String) storyData.get("date");
		String description = (String) storyData.get("description");
		imageUrl = (String) storyData.get("imageLink");
		storyUrl = (String) storyData.get("url");
		
		//call a method to send our data to our child fragment
		displayDetails(title, date, description, imageUrl, storyUrl);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//inflate our custom menu for this activity so that the user can save stories as favorites
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detail_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	//override the default "back button" method to allow us to detect it and go back to MainActivity
	@Override
	public void onBackPressed() {
		System.out.println("Back button pressed!");
		this.finish();
	}

	// call this function when we have signaled the activity is done, and pass
	// the rating and title back to MainActivity
	@Override
	public void finish() {
		//create an intent to send back to MainActivity
		Intent data = new Intent();
		
		//if title is equal to null at this point, then we know the phone has been rotated to landscape
		//from the protrait view/single activity, so grab the data we need to repopulate the DetailViewFragment
		if (title == null)
		{
			title = detailFragment.title;
			date = detailFragment.date;
		} else {
			
			date = (String) detailFragment.dateView.getText();
		}
		//make sure the rating isn't null, which it can be when in landscape
		if (rating == null)
		{
			rating = detailFragment.rating;
		} else {
			rating = (int) detailFragment.ratingBar.getRating();
		}
		
		//grab the rest of the data from the fragment UI to return to the MainActivity
		image = detailFragment.image;
		description = detailFragment.description;
		storyUrl = detailFragment.storyUrl;
		imageUrl = detailFragment.imageUrl;
		
		//add the data to our intent
		data.putExtra("date", date);
		data.putExtra("link", storyUrl);
		data.putExtra("description", description);
		data.putExtra("imageLink", imageUrl );
		
		//we only really need to pass the title and rating if the device is in portrait
		data.putExtra("title", title);
		data.putExtra("rating", rating);
		
		setResult(RESULT_OK, data);
		super.finish();
	}

	// the below function detects which button in the action bar is selected - either the back button or the favorites icon
	public boolean onOptionsItemSelected(MenuItem item) {
		//get the id of whatever is selected, so we know if it is the favorites icon
		int id = item.getItemId();
		if (id == R.id.menu_detail_favorite)
		{  
			FileManager fileManager = FileManager.GetInstance();
			if (alreadySaved == false) {
				JSONObject story = new JSONObject();
				try {
					story.put("title", title);
					story.put("date", date);
					story.put("imageUrl", imageUrl);
					story.put("storyUrl", storyUrl);
					story.put("rating", rating);
					story.put("description", description);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String isFavorite = fileManager.writeFavorites(getApplicationContext(), MainActivity.favFileName, story);
				if (isFavorite != null) {
					if (isFavorite.equals("Story is already a favorite"))
					{
						alreadySaved = true;
						//alert user they previously saved this story and set our boolean to false to allow them to delete it if they want to
						Toast.makeText(getApplicationContext(),
								"This story was already a favorite.  Tap the icon again to unfavorite.",
								Toast.LENGTH_LONG).show();
						
					} else {
						alreadySaved = true;
						//story successfully saved, alert user
						Toast.makeText(getApplicationContext(),
								"Story successfully saved!  To remove it, tap the icon again.",
								Toast.LENGTH_LONG).show();
					}
				} else {
					alreadySaved = true;
					//story successfully saved, alert user
					Toast.makeText(getApplicationContext(),
							"Story successfully saved!  To remove it, tap the icon again.",
							Toast.LENGTH_LONG).show();
				}
				
			} else {
				//story was already saved previously, so delete it and alert the user
				fileManager.deleteFavorite(getApplicationContext(), MainActivity.favFileName, title);
				Toast.makeText(getApplicationContext(),
						"Story removed from favorites.  To add again, tap the icon.",
						Toast.LENGTH_LONG).show();
				alreadySaved = false;
			}
			
		} else {
			//since there is only the favorites or the back button, it must be back
			this.finish();
		}
		return true;
	}

	// this function simply communicates our data to our attached fragment
	public void displayDetails(String storyTitle, String storyDate,
			String storyDescription, String imageLink, final String storyLink) {
		detailFragment.populateData(storyTitle, storyDate, storyDescription,
				imageLink, storyLink, null);
	}

	//lets us set the rating for our child fragment's "rating" integer
	public void setRating(int number) {
		rating = number;
	}

	//this method allows the DetailsFragment to grab the DetailView class's current value for "rating".
	@Override
	public Integer getRating() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
