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

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.falcon4ever.fIRC3.service.ChatService;
import com.falcon4ever.fIRC3.service.ChatService.LocalBinder;
import com.falcon4ever.fIRC3.service.IRCConnection;
import com.falcon4ever.fIRC3.utils.DBProfileManager;
import com.falcon4ever.fIRC3.utils.ProfileState;

// TODO: Update server connection status

public class ServerlistActivity extends ListActivity {
		
	private ArrayList<ProfileState> mServers = new ArrayList<ProfileState>();
	private ServerlistAdapter mAdapter;	

	private ChatService mService;
	private boolean mBound = false;
    
	private Handler IncomingHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{					
				// Initialize panels
				case ChatService.MSG_UI_SERVERLIST_UPDATE:				
				{
					updateConnectionStatus();					
				}
				break;
				
				default:
					Log.e(getClass().getSimpleName(), "Unhandled message IncomingHandler " + msg.what);
			}
		}
	};
	
	private void updateConnectionStatus()
	{
		// Cycle over current server list
		for(int i = 0; i < mServers.size(); i++) 
        {
			mServers.get(i).setConnected(						
					mService.getConnectionStatus(
							mServers.get(i).getProfileId()
							)
			);
        }
		mAdapter.notifyDataSetChanged();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serverlist);    
        
        // Set and Update listview
        mAdapter = new ServerlistAdapter(this, R.layout.serverlist_row, mServers);
        setListAdapter(mAdapter);
        
        registerForContextMenu(getListView());
	}
        
    @Override
	protected void onResume() {
        super.onResume();        
        
	    // Bind to LocalService
		Intent intent = new Intent(this, ChatService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);       
        
		queryProfiles();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
                
        // Unbind from the service
        if (mBound) {
        	unbindService(mConnection);
            mBound = false;
        }  
    }
        
    private void queryProfiles()
    {    	    	
    	DBProfileManager db = new DBProfileManager(this);
        ArrayList<ArrayList<Object>> profiles = db.getAllRowsAsArrays();
        db.close();
        
        mServers.clear();
        
        for(int i = 0; i < profiles.size(); i++) 
        {
        	ArrayList<Object> values = profiles.get(i);
        	
        	ProfileState ps = new ProfileState();
        	ps.setProfileId(Integer.parseInt(values.get(0).toString()));
        	ps.setProfileName(values.get(1).toString());
        	ps.setConnected(IRCConnection.CONNECTION_OFFLINE);
        	        	
        	Log.d(getClass().getSimpleName(), "Loading: pID " + values.get(0).toString() + ", ProfileName " + values.get(1).toString());        	
        	
        	mServers.add(ps);
        }
        
        mAdapter.notifyDataSetChanged();        
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////  
    // ServiceConnection
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    private ServiceConnection mConnection = new ServiceConnection() {
    	
        public void onServiceConnected(ComponentName className, IBinder service) {        	
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setHandler(IncomingHandler);
            
            updateConnectionStatus();
        }

        public void onServiceDisconnected(ComponentName arg0) {
        	mBound = false;
        	mService.setHandler(null);
        }
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////  
    // Menu at bottom
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.serverlist_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) 
        {        
	        case R.id.serverlist_new_profile:   
	        {
	        	Intent intent = new Intent();                		
        		intent.setClassName(ServerlistActivity.this, "com.falcon4ever.fIRC3.ProfileActivity");
        		intent.putExtra("profile_type", "new_profile");
        		startActivity(intent);
	        }
	        return true;
	        
	        case R.id.serverlist_connect_all:   
	        	if (mBound) {	
	        		mService.connectAll();
	             }
	        return true;
	        
	        case R.id.serverlist_disconnect_all:    
	        	if (mBound) {
	        		mService.disconnectAll();	        		
	        	}
	        	// check status
	        return true;
	        
	        case R.id.serverlist_help:
	        	Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.multigesture.net/projects/firc/"));
    			startActivity(browserIntent);
	        return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Enter ChatView
	///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {
        super.onListItemClick(l, v, position, id);
                       
        if(mService.getConnectionStatus(mServers.get(position).getProfileId()) < 1)
        {
        	Toast.makeText(this, "Profile is not connected", Toast.LENGTH_SHORT).show();	
        	return;        
        }              
        
        Intent intent = new Intent();                		
		intent.setClassName(ServerlistActivity.this, "com.falcon4ever.fIRC3.ChatActivity");
		intent.putExtra("profile_id", mServers.get(position).getProfileId());
		startActivity(intent); 
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Context menu
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 0, 0, "Edit profile");
        menu.add(0, 1, 0, "Remove profile");
        menu.add(0, 2, 0, "Connect");
        menu.add(0, 3, 0, "Disconnect");
    }
    
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    
        switch(item.getItemId()) 
        {
	        case 0:
	    	{	    		
	    		Intent intent = new Intent();                		
        		intent.setClassName(ServerlistActivity.this, "com.falcon4ever.fIRC3.ProfileActivity");
        		intent.putExtra("profile_type", "edit_profile");
        		intent.putExtra("profile_id", mServers.get(info.position).getProfileId());
        		startActivity(intent);
	    	}
	    	break;
	    	
        	case 1:
        	{
        		// Disconnect !
        		mService.disconnect(mServers.get(info.position).getProfileId());
        		
        		// Remove from service
        		DBProfileManager db = new DBProfileManager(this);
        		db.deleteRow(mServers.get(info.position).getProfileId()); 
        		db.close();
        		
        		queryProfiles();
        	}
            return true;
            
        	case 2:
	    	{
	    		// Connect	    		 
	    		mService.connect(mServers.get(info.position).getProfileId());
	    	}
	    	break;
	    	
        	case 3:
	    	{
	    		// Disconnect
	    		mService.disconnect(mServers.get(info.position).getProfileId());
	    	}
	    	break;
        }
        return super.onContextItemSelected(item);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////  
    // ServerlistAdapter
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    private class ServerlistAdapter extends ArrayAdapter<ProfileState> {
    	private ArrayList<ProfileState> mItems;
        private LayoutInflater mInflater;

        public ServerlistAdapter(Context context, int textViewResourceId, ArrayList<ProfileState> items) {
            super(context, textViewResourceId, items);
            mItems = items;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                
    		// Inflate XML gui template
            if (convertView == null) 
            {
            	convertView = mInflater.inflate(R.layout.serverlist_row, parent, false);
            }
            
            // Get item from adapter
            ProfileState o = mItems.get(position);
            if (o != null) 
            {
        		// Get Views
                TextView tvServerAddress = (TextView)convertView.findViewById(R.id.serverlist_profile);
                TextView tvServerStatus = (TextView)convertView.findViewById(R.id.serverlist_status);
                ImageView ivServerStatus = (ImageView)convertView.findViewById(R.id.serverlist_status_icon);
                
                // Set Views
                if(tvServerAddress != null)
                {
                	tvServerAddress.setText("Profile: " + o.getProfile_name());                        	
                }
                
                if(tvServerStatus != null)
                {                    	
                	if(o.getConnected() == IRCConnection.CONNECTION_OFFLINE)
                		tvServerStatus.setText("Status: Offline");
                	else if(o.getConnected() == IRCConnection.CONNECTION_CONNECTING)
                		tvServerStatus.setText("Status: Connecting...");
                	else if(o.getConnected() == IRCConnection.CONNECTION_ONLINE)
                		tvServerStatus.setText("Status: Online");
                	else
                		tvServerStatus.setText("Status: Unknown");                    	
                }
                
                if(ivServerStatus != null)
                {
                	if(o.getConnected() == IRCConnection.CONNECTION_OFFLINE)
                		ivServerStatus.setImageDrawable(getContext().getResources().getDrawable(android.R.drawable.presence_offline));
                	else if(o.getConnected() == IRCConnection.CONNECTION_CONNECTING)
                		ivServerStatus.setImageDrawable(getContext().getResources().getDrawable(android.R.drawable.presence_invisible));
                	else if(o.getConnected() == IRCConnection.CONNECTION_ONLINE)
                		ivServerStatus.setImageDrawable(getContext().getResources().getDrawable(android.R.drawable.presence_online));
                	else
                		ivServerStatus.setImageDrawable(getContext().getResources().getDrawable(android.R.drawable.presence_offline));                	               	
                }
            }
            
            return convertView;
        }
    }
}
