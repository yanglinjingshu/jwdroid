package com.jwdroid;

import com.jwdroid.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TerritoryList extends Activity {
	
	private static final String TAG = "JWDroidTerritoryListActivity";
	
	private TerritoryDbOpenHelper mDbOpenHelper = new TerritoryDbOpenHelper(this);
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.territory_list);
        
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.territory_list, menu);
		return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {		
	    switch (item.getItemId()) {
	    	case R.id.menu_territory_add:
	    		Intent intent = new Intent(this,TerritoryAdd.class);	    		
	    		startActivity(intent);
	    		//return true;
	    }
	    
	    return false;
	}
    
    @Override
    protected void onResume() {    	
    	super.onResume();
    	
		ListView listView = (ListView)findViewById(R.id.territory_list);
	    
	    SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery("SELECT id _id,name,desc FROM territory", new String[] {});
	    
	    SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this, 
	    	R.layout.territory_list_item, 
	    	cursor, 
	    	new String[] { "name", "desc" }, 
	    	new int[] { R.id.territory_list_item_name, R.id.territory_list_item_desc });
	    listView.setAdapter(listAdapter);
    }
}