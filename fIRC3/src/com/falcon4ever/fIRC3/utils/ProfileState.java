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

public class ProfileState {

	private int    mProfileId;
	private String mProfileName;
	private String mProfileNickname;
	private String mProfileAltnick;
	private String mProfileServer; 
	private int    mProfilePort; 
	private String mProfileChatrooms;
	private String mProfileIdent;
	private String mProfileRealname;
	private int    mProfileEncoding;
	private String mProfileOnconnect;
	
	private Boolean mConnected;	// Remove

	public int getProfileId() {
		return mProfileId;
	}

	public void setProfileId(int profileId) {
		mProfileId = profileId;
	}

	public String getProfile_name() {
		return mProfileName;
	}

	public void setProfileName(String profileName) {
		mProfileName = profileName;
	}

	public String getProfileNickname() {
		return mProfileNickname;
	}

	public void setProfileNickname(String profileNickname) {
		mProfileNickname = profileNickname;
	}

	public String getProfileAltnick() {
		return mProfileAltnick;
	}

	public void setProfileAltnick(String profileAltnick) {
		mProfileAltnick = profileAltnick;
	}

	public String getProfileServer() {
		return mProfileServer;
	}

	public void setProfileServer(String profileServer) {
		mProfileServer = profileServer;
	}

	public int getProfilePort() {
		return mProfilePort;
	}

	public void setProfilePort(int profilePort) {
		mProfilePort = profilePort;
	}

	public String getProfileChatrooms() {
		return mProfileChatrooms;
	}

	public void setProfileChatrooms(String profileChatrooms) {
		mProfileChatrooms = profileChatrooms;
	}

	public String getProfileIdent() {
		return mProfileIdent;
	}

	public void setProfileIdent(String profileIdent) {
		mProfileIdent = profileIdent;
	}

	public String getProfileRealname() {
		return mProfileRealname;
	}

	public void setProfileRealname(String profileRealname) {
		mProfileRealname = profileRealname;
	}

	public int getProfileEncoding() {
		return mProfileEncoding;
	}

	public void setProfileEncoding(int profileEncoding) {
		mProfileEncoding = profileEncoding;
	}

	public String getProfileOnconnect() {
		return mProfileOnconnect;
	}

	public void setProfileOnconnect(String profileOnconnect) {
		mProfileOnconnect = profileOnconnect;
	}

	public Boolean getConnected() {
		return mConnected;
	}

	public void setConnected(Boolean connected) {
		mConnected = connected;
	}	
}
