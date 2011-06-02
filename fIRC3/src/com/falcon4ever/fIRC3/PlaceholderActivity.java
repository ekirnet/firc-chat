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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

// Notes: Just an experiment to show current playing track info

public class PlaceholderActivity extends Activity {
	private String mTrack = null;
	private String mArtist = null;
	private String mAlbum = null;    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.placeholder);
	}
	
	@Override
	protected void onResume() {
        super.onResume();        
                
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.music.metachanged");		
		registerReceiver(mReceiver, filter);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
      
        unregisterReceiver(mReceiver);
    }
    
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {	
		@Override
		public void onReceive(Context context, Intent intent)
		{								
            mTrack = intent.getStringExtra("track");
            mArtist = intent.getStringExtra("artist");
            mAlbum = intent.getStringExtra("album");       
            
            String lastSong  = "Playing: \"" + mArtist + " - " + mTrack + " (" + mAlbum + ")\"";            
			Log.d("NowPlaying", lastSong);
		}
	};
}
