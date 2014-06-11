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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailView extends Activity{
	
	ImageView storyImage;
	Button webBtn;
	String imageUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailview);
		
		Bundle data = getIntent().getExtras();
		@SuppressWarnings("unchecked")
		HashMap<String, Object> storyData = (HashMap<String, Object>) data.get("data");
		String title = (String) storyData.get("headline");
		String date = (String) storyData.get("date");
		String description = (String) storyData.get("description");
		imageUrl = (String) storyData.get("imageLink");
		
		//get our various views within our xml and assign our passed data
		
		storyImage = (ImageView) findViewById(R.id.detail_image);
		webBtn = (Button) findViewById(R.id.detail_webBtn);
		
		TextView titleView = (TextView) findViewById(R.id.detail_title);
		titleView.setText(title);
		
		TextView dateView = (TextView) findViewById(R.id.detail_date);
		dateView.setText(date);
		
		TextView descriptionView = (TextView) findViewById(R.id.detail_description);
		descriptionView.setText(description);
		
		//need to make sure we have internet or else we don't want to load the story's image or enable the web link button
		NetworkManager manager = new NetworkManager();
		Boolean isConnected = manager.connectionStatus(getBaseContext());
		if (isConnected)
		{  
			//we have internet currently, so get the image to replace the default one using internal async class
			if (imageUrl != null)
			{//make sure that we had originally found a url for an image to begin with
				DetailView.getImage getImage = new getImage();
				getImage.execute();
			}		
			
		} else {
			//no internet, so leave the default image in place, and disable the web button
			webBtn.setEnabled(false);
		}
	}
	
	//use an async class to get our image from the url
	public class getImage extends AsyncTask<String, Void, String> {
		Bitmap image;
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
			if (image != null)
			{
				storyImage.setImageBitmap(image);
			}
		}
	
	}
	
	@Override
	public void onBackPressed() {
		System.out.println("Back button pressed!");
		this.finish();
	}
	
	@Override
	public void finish() {
		
	}
}
