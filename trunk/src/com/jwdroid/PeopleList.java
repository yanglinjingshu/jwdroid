package com.jwdroid;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.londatiga.android.R;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PeopleList extends FragmentActivity implements LoaderCallbacks<Cursor> {
	
	private PeopleListAdapter mListAdapter;	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private ListView mListView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_list);
              
        // Set up territory list
        
        mListView = (ListView)findViewById(R.id.people_list);
            
	    
	  
	    mListAdapter = new PeopleListAdapter(this, new ArrayList<PeopleItem>());
	    mListAdapter.registerDataSetObserver(new DataSetObserver() {
	    	public void onChanged() {
	    		findViewById(R.id.people_list_empty).setVisibility( mListAdapter.getCount() == 0 ? View.VISIBLE : View.GONE );
	    	}
		});
	    
	    mListView.setAdapter(mListAdapter);   
	    
	    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view,
    				int position, long id) {
    			
    			PeopleItem item = mListAdapter.getItem(position);
    			
    			Intent intent = new Intent(PeopleList.this, Door.class);
	    		intent.putExtra("territory", item.territoryId);
	    		intent.putExtra("door", item.doorId);
	    		intent.putExtra("person", item.id);
	    		startActivityForResult(intent,1);	
	    				    			
    		}
		});   
	    
	    getSupportLoaderManager().restartLoader(0, null, this);
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

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		PeopleListLoader loader = new PeopleListLoader(this, mDbOpenHelper);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		ArrayList<PeopleItem> data = new ArrayList<PeopleItem>();
		while(cursor.moveToNext()) {
			PeopleItem item = new PeopleItem();
			item.id = cursor.getLong(0);
			item.doorId = cursor.getLong(1);
			item.doorName = cursor.getString(2);
			item.territoryId = cursor.getLong(3);
			item.territoryName = cursor.getString(4);
			item.personName = cursor.getString(5);			
			item.visitDate = new Time();				
			item.visitDate.set(cursor.getLong(6)*1000);
			item.visitType = cursor.getInt(7);
			item.visitDesc = cursor.getString(8);
			item.doorColor1 = cursor.getInt(9);
			item.doorColor2 = cursor.getInt(10);
			item.visitsNum = cursor.getInt(11);
			
			data.add(item);
		}
		mListAdapter.swapData(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mListAdapter.swapData(new ArrayList<PeopleItem>());	
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		getSupportLoaderManager().getLoader(0).forceLoad();
	}
	
	
	private class PeopleItem {
		Long id, doorId, territoryId;		
		String personName, doorName, territoryName, visitDesc;
		Time visitDate;
		Integer visitsNum,visitType, doorColor1, doorColor2;		
	}
	
	
	static public class PeopleListLoader extends AsyncLoader<Cursor>  {
		
		private AppDbOpenHelper mDbOpenHelper;

		public PeopleListLoader(Context context, AppDbOpenHelper db) {
			super(context);			
			mDbOpenHelper = db;
		}		

		@Override
		public Cursor loadInBackground() {
			SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
			Cursor rs = db.rawQuery("SELECT person.rowid _id, " +
									"		person.door_id, " +
									"		door.name, " +
									"		door.territory_id, " +
									"		territory.name, " +
									"		person.name, " +
									"		strftime('%s',visit.date), " +
									"		visit.type, " +
									"		visit.desc, " +
									"		door.color1, " +
									"		door.color2, " +
									"		(SELECT COUNT(*) FROM visit WHERE person_id=person.ROWID AND door_id=person.door_id) " +
									"FROM person " +
									"LEFT JOIN door ON person.door_id=door.ROWID " +
									"LEFT JOIN visit ON visit.ROWID IN (SELECT ROWID FROM visit WHERE person_id=person.ROWID AND door_id=person.door_id AND type!=? ORDER BY date DESC LIMIT 1) " +
									"LEFT JOIN territory ON door.territory_id=territory.ROWID " +
									"WHERE reject=0 AND visit.ROWID IS NOT NULL " +
									"ORDER BY visit.date ASC", new String[] {String.valueOf(Visit.TYPE_NA)});
			return rs;
		}
	}
	
	
	private static class PeopleListAdapter extends SimpleArrayAdapter<PeopleItem> {		
	       
        public PeopleListAdapter(Context context, ArrayList<PeopleItem> items) {
           super(context, items);            
        }

        public long getItemId(int position) {
            return mItems.get(position).id;
        }    

        public View getView(int position, View convertView, ViewGroup parent) {

        	ViewHolder holder;
        	PeopleItem item = mItems.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.people_list_item, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.people_item_name);
                holder.door_name = (TextView) convertView.findViewById(R.id.people_item_door_name);
                holder.desc = (TextView) convertView.findViewById(R.id.people_item_desc);
                holder.visit_type = (ImageView) convertView.findViewById(R.id.people_item_visit_type_icon);
                holder.color1 = convertView.findViewById(R.id.people_item_color1);
                holder.color2 = convertView.findViewById(R.id.people_item_color2);
                                
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.name.setText(item.personName);
            
    		String snippet = "<s><b>"+DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(item.visitDate.toMillis(true))) + "</b></s>: "+item.visitDesc;    		
    		holder.desc.setText( Html.fromHtml(snippet) );  
    		
    		holder.visit_type.setImageResource(Visit.TYPE_ICONS[item.visitType]);
    		
    		holder.door_name.setText(Html.fromHtml(item.visitsNum+" "+Util.pluralForm(mContext, item.visitsNum, mContext.getResources().getStringArray(R.array.plural_visits)) + " &bull; "+item.territoryName+", "+item.doorName));
    		
    		holder.color1.setBackgroundColor( mContext.getResources().getColor(Door.COLORS[item.doorColor1]) );
        	holder.color2.setBackgroundColor( mContext.getResources().getColor(Door.COLORS[item.doorColor2]) );
        	
        	        	
            return convertView;
        }

        static class ViewHolder {
        	ImageView visit_type;
            TextView name,door_name,desc;
            View color1,color2;
        }
        
    }
	

}
