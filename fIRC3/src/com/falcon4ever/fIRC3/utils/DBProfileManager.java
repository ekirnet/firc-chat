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

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Based on: http://www.anotherandroidblog.com/2010/08/04/android-database-tutorial/

public class DBProfileManager {

	private Context context; 
	private SQLiteDatabase db;

	private final String DB_NAME = "firc_db";
	private final int 	 DB_VERSION = 1;
	
	private final String TABLE_NAME = "db_profiles";
	private final String TABLE_ROW_ID = "id";
	
	private final String TABLE_ROW_01 = "profile_name";			// TEXT
	private final String TABLE_ROW_02 = "profile_nickname";		// TEXT
	private final String TABLE_ROW_03 = "profile_altnick";		// TEXT
	private final String TABLE_ROW_04 = "profile_server_address";//TEXT 
	private final String TABLE_ROW_05 = "profile_port";			// INTEGER 
	private final String TABLE_ROW_06 = "profile_chatrooms";	// TEXT
	private final String TABLE_ROW_07 = "profile_ident";		// TEXT
	private final String TABLE_ROW_08 = "profile_realname";		// TEXT
	private final String TABLE_ROW_09 = "profile_encoding";		// INTEGER
	private final String TABLE_ROW_10 = "profile_onconnect";	// TEXT
	
	private CustomSQLiteOpenHelper helper;
	
	public DBProfileManager(Context context)
	{
		this.setContext(context);
 
		// create or open the database
		helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();		
	}
	
	public void close()
	{
		if (helper != null) 
			helper.close();	
		
		if (db != null)
			db.close();
	}
	
	public void addRow(String r01, String r02, String r03, String r04, int r05, String r06, String r07, String r08, int r09, String r10)
	{	
		ContentValues values = new ContentValues();
		values.put(TABLE_ROW_01, r01);
		values.put(TABLE_ROW_02, r02);
		values.put(TABLE_ROW_03, r03);
		values.put(TABLE_ROW_04, r04);
		values.put(TABLE_ROW_05, r05);
		values.put(TABLE_ROW_06, r06);
		values.put(TABLE_ROW_07, r07);
		values.put(TABLE_ROW_08, r08);
		values.put(TABLE_ROW_09, r09);
		values.put(TABLE_ROW_10, r10);
		
		// ask the database object to insert the new data
		try
		{
			long rowID = db.insert(TABLE_NAME, null, values);
			Log.d(getClass().getSimpleName(), "SQLiteDatabase addRow - Row ID: " + Integer.toString((int)rowID));
		}
		catch(Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}
	
	public void deleteRow(long rowID)
	{
		Log.d(getClass().getSimpleName(), "SQLiteDatabase delete - Row ID: " + Integer.toString((int)rowID));
		
		// ask the database manager to delete the row of given id
		try 
		{
			db.delete(TABLE_NAME, TABLE_ROW_ID + "=" + rowID, null);
		}
		catch (Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}
	
	public void updateRow(long rowID, String r01, String r02, String r03, String r04, int r05, String r06, String r07, String r08, int r09, String r10)
	{
		Log.d(getClass().getSimpleName(), "SQLiteDatabase update - Row ID: " + Integer.toString((int)rowID));
		
		// this is a key value pair holder used by android's SQLite functions
		ContentValues values = new ContentValues();
		values.put(TABLE_ROW_01, r01);
		values.put(TABLE_ROW_02, r02);
		values.put(TABLE_ROW_03, r03);
		values.put(TABLE_ROW_04, r04);
		values.put(TABLE_ROW_05, r05);
		values.put(TABLE_ROW_06, r06);
		values.put(TABLE_ROW_07, r07);
		values.put(TABLE_ROW_08, r08);
		values.put(TABLE_ROW_09, r09);
		values.put(TABLE_ROW_10, r10);
 
		// ask the database object to update the database row of given rowID
		try 
		{
			db.update(TABLE_NAME, values, TABLE_ROW_ID + "=" + rowID, null);
		}
		catch (Exception e)
		{
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
	}
	
	public ArrayList<Object> getRowAsArray(long rowID)
	{
		ArrayList<Object> rowArray = new ArrayList<Object>();
		Cursor cursor;
 
		try
		{
			cursor = db.query
			(
					TABLE_NAME,
					new String[] { TABLE_ROW_ID, TABLE_ROW_01, TABLE_ROW_02, TABLE_ROW_03, TABLE_ROW_04, TABLE_ROW_05, TABLE_ROW_06, TABLE_ROW_07, TABLE_ROW_08, TABLE_ROW_09, TABLE_ROW_10 },
					TABLE_ROW_ID + "=" + rowID,
					null, null, null, null, null
			);
 
			// move the pointer to position zero in the cursor.
			cursor.moveToFirst();
 
			// if there is data available after the cursor's pointer, add
			// it to the ArrayList that will be returned by the method.
			if (!cursor.isAfterLast())
			{
				do
				{
					rowArray.add(cursor.getLong(0));
					rowArray.add(cursor.getString(1));
					rowArray.add(cursor.getString(2));
					rowArray.add(cursor.getString(3));
					rowArray.add(cursor.getString(4));
					rowArray.add(cursor.getLong(5));
					rowArray.add(cursor.getString(6));
					rowArray.add(cursor.getString(7));
					rowArray.add(cursor.getString(8));
					rowArray.add(cursor.getLong(9));
					rowArray.add(cursor.getString(10));
				}
				while (cursor.moveToNext());
			}
 
			// let java know that you are through with the cursor.
			cursor.close();
		}
		catch (SQLException e) 
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
 
		// return the ArrayList containing the given row from the database.
		return rowArray;
	}
	
	public ArrayList<ArrayList<Object>> getAllRowsAsArrays()
	{
		// create an ArrayList that will hold all of the data collected from
		// the database.
		ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();
 
		// this is a database call that creates a "cursor" object.
		// the cursor object store the information collected from the
		// database and is used to iterate through the data.
		Cursor cursor;
 
		try
		{
			// ask the database object to create the cursor.
			cursor = db.query(
					TABLE_NAME,
					new String[]{TABLE_ROW_ID, TABLE_ROW_01, TABLE_ROW_02, TABLE_ROW_03, TABLE_ROW_04, TABLE_ROW_05, TABLE_ROW_06, TABLE_ROW_07, TABLE_ROW_08, TABLE_ROW_09, TABLE_ROW_10},
					null, null, null, null, null
			);
 
			// move the cursor's pointer to position zero.
			cursor.moveToFirst();
 
			// if there is data after the current cursor position, add it
			// to the ArrayList.
			if (!cursor.isAfterLast())
			{
				do
				{
					ArrayList<Object> dataList = new ArrayList<Object>();
 
					dataList.add(cursor.getLong(0));
					dataList.add(cursor.getString(1));
					dataList.add(cursor.getString(2));
					dataList.add(cursor.getString(3));
					dataList.add(cursor.getString(4));
					dataList.add(cursor.getLong(5));
					dataList.add(cursor.getString(6));
					dataList.add(cursor.getString(7));
					dataList.add(cursor.getString(8));
					dataList.add(cursor.getLong(9));
					dataList.add(cursor.getString(10));
 
					dataArrays.add(dataList);
				}
				// move the cursor's pointer up one position.
				while (cursor.moveToNext());
			}
		}
		catch (SQLException e)
		{
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
 
		// return the ArrayList that holds the data collected from
		// the database.
		return dataArrays;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper
	{
		public CustomSQLiteOpenHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION);
		}
 
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			Log.d(getClass().getSimpleName(), "onCreate(SQLiteDatabase db)");
			
			// This string is used to create the database.  It should
			// be changed to suit your needs.
			String newTableQueryString = "create table " +
										TABLE_NAME +
										" (" +
										TABLE_ROW_ID + " integer primary key autoincrement not null," +
										TABLE_ROW_01 + " text," +
										TABLE_ROW_02 + " text," +
										TABLE_ROW_03 + " text," +
										TABLE_ROW_04 + " text," +
										TABLE_ROW_05 + " integer," + 
										TABLE_ROW_06 + " text," +
										TABLE_ROW_07 + " text," +
										TABLE_ROW_08 + " text," +
										TABLE_ROW_09 + " integer," +
										TABLE_ROW_10 + " text" +
										");";
			
			// execute the query string to the database.
			db.execSQL(newTableQueryString);
		}
  
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			// only used when updating database
		}
	}
}
