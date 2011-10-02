package com.jwdroid;

import java.util.ArrayList;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import net.londatiga.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReportList extends Activity {

	static private final int DIALOG_DELETE = 1;
	
	private ArrayAdapter mListAdapter;	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private ListView mListView;
	 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_list);
        
        
        mListView = (ListView)findViewById(R.id.report_list);
            	    
	  
	    mListAdapter = new ArrayAdapter(this, R.layout.report_list_item);
	    
	    SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
	    Long firstDateVisits = Util.dbFetchLong(db, "SELECT MIN(strftime('%s',date)) FROM visit", new String[] {});
	    Long firstDateSessions = Util.dbFetchLong(db, "SELECT MIN(strftime('%s',date)) FROM session", new String[] {});
	    Time minDate = new Time();
	    minDate.setToNow();
	    if(firstDateVisits != null && firstDateVisits > 0)
	    	minDate.set(firstDateVisits*1000);
	    if(firstDateSessions != null && firstDateSessions > 0 && firstDateSessions*1000 < minDate.toMillis(true))
	    	minDate.set(firstDateSessions*1000);
	    
	    Time now = new Time();
	    now.setToNow();
	    
	    minDate.monthDay = 1;
	    while(now.toMillis(true) >= minDate.toMillis(true)) {
	    	
	    	mListAdapter.add(new SimpleArrayItem(now.year*100 + now.month+1, Report.MONTHS[now.month]+" "+now.year));
	    	now.month--;	    	
	    	now.normalize(true);
	    }
	    
	    mListAdapter.registerDataSetObserver(new DataSetObserver() {
	    	public void onChanged() {
	    		findViewById(R.id.report_list_empty).setVisibility( mListAdapter.getCount() == 0 ? View.VISIBLE : View.GONE );
	    	}
		});
	    
	    mListView.setAdapter(mListAdapter);   
	    
	    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view,
    				int position, long id) {
    			
    			SimpleArrayItem item = (SimpleArrayItem)mListAdapter.getItem(position);
    			
    			Intent intent = new Intent(ReportList.this, Report.class);
	    		intent.putExtra("month", String.valueOf(item.id));
	    		startActivityForResult(intent,1);	
	    				    			
    		}
		});
	    
	    
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {    	
	    switch (item.getItemId()) {
	    case R.id.menu_preferences:
	    	Intent intent = new Intent(this, Preferences.class);
	    	startActivity(intent);
	    	break;
	    	
	    case R.id.menu_feedback:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chivchalov@gmail.com"});
			intent.putExtra(Intent.EXTRA_SUBJECT,"JW Droid");
			startActivity(Intent.createChooser(intent, null));
			break;
			
	    case R.id.menu_help:
	    	intent = new Intent(this, Help.class);
	    	startActivity(intent);
	    	break;
	    }
	    
	    return false;
	}


	
	

}
