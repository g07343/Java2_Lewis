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
 * As of week 4, it also has the capability to display any saved "Favorite" stories the user wanted to keep.
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
	static Integer rating;
	Bitmap image;
	boolean alreadySaved;
	boolean changedFavStatus;
	HashMap<String, Object> storyData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		setContentView(R.layout.fragment_detail);
			
		//in case the user edits the favorites status after launching this activity from the "favorites" activity,
		//we need a boolean to send back instructing the calling activity to refresh its listview
		changedFavStatus = false;
		
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
		date = (String) storyData.get("date");
		description = (String) storyData.get("description");
		imageUrl = (String) storyData.get("imageLink");
		storyUrl = (String) storyData.get("url");
		rating = (Integer) storyData.get("rating");
		//check if rating is null or not, which can be different depending on if DetailView was launched
		//from the MainActivity, or from the FavoritesActivity
		if (rating != null)
		{
			setRating(rating);
		} else {
			rating = 0;
		}
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
		data.putExtra("changedFav", changedFavStatus);
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
			//set this to true, even if the user doesn't end up changing it (for now)
			//this is VERY important, as it allows the favorites activity to reset it's listview in the event
			//the user chose to "unfavorite" a story from here.  This way we don't display old data
			changedFavStatus = true;
			
			//grab our FileManager singleton to allow the user to either favorite or unfavorite the story
			FileManager fileManager = FileManager.GetInstance();
			
			//check our boolean that tells us whether the story has been saved or not.  By default, this is false the first time
			if (alreadySaved == false) {
				//create a JSONObject to hold the stories data
				JSONObject story = new JSONObject();
				try {
					//grab all needed data for the story and add to JSONObject
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
				
				//attempt to save/favorite the story, and capture the returned string for evaluation
				String isFavorite = fileManager.writeFavorites(getApplicationContext(), MainActivity.favFileName, story);
				
				//if the string is valid and equals "Story is already a favorite", it had already been saved
				if (isFavorite != null) {
					if (isFavorite.equals("Story is already a favorite"))
					{
						//alert user they previously saved this story and set our boolean to false to allow them to delete it if they want to
						alreadySaved = true;
						Toast.makeText(getApplicationContext(),
								"This story was already a favorite.  Tap the icon again to unfavorite.",
								Toast.LENGTH_LONG).show();
						
					} else {
						alreadySaved = true;
						//story successfully saved, alert user
						Toast.makeText(getApplicationContext(),
								"Story favorited!  To undo, tap the icon again.",
								Toast.LENGTH_LONG).show();
					}
				} else {
					//set our boolean to "true", since the returned string was null, meaning it was written successfully
					alreadySaved = true;
					//story successfully saved, alert user
					Toast.makeText(getApplicationContext(),
							"Story favorited!  To undo, tap the icon again.",
							Toast.LENGTH_LONG).show();
				}
				
			} else {
				//story was already saved previously, so delete it and alert the user
				fileManager.deleteFavorite(getApplicationContext(), MainActivity.favFileName, title);
				Toast.makeText(getApplicationContext(),
						"Story removed from favorites.  To add again, tap the icon.",
						Toast.LENGTH_LONG).show();
				
				//set our boolean to false, so that the user can re-add if they choose to
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
				imageLink, storyLink, rating);
	}

	//lets us set the rating for our child fragment's "rating" integer
	public void setRating(int number) {
		rating = number;
	}

	//this method allows the DetailsFragment to grab the DetailView class's current value for "rating".
	@Override
	public Integer getRating() {
		// TODO Auto-generated method stub
		return rating;
	}
	
}
