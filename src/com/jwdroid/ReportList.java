package com.jwdroid;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

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
	
	private ReportListAdapter mListAdapter;	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private ListView mListView;
	 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_list);
        
        
        mListView = (ListView)findViewById(R.id.report_list);
            	    
	  
	    mListAdapter = new ReportListAdapter(this,null);
	    
	    mListView.setAdapter(mListAdapter);   
	    
	    mListAdapter.registerDataSetObserver(new DataSetObserver() {
	    	public void onChanged() {
	    		findViewById(R.id.report_list_empty).setVisibility( mListAdapter.getCount() == 0 ? View.VISIBLE : View.GONE );
	    	}
		});
	    
	    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view,
    				int position, long id) {
    			
    			ReportItem item = (ReportItem)mListAdapter.getItem(position);
    			
    			Intent intent = new Intent(ReportList.this, Report.class);
	    		intent.putExtra("month", String.format("%04d%02d",item.year,item.month+1));
	    		startActivityForResult(intent,1);	
	    				    			
    		}
		});
	    
	    updateContent();
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
	    	startActivityForResult(intent,1);
	    	break;
	    }
	    
	    return false;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	updateContent();
    }
    
    private void updateContent() {
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
	    
	    ArrayList<ReportItem> items = new ArrayList<ReportItem>();
	    
	    minDate.monthDay = 1;
	    while(now.toMillis(true) >= minDate.toMillis(true)) {
	    	ReportItem i = new ReportItem();
	    	i.month = now.month;
	    	i.year = now.year;
	    	i.minutes = Util.dbFetchInt(db, "SELECT SUM(minutes) FROM session WHERE strftime('%Y%m',date)=?", new String[]{ String.format("%04d%02d", now.year, now.month+1) });
	    	items.add(i);
	    	now.month--;	    	
	    	now.normalize(true);
	    }
	    
	    mListAdapter.swapData(items);
    }
    
    
    private class ReportItem {		
		Integer month,year,minutes;
	}
	
	
	
	private static class ReportListAdapter extends SimpleArrayAdapter<ReportItem> {		
	       
        public ReportListAdapter(Context context, ArrayList<ReportItem> items) {
           super(context, items);            
        }

        public long getItemId(int position) {
        	return 0;
        }    

        public View getView(int position, View convertView, ViewGroup parent) {

        	ViewHolder holder;
        	ReportItem item = mItems.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.report_list_item, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.report_item_name);
                holder.minutes = (TextView) convertView.findViewById(R.id.report_item_minutes);
                                
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.name.setText(mContext.getResources().getStringArray(R.array.months)[item.month]+" "+item.year);
            holder.minutes.setText( String.format("%d:%02d", item.minutes/60, item.minutes%60) );
            	
            return convertView;
        }

        static class ViewHolder {        	
            TextView name,minutes;
        }
        
    }
	
	

}
