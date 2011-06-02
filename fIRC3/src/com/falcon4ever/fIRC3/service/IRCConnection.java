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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.util.Log;

import com.falcon4ever.fIRC3.utils.ChatMessage;
import com.falcon4ever.fIRC3.utils.ProfileState;

// http://www.ietf.org/rfc/rfc1459.txt
// http://www.zigwap.com/mirc/raw_events.php
// http://www.mirc.net/raws/

// TODO: Output to logfile on SDcard

public class IRCConnection implements Runnable {
	
	// Thread
	private Thread mIrcThread;	
	private volatile boolean mThreadDone = false;
	
	// Connection
	private Socket mIrcSocket = null;
	private BufferedReader mIrcInput = null;
	private BufferedWriter mIrcOutput = null;	
		
	// Profile and encoding
	private ProfileState mProfile;
	private String mEncoding;
    private int mCurrentScreen = 0; 
    private ChatService mParent;
    
	//[channel name][channel msg]
	private HashMap<String, ArrayList<ChatMessage>> mChatMessagesList = new HashMap<String, ArrayList<ChatMessage>>();
	
	private synchronized HashMap<String, ArrayList<ChatMessage>> getChatMessagesList() {
		return mChatMessagesList;
	}

	private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm"); // ("HH:mm:ss");
	
	// Constants	
	public static final int MAX_CHAT_HISTORY = 1000;
	
	public static final int CONNECTION_OFFLINE = 0;
	public static final int CONNECTION_WORKING = 1;
	public static final int CONNECTION_CONNECTED = 2;	
	public static final int CONNECTION_DISCONNECTED = 3;
	
	///////////////////////////////////////////////////////////////////////////////////////////////  
    // IRCConnection
    ///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Constructor	
	public IRCConnection(ProfileState profile, String encoding, ChatService parent)
	{		
		mProfile = profile;
		mEncoding = encoding;
		mParent = parent;
		mCurrentScreen = 0;
	}
	
	public void run() 
	{
		addChannel("server");		
		Log.d(getClass().getSimpleName(), "Starting profile: " + mProfile.getProfile_name());
		
		try {
			//1. creating a socket to connect to the server
			mIrcSocket = new Socket(mProfile.getProfileServer(), mProfile.getProfilePort());
			
			Log.d(getClass().getSimpleName(), mProfile.getProfile_name() + ": Connected to " + mProfile.getProfileServer() + " in port " + mProfile.getProfilePort());
					
			//2. get Input and Output streams
			mIrcInput = new BufferedReader(new InputStreamReader(mIrcSocket.getInputStream(), mEncoding));
			mIrcOutput = new BufferedWriter(new OutputStreamWriter(mIrcSocket.getOutputStream(), mEncoding));
			mIrcOutput.flush();
												
			// 4.1.2 Nick message TODO: set limit
			int nickname_limit =9;
			if(mProfile.getProfileNickname().length() > nickname_limit)
				mProfile.setProfileName(mProfile.getProfileNickname().substring(0, nickname_limit));
			sendMessage("NICK " + mProfile.getProfileNickname());				
			
			// 4.1.3 User message
			sendMessage("USER " + mProfile.getProfileIdent() + " 0 * :" + mProfile.getProfileRealname());
			
			//3: Communicating with the server
			do
			{			 
				processMessage(mIrcInput.readLine());							
			}
			while(!mThreadDone);
		}
		catch(UnknownHostException unknownHost)
		{
			Log.e(getClass().getSimpleName(), mProfile.getProfile_name() + ": You are trying to connect to an unknown host!");
		}
		catch(IOException ioException)
		{
			//Log.e(getClass().getSimpleName(), connName + " Error1" + ioException.getMessage(), ioException);
			Log.d(getClass().getSimpleName(), mProfile.getProfile_name() + ": Forcing shutdown? " + ioException.getMessage());		
		}
		finally
		{	
			if(mIrcOutput != null)
				sendMessage("QUIT :Powered by fIRC v3.0, the android IRC client.");
						
			//4: Closing connection
			try
			{
				if(mIrcInput != null)
					mIrcInput.close();
				if(mIrcOutput != null)					
					mIrcOutput.close();
				if(mIrcSocket != null)
					mIrcSocket.close();
				Log.d(getClass().getSimpleName(), mProfile.getProfile_name() + ": Closed socket");				
			}
			catch(IOException ioException)
			{
				ioException.printStackTrace();
				Log.e(getClass().getSimpleName(), mProfile.getProfile_name() + ": ioException while closing connection", ioException);
			}
		}				
		
		Log.d(getClass().getSimpleName(), mProfile.getProfile_name() + ": Finished");
		mIrcThread = null;		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////  
    // Process
    ///////////////////////////////////////////////////////////////////////////////////////////////

	// http://www.ietf.org/rfc/rfc1459.txt	
	private void processMessage(String msg)
 	{
		Log.d(getClass().getSimpleName(), "Incoming message: " + msg);				
		
		// If something went wrong, kill thread
		if(msg == null)
		{
			mThreadDone = true;
			return;
		}
		
		// 4.6.2 Ping message
		if(msg.substring(0,4).equalsIgnoreCase("PING"))
		{	
			addMessage("server", "< " + msg, 2);
		
			int values = msg.indexOf(":");
			// 4.6.3 Pong message
			
			sendMessage("PONG " + msg.substring(values));			
			return;
		}
		
		// Figure out what's happening
		String cmds_text = msg;
		String[] cmds_item = null;
		cmds_item = cmds_text.split(" ");
		
		if(cmds_item[1].equalsIgnoreCase("001"))
		{
			sendMessage("USERHOST " + mProfile.getProfileNickname());
		}
		//else if(cmds_item[1].equalsIgnoreCase("422"))
		else if(cmds_item[1].equalsIgnoreCase("376"))
		{
			String[] chatrooms = null;
			chatrooms = mProfile.getProfileChatrooms().split("\n");
			
			for(String room : chatrooms)
			{
				sendMessage("JOIN " + room);
				//addChannel(room);						
			}
		}		
		else if(cmds_item[1].equalsIgnoreCase("JOIN"))
		{
			// TODO Handle join by others
			// Handle own channel join 
			String[] values_item = cmds_item[0].substring(1).split("!");			
			if(values_item[0].equalsIgnoreCase(mProfile.getProfileNickname()))
			{
				int values = cmds_item[2].indexOf(":") + 1;			
				addMessage(cmds_item[2].substring(values), "Now talking in " + cmds_item[2].substring(values), 2);
			}
		}
		
		
		if(cmds_item[1].equalsIgnoreCase("PRIVMSG"))
		{
			int values = msg.indexOf(":",1) + 1;
			String nickname = cmds_item[0].substring(1, cmds_item[0].indexOf("!"));
			
			Date date = new Date();			
			String line = "[" + mDateFormat.format(date) + "] <" + nickname +  "> " + msg.substring(values);
			
			addMessage(cmds_item[2], line, 1);
		}
		else
			addMessage("server", "< " + msg, 2);		
 	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////  
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

	public void sendMessage(String msg)
 	{
 		try
 		{	
 			mIrcOutput.write(msg);
 			mIrcOutput.newLine();
 			mIrcOutput.flush();
 			
 			Log.d(getClass().getSimpleName(), mProfile.getProfile_name() + ": RAW sendMessage: " + msg);
 			
 			// LOG OUTPUT
			addMessage("server", "> " + msg, 2);
			
			// Incase of privmsg			
			String[] cmds_item = msg.split(" ");			
			if(cmds_item[0].equalsIgnoreCase("PRIVMSG"))
			{	
				Date date = new Date();			
				//String line = "[" + mDateFormat.format(date) + "] <" + mProfile.getProfile_nickname() +  "> " + cmds_item[2].substring(1);
				//String[] chatmsg = msg.split(":");
				
				String chatmsg = msg.substring(msg.indexOf(":")+1);
				
				String line = "[" + mDateFormat.format(date) + "] <" + mProfile.getProfileNickname() +  "> " + chatmsg;
				
				addMessage(cmds_item[1], line, 0);
			}
 		}
 		catch(IOException ioException)
 		{
 			ioException.printStackTrace();
 		}
 	}
		
	public int getCurrentScreen() {		
		return mCurrentScreen;
	}

	public void setCurrentScreen(int currentScreen) {		
		mCurrentScreen = currentScreen;
	}
	
	public String getConnectionName()
	{
		return mProfile.getProfile_name();
	}
	
	public int getConnectionID()
	{
		return mProfile.getProfileId();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////  
    // Thread
    ///////////////////////////////////////////////////////////////////////////////////////////////

	public void startThread() {		
        if ( mIrcThread == null ) 
        { 
        	mIrcThread = new Thread(this);
        	mThreadDone = false; 
        	mIrcThread.start(); 
        } 
    } 
 
    public void stopThread() {
    	mThreadDone = true;
    	
    	try
		{				
			if(mIrcSocket != null)
				mIrcSocket.close();
			Log.d(getClass().getSimpleName(), mProfile.getProfile_name() + ": Forcing shutdown (closing socket)");				
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
			Log.e(getClass().getSimpleName(), mProfile.getProfile_name() + ": Error while forcing shutdown", ioException);
		}
    }
	
	///////////////////////////////////////////////////////////////////////////////////////////////  
    // Chat History
    ///////////////////////////////////////////////////////////////////////////////////////////////
	
    public synchronized void initializePanels()
    {
	    if(mParent.getHandler() != null)
		{	
	    	if(mParent.getActiveProfile() == mProfile.getProfileId())
	    	{
	    		// Dirty replay messages
		    	for (Entry<String, ArrayList<ChatMessage>> entry : getChatMessagesList().entrySet()) 
		    	{
		    		for(ChatMessage chatmsg : entry.getValue())
		    		{
		    			mParent.getHandler().obtainMessage(ChatService.MSG_UI_INIT, 0, 0, chatmsg).sendToTarget();
		    		}
		    	}
	    		    	
		    	// Finished update panels
	    		mParent.getHandler().obtainMessage(ChatService.MSG_UI_INIT_DONE).sendToTarget();
	    	}
		}
    }
    
	private void addChannel(String channelName)	{		
		getChatMessagesList().put(channelName, new ArrayList<ChatMessage>());		
	}
	
	private void addMessage(String channel, String message, int type) {		
		ChatMessage msg = new ChatMessage();	        
		msg.setChatMessage(message);
		msg.setChannel(channel);
		msg.setType(type);	
		
		if(getChatMessagesList().containsKey(channel))
		{
			if(getChatMessagesList().get(channel).size() >= MAX_CHAT_HISTORY)
				getChatMessagesList().get(channel).remove(0);
			
			getChatMessagesList().get(channel).add(msg);			
		}			
		else
		{			
			getChatMessagesList().put(channel, new ArrayList<ChatMessage>());
			getChatMessagesList().get(channel).add(msg);
		}
		
		if(mParent.getHandler() != null)
		{			
			if(mParent.getActiveProfile() == mProfile.getProfileId())
				mParent.getHandler().obtainMessage(ChatService.MSG_UI_UPDATE,0,0,msg).sendToTarget();			
		}
	}
}
