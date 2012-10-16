package com.jwdroid.ui;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.londatiga.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jwdroid.AppDbOpenHelper;
import com.jwdroid.ChronoService;
import com.jwdroid.Util;

public class Chrono extends Activity {
	
	private static final int DIALOG_FINISH = 1;
	private static final int DIALOG_START = 2;
	
	private ChronoService mService;
	private boolean mBound;
	
	private Timer mTimer = null;
	private boolean mShowColon = false;
	
	private Long mDialogItemId;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		    
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	
	        final ChronoService.LocalBinder binder = (ChronoService.LocalBinder) service;
	        mService = binder.getService();

	        
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
				
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Intent intent = new Intent(Chrono.this, ChronoService.class);
		if(prefs.getLong("chronoStartTime", -1) != -1)
			startService(intent);
		bindService(intent, mConnection, 0);
		
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
				prefs.edit().putBoolean("chronoCalcAuto", isChecked).commit();
				recalcVisits();
				initUI();
			}
		});
		
		
		((ImageButton)findViewById(R.id.btn_books_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				prefs.edit().putInt("chronoBooks", Math.max(prefs.getInt("chronoBooks", 0) - 1, 0)).commit();
				((TextView)findViewById(R.id.text_books)).setText(String.valueOf(prefs.getInt("chronoBooks", 0)));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_books_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				prefs.edit().putInt("chronoBooks", prefs.getInt("chronoBooks", 0) + 1).commit();
				((TextView)findViewById(R.id.text_books)).setText(String.valueOf(prefs.getInt("chronoBooks", 0)));
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_brochures_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				prefs.edit().putInt("chronoBrochures", Math.max(prefs.getInt("chronoBrochures", 0) - 1, 0)).commit();
				((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(prefs.getInt("chronoBrochures", 0)));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_brochures_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				prefs.edit().putInt("chronoBrochures", prefs.getInt("chronoBrochures", 0) + 1).commit();
				((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(prefs.getInt("chronoBrochures", 0)));
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_magazines_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				prefs.edit().putInt("chronoMagazines", Math.max(prefs.getInt("chronoMagazines", 0) - 1, 0)).commit();
				((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(prefs.getInt("chronoMagazines", 0)));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_magazines_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService == null) return;
				prefs.edit().putInt("chronoMagazines", prefs.getInt("chronoMagazines", 0) + 1).commit();
				((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(prefs.getInt("chronoMagazines", 0)));
			}
	    });
	    
	    
	    ((ImageButton)findViewById(R.id.btn_returns_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService == null) return;
				prefs.edit().putInt("chronoReturns", Math.max(prefs.getInt("chronoReturns", 0) - 1, 0)).commit();
				((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(prefs.getInt("chronoMagazines", 0)));
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_returns_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				if(mService == null) return;
				prefs.edit().putInt("chronoReturns", prefs.getInt("chronoReturns", 0) + 1).commit();
				((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(prefs.getInt("chronoReturns", 0)));
			}
	    });
	    
	    
	    
	    ((ImageButton)findViewById(R.id.btn_timer_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService == null) return;
				int minutes = prefs.getInt("chronoMinutes", 0);
				if(ChronoService.getCurrentMinutes(Chrono.this) > 10) mService.setMinutes(minutes-10);
				updateUI();
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_timer_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mService == null) return;
				mService.setMinutes(prefs.getInt("chronoMinutes", 0)+10);
				updateUI();
			}
	    }); 
	    
	    ((CheckBox)findViewById(R.id.chk_calc_auto)).setChecked( prefs.getBoolean("chronoCalcAuto", true) );
		
		
	}
	
	 @Override
	    protected void onPause() {    
	    	super.onPause();
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
	    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Chrono.this);
			LayoutInflater factory = LayoutInflater.from(this);
	    	switch(id) {
	    	case DIALOG_FINISH:	            
	    		dialog = new AlertDialog.Builder(this)
	    					.setMessage(R.string.msg_chrono_finish)
	    					.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(mService != null) {
										
										Time beginTime = new Time();
										beginTime.set(prefs.getLong("chronoBeginTime", 0));										
										
										SQLiteDatabase db = AppDbOpenHelper.getInstance(Chrono.this).getWritableDatabase();
										db.execSQL("INSERT INTO session (date, minutes,magazines,books,brochures,returns) VALUES(?,?,?,?,?,?)",
												new Object[] { 
													beginTime.format3339(false), 
													ChronoService.getCurrentMinutes(Chrono.this), 
													prefs.getInt("chronoMagazines", 0), 
													prefs.getInt("chronoBooks", 0),
													prefs.getInt("chronoBrochures", 0), 
													prefs.getInt("chronoReturns", 0) });
										
										long sessionId = Util.dbFetchLong(db, "SELECT last_insert_rowid()", new String[]{});
										
										mService.stop();
										
										prefs.edit()
											.remove("chronoStartTime")
											.remove("chronoBeginTime")
											.remove("chronoMinutes")
											.remove("chronoBooks")
											.remove("chronoReturns")
											.remove("chronoBrochures")
											.remove("chronoMagazines")
											.commit();
										
										if(prefs.getBoolean("autobackup", true)) {
											BackupList.createBackup(Chrono.this);
										}
										
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
									
									prefs.edit()
										.remove("chronoStartTime")
										.putLong("chronoBeginTime", System.currentTimeMillis())
										.putInt("chronoBrochures", 0)
										.putInt("chronoBooks", 0)
										.putInt("chronoBrochures", 0)
										.putInt("chronoReturns", 0)
										.commit();
									
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Chrono.this);
		Editor editor = prefs.edit();
		
		if(mService == null || !prefs.getBoolean("chronoCalcAuto", true))
			return;
		
		Time now = new Time();
		now.setToNow();
		
		SQLiteDatabase db = AppDbOpenHelper.getInstance(Chrono.this).getReadableDatabase();
		
		Cursor rs = db.rawQuery("SELECT SUM(books),SUM(brochures),SUM(magazines) FROM visit WHERE strftime('%s',date) >= ? AND strftime('%s',date) <= ?", new String[]{ String.valueOf(prefs.getLong("chronoBeginTime", 0)), String.valueOf(now.toMillis(true))});
		rs.moveToFirst();
		editor.putInt("chronoBooks", rs.getInt(0))
			.putInt("chronoBrochures", rs.getInt(1))
			.putInt("chronoMagazines", rs.getInt(2));
		rs.close();		
		
		rs = db.rawQuery("SELECT COUNT(*) FROM visit WHERE type > 1 AND strftime('%s',date) >= ? AND strftime('%s',date) <= ?", new String[]{ String.valueOf(prefs.getLong("chronoBeginTime", 0)), String.valueOf(now.toMillis(true))});
		rs.moveToFirst();
		editor.putInt("chronoReturns", rs.getInt(0));
		rs.close();		
		
		editor.commit();
		
	}
	
	private void updateUI() {
		if(mService == null)
			return;
				
		int minutes = ChronoService.getCurrentMinutes(this);
		
		mShowColon = !mShowColon;
		if(mService.getPaused())
			mShowColon = true;
		findViewById(R.id.lbl_timer_colon).setVisibility(mShowColon ? View.VISIBLE : View.INVISIBLE);
		((TextView)findViewById(R.id.lbl_timer_hour)).setText( String.format("%d", minutes / 60) );
		((TextView)findViewById(R.id.lbl_timer_minute)).setText( String.format("%02d", minutes % 60) );
	}
	
	private void initUI() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Chrono.this);
		
		findViewById(R.id.lbl_timer_colon).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_start).setVisibility(mService == null ? View.VISIBLE : View.GONE);
		findViewById(R.id.btn_pause).setVisibility(mService == null || mService.getPaused() ? View.GONE : View.VISIBLE);
		findViewById(R.id.btn_resume).setVisibility(mService == null || !mService.getPaused() ? View.GONE : View.VISIBLE);
		findViewById(R.id.btn_finish).setVisibility(mService == null ? View.GONE : View.VISIBLE);
		
		//findViewById(R.id.lbl_started_time).setVisibility(mService == null ? View.INVISIBLE : View.VISIBLE);
		if(mService == null)
			((TextView)findViewById(R.id.lbl_started_time)).setText( R.string.lbl_chrono_stopped );
		else
			((TextView)findViewById(R.id.lbl_started_time)).setText( String.format(getResources().getString(R.string.lbl_chrono_started_time), DateFormat.getTimeInstance(DateFormat.SHORT).format( new Date(prefs.getLong("chronoBeginTime", 0)) )));
		
		boolean calcAuto = prefs.getBoolean("chronoCalcAuto", true);
		
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
						
		((TextView)findViewById(R.id.text_books)).setText(String.valueOf(
				prefs.getInt("chronoBooks", 0)));
			
		((TextView)findViewById(R.id.text_brochures)).setText(String.valueOf(
				prefs.getInt("chronoBrochures", 0)));
			
		((TextView)findViewById(R.id.text_magazines)).setText(String.valueOf(
				prefs.getInt("chronoMagazines", 0)));
			
		((TextView)findViewById(R.id.text_returns)).setText(String.valueOf(
				prefs.getInt("chronoReturns", 0)));
	}
}
