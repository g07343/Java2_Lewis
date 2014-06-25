/*
 * Author Matthew Lewis
 * 
 * Project SportsCaster
 * 
 * Package com.matthewlewis.sportscaster
 * 
 * File MainActivityFragment.java
 * 
 * Purpose The MainActivityFragment contains all the necessary logic to contain and update the User Interface for the MainActivity. 
 * 
 */
package com.matthewlewis.sportscaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivityFragment extends Fragment implements OnClickListener{

	NetworkManager manager = new NetworkManager();
	static TextView statusField;
	private static ListView listview;
	Button reloadBtn;
	private SimpleAdapter adapter;
	ArrayList<HashMap<String, Object>> storyItems;
	ArrayList<HashMap<String, Object>> newList;
	View listFooter;
	String searchedString;
	
	public interface mainFragmentInterface {
		
		void applyAdapter(Context context, ArrayList<HashMap<String, Object>> list);
		void itemSelected(int position, String title);
		void updateStatus(String status);
	}
	
	private mainFragmentInterface parentActivity;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		if (activity instanceof mainFragmentInterface)
		{
			parentActivity = (mainFragmentInterface)activity;
		} else {
			throw new ClassCastException(activity.toString() + "must implement mainFragmentInterface");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		if (savedInstanceState != null)
		{
			ArrayList<HashMap<String, Object>> savedList = (ArrayList<HashMap<String, Object>>) savedInstanceState.getSerializable("data");
			if(savedList != null)
			{
				setData(getActivity(), savedList);
			}
			String searchedTerm = savedInstanceState.getString("searched");
			if (searchedTerm != null)
			{
				setFooter(searchedTerm);
			}
		}
		
		View view = inflater.inflate(R.layout.activity_main, container);
		
		//grab and assign our interface elements contained within the fragment
		reloadBtn = (Button) view.findViewById(R.id.reload_btn);
		statusField = (TextView) view.findViewById(R.id.internet_warning);
		listview = (ListView) view.findViewById(R.id.list);
		
		//apply our listview header here
		View listHeader = inflater.inflate(R.layout.header,
				null);
		listview.addHeaderView(listHeader);
		
		//hide our reloadBtn and statusField by default, and let the MainActivity communicate when to show them
		//and what they'll display
		reloadBtn.setVisibility(View.GONE);
		reloadBtn.setOnClickListener(this);
		statusField.setVisibility(View.GONE);
		
		//set up an onItemClick listener for our listview to let MainActivity know what is selected via the interface
		listview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view,
	                int position, long id) {
	        	//call the interfaced method startDetailsActivity, and pass the selected number.  
	        	//Since the parentActivity has the data already, no need to pass the data itself back.
	        	if (adapter.getCount() < 10)
	        	{
	        		TextView selectedStory = (TextView) view.findViewById(R.id.title);
	        		String selectedTitle = selectedStory.getText().toString();
	        		//System.out.println("User filtered stories.  Selected was:  " + selectedTitle);
	        		parentActivity.itemSelected(position, selectedTitle);
	        	} else {
	        		parentActivity.itemSelected(position, null);
	        	}
	        	System.out.println("NUMBER OF ITEMS IN LISTVIEW IS:  " + adapter.getCount());      	
	        }
	    });
		
		
		//now that the view is created, return it for display
		return view;
	}

	//set up an onClick listener for our internet "reload" button to check for network connectivity in the event there is none
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		Boolean connected = manager.connectionStatus(MainActivity.context);
		if (connected) {
			//hide our button now, since we no longer need it
			reloadBtn.setVisibility(View.GONE);
			statusField.setVisibility(View.GONE);
			//now that we have Internet, get data
			((MainActivity) MainActivity.context).startRetrieval();
		}
	}
	
	//this method sets the listView's adapter using an array of hashmap objects passed from the MainActivity class 
	public void setData(final Context context, final ArrayList<HashMap<String, Object>> list) {
		//create a SimpleAdapter to use with the above passed array of data
		storyItems = list;
		
		adapter = new SimpleAdapter(context, list,
						R.layout.row, new String[] { (String) "headline",
								(String) "date", "description", "icon" },
						new int[] { R.id.title, R.id.date, R.id.description,
								R.id.sport_icon });
				System.out.println("SetData method called on Fragment side!");
				//set our adapter
				listview.setAdapter(adapter);
				statusField.setVisibility(View.GONE);
				setFooter(null);
	}
	
	//this method gets called by the parentActivity when no internet is detected and no local storage exists
	//it basically displays the necessary UI to alert the user, and allow them to recheck it again.
	public void toggleStatusUI(String message) {
		
		if(message != null)
		{	//since we received a message, set it to the statusField and make sure it's visible
			statusField.setVisibility(View.VISIBLE);
			statusField.setText(message);
		} else {
			statusField.setVisibility(View.VISIBLE);
			//no Internet or saved data, alert user they need Internet!
			statusField.setText("No internet! Please check your internet connection.");
			
			//display our button, which rechecks Internet
			reloadBtn.setVisibility(View.VISIBLE);
		}
	}
	
	public void filterData(CharSequence s) {
		newList = new ArrayList<HashMap<String, Object>>();
		String searchParam = s.toString();
		searchedString = searchParam;
		for(int i = 0; i < adapter.getCount(); i++)
		{
			@SuppressWarnings("unchecked")
			HashMap<String, Object> currentStory = (HashMap<String, Object>) adapter.getItem(i);
			//System.out.println("Passed converted char sequence is:  " + searchParam);
			String currentTitle = (String) currentStory.get("headline");
			String currentTitleLowerCase = currentTitle.toLowerCase(Locale.US);
			if (currentTitle.contains(searchParam) || currentTitleLowerCase.contains(searchParam) || currentTitle.contains(searchParam.toLowerCase(Locale.US)) || currentTitleLowerCase.contains(searchParam.toLowerCase()))
			{
				HashMap<String, Object> dataMap = storyItems.get(i);
				newList.add(dataMap);
			}
		}
		
		setData(MainActivity.context, newList);
		setFooter(searchParam);
	}
	
	//this method controls our dynamic footer, which displays data relevant to the stories within the listview.  When the user enters a search
	//the footer displays the number of results, the searched string, and a button to reset the listview.
	public void setFooter(String searchedString) {
		//need to remove any instances of previous listView footer
		if (listFooter != null)
		{
			listview.removeFooterView(listFooter);
		}
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		//apply our listview header here
				listFooter = inflater.inflate(R.layout.footer,
						null);
				
				TextView numResults = (TextView) listFooter.findViewById(R.id.footer_intLabel);
				TextView defaultLabel = (TextView) listFooter.findViewById(R.id.footer_regularLabel);
				final TextView searchedLabel = (TextView) listFooter.findViewById(R.id.footer_searchLabel);
				final Button resetBtn = (Button) listFooter.findViewById(R.id.footer_resetBtn);
				
				int listLength = adapter.getCount();
				String lengthString = String.valueOf(listLength);
				numResults.setText(lengthString);
				
				//populate our data according to whether a string is passed, which determines if the user searched something
				if (searchedString != null)
				{
					defaultLabel.setText(R.string.footer_regularLabel);
					searchedLabel.setText("\""+searchedString+"\"");
					searchedLabel.setVisibility(View.VISIBLE);
					resetBtn.setVisibility(View.VISIBLE);
					if(listLength < 1)
					{
						numResults.setTextColor(Color.RED);
					}		
					
					//set up our button to reset our listView to it's default stories
					resetBtn.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							searchedLabel.setVisibility(View.GONE);
							resetBtn.setVisibility(View.GONE);
							//reset our listview using parentActivity's unmodified data
							parentActivity.applyAdapter(getActivity(), MainActivity.list);
						}
						
					});
					
				} else {
					//user did not search anything, so set up the footer to display its default state
					defaultLabel.setText("stories retrieved!");
					searchedLabel.setVisibility(View.GONE);
					resetBtn.setVisibility(View.GONE);
				}
				
				listview.addFooterView(listFooter);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		if (newList != null)
		{
			savedInstanceState.putSerializable("data", newList);
		} else {
			savedInstanceState.putSerializable("data", storyItems);
		}
		
		//if there was a term that was searched for, grab it and save it so we can repopulate the list accordingly 
		TextView searchedLabel = (TextView) listFooter.findViewById(R.id.footer_searchLabel);
		String searched = searchedLabel.getText().toString();
		if (!(searched.equals("searched string")))
		{
			System.out.println("Saving the searched term:  " + searched);
			savedInstanceState.putString("searched", searched);
		}
	}
}
