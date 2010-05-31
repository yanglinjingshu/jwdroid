package com.jwdroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TerritoryDbOpenHelper extends SQLiteOpenHelper {	
	private static final int DATABASE_VERSION = 6;
	private static final String DATABASE_NAME = "jwdroid";
	private static final String TAG = "JWTerritoryDbOpenHelper";

	TerritoryDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE territory (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT, notes TEXT)");
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading from " + oldVersion + " to " + newVersion);
		if(oldVersion < 6)
		{
			db.execSQL("DROP TABLE territory");
			db.execSQL("CREATE TABLE territory (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT, desc TEXT, notes TEXT)");
			
		}
		
	}

}
