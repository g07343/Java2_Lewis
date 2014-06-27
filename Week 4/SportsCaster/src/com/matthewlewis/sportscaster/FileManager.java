/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File FileManager.java
 * 
 * Purpose The FileManager class exists as a singleton, and is solely responsible for reading and writing the data file
 * to the device.  As of week 4, FileManager also is in charge of maintaining the user's favorited stories.  It writes, reads, and 
 * deletes them from a "favorites.txt" file.
 * 
 */
package com.matthewlewis.sportscaster;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class FileManager {

	private static FileManager _instance;

	/**
	 * Instantiates a new file manager.
	 */
	private FileManager() {

	}

	/**
	 * Gets the instance.
	 * 
	 * @return the file manager
	 */
	public static FileManager GetInstance() {
		if (_instance == null) {
			_instance = new FileManager();
		}

		return _instance;
	}

	/**
	 * This function writes our data to the device storage using an output
	 * stream. It returns a boolean depending on the result.
	 */
	public Boolean WriteFile(Context context, String fileName, String content) {
		Boolean wasWritten = false;

		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			Log.i("WRITE_FILE", "Success");
			wasWritten = true;
		} catch (FileNotFoundException e) {
			Log.i("WRITE_FILE", "Error writing file");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("WRITE_FILE", "Failure");
		}

		return wasWritten;
	}

	//this function writes our favorites to a file on the device, for later retrieval
	public String writeFavorites(Context context, String fileName, JSONObject story) {
		
		//set up a default string to return, in the event the user already saved a story
		String wasWritten = "Story is already a favorite";
		
		//string to contain the saved data
		String savedContent = "";
		
		//JSONObject/Array to format the data once we have it
		JSONObject favorites;
		JSONArray savedStories;
		
		//for this operation, we need both an input and output stream
		FileOutputStream fos = null;
		FileInputStream fis = null;
		
		//grab a reference to our saved file using the string in MainActivity
		File savedData = MainActivity.context
				.getFileStreamPath(MainActivity.favFileName);
		
		//if the file exists...
		if (savedData.exists()) {
			try {
				try {
					//open input stream
					fis = context.openFileInput(fileName);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//grab our data using a bufferedInput 
				BufferedInputStream bufferedInput = new BufferedInputStream(fis);
				byte[] contentBytes = new byte[1024];
				int bytesRead = 0;
				StringBuffer buffer = new StringBuffer();

				try {
					while ((bytesRead = bufferedInput.read(contentBytes)) != -1) {
						savedContent = new String(contentBytes, 0, bytesRead);
						buffer.append(savedContent);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//set our completed data to our string from our buffer
				savedContent = buffer.toString();
				
				try {
					//make sense of returned string by setting to a JSONObject, and then pull out the contained array
					favorites = new JSONObject(savedContent);
					
					//grab the array, which we will read from, and then write to below
					savedStories = favorites.getJSONArray("stories");
					
					//check to make sure we haven't already saved this story (since when in landscape the 'favorites' icon pulls double duty and doesn't alert the user
					//as to whether or not the story has already been saved)
					for (int i=0; i < savedStories.length(); i++)
					{
						JSONObject savedObject = savedStories.getJSONObject(i);
						String sentTitle = story.getString("title");
						String title = savedObject.getString("title");
						
						//check our title for the story passed against the ones found and if it matches, we don't want to save the passed JSONObject (no duplicates!!!)
						if (title.equals(sentTitle))
						{   //return wasWritten as a string, which we can check against null, thereby letting our caller know that the story was already saved previously
							return wasWritten;
						}
					}
					
					//since we now know that the story wasn't previously saved, add it to the array
					savedStories.put(story);
					
					//remove old instance of JSONArray from JSONObject
					favorites.remove("stories");
					
					//add the new JSONArray to the object
					favorites.put("stories", savedStories);
					
					//convert to string for saving again
					String convertedJSON = favorites.toString();
					try {
						fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						fos.write(convertedJSON.getBytes());
						wasWritten = null;
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.i("WRITE_FILE", "Success");
					
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} finally {
				try {
					fis.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		} else {
			//no file exists yet so make one and save our first story!  Set up in the same way 
			//that we may read it later
			favorites = new JSONObject();
			savedStories = new JSONArray();
			
			//add the new story to the array
			savedStories.put(story);
			try {
				//add the JSONArray to the container JSONObject
				favorites.put("stories", savedStories);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//convert to string for saving
			String convertedJSON = favorites.toString();
			try {
				fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				fos.write(convertedJSON.getBytes());
				Log.i("WRITE_FILE", "Success");
	
				//set our string to null (weird I know) to inform the calling Activity that it was successful
				wasWritten = null;
			} catch (FileNotFoundException e) {
				Log.i("WRITE_FILE", "Error writing file");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("WRITE_FILE", "Failure");
			}
		}	
		//return our string, which will communicate whether or not the story was saved, or already existed
		return wasWritten;
	}
	
	/**
	 * This function is responsible for reading our saved file. It returns the
	 * read data as a string to the caller.
	 */
	public String readFile(Context context, String fileName) {
		String content = "";
		FileInputStream fis = null;
		
		//grab our file so we can check if it exists
		File savedData = MainActivity.context
				.getFileStreamPath(MainActivity.fileName);
		if (savedData.exists()) {
			try {
				//read the contents of the file
				fis = context.openFileInput(fileName);
				BufferedInputStream bufferedInput = new BufferedInputStream(fis);
				byte[] contentBytes = new byte[1024];
				int bytesRead = 0;
				StringBuffer buffer = new StringBuffer();

				while ((bytesRead = bufferedInput.read(contentBytes)) != -1) {
					content = new String(contentBytes, 0, bytesRead);
					buffer.append(content);
				}
				content = buffer.toString();
			} catch (Exception e) {

				Log.i("READ_FILE", "Error reading file");
				return content;
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("READ_FILE", "Error closing input stream");
				}
			}
		}
		//return the contenets of the file
		return content;
	}
	
	//we use this function to remove a specific story that the user no longer wants to mark as "favorite"
	public void deleteFavorite(Context context, String fileName, String storyTitle) {
		
		//set up our JSONObject/Array and input and output streams
		JSONObject favorites;
		JSONArray savedStories;
		FileOutputStream fos = null;
		FileInputStream fis = null;
		String savedContent = "";
		
		//check to ensure our file exists
		File savedData = MainActivity.context
				.getFileStreamPath(MainActivity.favFileName);
		
		//grab the data contained in the file
		if (savedData.exists())
		{
			try {
				fis = context.openFileInput(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedInputStream bufferedInput = new BufferedInputStream(fis);
			byte[] contentBytes = new byte[1024];
			int bytesRead = 0;
			StringBuffer buffer = new StringBuffer();

			try {
				while ((bytesRead = bufferedInput.read(contentBytes)) != -1) {
					savedContent = new String(contentBytes, 0, bytesRead);
					buffer.append(savedContent);
				}
				
				//set the buffer to our string
				savedContent = buffer.toString();
				try {
					//convert our unformatted string to a JSONObject, and pull out our array 
					favorites = new JSONObject(savedContent);
					savedStories = favorites.getJSONArray("stories");
					JSONArray newStories = new JSONArray();
					
					//loop through the array checking the passed title against the one contained within the JSONObject
					for (int i =0; i < savedStories.length(); i++)
					{
						JSONObject story = savedStories.getJSONObject(i);
						String title = story.getString("title");
						
						//since we can't use "JSONArray.remove() in anything below API 19, create a new JSONArray instead of removing the one object from the old one
						//and replace the old JSONArray entirely
						if (!(title.equals(storyTitle)))
						{
							newStories.put(story);
						}
					}
					
					//remove old JSONArray since it contains old data now
					favorites.remove("stories");
					
					//put the new JSONArray that contains ony the stories we want to keep
					favorites.put("stories", newStories);
					
					//convert to string for saving
					String convertedJSON = favorites.toString();
					fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
					
					//write to file
					fos.write(convertedJSON.getBytes());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	//this simply reads the favorites and returns them as a JSONArray
	public JSONArray readFavorites(Context context, String fileName) {
		//set up our JSONObject/Array and input/output streams
		JSONObject favorites;
		JSONArray savedStories;
		FileInputStream fis = null;
		String savedContent = "";
		
		//create reference to file and check if it exists
		File savedData = MainActivity.context.getFileStreamPath(MainActivity.favFileName);
		if (savedData.exists())
		{
			try {
				fis = context.openFileInput(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//read our file with bufferedInputStream
			BufferedInputStream bufferedInput = new BufferedInputStream(fis);
			byte[] contentBytes = new byte[1024];
			int bytesRead = 0;
			StringBuffer buffer = new StringBuffer();
			
			try {
				while ((bytesRead = bufferedInput.read(contentBytes)) != -1) {
					savedContent = new String(contentBytes, 0, bytesRead);
					buffer.append(savedContent);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//set our stringBuffer to a String
			savedContent = buffer.toString();
			try {
				//convert unformatted string to JSONObject
				favorites = new JSONObject(savedContent);
				//grab our array of saved stories
				savedStories = favorites.getJSONArray("stories");
				
				//return our array of saved stories
				return savedStories;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		Log.i("FILEMANAGER_READFAVORIES","Returned null when attempting to read favorites JSONArray from file!");
		return null;
	}
}
