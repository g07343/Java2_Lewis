/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File InfoFragment.java
 * 
 * Purpose the infoFragment simply presents a stylized activity to the user conveying basic information about the app.
 * 
 */
package com.matthewlewis.sportscaster;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class InfoFragment extends Activity{
	
	//get reference to this activity so we can finish it
	Activity thisActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//set our reference
		thisActivity = this;
		
		//set our content view
		setContentView(R.layout.info);
		
		//grab our close btn and apply an onClickListener to finish the activity
		Button closeBtn = (Button) findViewById(R.id.info_closeBtn);
		closeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//finish our activity
				thisActivity.finish();
			}
			
		});
	}

	
}
