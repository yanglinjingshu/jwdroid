package com.jwdroid;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.londatiga.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.CheckBox;

public class Chrono extends Activity {
	
	private static final int DIALOG_FINISH = 1;
	private static final int DIALOG_START = 2;
	
	private ChronoService mService;
	private boolean mBound;
	
	private Timer mTimer = null;
	private boolean mShowColon = false;
	
	private Long mDialogItemId;
	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		    
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	
	        final ChronoService.LocalBinder binder = (ChronoService.LocalBinder) service;
	        mService = binder.getService();
	        if(mService.calcAuto == null)
	        	mService.calcAuto = ((CheckBox)findViewById(R.id.chk_calc_auto)).isChecked();
	        else
	        	((CheckBox)findViewById(R.id.chk_calc_auto)).setChecked(mService.calcAuto);
	        
	        recalcVisits();
	        initUI();
	        
	        updateUI();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        mService = null;
	        initUI();
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.chrono);
				initUI();
		
		bindService(new Intent(Chrono.this, ChronoService.class), mConnection, 0);
		
		mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if(mService != null) {
					runOnUiThread(new Runnable() {						
						@Override
						public void run() {							
							updateUI();
						}
					});
				}
			}
		}, 0, 500);
		
		
				
		findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {	
				showDialog(DIALOG_START);				
			}
		});
		
		findViewById(R.id.btn_finish).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				showDialog(DIALOG_FINISH);
			}
		});
		
		findViewById(R.id.btn_pause).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				if(mService == null)
					return;
				mService.setPaused(true);
				v.setVisibility(View.GONE);
				findViewById(R.id.btn_resume).setVisibility(View.VISIBLE);
				updateUI();
			}
		});
		
		findViewById(R.id.btn_resume).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				if(mService == null)
					return;
				mService.setPaused(false);
				v.setVisibility(View.GONE);
				findViewById(R.id.btn_pause).setVisibility(View.VISIBLE);
				updateUI();
			}
		});
		
		((CheckBox)findViewById(R.id.chk_calc_auto)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mService == null)
					return;
				mService.calcAuto = isChecked;
				recalcVisits();
				initUI();
			}
		});
		
		
		((ImageButton)findViewById(R.id.btn_books_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				if(mService.books > 0) mService.books--;
				((TextView)findViewById(R.id.text_books)).setText(String.valueOf(mService.books));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_books_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				mService.books++;
				((TextView)findViewById(R.id.text_books)).setText(String.valueOf(mService.books));
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_brochures_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				if(mService.brochures > 0) mService.brochures--;
				((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(mService.brochures));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_brochures_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				mService.brochures++;
				((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(mService.brochures));
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_magazines_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				if(mService.magazines > 0) mService.magazines--;
				((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(mService.magazines));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_magazines_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService == null) return;
				mService.magazines++;
				((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(mService.magazines));
			}
	    });
	    
	    
	    ((ImageButton)findViewById(R.id.btn_returns_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService == null) return;
				if(mService.returns > 0) mService.returns--;
				((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(mService.returns));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_returns_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				mService.returns++;
				((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(mService.returns));
			}
	    });
	    
	    
	    
	    ((ImageButton)findViewById(R.id.btn_timer_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService == null) return;
				if(mService.minutes > 10) mService.setMinutes(mService.minutes-10);
				updateUI();
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_timer_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService == null) return;
				mService.setMinutes(mService.minutes+10);
				updateUI();
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
	    	AlertDialog.Builder builder;
			LayoutInflater factory = LayoutInflater.from(this);
	    	switch(id) {
	    	case DIALOG_FINISH:	            
	    		dialog = new AlertDialog.Builder(this)
	    					.setMessage(R.string.msg_chrono_finish)
	    					.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(mService != null) {
										
										SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
										db.execSQL("INSERT INTO session (date, minutes,magazines,books,brochures,returns) VALUES(?,?,?,?,?,?)",
												new Object[] { mService.startTime.format3339(false), mService.minutes, mService.magazines, mService.books, mService.brochures, mService.returns });
										long sessionId = Util.dbFetchLong(db, "SELECT last_insert_rowid()", new String[]{});
										mService.stop();
										
										Intent intent = new Intent(Chrono.this, Session.class);
										intent.putExtra("session", sessionId);
										startActivity(intent);
										finish();
									}
							}
						})
						.setNegativeButton(R.string.btn_no, null).create();    		
	    		break;	    	
	    		
	    	case DIALOG_START:	            
	    		dialog = new AlertDialog.Builder(this)
	    					.setMessage(R.string.msg_chrono_start)
	    					.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent intent = new Intent(Chrono.this, ChronoService.class);
									startService(intent);
									bindService(new Intent(Chrono.this, ChronoService.class), mConnection, 0);
							}
						})
						.setNegativeButton(R.string.btn_no, null).create();    		
	    		break;	   
	    	}
	    	
	    	
	    	
	    	return dialog;
	    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(mConnection);
	}
	
	private void recalcVisits() {
		if(mService == null || !mService.calcAuto)
			return;
		
		Time now = new Time();
		now.setToNow();
		
		SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
		
		Cursor rs = db.rawQuery("SELECT SUM(books),SUM(brochures),SUM(magazines) FROM visit WHERE strftime('%s',date) >= ? AND strftime('%s',date) <= ?", new String[]{ String.valueOf(mService.startTime.toMillis(true)), String.valueOf(now.toMillis(true))});
		rs.moveToFirst();
		mService.books = rs.getInt(0);
		mService.brochures = rs.getInt(1);
		mService.magazines = rs.getInt(2);
		rs.close();
		
		
		rs = db.rawQuery("SELECT COUNT(*) FROM visit WHERE type > 0 AND strftime('%s',date) >= ? AND strftime('%s',date) <= ?", new String[]{ String.valueOf(mService.startTime.toMillis(true)), String.valueOf(now.toMillis(true))});
		rs.moveToFirst();
		mService.returns = rs.getInt(0);
		rs.close();
		
		
	}
	
	private void updateUI() {
		if(mService == null)
			return;
		
		mShowColon = !mShowColon;
		if(mService.getPaused())
			mShowColon = true;
		findViewById(R.id.lbl_timer_colon).setVisibility(mShowColon ? View.VISIBLE : View.INVISIBLE);
		((TextView)findViewById(R.id.lbl_timer_hour)).setText( String.format("%d", mService.minutes / 60) );
		((TextView)findViewById(R.id.lbl_timer_minute)).setText( String.format("%02d", mService.minutes % 60) );
	}
	
	private void initUI() {
		findViewById(R.id.lbl_timer_colon).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_start).setVisibility(mService == null ? View.VISIBLE : View.GONE);
		findViewById(R.id.btn_pause).setVisibility(mService == null || mService.getPaused() ? View.GONE : View.VISIBLE);
		findViewById(R.id.btn_resume).setVisibility(mService == null || !mService.getPaused() ? View.GONE : View.VISIBLE);
		findViewById(R.id.btn_finish).setVisibility(mService == null ? View.GONE : View.VISIBLE);
		
		//findViewById(R.id.lbl_started_time).setVisibility(mService == null ? View.INVISIBLE : View.VISIBLE);
		if(mService == null)
			((TextView)findViewById(R.id.lbl_started_time)).setText( R.string.lbl_chrono_stopped );
		else
			((TextView)findViewById(R.id.lbl_started_time)).setText( String.format(getResources().getString(R.string.lbl_chrono_started_time), DateFormat.getTimeInstance(DateFormat.SHORT).format( new Date(mService.startTime.toMillis(true)) )));
		
		boolean calcAuto = (mService == null || mService.calcAuto ? true : false);
		
		findViewById(R.id.btn_books_less).setEnabled(!calcAuto);
		findViewById(R.id.btn_books_more).setEnabled(!calcAuto);
		findViewById(R.id.btn_magazines_less).setEnabled(!calcAuto);
		findViewById(R.id.btn_magazines_more).setEnabled(!calcAuto);
		findViewById(R.id.btn_brochures_less).setEnabled(!calcAuto);
		findViewById(R.id.btn_brochures_more).setEnabled(!calcAuto);
		findViewById(R.id.btn_returns_less).setEnabled(!calcAuto);
		findViewById(R.id.btn_returns_more).setEnabled(!calcAuto);
		
		findViewById(R.id.btn_books_less).setClickable(!calcAuto);
		findViewById(R.id.btn_books_more).setClickable(!calcAuto);
		findViewById(R.id.btn_magazines_less).setClickable(!calcAuto);
		findViewById(R.id.btn_magazines_more).setClickable(!calcAuto);
		findViewById(R.id.btn_returns_less).setClickable(!calcAuto);
		findViewById(R.id.btn_returns_more).setClickable(!calcAuto);
		
		findViewById(R.id.btn_timer_less).setEnabled(mService != null);
		findViewById(R.id.btn_timer_more).setEnabled(mService != null);
		findViewById(R.id.btn_timer_less).setClickable(mService != null);
		findViewById(R.id.btn_timer_more).setClickable(mService != null);
		
		int books = mService == null ? 0 : mService.books;		
		((TextView)findViewById(R.id.text_books)).setText(String.valueOf(books));
		
		int brochures = mService == null ? 0 : mService.brochures;		
		((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(brochures));
		
		int magazines = mService == null ? 0 : mService.magazines;		
		((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(magazines));
		
		int returns = mService == null ? 0 : mService.returns;		
		((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(returns));
	}
}
