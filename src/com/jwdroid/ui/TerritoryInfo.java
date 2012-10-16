package com.jwdroid.ui;

import java.text.DateFormat;
import java.util.Date;

import com.jwdroid.AppDbOpenHelper;

import net.londatiga.android.R;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class TerritoryInfo extends Activity {
	
	private static final int DIALOG_DATE_STARTED = 1;
	private static final int DIALOG_DATE_FINISHED = 2;
	
	
	private Long mTerritoryId;
	
	private Time mStarted, mFinished=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.territory_info);
		
		mTerritoryId = getIntent().getExtras().getLong("territory");
		
		SQLiteDatabase db = AppDbOpenHelper.getInstance(TerritoryInfo.this).getWritableDatabase();
		Cursor rs = db.rawQuery("SELECT name,notes,strftime('%s',started),strftime('%s',finished),strftime('%s',modified) FROM territory WHERE ROWID=?", new String[] {mTerritoryId.toString()});
		rs.moveToFirst();
		String name = rs.getString(0);
		String notes = rs.getString(1);
		mStarted = new Time();
		mStarted.set(rs.getLong(2)*1000);
		if(!rs.isNull(3)) {
			mFinished = new Time();
			mFinished.set(rs.getLong(3)*1000);
		}
		Time modified = new Time();
		modified.set(rs.getLong(4)*1000);
		
		((TextView)findViewById(R.id.territory_name)).setText(name);
		((EditText)findViewById(R.id.edit_notes)).setText(notes);
		((Button)findViewById(R.id.btn_started)).setText( DateFormat.getDateInstance(DateFormat.SHORT).format( new Date(mStarted.toMillis(true)) ));
		
		if(!rs.isNull(3))
			((Button)findViewById(R.id.btn_finished)).setText( DateFormat.getDateInstance(DateFormat.SHORT).format( new Date(mFinished.toMillis(true)) ) );
		if(!rs.isNull(4))
			((TextView)findViewById(R.id.lbl_modified)).setText( DateFormat.getDateInstance(DateFormat.SHORT).format( new Date(modified.toMillis(true)) ) );
		
		
		((Button)findViewById(R.id.btn_started)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_DATE_STARTED);
			}
		});
		((Button)findViewById(R.id.btn_finished)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_DATE_FINISHED);
			}
		});
		
		
		
		((Button)findViewById(R.id.title_btn_ok)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mStarted.switchTimezone("UTC");
				if(mFinished != null)
					mFinished.switchTimezone("UTC");
				SQLiteDatabase db = AppDbOpenHelper.getInstance(TerritoryInfo.this).getWritableDatabase();
				String notes = ((EditText)findViewById(R.id.edit_notes)).getText().toString();				
				db.execSQL("UPDATE territory SET notes=?,started=?,finished=? WHERE ROWID=?", new Object[] {notes, mStarted.format3339(false), mFinished == null ? null : mFinished.format3339(false), mTerritoryId});
				finish();
			}
		});
		
	}
	
	@Override
    protected void onPause() {    
    	super.onPause();
    	
    }
	
	 @Override
	    protected Dialog onCreateDialog(int id) {
	        switch (id) {
	            case DIALOG_DATE_STARTED:
	                return new DatePickerDialog(this,
	                        new DatePickerDialog.OnDateSetListener() {								
								@Override
								public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
									mStarted.set(0,0,0, dayOfMonth, monthOfYear, year);
									mStarted.normalize(true);
									((Button)findViewById(R.id.btn_started)).setText( DateFormat.getDateInstance(DateFormat.SHORT).format( new Date(mStarted.toMillis(true)) ));
								}
							},
							2000,1,1);	               
	            case DIALOG_DATE_FINISHED:
	                return new DatePickerDialog(this,
	                        new DatePickerDialog.OnDateSetListener() {								
								@Override
								public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
									if(mFinished == null)
										mFinished = new Time();
									mFinished.set(0,0,0, dayOfMonth, monthOfYear, year);
									mFinished.normalize(true);
									((Button)findViewById(R.id.btn_finished)).setText( DateFormat.getDateInstance(DateFormat.SHORT).format( new Date(mFinished.toMillis(true)) ) );
								}
							},
							2000,1,1);    
	        }
	        return null;
	    }	
		
		
	    @Override
	    protected void onPrepareDialog(int id, Dialog dialog) {
	        switch (id) {
	            case DIALOG_DATE_STARTED:
	                ((DatePickerDialog) dialog).updateDate(mStarted.year, mStarted.month, mStarted.monthDay);
	                break;
	            case DIALOG_DATE_FINISHED:
	            	Time set = new Time();
	            	if(mFinished == null)
	            		set.setToNow();
	            	else
	            		set = mFinished;
	                ((DatePickerDialog) dialog).updateDate(set.year, set.month, set.monthDay);
	                break;
	        }
	    }  	
}
