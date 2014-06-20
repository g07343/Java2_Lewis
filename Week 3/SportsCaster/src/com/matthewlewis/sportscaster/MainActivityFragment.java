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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
	
	public interface mainFragmentInterface {
		
		void applyAdapter(Context context, ArrayList<HashMap<String, Object>> list);
		void itemSelected(int position);
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
	        	parentActivity.itemSelected(position);
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
				SimpleAdapter adapter = new SimpleAdapter(context, list,
						R.layout.row, new String[] { (String) "headline",
								(String) "date", "description", "icon" },
						new int[] { R.id.title, R.id.date, R.id.description,
								R.id.sport_icon });
				System.out.println("SetData method called on Fragment side!");
				//set our adapter
				listview.setAdapter(adapter);
				statusField.setVisibility(View.GONE);
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
}
