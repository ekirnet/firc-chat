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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.falcon4ever.fIRC3.service.ChatService;

public class HomeActivity extends Activity {

	private Shortcut mIcons[] = { 
		new Shortcut(R.drawable.ic_menu_start_conversation, "Chat", 			"com.falcon4ever.fIRC3.ServerlistActivity"),
		new Shortcut(R.drawable.ic_menu_refresh, 			"File transfers", 	"com.falcon4ever.fIRC3.PlaceholderActivity"),
		new Shortcut(R.drawable.ic_menu_preferences, 		"Settings", 		"com.falcon4ever.fIRC3.PlaceholderActivity"),
		new Shortcut(R.drawable.ic_menu_help, 				"Manual", 			"homepage"),
		new Shortcut(R.drawable.ic_menu_info_details, 		"About", 			"about"),
		new Shortcut(R.drawable.ic_lock_power_off, 			"Logout", 			"logout"),
	};	
	private LinearLayout mIconRows[] = new LinearLayout[4];
	private String mVersion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
                        
        try {
            PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersion = manager.versionName;
            setTitle("fIRC - The Android IRC Client (v" + mVersion + ")");
        } 
        catch (NameNotFoundException e) {
        	mVersion = "";
        }
                                
        // Initialize icon rows                
        for(int i = 0; i < mIconRows.length; i++)
        {
        	mIconRows[i] = new LinearLayout(this);
        	mIconRows[i].setOrientation(LinearLayout.HORIZONTAL);
        }
        
        // Place icons
        switch(getResources().getConfiguration().orientation)
        {
        	case Configuration.ORIENTATION_UNDEFINED:
        	case Configuration.ORIENTATION_SQUARE:
        	case Configuration.ORIENTATION_PORTRAIT:
        	{        		     		
        		for(int i = 0; i < mIconRows.length; i++)
        			mIconRows[i].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));   
        		     
        		addIcons(Configuration.ORIENTATION_PORTRAIT, 3);
        	}
        	break;
        	
        	case Configuration.ORIENTATION_LANDSCAPE:
        	{	
        		mIconRows[1].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));   			   				
        		mIconRows[2].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1)); 
				        		        		
        		addIcons(Configuration.ORIENTATION_LANDSCAPE, 5);
        	}
        	break;
        	        		
        	default:        	
        }
               
        // Add views
        final LinearLayout homeIcons = (LinearLayout)findViewById(R.id.home_screen);
        for(int i = 0; i < mIconRows.length; i++)
        	homeIcons.addView(mIconRows[i]);
        
        // Start background service
        startService(new Intent(this, ChatService.class));  
       
        // One time disclaimer
        SharedPreferences settings = getSharedPreferences("fIRC_settings", 0);
        boolean disclaimer = settings.getBoolean("firc_disclaimer", false);
        
        if(!disclaimer) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
	        builder.setIcon(android.R.drawable.ic_dialog_alert);
	        builder.setTitle(R.string.alert_dialog_title);
	        builder.setMessage(R.string.alert_dialog_msg);
	        builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	SharedPreferences settings = getSharedPreferences("fIRC_settings", 0);
					SharedPreferences.Editor editor = settings.edit();					
					editor.putBoolean("firc_disclaimer", true);
					editor.commit();
	            }
	        });        
	        builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	stopService(new Intent(HomeActivity.this, ChatService.class));
        			finish();
	            }
	        });
	        builder.show();
	    }  
    }
    
    private void addIcons(int orientation, int maxicons)
    {
    	int row = 0, counter = 0;    	
    	if(orientation == Configuration.ORIENTATION_LANDSCAPE)
    		row = 1;
    	
    	for(int i = 0; i < mIcons.length; i++)
		{
    		final int j = i;
    		++counter;
			    		
    		LinearLayout iconWrap = new LinearLayout(this);
    		iconWrap.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
    		iconWrap.setOrientation(LinearLayout.VERTICAL);
    		
    		LinearLayout iconImageWrap = new LinearLayout(this);
    		iconImageWrap.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
    		iconImageWrap.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
    		
    		ImageView iconImage = new ImageView(this);   
    		iconImage.setImageResource(mIcons[i].getResId());
    		iconImage.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    		
    		TextView iconText = new TextView(this);
    		iconText.setText(mIcons[i].getLabel());
    		iconText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
    		iconText.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, (float) 1.35));
    		
    		iconImageWrap.addView(iconImage);
    		iconWrap.addView(iconImageWrap);
    		iconWrap.addView(iconText);
    		
    		iconWrap.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) 
            	{
            		// Set action
            		if(mIcons[j].getClassname().equals("logout"))
            		{       
            			stopService(new Intent(HomeActivity.this, ChatService.class));
            			finish();
            		}
            		else if(mIcons[j].getClassname().equals("homepage"))
            		{
            			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.multigesture.net/projects/firc/"));
            			startActivity(browserIntent);
            		}
            		else if(mIcons[j].getClassname().equals("about"))
            		{
            		    AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
            		    alertDialog.setTitle("About");            		                		    
            		    alertDialog.setMessage(
    		    		"Author: Laurence Muller\n" +
    		    		"E-mail: info@falcon4ever.com\n" +
            		    "Website: multigesture.net/firc\n\n" +
            		    "fIRC version: v" + mVersion + "\n" +
            		    "License type: Freeware\n\n" + 
            		    "Special thanks to (*donated):\n\nThisRob*, SanMehat, languish, Disconnect, hmepass, JesusFreke, kirberich, rashed2020, kash and everyone else I forgot.");
            		    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            		    	public void onClick(DialogInterface dialog, int which) {
            		    		return;
            		    	} 
            		    });             		    
            		    alertDialog.show();            		    
            		}
            		else
                    {
                    	Intent intent = new Intent();                		
                		intent.setClassName(HomeActivity.this, mIcons[j].getClassname());
                		startActivity(intent); 
                    }   
            	}
            });
    		mIconRows[row].addView(iconWrap);
    		
			if(counter == maxicons) 
			{
				++row;   
				counter = 0;
			}
		}
    }
	
	private class Shortcut
	{
		public int mResId;
		public String mLabel;
		public String mClassname;
		
		Shortcut(int resId, String label, String classname)
		{	
			mResId = resId;
			mLabel = label;
			mClassname = classname;
		}
		
		public int getResId() {
			return mResId;
		}
		
		public String getLabel() {
			return mLabel;
		}
		
		public String getClassname() {
			return mClassname;
		}
	}
}