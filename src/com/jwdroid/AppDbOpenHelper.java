package com.jwdroid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AppDbOpenHelper extends SQLiteOpenHelper {	
	private static final int DATABASE_VERSION = 41;
	private static final String DATABASE_NAME = "jwdroid";
	private static final String TAG = "JWTerritoryDbOpenHelper";

	AppDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.w(TAG, "onCreate");
        onUpgrade(db, 0, DATABASE_VERSION);
    }
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading from " + oldVersion + " to " + newVersion);
		
		if(oldVersion < 20)	{
			db.execSQL("DROP TABLE IF EXISTS territory");
			db.execSQL("CREATE TABLE territory (name TEXT NOT NULL, notes TEXT, created TEXT NOT NULL)");			
		}		
		if(oldVersion < 22) {
			db.execSQL("CREATE TABLE `door` (territory_id INTEGER NOT NULL, group_id INTEGER NOT NULL, col INTEGER NOT NULL, row INTEGER NOT NULL, name TEXT NOT NULL, color1 INTEGER NOT NULL, color2 INTEGER NOT NULL, visits_num INTEGER NOT NULL, last_date INTEGER , last_person_name TEXT , last_desc TEXT )");
			db.execSQL("CREATE INDEX door_territory_id ON door (territory_id,group_id)");
		}
		
		if(oldVersion < 23) {
			db.execSQL("CREATE TABLE `person` (door_id INTEGER NOT NULL, name TEXT NOT NULL )");
			db.execSQL("CREATE TABLE `visit` (territory_id INTEGER NOT NULL, door_id INTEGER NOT NULL, person_id INTEGER NOT NULL, `date` INTEGER NOT NULL, desc TEXT NOT NULL, type INTEGER NOT NULL DEFAULT 0, calc_auto INTEGER NOT NULL DEFAULT 1, brochures INTEGER NOT NULL DEFAULT 0, books INTEGER NOT NULL DEFAULT 0, magazines INTEGER NOT NULL DEFAULT 0)");
			db.execSQL("CREATE INDEX person_door_id ON person (door_id)");
			db.execSQL("CREATE INDEX visit_door_id ON visit (door_id,person_id,date DESC)");
			db.execSQL("CREATE INDEX visit_territory_id ON visit (territory_id)");
			db.execSQL("CREATE INDEX visit_date ON visit (date)");
		}
		
		if(oldVersion < 24) {
			db.execSQL("ALTER TABLE `person` ADD reject INTEGER NOT NULL DEFAULT 0");
		}
		
		if(oldVersion < 25) {
			db.execSQL("ALTER TABLE `door` ADD last_person_reject INTEGER");
		}
		if(oldVersion < 32) {			
			db.execSQL("ALTER TABLE `door` ADD order_num INTEGER NOT NULL DEFAULT 0");		
			db.execSQL("DROP INDEX IF EXISTS door_territory_id");
			db.execSQL("CREATE INDEX door_territory_id ON door (territory_id,group_id,order_num)");
		}
		if(oldVersion < 37) {
			long lastTerritoryId = 0;
			int lastGroupId = 0;
			int orderNum = 0;
			Cursor rs = db.rawQuery("SELECT ROWID,territory_id,group_id FROM door ORDER BY territory_id ASC, group_id ASC, order_num ASC", new String[] {});
			while(rs.moveToNext()) {
				long id = rs.getLong(0);
				long territoryId = rs.getLong(1);
				int groupId = rs.getInt(2);
				if(territoryId != lastTerritoryId || groupId != lastGroupId)
					orderNum = 0;
				lastTerritoryId = territoryId;
				lastGroupId = groupId;
				orderNum++;
				
				db.execSQL("UPDATE door SET order_num=? WHERE ROWID=?", new Object[] {orderNum, id});
				Log.i(TAG, String.format("t=%d g=%d o: %d", territoryId, groupId, orderNum));
			}
		}
		if(oldVersion < 38) {
			db.execSQL("ALTER TABLE `territory` ADD started INTEGER");
			db.execSQL("ALTER TABLE `territory` ADD finished INTEGER");
			db.execSQL("ALTER TABLE `territory` ADD modified INTEGER");			
		}
		if(oldVersion < 39) {
			db.execSQL("UPDATE territory SET modified=(SELECT date FROM visit WHERE territory_id=territory.ROWID ORDER BY date DESC LIMIT 1)");
			db.execSQL("UPDATE territory SET started=created");
		}		
		if(oldVersion < 40) {
			db.execSQL("ALTER TABLE `door` ADD manual_color INTEGER NOT NULL DEFAULT 0");
		}

		if(oldVersion < 41) {
			db.execSQL("CREATE TABLE `session` (date INTEGER NOT NULL, desc TEXT, minutes INTEGER NOT NULL, books INTEGER NOT NULL, brochures INTEGER NOT NULL, returns TEXT NOT NULL, magazines INTEGER NOT NULL)");
			db.execSQL("CREATE INDEX session_date ON session (date)");
		}
	}
	
	
	static public void copyDataBase() throws IOException{

      
        OutputStream databaseOutputStream = new FileOutputStream("/mnt/sdcard/jwdroid");
        InputStream databaseInputStream = new FileInputStream("/data/data/com.jwdroid/databases/jwdroid");

        byte[] buffer = new byte[1];
        int length;
        while ( (length = databaseInputStream.read(buffer)) > 0 ) {
                databaseOutputStream.write(buffer);
                Log.w("Bytes: ", ((Integer)length).toString());
                Log.w("value", buffer.toString());
        }

        databaseOutputStream.flush();
        databaseOutputStream.close();
        databaseInputStream.close();
 } 
	

}
