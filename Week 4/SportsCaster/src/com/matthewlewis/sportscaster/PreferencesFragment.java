/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File PreferencesFragment.java
 * 
 * Purpose The PreferencesFragment is in charge of gathering the user's name and favorite sport and saving them to the SharedPreferences for the app.
 * It can also be called up to allow the user to edit these values at a later time by tapping the "Preferences" option from within MainActivity's ActionBar.
 * 
 */
package com.matthewlewis.sportscaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PreferencesFragment extends DialogFragment{

	TextView titleView;
	TextView subHeading;
	EditText nameField;
	EditText sportField;
	Button submitBtn;
	TextView detailDisclosure;	
	SharedPreferences prefs;
	
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View prefsFrag = inflater.inflate(R.layout.preferences_dialog, null);
		builder.setView(prefsFrag);
		
		//grab our dialog's interface elements for manipulation
		titleView = (TextView) prefsFrag.findViewById(R.id.preferences_title);
		subHeading = (TextView) prefsFrag.findViewById(R.id.preferences_subHeading);
		nameField = (EditText) prefsFrag.findViewById(R.id.preferences_nameField);
		sportField = (EditText) prefsFrag.findViewById(R.id.preferences_sportField);
		submitBtn = (Button) prefsFrag.findViewById(R.id.preferences_saveBtn);
		detailDisclosure = (TextView) prefsFrag.findViewById(R.id.preferences_detailDisclosure);
		
		//depending on if there is already user data, we need to alter how this dialog displays, so check for defaults first
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		String savedName = prefs.getString("userName", "");
		
		if(savedName.isEmpty()) {
			//no user data yet, show the default interface
			
		} else {
			//old user data found, go ahead and alter the interface so it doesn't appear as "first run"
			titleView.setText("Change yourself!");
			subHeading.setText("Be whoever you want.  Set it below");
			nameField.setText(prefs.getString("userName", ""));
			sportField.setText(prefs.getString("sport", ""));
			detailDisclosure.setVisibility(View.GONE);
		}
		
		//set up our button so it overwrites (or initially saves) the inputted data to defaultPreferences
		submitBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String userName = nameField.getText().toString();
				String sport = sportField.getText().toString();
				
				//check to make sure that the user put something in both fields before allowing saving
				if (userName.isEmpty() || sport.isEmpty()) {
					detailDisclosure.setText("Make sure to put a name and your favorite sport above!");
					detailDisclosure.setTextColor(Color.RED);
					detailDisclosure.setVisibility(View.VISIBLE);
				} else {
					//valid data was entered so save it, and update the MainActivityFragment to display userName using interface method
					prefs.edit().putString("userName", userName).apply();
					prefs.edit().putString("sport", sport).apply();
					MainActivity.updateFragment();
					PreferencesFragment.this.getDialog().cancel();
				}
			}
			
		});
		
		return builder.create();
	}
	
	public static PreferencesFragment newInstance() {
		
		PreferencesFragment prefFrag = new PreferencesFragment();
		
		return prefFrag;
	}

}
