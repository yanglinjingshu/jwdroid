package com.jwdroid;

import java.util.Timer;
import java.util.TimerTask;

import com.jwdroid.ui.Chrono;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.widget.Toast;

public class ChronoService extends Service {
		
	
	
	private Timer mTimer = new Timer();
	
	 // Binder given to clients
    private final LocalBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(prefs.getLong("chronoStartTime", -1) == -1) {
			prefs.edit()
				.putLong("chronoStartTime", System.currentTimeMillis())
				.putInt("chronoMinutes", 0)
				.commit();
		}
				
		startForeground(1, updateNotify(true));
		
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				manager.notify(1, updateNotify(false));
			}
		};
				
		mTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChronoService.this);
				if(prefs.getLong("chronoStartTime", -1) <= 0)
					return;
			
				Message msg = handler.obtainMessage();
				handler.sendMessage(msg);
				
			}
		}, 0, 60000);
	
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return START_REDELIVER_INTENT;
	}
	
	public void stop() {
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(1);
		mTimer.cancel();
		stopSelf();
	}
	
	@Override
	public void onDestroy() {
	
		super.onDestroy();
		
		mTimer.cancel();
	}
	
	 
	 public void setPaused(boolean paused) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChronoService.this);
		if(paused) {						
			prefs.edit()
				.putInt("chronoMinutes", getCurrentMinutes(this))
				.putLong("chronoStartTime", 0)
				.commit();
		}
		else {
			prefs.edit().putLong("chronoStartTime", System.currentTimeMillis()).commit();
		}
	 }
	 public boolean getPaused() {
		 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChronoService.this);
		 return prefs.getLong("chronoStartTime", -1) == 0;
	 }
	 
	 public void setMinutes(int newMinutes) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		prefs.edit().putInt("chronoMinutes", newMinutes).commit();
		 
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(1, updateNotify(false));
	 }
	 
	 private Notification updateNotify(boolean showTicker) {
		 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		 
		 int minutes = getCurrentMinutes(this);
		 
		Notification notification = new Notification(R.drawable.ic_stat_notify_template, showTicker ? getResources().getString(R.string.msg_chrono_service_started) : null, 0);
		Intent notificationIntent = new Intent(ChronoService.this, Chrono.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(ChronoService.this, 0, notificationIntent, 0);
		String title = getResources().getString(R.string.lbl_chrono_current_time)+" "+String.format("%d:%02d", minutes/60, minutes%60);
		notification.setLatestEventInfo(ChronoService.this, title, getResources().getString(R.string.lbl_chrono_service_note), pendingIntent);
		return notification;
	 }

	public static int getCurrentMinutes(Context context) {	
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		int minutes = prefs.getInt("chronoMinutes", 0);
		long startTime = prefs.getLong("chronoStartTime", -1);
		if(startTime > 0) {
			long addTime = System.currentTimeMillis() - startTime;			
			minutes = Math.round(minutes + addTime/60000);
		}
		return minutes;
	}

    
    public class LocalBinder extends Binder {
    	
        public ChronoService getService() {
            return ChronoService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

	

}
