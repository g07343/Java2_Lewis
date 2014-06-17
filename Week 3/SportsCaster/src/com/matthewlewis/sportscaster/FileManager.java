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
}
