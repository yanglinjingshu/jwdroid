package com.jwdroid;

import net.londatiga.android.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainMenu extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.main_menu);
		 
		 
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
		}
		return super.onOptionsItemSelected(item);
	}

}
