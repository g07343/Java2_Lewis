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
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;

public class DetailView extends Activity implements DetailViewFragment.detailsFragmentInterface {
	
	private String imageUrl;
	private String title;
	private String storyUrl;
	DetailViewFragment detailFragment;
	String description;
	String date;
	int rating;
	Bitmap image;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		setContentView(R.layout.fragment_detail);
			
		// set up our reference to the MainActivityFragment so we can use it to
		// call methods when needed
		detailFragment = (DetailViewFragment) getFragmentManager().findFragmentById(R.id.detail_fragment);
		detailFragment.showInterface(true);
		
		// check orientation
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
		
		displayDetails(title, date, description, imageUrl, storyUrl);
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
		Intent data = new Intent();
		rating = (int) detailFragment.ratingBar.getRating();
		//if title is equal to null at this point, then we know the phone has been rotated to landscape
		//from the protrait view/single activity, so grab the data we need to repopulate the DetailViewFragment
		if (title == null)
		{
			title = detailFragment.title;
			rating = detailFragment.rating;
			image = detailFragment.image;
			date = detailFragment.date;
			description = detailFragment.description;
			storyUrl = detailFragment.storyUrl;
			imageUrl = detailFragment.imageUrl;
			data.putExtra("date", date);
			data.putExtra("link", storyUrl);
			data.putExtra("description", description);
			data.putExtra("imageLink", imageUrl );
		}
		
		//we only really need to pass the title and rating if the device is in portrait
		data.putExtra("title", title);
		data.putExtra("rating", rating);
		setResult(RESULT_OK, data);
		super.finish();
	}

	// the below function simply detects when the user returns to the
	// MainActivity using the back button in the ActionBar
	public boolean onOptionsItemSelected(MenuItem item) {
		this.finish();
		return true;
	}

	// this function simply communicates our data to our attached fragment
	public void displayDetails(String storyTitle, String storyDate,
			String storyDescription, String imageLink, final String storyLink) {
		detailFragment.populateData(storyTitle, storyDate, storyDescription,
				imageLink, storyLink);
	}

	public void setRating(int number) {
		rating = number;
		System.out.println("Rating is:  " + rating);
	}
}
