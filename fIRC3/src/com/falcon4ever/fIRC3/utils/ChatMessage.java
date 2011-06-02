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

package com.falcon4ever.fIRC3.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable {
	private int mType;
	private String mChannel;
	private String mChatMessage;    
    		
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mType);
        out.writeString(mChannel);
        out.writeString(mChatMessage);
    }

    public static final Parcelable.Creator<ChatMessage> CREATOR
            = new Parcelable.Creator<ChatMessage>() {
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };
    
    private ChatMessage(Parcel in) {
    	mType = in.readInt();
    	mChannel = in.readString();
    	mChatMessage = in.readString();
    }
    
    ///////
    
	public ChatMessage(){		
	}
	
	public String getChatMessage() {
		return mChatMessage;
	}
	
	public void setChatMessage(String chatMessage) {
		mChatMessage = chatMessage;
	}
	
	public int getType() {
		return mType;
	}
	
	public void setType(int type) {
		mType = type;
	}

	public String getChannel() {
		return mChannel;
	}
	
	public void setChannel(String channel) {
		mChannel = channel;
	}
}
