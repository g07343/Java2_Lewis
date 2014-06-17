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
import android.content.Intent;
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
		void startDetailsActivity(int position);
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
		reloadBtn = (Button) view.findViewById(R.id.reload_btn);
		statusField = (TextView) view.findViewById(R.id.internet_warning);
		listview = (ListView) view.findViewById(R.id.list);
		
		View listHeader = inflater.inflate(R.layout.header,
				null);
		listview.addHeaderView(listHeader);
		
		
		reloadBtn.setOnClickListener(this);
		listview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view,
	                int position, long id) {
	        	//call the interfaced method startDetailsActivity, and pass the selected number.  
	        	//Since the parentActivity has the data already, no need to pass it back.
	        	parentActivity.startDetailsActivity(position);
	        }
	    });
		
		
		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		Boolean connected = manager.connectionStatus(MainActivity.context);
		if (connected) {
			//hide our button now, since we no longer need it
			reloadBtn.setVisibility(View.GONE);
			//now that we have Internet, get data
			((MainActivity) MainActivity.context).startRetrieval();
		}
	}
	
	public void applyAdapter(final Context context, final ArrayList<HashMap<String, Object>> list) {
		//create a SimpleAdapter in conjunction with the above created data
		SimpleAdapter adapter = new SimpleAdapter(context, list,
				R.layout.row, new String[] { (String) "headline",
						(String) "date", "description", "icon" },
				new int[] { R.id.title, R.id.date, R.id.description,
						R.id.sport_icon });
		System.out.println("fragment applyAdapter method called on Fragment side!");
		//set our adapter
		listview.setAdapter(adapter);
		
	}
	
	public void setData(final Context context, final ArrayList<HashMap<String, Object>> list) {
		//create a SimpleAdapter in conjunction with the above created data
				SimpleAdapter adapter = new SimpleAdapter(context, list,
						R.layout.row, new String[] { (String) "headline",
								(String) "date", "description", "icon" },
						new int[] { R.id.title, R.id.date, R.id.description,
								R.id.sport_icon });
				System.out.println("SetData method called on Fragment side!");
				//set our adapter
				listview.setAdapter(adapter);
	}
	
	public void startResultActivity(int position, ArrayList<HashMap<String, Object>> list, Context context) {
		//convert the selected int to a position relative to our hashmap array
    	int actualSelected = position -=1;
    	HashMap<String, Object> dataMap = list.get(actualSelected);
    	String title = (String) dataMap.get("headline");
    	String date = (String) dataMap.get("date");
    	String description = (String) dataMap.get("description");
    	
    	System.out.println("Selected story was:  " + title + "  "  + date + "  " + description);
    	//now that we have determined which story the user selected, load the detail view passing the data
    	Intent showDetail = new Intent(context, DetailView.class);
    	showDetail.putExtra("data", dataMap);
    	((Activity) context).startActivityForResult(showDetail, 0); 
    	
    	//parentActivity.startActivityForResult(showDetail, 0);
	}
}
