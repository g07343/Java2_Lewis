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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class DetailView extends Activity{
	
	private ImageView storyImage;
	private Button webBtn;
	private Button shareBtn;
	private RatingBar ratingBar;
	private String imageUrl;
	private String title;
	private String storyUrl;
	private Bitmap image;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailview);
		
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
		
		//get our various views within our xml and assign our passed data
		storyImage = (ImageView) findViewById(R.id.detail_image);
		webBtn = (Button) findViewById(R.id.detail_webBtn);
		shareBtn = (Button) findViewById(R.id.detail_shareBtn);
		ratingBar = (RatingBar) findViewById(R.id.detail_rating);
		
		
		// check to see if we have a saved instance - if the view was destroyed, else set to the default image
		if (savedInstanceState != null) {
			image = (Bitmap) savedInstanceState.getParcelable("image");
			if (image != null)
			{  //view was destroyed previously so set saved image
				storyImage.setImageBitmap(image);
			} else {
				//no image was saved, so load the default image instead
				Drawable defaultImage = getResources().getDrawable(R.drawable.generic);
				storyImage.setImageDrawable(defaultImage);
			}
			
		} else {
			//no savedInstanceState, so load the default image
			Drawable defaultImage = getResources().getDrawable(R.drawable.generic);
			storyImage.setImageDrawable(defaultImage);
		}
		
		//grab our various textViews and set their data from the Intent
		TextView titleView = (TextView) findViewById(R.id.detail_title);
		titleView.setText(title);
		
		TextView dateView = (TextView) findViewById(R.id.detail_date);
		dateView.setText(date);
		
		TextView descriptionView = (TextView) findViewById(R.id.detail_description);
		descriptionView.setText(description);
		descriptionView.setMovementMethod(new ScrollingMovementMethod());
		
		//need to make sure we have internet or else we don't want to load the story's image or enable the web link button
		NetworkManager manager = new NetworkManager();
		Boolean isConnected = manager.connectionStatus(getBaseContext());
		if (isConnected)
		{  
			//check to make sure we don't already have the image (from before the view was destroyed)
			if (image == null)
			{
				//we have internet currently, so get the image to replace the default one using internal async class
				if (imageUrl != null)
				{//make sure that we had originally found a url for an image to begin with
					DetailView.getImage getImage = new getImage();
					getImage.execute();
				}
			} else {
				//since we still have the image from before, load that
				storyImage.setImageBitmap(image);
			}
			
			
			//check to make sure we were able to get a valid link to the story on ESPN
			if (!storyUrl.equals("No link provided for this story."))
			{
				//set an onClickListener to the webBtn so that we can launch our implicit intent
				webBtn.setOnClickListener(new OnClickListener(){
					
					@Override
					public void onClick(View v) {
						//use our web url to create an implicit intent so the user can visit the stories page on ESPN
						System.out.println("URL was:  " + storyUrl);
						
						Uri webpage = Uri.parse(storyUrl);
						Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
						startActivity(webIntent);
					}
					
				});
				
				//also set up an implicit intent to allow the user to share the story
				shareBtn.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						//set up our intent to allow the user to share the story
						Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
						emailIntent.setType("plain/text");
						
						//set a body for the email, in this case, the link to the story
						emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, storyUrl);
						
						//set a title for the email
						emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this awesome sports story I found with SportsCaster!");
						startActivity(emailIntent);
					}
					
				});
				
			} else {
				//since MainActivity was unable to find a link for this story, disable these buttons
				webBtn.setEnabled(false);
				shareBtn.setEnabled(false);
			}		
			
		} else {
			//no internet, so leave the default image in place, and disable the web button
			webBtn.setEnabled(false);
			shareBtn.setEnabled(false);
		}
	}
	
	//use an async class to get our image from the url
	public class getImage extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				URL imageLink = new URL(imageUrl);
				try {
					image = BitmapFactory.decodeStream(imageLink.openConnection().getInputStream());
				} catch (IOException e) {
					Log.i("GET_IMAGE", "Error retrieving image from URL");
					e.printStackTrace();
				}
			} catch (MalformedURLException e) {
				Log.i("GET_IMAGE", "Error creating URL from string");
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String s) {
			//verify we actually got the image and if so, set to the imageView
			if (image != null)
			{
				storyImage.setImageBitmap(image);
			}
		}
	
	}
	
	//override the default "back button" method to allow us to detect it and go back to MainActivity
	@Override
	public void onBackPressed() {
		System.out.println("Back button pressed!");
		this.finish();
	}
	
	//call this function when we have signaled the activity is done, and pass the rating and title back to MainActivity
	@Override
	public void finish() {
		Intent data = new Intent();
		float rating = ratingBar.getRating();
		Integer intRating = (int)rating;
		data.putExtra("title", title);
		data.putExtra("rating", intRating);
		setResult(RESULT_OK, data);
		super.finish();
	}
	
	//the below function simply detects when the user returns to the MainActivity using the back button in the ActionBar
	public boolean onOptionsItemSelected(MenuItem item) {
		this.finish();
		return true;
	}
	
	//make sure to save our image for when the view gets destroyed to avoid redownloading it.
	//Since the activity by default holds onto the intent it was passed, we don't need to save the other data, since we would simply 
	//be reloading it in the same way.
		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
			//save the image, even if it is null, meaning there was no internet connection originally
			//we can check whether the image is null in the onCreate function and react accordingly
			System.out.println("DETAILS VIEW DESTROYED!!!");
			
			savedInstanceState.putParcelable("image", image);
			
			super.onSaveInstanceState(savedInstanceState);
		}
}
