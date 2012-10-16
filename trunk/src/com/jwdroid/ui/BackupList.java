package com.jwdroid.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jwdroid.AlphanumComparator;
import com.jwdroid.SimpleArrayAdapter;

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
import android.os.Environment;
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

public class BackupList extends Activity {

	static private final int DIALOG_DELETE = 1;
	static private final int DIALOG_RESTORE = 2;
	static private final int DIALOG_CREATE = 3;
	
	private BackupListAdapter mListAdapter;	
	private ListView mListView;
	
	private File mRoot, mDir;
	
	private Long mDialogItemId;
	 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_list);
                
        mListView = (ListView)findViewById(R.id.backup_list);
        
        mRoot = Environment.getExternalStorageDirectory();
         
        mDir = new File(mRoot, "jwdroid");
        if(!mDir.exists())
        	mDir.mkdir();
            	    
	  
	    mListAdapter = new BackupListAdapter(this,null);
	    
	    mListView.setAdapter(mListAdapter);   
	    
	    mListAdapter.registerDataSetObserver(new DataSetObserver() {
	    	public void onChanged() {
	    		findViewById(R.id.backup_list_empty).setVisibility( mListAdapter.getCount() == 0 ? View.VISIBLE : View.GONE );
	    	}
		});
	    
	    
	    final QuickAction listActions 	= new QuickAction(this);
	    listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_backup_restore), getResources().getDrawable(R.drawable.ac_doc_export)));
		listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_backup_delete), getResources().getDrawable(R.drawable.ac_trash)));
		listActions.animateTrack(false);
		listActions.setAnimStyle(QuickAction.ANIM_MOVE_FROM_RIGHT);	
	    
	    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {	    	
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view,
    				int position, long id) {
    			listActions.show(view, id);	    			
    		}
		});
		
		listActions.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {				
			@Override
			public void onItemClick(int pos) {
				Bundle args;
				SQLiteDatabase db;
				Cursor rs;
				switch(pos) {
				case 0:	// Восстановить						
					mDialogItemId = listActions.getId();
			  		showDialog(DIALOG_RESTORE);
			  		break;
				case 1:	// Удалить
					mDialogItemId = listActions.getId();
			  		showDialog(DIALOG_DELETE);
			  		break;
				}
			}
		});
	    
	    
	    findViewById(R.id.title_btn_backup).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_CREATE);
			}
		});
	    
	    updateContent();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {    	
    	Dialog dialog=null;
    	LayoutInflater factory = LayoutInflater.from(this);
    	
    	switch(id) {
    	case DIALOG_CREATE:               
     		dialog = new AlertDialog.Builder(this)
     					.setMessage(R.string.msg_create_backup)
     					.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,	int which) {
								createBackup(BackupList.this);
								updateContent();								
							}
							
						})
     					.setNegativeButton(R.string.btn_no, null).create(); 
    		break;
    		
    	case DIALOG_RESTORE:
    		dialog = new AlertDialog.Builder(this) 	
    				.setCancelable(true)
    				.setMessage(R.string.msg_restore_backup)
    				.setPositiveButton(R.string.btn_yes, null)
    				.setNegativeButton(R.string.btn_no, null)
    				.create();
    		break; 
    		
    	case DIALOG_DELETE:
    		dialog = new AlertDialog.Builder(this) 	
    				.setCancelable(true)
    				.setMessage(R.string.msg_delete_backup)
    				.setPositiveButton(R.string.btn_yes, null)
    				.setNegativeButton(R.string.btn_no, null)
    				.create();
    		break;   
    	}
    	
    	return dialog;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {    	
    	super.onPrepareDialog(id, dialog);
    	
    	switch(id) {	  
    		case DIALOG_RESTORE: {	
    			AlertDialog alertDialog = (AlertDialog)dialog;
		    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							try {
								
								new File("/data/data/com.jwdroid/databases/jwdroid").delete();
			    				
			    	    		File root = Environment.getExternalStorageDirectory(); 
			    	    		
			    				Time time = new Time();
			    				time.setToNow();
			    				
			    				String filename = root.getAbsolutePath()+"/jwdroid/backup_"+mDialogItemId;
			    				
			    				OutputStream databaseOutputStream = new FileOutputStream("/data/data/com.jwdroid/databases/jwdroid");
			    		        InputStream databaseInputStream = new FileInputStream(filename);

			    		        byte[] buffer = new byte[1];
			    		        int length;
			    		        while ( (length = databaseInputStream.read(buffer)) > 0 ) {
			    		                databaseOutputStream.write(buffer);
			    		        }

			    		        databaseOutputStream.flush();
			    		        databaseOutputStream.close();
			    		        databaseInputStream.close();
			    		        
			    		        Toast.makeText(BackupList.this, getResources().getString(R.string.msg_restore_backup_success), Toast.LENGTH_LONG).show();
			    			    
			    			}
			    			catch (Exception e) {
			    				Toast.makeText(BackupList.this, getResources().getString(R.string.msg_restore_backup_failed), Toast.LENGTH_LONG).show();
			    			}
						}
					});
    			
	    		break;
    		}
	    	case DIALOG_DELETE: {		    	
		    	AlertDialog alertDialog = (AlertDialog)dialog;
		    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							File file = new File(mDir, "backup_"+mDialogItemId);
							file.delete();
							updateContent();
						}
					});
	    		break;
	    	}
    	}
    }
    
    
    
    private void updateContent() {
    	
	    FilenameFilter filter = new BackupFilenameFilter();
	    
	    ArrayList<String> names = new ArrayList<String>();
	    
	    File[] files = mDir.listFiles(filter);
	    for(File file : files) {
	    	names.add(file.getName());
	    }
	    
	    Collections.sort(names, new AlphanumComparator());
	    Collections.reverse(names);
	    
	    ArrayList<BackupItem> items = new ArrayList<BackupItem>();
	    
	    for(String filename : names) {
	    	
	    	BackupItem i = new BackupItem();
	    	
	    	File file = new File(mDir, filename);
	    	Matcher m = Pattern.compile("^backup_(\\d+)$").matcher(file.getName());
	    	m.find();
	    	i.time = new Time();
	    	i.time.set(Long.parseLong(m.group(1)));
	    	i.size = file.length();
	    	
	    	items.add(i);
	    }
	    
	    
	    
	    mListAdapter.swapData(items);
    }
    
    static public void createBackup(Context context) {
    	try {
			
    		File root = Environment.getExternalStorageDirectory(); 
    		
			Time time = new Time();
			time.setToNow();
			
			String filename = root.getAbsolutePath()+"/jwdroid/backup_"+time.toMillis(true);
			
			OutputStream databaseOutputStream = new FileOutputStream(filename);
	        InputStream databaseInputStream = new FileInputStream("/data/data/com.jwdroid/databases/jwdroid");

	        byte[] buffer = new byte[1];
	        int length;
	        while ( (length = databaseInputStream.read(buffer)) > 0 ) {
	                databaseOutputStream.write(buffer);
	        }

	        databaseOutputStream.flush();
	        databaseOutputStream.close();
	        databaseInputStream.close();
	        
	        // Удаляем старые
	        
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		    int numBackups = Integer.parseInt(prefs.getString("num_backups", "20"));
		    if(numBackups > 0) {	        
		        FilenameFilter filter = new BackupFilenameFilter();
			    ArrayList<String> items = new ArrayList<String>();
			    
			    File dir = new File(root, "jwdroid");
			    File[] files = dir.listFiles(filter);
			    for(File file : files) {
			    	items.add(file.getName());
			    }
			    
			    Collections.sort(items, new AlphanumComparator());
			    
			    
			    
			    for(int i=0; i<items.size()-numBackups; i++) {
			    	new File(dir, items.get(i)).delete();
			    }
		    }
		    
		    Toast.makeText(context, context.getResources().getString(R.string.msg_backup_created), Toast.LENGTH_LONG).show();
		}
		catch (Exception e) {
			Toast.makeText(context, context.getResources().getString(R.string.msg_backup_failed), Toast.LENGTH_LONG).show();
		}
    }
    
    
    private class BackupItem {		
    	Time time;
		Long size;
	}
    
    private static class BackupFilenameFilter implements FilenameFilter {
    	
    	Pattern p;
    	
    	public BackupFilenameFilter() {
    		p = Pattern.compile("^backup_\\d+$");			
		}

		@Override
		public boolean accept(File dir, String name) {
			Matcher m = p.matcher(name);			
			return m.matches();
		}
    	
    }
	
	
	
	private static class BackupListAdapter extends SimpleArrayAdapter<BackupItem> {		
	       
        public BackupListAdapter(Context context, ArrayList<BackupItem> items) {
           super(context, items);            
        }

        public long getItemId(int position) {
        	return getItem(position).time.toMillis(true);
        }    

        public View getView(int position, View convertView, ViewGroup parent) {

        	ViewHolder holder;
        	BackupItem item = mItems.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.backup_list_item, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.backup_item_name);
                holder.size = (TextView) convertView.findViewById(R.id.backup_item_size);
                                
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            
            Date date = new Date(item.time.toMillis(true));
            holder.name.setText( Html.fromHtml(DateFormat.getDateInstance(DateFormat.LONG).format(date)+", "+DateFormat.getTimeInstance(DateFormat.SHORT).format(date)) );
            
            holder.size.setText( String.format("%d KB", item.size/1024) );
            	
            return convertView;
        }

        static class ViewHolder {        	
            TextView name,size;
        }
        
    }
	
	

}
