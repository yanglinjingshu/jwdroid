package com.jwdroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.OutputStream;
import java.security.PermissionCollection;
import java.security.acl.Permission;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import net.londatiga.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;


public class TerritoryList extends FragmentActivity implements LoaderCallbacks<Cursor> {
	
	private static final String TAG = "JWDroidTerritoryListActivity";
	
	private static final int MENU_DELETE = Menu.FIRST + 1;
	
	private static final int DIALOG_DELETE = 1;
	private static final int DIALOG_ADD = 2;
	private static final int DIALOG_CHANGE_NAME = 3;
	
	private TerritoryListAdapter mListAdapter;	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private long mTerritoryForDelete;
	private ListView mListView;
	
	private Long mDialogItemId;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.territory_list);
              
        // Set up territory list
        
        mListView = (ListView)findViewById(R.id.territory_list);
        registerForContextMenu(mListView);
	    
	    
	  
	    mListAdapter = new TerritoryListAdapter(this, new ArrayList<TerritoryItem>());
	    mListAdapter.registerDataSetObserver(new DataSetObserver() {
	    	public void onChanged() {
	    		findViewById(R.id.territory_list_empty).setVisibility( mListAdapter.getCount() == 0 ? View.VISIBLE : View.GONE );
	    	}
		});
	    
	    mListView.setAdapter(mListAdapter);   
	    
	    final QuickAction listActions 	= new QuickAction(this);
		listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_territory_change_name), getResources().getDrawable(R.drawable.ac_pencil)));		
		listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_territory_info), getResources().getDrawable(R.drawable.ac_info)));
		listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_territory_delete), getResources().getDrawable(R.drawable.ac_trash)));
		listActions.animateTrack(false);
		listActions.setAnimStyle(QuickAction.ANIM_MOVE_FROM_RIGHT);			
    	
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int pos, long id) {
				listActions.show(v, id);
				return true;
			}
		}); 	
		
		listActions.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {				
			@Override
			public void onItemClick(int pos) {
				Bundle args;
				SQLiteDatabase db;
				Cursor rs;
				switch(pos) {
				case 0:	// Название						
					mDialogItemId = listActions.getId();
			  		showDialog(DIALOG_CHANGE_NAME);
					break;
				case 1: // Информация
					Intent intent = new Intent(TerritoryList.this, TerritoryInfo.class);
					intent.putExtra("territory", listActions.getId());
					startActivity(intent);
					break;
				case 2:	// Удалить						
					mTerritoryForDelete = listActions.getId();
			  		showDialog(DIALOG_DELETE);
					break;
				}
			}
		});
	    
	    
	    mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
	    	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
	    		Intent intent = new Intent(TerritoryList.this, Territory.class);
	    		intent.putExtra("territory", id);
	    		startActivity(intent);
	    	}
		});
	    
	    ((Button)findViewById(R.id.title_btn_add)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_ADD);				
			}
		});
	    
	    getSupportLoaderManager().initLoader(0, null, this);
	    
	    
	    	    
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
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		switch(v.getId()) {
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  
		switch(item.getItemId()) {
		}
	  
	  return true;
	  
	}
    
    @Override
    protected void onResume() {    	
    	super.onResume();
    	TerritoryList.this.getSupportLoaderManager().getLoader(0).forceLoad();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {    	
    	Dialog dialog=null;
    	LayoutInflater factory = LayoutInflater.from(this);
    	SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        final View dlgEditLayout = factory.inflate(R.layout.dlg_edit, null);
    	switch(id) {
    	case DIALOG_DELETE:
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setCancelable(true)
    			   .setMessage(R.string.dlg_territory_delete_msg)
    			   .setTitle(R.string.dlg_territory_delete_title)
    			   .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
				  		db.execSQL("DELETE FROM territory WHERE rowid=?", new Long[] { mTerritoryForDelete });
				  		db.execSQL("DELETE FROM person WHERE (SELECT territory_id FROM door WHERE door.ROWID=person.door_id LIMIT 1)=?", new Long[] { mTerritoryForDelete });
				  		db.execSQL("DELETE FROM door WHERE territory_id=?", new Long[] { mTerritoryForDelete });				  		
				  		db.execSQL("DELETE FROM visit WHERE territory_id=?", new Long[] { mTerritoryForDelete });
				  		Toast.makeText(TerritoryList.this, R.string.msg_territory_deleted, Toast.LENGTH_SHORT).show();				  		
				  		TerritoryList.this.getSupportLoaderManager().getLoader(0).forceLoad();
					}
				})
    			   .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
    				   public void onClick(DialogInterface dialog, int which) {
    					   dialog.cancel();
					}
				});
    		dialog = builder.create();
    		break;
    		
    	case DIALOG_ADD:
    		
            ((TextView)dlgEditLayout.findViewById(R.id.lbl_dlgedit_note)).setText(R.string.dlg_territory_add_note);
            ((TextView)dlgEditLayout.findViewById(R.id.lbl_dlgedit_note)).setVisibility(View.VISIBLE);
            
    		dialog = new AlertDialog.Builder(this)
    					.setTitle(R.string.dlg_territory_add_label)
    					.setView(dlgEditLayout)
    					.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {

								int error = 0;
								
								Editable editable = ((EditText)dlgEditLayout.findViewById(R.id.edit_dlgedit_text)).getText();
									
								if(editable.length() == 0)
									error = R.string.err_empty_name;
								
								if(error > 0)
								{
									Toast.makeText(dlgEditLayout.getContext(), error, Toast.LENGTH_SHORT).show();
									return;
								}
								
								
								SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
								db.execSQL("INSERT INTO territory (name,created,started) VALUES(?,datetime('now'),datetime('now'))", new String[] { editable.toString() });
									
								Toast.makeText(dlgEditLayout.getContext(), R.string.msg_territory_added, Toast.LENGTH_SHORT).show();
								
								getSupportLoaderManager().getLoader(0).forceLoad();
														
						}
					})
					.setNegativeButton(R.string.btn_cancel, null).create();
					
    		break;
    		
    	case DIALOG_CHANGE_NAME:
    		    		
    		((TextView)dlgEditLayout.findViewById(R.id.lbl_dlgedit_note)).setText(R.string.dlg_territory_add_note);
            ((TextView)dlgEditLayout.findViewById(R.id.lbl_dlgedit_note)).setVisibility(View.VISIBLE);
                        
    		dialog = new AlertDialog.Builder(this)
    					.setTitle(R.string.dlg_territory_add_label)
    					.setView(dlgEditLayout)
    					.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {

								int error = 0;
								
								Editable editable = ((EditText)dlgEditLayout.findViewById(R.id.edit_dlgedit_text)).getText();
									
								if(editable.length() == 0)
									error = R.string.err_empty_name;
								
								if(error > 0)
								{
									Toast.makeText(dlgEditLayout.getContext(), error, Toast.LENGTH_SHORT).show();
									return;
								}
								
								
								SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
								db.execSQL("UPDATE territory SET name=? WHERE ROWID=?", new Object[] { editable.toString(), mDialogItemId });
								
								mListAdapter.getItemById(mDialogItemId).name = editable.toString();
								mListAdapter.notifyDataSetChanged();
														
						}
					})
					.setNegativeButton(R.string.btn_cancel, null).create();
					
    		break;
    	}
    	
    	return dialog;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	super.onPrepareDialog(id, dialog);
    	
    	SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
    	
    	switch(id) {
    	case DIALOG_CHANGE_NAME:
    		String name = Util.dbFetchString(db, "SELECT name FROM territory WHERE ROWID=?", new String[] {mDialogItemId.toString()});
            ((EditText)dialog.findViewById(R.id.edit_dlgedit_text)).setText(name);
            break;
    	}
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		Loader loader = null;
		if(id == 0)
			loader = new TerritoryListLoader(this, mDbOpenHelper);
		else if(id == 1)
			loader = new TerritoryDoorsLoader(this, mDbOpenHelper);		
		
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(loader.getId() == 0) {
			ArrayList<TerritoryItem> data = new ArrayList<TerritoryItem>();
			while(cursor.moveToNext()) {
				TerritoryItem item = new TerritoryItem();
				item.id = cursor.getLong(0);
				item.name = cursor.getString(1);
				item.started = new Time();				
				item.started.set(cursor.getLong(2)*1000);

				if(!cursor.isNull(3)) {
					item.finished = new Time();
					item.finished.set(cursor.getLong(3)*1000);
				}
				item.doorColors = new ArrayList<Integer>();
				
				data.add(item);
			}
			mListAdapter.swapData(data);
			getSupportLoaderManager().restartLoader(1, null, this);
		}
		else if(loader.getId() == 1) {
			HashMap<Long,ArrayList<Integer>> territoryColors = new HashMap<Long,ArrayList<Integer>>();
			cursor.moveToFirst();			
			while(cursor.moveToNext()) {
				Long territoryId = cursor.getLong(0);
				Integer color = cursor.getInt(1);
				if(!territoryColors.containsKey(territoryId))
					territoryColors.put(territoryId, new ArrayList<Integer>());
				territoryColors.get(territoryId).add(color);
			}					
			for(Long territoryId : territoryColors.keySet()) {
				TerritoryItem item = mListAdapter.getItemById(territoryId);
				if(item != null)
					item.doorColors = territoryColors.get(territoryId);
			}
			mListAdapter.notifyDataSetChanged();
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mListAdapter.swapData(new ArrayList<TerritoryItem>());		
	}
	
	private class TerritoryItem {
		long id;
		String name;
		Time started,finished=null;
		ArrayList<Integer> doorColors;
	}
	
	
	static public class TerritoryListLoader extends AsyncLoader<Cursor>  {
		
		private AppDbOpenHelper mDbOpenHelper;

		public TerritoryListLoader(Context context, AppDbOpenHelper db) {
			super(context);			
			mDbOpenHelper = db;
		}		

		@Override
		public Cursor loadInBackground() {
			SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
			Cursor rs = db.rawQuery("SELECT rowid _id,name,strftime('%s',started), strftime('%s', finished) FROM territory ORDER BY started DESC", new String[] {});
			return rs;
		}
	}
	
	static public class TerritoryDoorsLoader extends AsyncLoader<Cursor>  {
		
		private AppDbOpenHelper mDbOpenHelper;

		public TerritoryDoorsLoader(Context context, AppDbOpenHelper db) {
			super(context);			
			mDbOpenHelper = db;
		}		

		@Override
		public Cursor loadInBackground() {
			SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
			Cursor rs = db.rawQuery("SELECT territory_id,color1 FROM door ORDER BY territory_id,group_id,order_num ASC", new String[] {});
			
			return rs;
		}
	}
	
	
	
	private static class TerritoryListAdapter extends SimpleArrayAdapter<TerritoryItem> {		
       
        public TerritoryListAdapter(Context context, ArrayList<TerritoryItem> items) {
           super(context, items);            
        }

        public long getItemId(int position) {
            return mItems.get(position).id;
        }    

        public View getView(int position, View convertView, ViewGroup parent) {

        	ViewHolder holder;
        	TerritoryItem item = mItems.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.territory_list_item, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.territory_list_item_name);
                holder.started = (TextView) convertView.findViewById(R.id.territory_list_item_started);
                holder.finished = (TextView) convertView.findViewById(R.id.territory_list_item_finished);
                holder.histogram = (HistogramView) convertView.findViewById(R.id.territory_list_item_histogram);                
                                
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.name.setText(item.name);
            holder.started.setText( DateFormat.getDateInstance(DateFormat.SHORT).format( new Date(item.started.toMillis(true)) ) );
            if(item.finished == null)
            	holder.finished.setVisibility(View.GONE);
            else {
            	holder.finished.setVisibility(View.VISIBLE);
            	holder.finished.setText(DateFormat.getDateInstance(DateFormat.SHORT).format( new Date(item.finished.toMillis(true)) ));
            }
        	holder.histogram.setColors(item.doorColors);
        	        	
            return convertView;
        }

        static class ViewHolder {
            TextView name,started,finished;
            HistogramView histogram;
        }
        
    }
    
    
}