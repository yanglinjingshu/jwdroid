package com.jwdroid;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TerritoryAdd extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.territory_add);
		
		
		( (Button)findViewById(R.id.btn_cancel) ).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TerritoryAdd.this.finish();
			}
		});
		
		( (Button)findViewById(R.id.btn_ok) ).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TerritoryAdd.this.addTerritory();
			}
		});
	}
	
	protected boolean validateData()
	{
		int error = 0;
		
		Editable name = ( (EditText)findViewById(R.id.edit_territory_name) ).getText();		
		if(name.length() == 0)
			error = R.string.err_empty_territory_name;
		
		if(error > 0)
		{
			Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	protected void addTerritory() {
		
		if(validateData())
		{
			TerritoryDbOpenHelper dbOpenHelper = new TerritoryDbOpenHelper(this);
			SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
			
			db.execSQL("INSERT INTO territory (name,desc) VALUES(?,?)", Util.getViewValues(this, new int[] { 
				R.id.edit_territory_name,
				R.id.edit_territory_desc
			}));
			
			Toast.makeText(this, R.string.msg_territory_added, Toast.LENGTH_SHORT).show();
			
			finish();
		}
		
		
	}
	
}
