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
 * to the device.  
 * 
 */
package com.matthewlewis.sportscaster;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

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

	public String writeFavorites(Context context, String fileName, JSONObject story) {
		String wasWritten = "Story is already a favorite";
		String savedContent = "";
		JSONObject favorites;
		JSONArray savedStories;
		FileOutputStream fos = null;
		FileInputStream fis = null;
		File savedData = MainActivity.context
				.getFileStreamPath(MainActivity.favFileName);
		
		if (savedData.exists()) {
			try {
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
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				savedContent = buffer.toString();
				
				try {
					favorites = new JSONObject(savedContent);
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
					savedStories.put(story);
					favorites.remove("stories");
					favorites.put("stories", savedStories);
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
			//no file exists yet so make one and save our first story!
			favorites = new JSONObject();
			savedStories = new JSONArray();
			savedStories.put(story);
			try {
				favorites.put("stories", savedStories);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String convertedJSON = favorites.toString();
			try {
				
				fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				fos.write(convertedJSON.getBytes());
				Log.i("WRITE_FILE", "Success");
				wasWritten = null;
			} catch (FileNotFoundException e) {
				Log.i("WRITE_FILE", "Error writing file");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("WRITE_FILE", "Failure");
			}
		}
		
		
		return wasWritten;
	}
	
	/**
	 * This function is responsible for reading our saved file. It returns the
	 * read data as a string to the caller.
	 */
	public String readFile(Context context, String fileName) {
		String content = "";
		FileInputStream fis = null;

		File savedData = MainActivity.context
				.getFileStreamPath(MainActivity.fileName);
		if (savedData.exists()) {
			try {
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
		return content;
	}
	
	//we use this function to remove a specific story that the user no longer wants to mark as "favorite"
	public void deleteFavorite(Context context, String fileName, String storyTitle) {
		JSONObject favorites;
		JSONArray savedStories;
		FileOutputStream fos = null;
		FileInputStream fis = null;
		String savedContent = "";
		File savedData = MainActivity.context
				.getFileStreamPath(MainActivity.favFileName);
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
				savedContent = buffer.toString();
				try {
					favorites = new JSONObject(savedContent);
					savedStories = favorites.getJSONArray("stories");
					JSONArray newStories = new JSONArray();
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
					favorites.remove("stories");
					favorites.put("stories", newStories);
					String convertedJSON = favorites.toString();
					fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
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
	
	public JSONArray readFavorites(Context context, String fileName) {
		JSONObject favorites;
		JSONArray savedStories;
		FileInputStream fis = null;
		String savedContent = "";
		File savedData = MainActivity.context.getFileStreamPath(MainActivity.favFileName);
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			savedContent = buffer.toString();
			try {
				favorites = new JSONObject(savedContent);
				savedStories = favorites.getJSONArray("stories");
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
