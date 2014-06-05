/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File FileManager.java
 * 
 * Purpose 
 * 
 */
package com.matthewlewis.sportscaster;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

public class FileManager {

	private static FileManager _instance;

	private FileManager() {

	}

	public static FileManager GetInstance() {
		if (_instance == null) {
			_instance = new FileManager();
		}
		
		return _instance;
	}
	
	public Boolean WriteFile(Context context, String fileName, String content) {
		Boolean wasWritten = false;
		
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			Log.i("WRITE_FILE", "Success");
			wasWritten = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i("WRITE_FILE", "Failure");
		}
				
		return wasWritten;
	}
	
	public String readFile(Context context, String fileName) {
		String content = "";
		FileInputStream fis = null;
		
		try {
			fis = context.openFileInput(fileName);
			BufferedInputStream bufferedInput = new BufferedInputStream(fis);
			byte[] contentBytes = new byte[1024];
			int bytesRead = 0;
			StringBuffer buffer = new StringBuffer();
			
			while((bytesRead = bufferedInput.read(contentBytes)) != -1) {
				content = new String(contentBytes, 0, bytesRead);
				buffer.append(content);
			}
			content = buffer.toString();
		} catch(Exception e) {
			Log.e("READ_FILE", "Error reading file");
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("READ_FILE", "Error closing input stream");
			}
		}
		return content;
	}
}
