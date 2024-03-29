/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File DetailViewFragment.java
 * 
 * Purpose This fragment contains the entire UI layout for the DetailView, and communicates with the DetailView class to both receive the data it displays,
 * and communicate back the rating selected for a story, if any.
 * 
 */
package com.matthewlewis.sportscaster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

public class DetailViewFragment extends Fragment implements OnRatingBarChangeListener {

	private ImageView storyImage;
	private Button webBtn;
	private Button shareBtn;
	RatingBar ratingBar;
	TextView titleView;
	TextView dateView;
	TextView descriptionView;
	Bitmap image;
	String imageUrl;
	String title;
	String date;
	String description;
	String storyUrl;
	Integer rating;
	
	public interface detailsFragmentInterface {
		
		void displayDetails(String storyTitle, String storyDate, String storyDescription, String imageLink, final String storyLink);
		void setRating(int number);
		Integer getRating();
	}
	
	private detailsFragmentInterface parentActivity;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		//make sure the activity we attach to implements our detailsFragmentInterface
		if (activity instanceof detailsFragmentInterface)
		{
			parentActivity = (detailsFragmentInterface)activity;
		} else {
			throw new ClassCastException(activity.toString() + "must implement detailsFragmentInterface");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.detailview, container);
		
		//grab our interface elements contained within the fragment
		storyImage = (ImageView) view.findViewById(R.id.detail_image);
		webBtn = (Button) view.findViewById(R.id.detail_webBtn);
		shareBtn = (Button) view.findViewById(R.id.detail_shareBtn);
		ratingBar = (RatingBar) view.findViewById(R.id.detail_rating);
		titleView = (TextView) view.findViewById(R.id.detail_title);
		dateView = (TextView) view.findViewById(R.id.detail_date);
		descriptionView = (TextView) view.findViewById(R.id.detail_description);
		
		//set up a listener for when the user changes the rating
		ratingBar.setOnRatingBarChangeListener(this);
		
		//check to see if the parentActivity has a "savedRating" value and apply is so
		Integer savedRating = parentActivity.getRating();
		if (savedRating != null)
		{
			ratingBar.setRating(savedRating);
		} 
		
		//set a text size according to device orientation, which helps us with the description text being too large
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			descriptionView.setTextSize(14);
		} else {
			descriptionView.setTextSize(18);
		}
		
		// check to see if we have a saved instance - if the view was destroyed, else set to the default image
		if (savedInstanceState != null) {
			//grab the data from our savedInstance so we can reapply it to our interface
			image = (Bitmap) savedInstanceState.getParcelable("image");
			title = (String) savedInstanceState.getString("title");
			date = (String) savedInstanceState.getString("date");
			description = (String) savedInstanceState.getString("description");
			storyUrl = (String) savedInstanceState.getString("storyLink");
			imageUrl = (String) savedInstanceState.getString("imageLink");
			rating = (int) savedInstanceState.getInt("rating");
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
		
		return view;
	}
	
	//this method is the primary way for the parentActivity to supply the data to the fragment for display to the user
	public void populateData(String storyTitle, String storyDate, String storyDescription, String imageUrlString, final String storyLink, Integer rating) {
		
		//set passed rating to ratingBar if not null
		if (rating != null)
		{
			ratingBar.setRating(rating);
		}
		//set passed data to textViews
		titleView.setText(storyTitle);
		
		dateView.setText(storyDate);
		
		descriptionView.setText(storyDescription);
		
		//set our global String to the one passed in so we can reference other places
		description = storyDescription;
		
		//if necessary, allow the descriptionTextView to scroll if the content is too long
		descriptionView.setMovementMethod(new ScrollingMovementMethod());
		
		//set our global imageUrlString so we can access it from other places
		imageUrl = imageUrlString;
		
		//need to make sure we have internet or else we don't want to load the story's image or enable the web link button
		NetworkManager manager = new NetworkManager();
		Boolean isConnected = manager.connectionStatus(MainActivity.context);
		if (isConnected)
		{  
			//check to make sure we don't already have the image (from before the view was destroyed)
			if (image == null)
			{
				//we have internet currently, so get the image to replace the default one using internal async class
				if (imageUrl != null)
				{//make sure that we had originally found a url for an image to begin with
					DetailViewFragment.getImage getImage = new getImage();
					getImage.execute();
				}
			} else {
				//since we still have the image from before, load that
				storyImage.setImageBitmap(image);
			}
			
			
			//check to make sure we were able to get a valid link to the story on ESPN
			if (!storyLink.equals("No link provided for this story."))
			{	//set our global string to the value passed so we can save it if needed
				storyUrl = storyLink;
				
				//set an onClickListener to the webBtn so that we can launch our implicit intent
				webBtn.setOnClickListener(new OnClickListener(){
					
					@Override
					public void onClick(View v) {
						//use our web url to create an implicit intent so the user can visit the stories page on ESPN
						System.out.println("URL was:  " + storyLink);
						
						Uri webpage = Uri.parse(storyLink);
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
						emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, storyLink);
						
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
		
		
		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
			//Save our data in case our fragments gets destroyed
			System.out.println("DetailFragment onSaveInstanceState runs!!!");
			String title = (String) titleView.getText();
			String date = (String) dateView.getText();
			
			//we need to save all of this data since we can't be sure if the view is being destroyed due to
			//the user rotating the device or something else. 
			savedInstanceState.putString("date", date);
			savedInstanceState.putString("storyLink", storyUrl);
			savedInstanceState.putString("imageLink", imageUrl);
			savedInstanceState.putString("description", description);
			savedInstanceState.putParcelable("image", image);
			savedInstanceState.putString("title", title);
			rating = (int) ratingBar.getRating();
			parentActivity.setRating(rating);
			
			//check if the parentActivity has a rating value already, so we can ensure we don't end up with a NPI.
			//this can happen when the device is rotated a couple times without the user actually inputting a value
			Integer saved = parentActivity.getRating();
			if (saved != null)
			{
				savedInstanceState.putInt("rating", saved);
			} else {
				rating = (int) ratingBar.getRating();
				savedInstanceState.putInt("rating", rating);
			}
			super.onSaveInstanceState(savedInstanceState);
		}
		
		//use this to alert the DetailView that the rating was set so that it passes back the correct rating
		@Override
		public void onRatingChanged(RatingBar ratingBar, float rating,
				boolean fromUser) {
			//grab our current rating since the user changed it and make sure to update parentActivity's value
			int intRating = (int)rating;
			parentActivity.setRating(intRating);
			System.out.println("Rating set on:  " + parentActivity.getClass().getName());
		}
		
		//we use this function to manually "destroy" an old story's image when the user selects a new one in landscape
		//in this way, the above logic within populateData() will retrieve the new story's image
		public void clearImage() {
			image = null;
		}
		
		//allows the parentActivity to manually apply a rating to our ratingBar
		public void setRating(int passedRating) {
			
			ratingBar.setRating(passedRating);
		}
}

