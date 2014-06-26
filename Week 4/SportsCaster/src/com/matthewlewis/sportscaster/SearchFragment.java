/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File SearchFragment.java
 * 
 * Purpose 
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
	
	private searchFragmentInterface parentActivity;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		if (activity instanceof searchFragmentInterface)
		{
			parentActivity = (searchFragmentInterface)activity;
		} else {
			throw new ClassCastException(activity.toString() + "must implement searchFragmentInterface");
		}
		
	}

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View searchFrag = inflater.inflate(R.layout.search_dialog, null);
		builder.setView(searchFrag);
		
		
		
		searchInput = (EditText) searchFrag.findViewById(R.id.search_textEntry);
		submitBtn = (Button) searchFrag.findViewById(R.id.search_submitBtn);
		cancelBtn = (Button) searchFrag.findViewById(R.id.search_cancel);
		submitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String searchString = searchInput.getText().toString();
				System.out.println("Search for:  " + searchString);
				parentActivity.applyFilter(searchString);
				SearchFragment.this.getDialog().cancel();
			}			
		});
		
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SearchFragment.this.getDialog().cancel();
			}			
		});
		
		return builder.create();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	public static SearchFragment newInstance() {
		
		SearchFragment searchFrag = new SearchFragment();
		
		return searchFrag;
	}

}
