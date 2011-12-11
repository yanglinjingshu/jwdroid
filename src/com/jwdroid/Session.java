package com.jwdroid;

import java.sql.Date;
import java.text.DateFormat;

import net.londatiga.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class Session extends Activity {
	
	private static final int DIALOG_DATE = 1;
	private static final int DIALOG_TIME = 2;
	private static final int DIALOG_DELETE = 3;
	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	
	Long mSessionId;
	
	Integer mMinutes=0,mBooks=0,mBrochures=0,mMagazines=0,mReturns=0;
	String mDesc;
	Time mDate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.session);
		
		mSessionId = getIntent().getExtras().getLong("session");
		
		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
		
		mDate = new Time();
		mDate.setToNow();
		
		if(mSessionId != 0) {
		
			Cursor rs = db.rawQuery("SELECT strftime('%s',date),desc,minutes,books,brochures,magazines,returns FROM session WHERE ROWID=?", new String[]{mSessionId.toString()});
			rs.moveToFirst();			
			
			mDate.set(rs.getLong(0)*1000);						
			
			mDesc = rs.getString(1);
			mMinutes = rs.getInt(2);
			mBooks = rs.getInt(3);
			mBrochures = rs.getInt(4);
			mMagazines = rs.getInt(5);
			mReturns = rs.getInt(6);
			
			rs.close();
		}
		
		Date date = new Date(mDate.toMillis(true));
		//((TextView)findViewById(R.id.title)).setText( String.format(getResources().getString(R.string.title_session), DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(date)));		
		((TextView)findViewById(R.id.title)).setText( String.format(getResources().getString(R.string.title_session), ""));
		
		((Button)findViewById(R.id.btn_date)).setText( DateFormat.getDateInstance(DateFormat.SHORT).format(date) );
		((Button)findViewById(R.id.btn_date)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_DATE);				
			}
		});
		((Button)findViewById(R.id.btn_time)).setText( DateFormat.getTimeInstance(DateFormat.SHORT).format(date) );
		((Button)findViewById(R.id.btn_time)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_TIME);				
			}
		});
		
		((TextView)findViewById(R.id.edit_desc)).setText(mDesc);
		
		((TextView)findViewById(R.id.lbl_timer_hour)).setText(String.format("%d",mMinutes/60));
		((TextView)findViewById(R.id.lbl_timer_minute)).setText(String.format("%02d",mMinutes%60));
		
		((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(mMagazines));
		((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(mBrochures));
		((TextView)findViewById(R.id.text_books)).setText(String.valueOf(mBooks));
		((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(mReturns));
		
		((EditText)findViewById(R.id.edit_desc)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {	
				mDesc = s.toString();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		
		((ImageButton)findViewById(R.id.btn_books_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mBooks > 0) mBooks--;
				((TextView)findViewById(R.id.text_books)).setText(String.valueOf(mBooks));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_books_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				mBooks++;
				((TextView)findViewById(R.id.text_books)).setText(String.valueOf(mBooks));
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_brochures_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mBrochures > 0) mBrochures--;
				((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(mBrochures));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_brochures_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				mBrochures++;
				((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(mBrochures));
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_magazines_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mMagazines > 0) mMagazines--;
				((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(mMagazines));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_magazines_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mMagazines++;
				((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(mMagazines));
			}
	    });
	    
	    
	    ((ImageButton)findViewById(R.id.btn_returns_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mReturns > 0) mReturns--;
				((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(mReturns));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_returns_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				mReturns++;
				((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(mReturns));
			}
	    });
	    
	    
	    
	    ((ImageButton)findViewById(R.id.btn_timer_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mMinutes > 10) mMinutes-=10;
				((TextView)findViewById(R.id.lbl_timer_hour)).setText(String.format("%d",mMinutes/60));
				((TextView)findViewById(R.id.lbl_timer_minute)).setText(String.format("%02d",mMinutes%60));
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_timer_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mMinutes+=10;
				((TextView)findViewById(R.id.lbl_timer_hour)).setText(String.format("%d",mMinutes/60));
				((TextView)findViewById(R.id.lbl_timer_minute)).setText(String.format("%02d",mMinutes%60));
			}
	    });
	    
	    
	    
	    findViewById(R.id.title_btn_ok).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
				
				if(mSessionId != 0) {				
					db.execSQL("UPDATE session SET date=?,desc=?,minutes=?,magazines=?,brochures=?,books=?,returns=? WHERE ROWID=?",
							new Object[] {mDate.format3339(false), mDesc, mMinutes, mMagazines, mBrochures, mBooks, mReturns, mSessionId});					
					
					Intent intent = new Intent(Session.this, Report.class);
					intent.putExtra("month", mDate.format("%Y%m"));
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
				else {
					db.execSQL("INSERT INTO session (date,desc,minutes,magazines,books,brochures,returns) VALUES(?,?,?,?,?,?,?)",							
							new Object[] {mDate.format3339(false), mDesc, mMinutes, mMagazines, mBooks, mBrochures, mReturns});
					setResult(1);
					finish();
				}
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.session, menu);
		getMenuInflater().inflate(R.menu.main_menu, menu);
		menu.findItem(R.id.menu_delete).setVisible(mSessionId != 0);
		return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {    	
	    switch (item.getItemId()) {
	    case R.id.menu_delete:
	    	showDialog(DIALOG_DELETE);
	    	break;	    
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
	    	
	    case R.id.menu_backups:
			intent = new Intent(this, BackupList.class);
	    	startActivity(intent);
	    	break;
	    }
	    
	    return false;
	}
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_TIME:
            	
                return new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {
							
							@Override
							public void onTimeSet(TimePicker view, int hourOfDay, int minute) {								
								mDate.set(0, minute, hourOfDay, mDate.monthDay, mDate.month, mDate.year);
								mDate.normalize(true);
								Date date = new Date(mDate.toMillis(true));
								//((TextView)findViewById(R.id.title)).setText( String.format(getResources().getString(R.string.title_session), DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(date)));
							}
						}, 
						mDate.hour, mDate.minute, android.text.format.DateFormat.is24HourFormat(this));
            case DIALOG_DATE:
                return new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
							
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
								mDate.set(0, mDate.minute, mDate.hour, dayOfMonth, monthOfYear, year);
								mDate.normalize(true);
								Date date = new Date(mDate.toMillis(true));
								//((TextView)findViewById(R.id.title)).setText( String.format(getResources().getString(R.string.title_session), DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(date)));
							}
						},
                        mDate.year, mDate.month, mDate.monthDay);
            case DIALOG_DELETE:
        		return new AlertDialog.Builder(this) 	
        				.setCancelable(true)
        				.setMessage(R.string.msg_delete_session)
        				.setPositiveButton(R.string.btn_ok, null)
        				.setNegativeButton(R.string.btn_cancel, null)
        				.create();
        }
        return null;
    }	
	
	
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_TIME:
                ((TimePickerDialog) dialog).updateTime(mDate.hour, mDate.minute);
                break;
            case DIALOG_DATE:
                ((DatePickerDialog) dialog).updateDate(mDate.year, mDate.month, mDate.monthDay);
                break;
            case DIALOG_DELETE: {		    	
		    	AlertDialog alertDialog = (AlertDialog)dialog;
		    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
							db.execSQL("DELETE FROM `session` WHERE rowid=?", new Long[] { mSessionId });					  		
					  		Toast.makeText(Session.this, R.string.msg_session_deleted, Toast.LENGTH_SHORT).show();
					  		setResult(1);
					  		finish();
						}
					});
	    		break;
            }
        }
    }  	
}
