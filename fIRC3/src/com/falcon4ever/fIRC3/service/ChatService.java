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

package com.falcon4ever.fIRC3.service;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.falcon4ever.fIRC3.utils.DBProfileManager;
import com.falcon4ever.fIRC3.utils.ProfileState;

public class ChatService extends Service {
	
	public static final int MSG_UI_INIT = 1;
	public static final int MSG_UI_INIT_DONE = 2;
	public static final int MSG_UI_UPDATE = 3;
		
	private Handler mHandler;
	private int mActiveProfile = -1;
    private final IBinder mBinder = new LocalBinder();

    // List of Connections
    private Hashtable<Integer, IRCConnection> mConnectionList = new Hashtable<Integer, IRCConnection>();
    
    @Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(getClass().getSimpleName(),"Start fIRC3 background service");		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(getClass().getSimpleName(),"Shutting down fIRC3 background service");
		disconnectAll();		
	}

    public class LocalBinder extends Binder {
    	public ChatService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ChatService.this;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    public void connect(int profile_id)
    {    	
    	DBProfileManager db = new DBProfileManager(this);
    	ArrayList<Object> curProfile = db.getRowAsArray(profile_id);        
        db.close();
        
        ProfileState ps = new ProfileState();
    	ps.setProfileId(Integer.parseInt(curProfile.get(0).toString()));
    	
    	ps.setProfileName(curProfile.get(1).toString());
    	ps.setProfileNickname(curProfile.get(2).toString());
    	ps.setProfileAltnick(curProfile.get(3).toString());
    	ps.setProfileServer(curProfile.get(4).toString());
    	ps.setProfilePort(Integer.parseInt(curProfile.get(5).toString()));        	
    	ps.setProfileChatrooms(curProfile.get(6).toString());
    	ps.setProfileIdent(curProfile.get(7).toString());
    	ps.setProfileRealname(curProfile.get(8).toString());
    	ps.setProfileEncoding(Integer.parseInt(curProfile.get(9).toString()));
    	ps.setProfileOnconnect(curProfile.get(10).toString());
    	        	
    	ps.setConnected(false);
    		
    	String[] encoding_array = getResources().getStringArray(com.falcon4ever.fIRC3.R.array.encoding_array);
    	String profile_encoding = encoding_array[Integer.parseInt(curProfile.get(9).toString())];
    	
    	addConnection(ps, profile_encoding);  
    	
    	Log.d(getClass().getSimpleName(), "Launching single profile: " + mConnectionList.get(profile_id).getConnectionName());
    	mConnectionList.get(profile_id).startThread();
    }
    
    public void disconnect(int profile_id)
    {
    	if(mConnectionList.containsKey(profile_id))
		{
			mConnectionList.get(profile_id).stopThread();
			mConnectionList.remove(profile_id);
		}
    }
    
    private void addConnection(ProfileState ps, String encoding)
    {
    	if(mConnectionList.containsKey(ps.getProfileId()))
		{
			mConnectionList.get(ps.getProfileId()).stopThread();
			mConnectionList.remove(ps.getProfileId());
		}
    
    	IRCConnection myConn = new IRCConnection(ps, encoding, this);    	    	
    	mConnectionList.put(ps.getProfileId(), myConn);    	
    }
        
    public void connectAll()
    {	
    	// Query all connections from db
    	DBProfileManager db = new DBProfileManager(this);
        ArrayList<ArrayList<Object>> profiles = db.getAllRowsAsArrays();
        db.close();
                
        // Fill profiles
        for(int i = 0; i < profiles.size(); i++) 
        {
        	ArrayList<Object> values = profiles.get(i);
        	
        	ProfileState ps = new ProfileState();
        	ps.setProfileId(Integer.parseInt(values.get(0).toString()));
        	
        	ps.setProfileName(values.get(1).toString());
        	ps.setProfileNickname(values.get(2).toString());
        	ps.setProfileAltnick(values.get(3).toString());
        	ps.setProfileServer(values.get(4).toString());
        	ps.setProfilePort(Integer.parseInt(values.get(5).toString()));        	
        	ps.setProfileChatrooms(values.get(6).toString());
        	ps.setProfileIdent(values.get(7).toString());
        	ps.setProfileRealname(values.get(8).toString());
        	ps.setProfileEncoding(Integer.parseInt(values.get(9).toString()));
        	ps.setProfileOnconnect(values.get(10).toString());
        	        	
        	ps.setConnected(false);
        		
        	String[] encoding_array = getResources().getStringArray(com.falcon4ever.fIRC3.R.array.encoding_array);
        	String profile_encoding = encoding_array[Integer.parseInt(values.get(9).toString())];
        	
        	addConnection(ps, profile_encoding);
        }
                
        for(IRCConnection conn : mConnectionList.values()) 
        {
        	Log.d(getClass().getSimpleName(), "Launching profile: " + conn.getConnectionName());
        	conn.startThread();
        }
    }
    
    public void disconnectAll()
    {
    	for(IRCConnection conn : mConnectionList.values())
    		conn.stopThread();
    	
    	mConnectionList.clear(); 
    	// TODO: Update status?
    }
    
    public boolean getConnectionStatus(int profileId)
    {
    	if(mConnectionList.containsKey(profileId))
    		return true;
    	else
    		return false;
    }
     	
	public IRCConnection getIrcConn(int profileId) 
	{
		if(mConnectionList.containsKey(profileId))
			return mConnectionList.get(profileId);
		else
			return null;
	}

	public synchronized Handler getHandler() {
		return mHandler;
	}

	public synchronized void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public synchronized void setActiveProfile(int activeProfile) {
		mActiveProfile = activeProfile;
	}

	public synchronized int getActiveProfile() {
		return mActiveProfile;
	}
}
