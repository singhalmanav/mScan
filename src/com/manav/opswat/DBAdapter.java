/*
 * @author manavsinghal
 * 
 *	DBAdapter.java - This class defines the SQLite database table structure and methods to access the database like Insert/Delete etc.
 */

package com.manav.opswat;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBAdapter {

	/* Define Column Names */
	public static final String KEY_ID = "_id";
	public static final String KEY_DATA = "data_id";
	public static final String KEY_FILENAME = "filename";
	public static final String KEY_TIMESTAMP = "created_at";

	private static final String TAG = "DBAdapter";

	/* Define Database Name */
	private static final String DATABASE_NAME = "metadb";
	/* Define Table Name */
	private static final String DATABASE_TABLE = "record";
	/* Define Database Version */
	private static final int DATABASE_VERSION = 1;

	/* Create table SQL string */
	private static final String DATABASE_CREATE =
			"CREATE TABLE IF NOT EXISTS record (_id integer primary key, data_id text not null, filename text not null, created_at long not null);";

	private final Context context; 
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) 
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper 
	{
		DatabaseHelper(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		public void onCreate(SQLiteDatabase db) 
		{
			db.execSQL(DATABASE_CREATE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion 
					+ " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS login");
			onCreate(db);
		}
	}    

	//---opens the database---
	public DBAdapter open() throws SQLException 
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}

	//---closes the database---    
	public void close() 
	{
		DBHelper.close();
	}

	//---insert a title into the database---
	public long insertTitle(String data, String fileName) 
	{
		Log.e("DB",data+" "+fileName);

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DATA, data);
		initialValues.put(KEY_FILENAME, fileName);
		initialValues.put(KEY_TIMESTAMP, java.lang.System.currentTimeMillis());
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	//---deletes a particular title---
	public boolean deleteTitle(String rowId) throws Exception 
	{
		return db.delete(DATABASE_TABLE, KEY_DATA + "='" + rowId +"'", null) > 0;
	}

	// --- retrieves all titles with oldest timestamp ---
	public Cursor getAllTitlesMinimum() 
	{
		return db.query(DATABASE_TABLE, new String[] {
				"min("+KEY_TIMESTAMP+")"}, 
				null, 
				null, 
				null, 
				null, 
				null);   
	}

	//---retrieves all the titles---
	public Cursor getAllTitles() 
	{
		return db.query(DATABASE_TABLE, new String[] {
				KEY_ID, 
				KEY_DATA,
				KEY_FILENAME,
				KEY_TIMESTAMP}, 
				null, 
				null, 
				null, 
				null, 
				KEY_TIMESTAMP+" DESC");

	}

	//---retrieves a particular title---
	public Cursor getTitle(int rowId) throws SQLException 
	{
		Cursor mCursor =
				db.query(true, DATABASE_TABLE, new String[] {
						KEY_ID,
						KEY_DATA,
						KEY_FILENAME,
						KEY_TIMESTAMP
				}, 
				KEY_ID + "='" + rowId +"'", 
				null,
				null, 
				null, 
				null, 
				null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

}
