/* 
 * fIRC - a free IRC client for the Android platform.
 * http://code.google.com/p/firc-chat/
 * 
 * Copyright (C) 2008-2011 Laurence Muller <laurence.muller@gmail.com>
 * http://www.multigesture.net/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package com.falcon4ever.fIRC3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.jared.commons.ui.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.falcon4ever.fIRC3.utils.DBProfileManager;

public class ProfileActivity extends Activity {
	
	private final String mDefaultNetwork = "EFnet";
	private final Random mGenerator = new Random();
	private Hashtable<String, Network> mNetworksList;
		
	/* 
	 * Silly hack since setOnItemSelectedListener(new ServerOnItemSelectedListener());
	 * is calling onItemSelected on initialization.
	 * http://stackoverflow.com/questions/5624825/spinner-onitemselected-executes-when-it-is-not-suppose-to/5918177#5918177
	 */
    private int mCount=0;
    private int mInitializedCount=0;
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        // Hide keyboard on Activity launch
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        setContentView(R.layout.profile);          

        // Load hardcoded serverlist (Slow?)
        loadServerlist();
        
        // Get extras
        final Bundle extras = getIntent().getExtras();        
        String profileType = extras.getString("profile_type");
        
        if(profileType.equals("new_profile"))
        {
        	newProfile();
        }
        else if(profileType.equals("edit_profile"))
        {        	
        	setTitle(R.string.profile_title_edit);        	
        	editProfile(extras.getInt("profile_id"));
        }
        else
        {
        	Log.d(getClass().getSimpleName(), "no extras");
        	finish();
        }
	}
	
	private void setupSpinners()
	{
	// Enumerate Networks (Groups)                
        // Get list of networks, store it and sort the List
        Enumeration<String> e = mNetworksList.keys();        
        List<String> ircNetworksNames = new ArrayList<String>();
        while(e.hasMoreElements())
        	ircNetworksNames.add(mNetworksList.get(e.nextElement()).getName());
        Collections.sort(ircNetworksNames);
        
        // Prepare to copy list to ircnetworks so it can be attached to the Adapter
        int defaultPos = 0;
        List<CharSequence> ircNetworks = new ArrayList<CharSequence>();
		for (int i=0; i< ircNetworksNames.size(); i++)
		{			
			// Set default Network
			if(ircNetworksNames.get(i).equals(mDefaultNetwork))
        		defaultPos = ircNetworks.size();
			
			ircNetworks.add((CharSequence)ircNetworksNames.get(i));
		}
        
        final Spinner profileNetworkSpinner = (Spinner) findViewById(R.id.profile_network_spinner);       
        ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, ircNetworks);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileNetworkSpinner.setAdapter(adapter1);
        profileNetworkSpinner.setOnItemSelectedListener(new NetworkOnItemSelectedListener());        
        profileNetworkSpinner.setSelection(defaultPos);
        
	// Enumerate Servers        
        final Spinner profileServerSpinner = (Spinner)findViewById(R.id.profile_server_spinner);        
        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, new ArrayList<CharSequence>());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileServerSpinner.setAdapter(adapter2);
        profileServerSpinner.setOnItemSelectedListener(new ServerOnItemSelectedListener());        
        setServerValues();
        
	// Enumerate encodings
        final Spinner profileEncodingSpinner = (Spinner) findViewById(R.id.profile_encoding_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.encoding_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileEncodingSpinner.setAdapter(adapter);
	}
	
	private void newProfile()
	{
		final Button profileSave = (Button)findViewById(R.id.profile_save);
		profileSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	// Prepare values
            	String r01 = ((EditText)findViewById(R.id.profile_name)).getText().toString(); 
            	String r02 = ((EditText)findViewById(R.id.profile_nickname)).getText().toString(); 
            	String r03 = ((EditText)findViewById(R.id.profile_altnick)).getText().toString();
            	String r04 = ((EditText)findViewById(R.id.profile_server_address)).getText().toString();
            	String port= ((EditText)findViewById(R.id.profile_server_port)).getText().toString();            	
            	int r05	   = Integer.parseInt(port);
            	String r06 = ((EditText)findViewById(R.id.profile_chatrooms)).getText().toString();
            	String r07 = ((EditText)findViewById(R.id.profile_ident)).getText().toString();
            	String r08 = ((EditText)findViewById(R.id.profile_realname)).getText().toString(); 
            	final Spinner profile_encoding_spinner = (Spinner) findViewById(R.id.profile_encoding_spinner);
            	int r09	   = profile_encoding_spinner.getSelectedItemPosition(); 
            	String r10 = ((EditText)findViewById(R.id.profile_onconnect)).getText().toString();
            	
            	// Connect to db and submit
            	DBProfileManager db = new DBProfileManager(ProfileActivity.this);
            	db.addRow(r01, r02, r03, r04, r05, r06, r07, r08, r09, r10);
            	db.close();
            	
            	finish();
            }
        });
        
        final Button profileCancel = (Button)findViewById(R.id.profile_cancel);
        profileCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Ignore changes
            	finish();
            }
        });
                
        setupSpinners();
        
    // Set defaults
        final EditText profileNickname = (EditText)findViewById(R.id.profile_nickname);    	
        profileNickname.setText("Android" + mGenerator.nextInt(100));
    	
    	final EditText profileAltnick = (EditText)findViewById(R.id.profile_altnick);
    	profileAltnick.setText("Android" + mGenerator.nextInt(100));
                
    	final EditText profileName = (EditText)findViewById(R.id.profile_name);
    	profileName.setText("New profile");
	}
	
	private void editProfile(final int profile_id)
	{
		setupSpinners();		
        
        // Set values
		DBProfileManager db = new DBProfileManager(ProfileActivity.this);
		ArrayList<Object> row = db.getRowAsArray(profile_id);            	
    	db.close();
    	    	
    	final EditText profileName = (EditText)findViewById(R.id.profile_name);
    	profileName.setText((String)row.get(1));
        
        final EditText profileNickname = (EditText)findViewById(R.id.profile_nickname);
        profileNickname.setText((String)row.get(2));
        
        final EditText profileAltnick = (EditText)findViewById(R.id.profile_altnick);
        profileAltnick.setText((String)row.get(3));
        
        final EditText profileServerAddress = (EditText)findViewById(R.id.profile_server_address);
        profileServerAddress.setText((String)row.get(4));
        
        final EditText profileServerPort = (EditText)findViewById(R.id.profile_server_port);
        profileServerPort.setText((String)row.get(5).toString());
        
        final EditText profileChatrooms = (EditText)findViewById(R.id.profile_chatrooms);
        profileChatrooms.setText((String)row.get(6));
        
        final EditText profileIdent = (EditText)findViewById(R.id.profile_ident);
        profileIdent.setText((String)row.get(7));
        
        final EditText profileRealname = (EditText)findViewById(R.id.profile_realname);
        profileRealname.setText((String)row.get(8));
        
        final Spinner profileEncodingSpinner = (Spinner) findViewById(R.id.profile_encoding_spinner);
        profileEncodingSpinner.setSelection(Integer.parseInt(row.get(9).toString()));
        
        final EditText profileOnconnect = (EditText)findViewById(R.id.profile_onconnect);
        profileOnconnect.setText((String)row.get(10));
                
		final Button profileSave = (Button)findViewById(R.id.profile_save);
		profileSave.setText("Update profile");
		profileSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	// Prepare values
            	String r01 = ((EditText)findViewById(R.id.profile_name)).getText().toString(); 
            	String r02 = ((EditText)findViewById(R.id.profile_nickname)).getText().toString(); 
            	String r03 = ((EditText)findViewById(R.id.profile_altnick)).getText().toString();
            	String r04 = ((EditText)findViewById(R.id.profile_server_address)).getText().toString();
            	String port= ((EditText)findViewById(R.id.profile_server_port)).getText().toString();            	
            	int r05	   = Integer.parseInt(port);
            	String r06 = ((EditText)findViewById(R.id.profile_chatrooms)).getText().toString();
            	String r07 = ((EditText)findViewById(R.id.profile_ident)).getText().toString();
            	String r08 = ((EditText)findViewById(R.id.profile_realname)).getText().toString(); 
            	final Spinner profile_encoding_spinner = (Spinner) findViewById(R.id.profile_encoding_spinner);
            	int r09	   = profile_encoding_spinner.getSelectedItemPosition(); 
            	String r10 = ((EditText)findViewById(R.id.profile_onconnect)).getText().toString();
            	
            	// Connect to db and submit
            	DBProfileManager db = new DBProfileManager(ProfileActivity.this);
            	db.updateRow(profile_id, r01, r02, r03, r04, r05, r06, r07, r08, r09, r10);            	
            	db.close();
            	
            	finish();
            }
        });
        
        final Button profileCancel = (Button)findViewById(R.id.profile_cancel);
        profileCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Ignore changes
            	finish();
            }
        });
        
        // hack to ignore 2 OnItemSelectedListener items
        mCount = 2;
	}
	
	public void setWizardValues()
	{				
		Log.d(getClass().getSimpleName(),"setWizardValues()");	
		
		final Spinner profileServerSpinner = (Spinner)findViewById(R.id.profile_server_spinner);		
		int pos = profileServerSpinner.getSelectedItemPosition();
		if(pos == AdapterView.INVALID_POSITION)
			return;
				
		String[] values  = profileServerSpinner.getItemAtPosition(pos).toString().split(":");		
		if(values.length == 2)
		{			
	    	final EditText profileServerAddress = (EditText)findViewById(R.id.profile_server_address);	    	
	        final EditText profileServerPort = (EditText)findViewById(R.id.profile_server_port);
	        profileServerAddress.setText(values[0]);
	        profileServerPort.setText(values[1]);
		}
	}
	
	public void setServerValues()
	{		
		Log.d(getClass().getSimpleName(),"setServerValues()");
		
		final Spinner profileNetworkSpinner = (Spinner) findViewById(R.id.profile_network_spinner);
		String currentNetwork = profileNetworkSpinner.getItemAtPosition(profileNetworkSpinner.getSelectedItemPosition()).toString();
		Network n = (Network)mNetworksList.get(currentNetwork);
         
        final Spinner profileServerSpinner = (Spinner)findViewById(R.id.profile_server_spinner);
        @SuppressWarnings("unchecked")
		ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) profileServerSpinner.getAdapter();
        adapter.clear();	// empty adapter
        
        // Iterate over all servers from current network
        for(int i = 0; i < n.getServers().size(); i++)
        {
        	// Iterate over all available ports
	        for (int j = 0; j < n.getServers().get(i).getPorts().size(); j++)				
	        	adapter.add(n.getServers().get(i).getAddress() + ":" +  Integer.toString(n.getServers().get(i).getPorts().get(j)));
        }
        
        // Choose a random server from the list after selecting a network to balance load
        profileServerSpinner.setSelection(mGenerator.nextInt(profileServerSpinner.getCount()));
        
        // Apply values
        setWizardValues();
	}
	
	public class NetworkOnItemSelectedListener implements OnItemSelectedListener {
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {	    	    		    	
	    	if (mInitializedCount < mCount)	        	
	            mInitializedCount++;
	    	else	
	    		setServerValues();
	    }

	    public void onNothingSelected(AdapterView<?> parent) { }
	}
	
	public class ServerOnItemSelectedListener implements OnItemSelectedListener {
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    	if (mInitializedCount < mCount)	
	            mInitializedCount++;
	    	else
		    	setWizardValues();	    	
	    }

	    public void onNothingSelected(AdapterView<?> parent) { }
	}
	
	private void loadServerlist()
	{
		ServerList serverlist = new ServerList();	
		mNetworksList = serverlist.getNetworksList();
	}		
	
	private class ServerList {
		
		Hashtable<String, Network> networksList = new Hashtable<String, Network>();
	        
		public ServerList() {
			importServerlist();
		}

		private void importServerlist()
		{
			// Original source: http://www.mirc.com/servers.html
	        InputStream is = getResources().openRawResource(R.raw.servers);
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        String readLine = null;
	        
	        int label = 0;
	        
			try {
	            // While the BufferedReader readLine is not null 
	            while ((readLine = br.readLine()) != null) 
	            {
	            	// Read Label to set label state
	            	if(readLine.equals("[timestamp]"))	// Reading timestamp
	            	{
	            		label = 0;
	            		continue;
	            	}
	            	else if(readLine.equals("[networks]"))	// Reading networks
	            	{
	            		label = 1;
	            		continue;
	            	}
	            	else if(readLine.equals("[servers]"))	// Reading server addresses
	            	{
	            		label = 2;
	            		// Used for networks without group name
	            		networksList.put("Random", new Network("Random"));
	            		continue;
	            	}
	            	else if(readLine.equals(""))
	            	{
	            		continue;
	            	}
	            	
	            	// Values
	            	if(label == 1)
	            	{
	            		String networks[] = readLine.split("=");
	            		
	            		// Add Groups
	            		networksList.put(networks[1], new Network(networks[1]));            		
	            	}
	        		else if(label == 2)
	        		{
	        			List<Integer> portsList = new ArrayList<Integer>();
	        			
	        			String servers[] = readLine.split("=");
	        			
	        			// Get name, group, address and portlist
	            		String name = servers[1].split("SERVER:")[0];            		
	            		String group = servers[1].split("GROUP:")[1];
	            		String address = servers[1].split(":")[1];            		
	            		String ports = servers[1].split(":")[2].split("GROUP")[0];
	            		
	            		// Split up port array
	            		String portArray[] = ports.split(",");            		
	            		for(int i=0; i < portArray.length; i++)
	            		{
	            			// Check if entry is a range
	            			String portNumbers[] = portArray[i].split("-");
	            			if(portNumbers.length == 2)
	            			{
	            				// Add all ports one by one
	            				for(int p = Integer.parseInt(portNumbers[0]); p <= Integer.parseInt(portNumbers[1]); p++)
	             					portsList.add(p);
	            			}
	            			else
	            			{
	            				// Add single port, no clue why some have a + sign
	            				if(portArray[i].charAt(0) == "+".charAt(0))		
	            					portsList.add(Integer.parseInt(portArray[i].substring(1)));                				
	                			else
	                				portsList.add(Integer.parseInt(portArray[i]));
	            			}
	            		}
	            		
	            		// All data is know, create a server object           		
	            		Server sv = new Server(name);            		
	            		sv.setAddress(address);
	            		sv.setPorts(portsList);
	            		
	            		// Add server object to the network list (check if the group exists)
	            		if(networksList.containsKey(group))            		
	            			networksList.get(group).getServers().add(sv);
	            		else
	            		{
	            			//Log.d("group", "Group does not exist in list: " + group);
	            			networksList.get("Random").getServers().add(sv);
	            		}
	            	}
	            }

		        // Close the InputStream and BufferedReader
		        is.close();
		        br.close();
		        
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}

		public Hashtable<String, Network> getNetworksList() {
			return networksList;
		}
	}
	
	public class Network
	{		
		private String mName;
		private List<Server> mServers = new ArrayList<Server>();

		public Network(String name) {
			mName = name;
		}
		
		public String getName() {
			return mName;
		}
		
		public List<Server> getServers() {
			return mServers;
		}
	}
	
	public class Server
	{		
		private String mName;
		private String mAddress;
		private List<Integer> mPorts = new ArrayList<Integer>();
		
		public Server(String name) {
			mName = name;
		}
		
		public String getName() {
			return mName;
		}
		
		public String getAddress() {
			return mAddress;
		}
		
		public void setAddress(String address) {
			mAddress = address;
		}
		
		public List<Integer> getPorts() {
			return mPorts;
		}
		
		public void setPorts(List<Integer> ports) {
			mPorts = ports;
		}
	}
}
