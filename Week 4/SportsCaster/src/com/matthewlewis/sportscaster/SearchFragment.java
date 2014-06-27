/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File SearchFragment.java
 * 
 * Purpose The SearchFragment presents a dialog to the user to input a string to search for a title within the retrieved stories.  Using the 
 * "applyFilter" interface method, it communicates the input to the MainActivity, which in turn delegates it to the MainActivityFragment to filter with.
 * 
 */
package com.matthewlewis.sportscaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SearchFragment extends DialogFragment {
	
	EditText searchInput;
	Button submitBtn;
	Button cancelBtn;
	
	
	public interface searchFragmentInterface {
		void applyFilter(CharSequence s);
	}
	//grab our parent activity
	private searchFragmentInterface parentActivity;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		//ensure our parentActivity implements our interface
		if (activity instanceof searchFragmentInterface)
		{
			parentActivity = (searchFragmentInterface)activity;
		} else {
			throw new ClassCastException(activity.toString() + "must implement searchFragmentInterface");
		}
		
	}

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//create our builder to construct our dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		//grab our inflator and inflate our layout
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View searchFrag = inflater.inflate(R.layout.search_dialog, null);
		builder.setView(searchFrag);
		
		
		//grab the interface elements we need 
		searchInput = (EditText) searchFrag.findViewById(R.id.search_textEntry);
		submitBtn = (Button) searchFrag.findViewById(R.id.search_submitBtn);
		cancelBtn = (Button) searchFrag.findViewById(R.id.search_cancel);
		
		//set up an onClickListener for when the user submits data to search
		submitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String searchString = searchInput.getText().toString();
				System.out.println("Search for:  " + searchString);
				parentActivity.applyFilter(searchString);
				SearchFragment.this.getDialog().cancel();
			}			
		});
		//set up an onClickListener for our cancel button to dismiss the dialog
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SearchFragment.this.getDialog().cancel();
			}			
		});
		//return our dialog after the builder creates it
		return builder.create();
	}

	//don't need to write any logic here as the fragment seems to automatically retain any user inputted data
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	//create and return an instance of SearchFragment
	public static SearchFragment newInstance() {
		
		SearchFragment searchFrag = new SearchFragment();
		
		return searchFrag;
	}

}
