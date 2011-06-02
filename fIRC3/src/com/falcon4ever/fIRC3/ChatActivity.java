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
import java.util.HashMap;
import java.util.Map.Entry;

import org.jared.commons.ui.R;
import org.jared.commons.ui.WorkspaceView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.falcon4ever.fIRC3.service.ChatService;
import com.falcon4ever.fIRC3.service.ChatService.LocalBinder;
import com.falcon4ever.fIRC3.utils.ChatMessage;

public class ChatActivity extends Activity {
	
	private HashMap<String, ArrayList<ChatMessage>> mChatMessagesList = new HashMap<String, ArrayList<ChatMessage>>();
	private HashMap<String, ChatAdapter> mChatAdapterList = new HashMap<String, ChatAdapter>();
	private HashMap<Integer, String> mScreenLUT = new HashMap<Integer, String>();

	private ChatService mService;
	private boolean mBound = false;
	private int mProfileId;
	
	private EditText mChatInput;
	private WorkspaceView mWorkView = null;
	private LayoutInflater mInflater;		
	
	private Handler IncomingHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			//Log.d("IncomingHandler", "UI msg: " + msg.what);
			switch (msg.what)
			{
				// Replay messages
				case ChatService.MSG_UI_INIT:				
				{
					ChatMessage chatmsg = (ChatMessage) msg.obj;					
					if(mChatMessagesList.containsKey(chatmsg.getChannel()))
					{	
						mChatMessagesList.get(chatmsg.getChannel()).add(chatmsg);			
					}			
					else
					{			
						// Add channel
						mChatMessagesList.put(chatmsg.getChannel(), new ArrayList<ChatMessage>());
						mChatMessagesList.get(chatmsg.getChannel()).add(chatmsg);
					}
				}
				break;
					
				// Initialize panels
				case ChatService.MSG_UI_INIT_DONE:				
				{
					addPanels();
				}
				break;
				
				// Update UI
				case ChatService.MSG_UI_UPDATE:
				{
					ChatMessage chatmsg = (ChatMessage)msg.obj;					
					
					if(mChatMessagesList.containsKey(chatmsg.getChannel()))
					{	
						mChatMessagesList.get(chatmsg.getChannel()).add(chatmsg);			
					}			
					else
					{			
						mChatMessagesList.put(chatmsg.getChannel(), new ArrayList<ChatMessage>());
						mChatMessagesList.get(chatmsg.getChannel()).add(chatmsg);
						addPanels();
						return;
					}
					
					// update adapter
					mChatAdapterList.get(chatmsg.getChannel()).notifyDataSetChanged();
				}
				break;
				
				default:
					Log.e(getClass().getSimpleName(), "Unhandled message IncomingHandler " + msg.what);
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View mainLayout = mInflater.inflate(R.layout.chat, null, false);
        setContentView(mainLayout);
        
		mChatInput = (EditText)findViewById(R.id.chat_input);
        final Button submitButton = (Button)findViewById(R.id.chat_submit);       
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	submitMessage();
            }            
        });
        
        final Bundle extras = getIntent().getExtras(); 
        mProfileId = extras.getInt("profile_id");
        Log.d(getClass().getSimpleName(),"Chat: profile_id: " + Integer.toString(mProfileId));
                        
		// Hide keyboard on Activity launch
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        // Bind to LocalService
		Intent intent = new Intent(this, ChatService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);  
		
		mChatMessagesList.put("server", new ArrayList<ChatMessage>());
   	}	
		
	@Override
	protected void onDestroy() {
        super.onDestroy();
                
        // Unbind from the service
        if (mBound) {
        	unbindService(mConnection);
            mBound = false;
        }
    }
	    
    @Override
    protected void onPause() {
        super.onPause();

        if (mBound) {
        	if(mWorkView != null)
        		mService.getIrcConn(mProfileId).setCurrentScreen(mWorkView.getCurrentScreen());
        	
        	mService.setHandler(null);
        	mService.setActiveProfile(-1);
        }  
    }
    
	private void submitMessage()
	{
		String msg = mChatInput.getText().toString();
		
		if(msg.length() > 0)
		{
			String newmessage = "";
						
			if(mScreenLUT.get(mWorkView.getCurrentScreen()).equalsIgnoreCase("server"))
			{
				// Send RAW message to server
				newmessage = msg;
			}
			else
			{
				// PRIVMSG dest :msg
				newmessage = "PRIVMSG " + mScreenLUT.get(mWorkView.getCurrentScreen()) + " :" + msg;
			}
			
			if (mBound)
	        {   
	        	if(mService.getIrcConn(mProfileId) != null)
	        		mService.getIrcConn(mProfileId).sendMessage(newmessage);
	        }			
		}
		
		mChatInput.setText("");
	}
	
	private void addPanels()
	{
		Log.d(getClass().getSimpleName(), "addPanels()");
		
		mChatAdapterList.clear();
		FrameLayout chatFrame = (FrameLayout)findViewById(R.id.chat_frame);			
		chatFrame.removeAllViews();
		mWorkView = null;
		
		// Create new WorkspaceView
		mWorkView = new WorkspaceView(this, null);
	    mWorkView.setTouchSlop(32);
	    
	    boolean hasChannels = false;
	    
	    int i = 0;
	    if(mBound)
	    {   	
		    for (Entry<String, ArrayList<ChatMessage>> entry: mChatMessagesList.entrySet()) {
		    	hasChannels = true;
		    			    	
		    	View view = mInflater.inflate(R.layout.chat_view, null, false);
				mWorkView.addView(view);
				
				TextView chatTitlebar = (TextView)view.findViewById(R.id.chat_titlebar);  
				chatTitlebar.setText(entry.getKey());  
				
		    	ListView lv = (ListView)view.findViewById(R.id.chat_list);
		    	lv.setFastScrollEnabled(true);
		    	
		    	ChatAdapter ca = new ChatAdapter(this, R.layout.chat_row, (ArrayList<ChatMessage>)entry.getValue());		    	
		    	mChatAdapterList.put(entry.getKey(), ca);
		    	mScreenLUT.put(i++, entry.getKey());
		    	
				lv.setAdapter(ca);	    	
			}
		    
		    if(hasChannels)
		    {
				chatFrame.addView(mWorkView);
				
				if(mWorkView != null)
	            	mWorkView.setCurrentScreen(mService.getIrcConn(mProfileId).getCurrentScreen());
		    }
	    }
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////  
    // ServiceConnection
    ///////////////////////////////////////////////////////////////////////////////////////////////
        
    private ServiceConnection mConnection = new ServiceConnection() {
    	
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            
            Log.d("ChatActivity", "onServiceConnected");
                        
            mService.setHandler(IncomingHandler);
            mService.setActiveProfile(mProfileId);
            mService.getIrcConn(mProfileId).initializePanels();
        }

        public void onServiceDisconnected(ComponentName className) {
        	mBound = false;        	
        }        
    };
        
	///////////////////////////////////////////////////////////////////////////////////////////////  
    // ChatAdapter
    ///////////////////////////////////////////////////////////////////////////////////////////////
          
    private class ChatAdapter extends ArrayAdapter<ChatMessage> {
    	
    	public static final int USER_TEXT_COLOR			= 0xff000000;
    	public static final int USER_BACKGROUND_COLOR 	= 0xfff8d8f8;
    	public static final int CHANNEL_TEXT_COLOR		= 0xff000000;
    	public static final int CHANNEL_BACKGROUND_COLOR 	= 0xffd8e8f8;
    	public static final int SERVER_TEXT_COLOR 		= 0xff000000;
    	public static final int SERVER_BACKGROUND_COLOR	= 0xffe0e0e0;
    	
        private ArrayList<ChatMessage> mItems;
        private LayoutInflater mInflater;
        
        public ChatAdapter(Context context, int textViewResourceId, ArrayList<ChatMessage> items) {
            super(context, textViewResourceId, items);
            mItems = items;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.chat_row, parent, false);
            }
            
            ChatMessage o = mItems.get(position);
            if (o != null) {
            	TextView tt = (TextView) convertView.findViewById(R.id.label);
            	if (tt != null) {
            		
            		if(o.getType() == 0)
            		{
            			tt.setTextColor(USER_TEXT_COLOR);
            			tt.setBackgroundColor(USER_BACKGROUND_COLOR);
            		}
            		else if(o.getType() == 1)
            		{
            			tt.setTextColor(CHANNEL_TEXT_COLOR);
            			tt.setBackgroundColor(CHANNEL_BACKGROUND_COLOR);
            		}
            		else
            		{
            			tt.setTextColor(SERVER_TEXT_COLOR);
            			tt.setBackgroundColor(SERVER_BACKGROUND_COLOR);
            		}
            		
            		tt.setText(o.getChatMessage());
            	}
            }                
            
            return convertView;
        }
    }
}
