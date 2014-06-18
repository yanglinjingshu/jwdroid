package com.jwdroid.ui;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jwdroid.BugSenseConfig;
import com.jwdroid.R;

public class MainMenu extends Activity {
	
	static final private int DIALOG_REVISION_NOTES = 1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 
		 BugSenseConfig.initAndStartSession(this);
		 
		 setContentView(R.layout.main_menu);
		 
		 showRevisionNotes(); 
		 
		 
		 
		 File root = Environment.getExternalStorageDirectory();
         
		 File dir = new File(root, "jwdroid");
		 if(!dir.exists())
			 dir.mkdir();
		 
		 /*try {
			AppDbOpenHelper.copyDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		 findViewById(R.id.btn_territories).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenu.this, TerritoryList.class);
	    		startActivity(intent);
			}
		 });
		 
		 findViewById(R.id.btn_people).setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenu.this, PeopleList.class);
	    		startActivity(intent);
			}
		 });
		 
		 findViewById(R.id.btn_chrono).setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenu.this, Chrono.class);
	    		startActivity(intent);
			}
		 });
		 
		 findViewById(R.id.btn_reports).setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenu.this, ReportList.class);
	    		startActivity(intent);
			}
		 });
		 
		 findViewById(R.id.btn_drive).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenu.this, BackupList.class);
		    	startActivity(intent);
			}
		 });
	}
	
	 @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.main_menu, menu);
			return true;
		}	
	 
	 
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
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
		return super.onOptionsItemSelected(item);
	}
	 
	    
	    
    @Override
    protected Dialog onCreateDialog(int id) {    	
    	Dialog dialog=null;
    	LayoutInflater factory = LayoutInflater.from(this);
    	
    	switch(id) {
    	case DIALOG_REVISION_NOTES:            
     		dialog = new AlertDialog.Builder(this)
     					.setTitle(R.string.msg_revision_notes)
     					.setMessage(R.string.msg_revision_notes_1_3)
     					.setPositiveButton(R.string.btn_ok, null).create(); 
    		break;
    	}
    	
    	return dialog;
    }
	 
	private void showRevisionNotes() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("revision_notes_1_3", false)) {
			Editor editor = prefs.edit();
			editor.putBoolean("revision_notes_1_3", true);
			editor.commit();
			
			showDialog(DIALOG_REVISION_NOTES);
		}
	}
	 

}
