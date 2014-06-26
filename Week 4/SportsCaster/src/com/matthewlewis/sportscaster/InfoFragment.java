package com.matthewlewis.sportscaster;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class InfoFragment extends Activity{

	Activity thisActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		thisActivity = this;
		
		setContentView(R.layout.info);
		
		Button closeBtn = (Button) findViewById(R.id.info_closeBtn);
		closeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				thisActivity.finish();
			}
			
		});
	}

	
}
