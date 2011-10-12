package com.jwdroid;

import java.util.Timer;
import java.util.TimerTask;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.widget.Toast;

public class ChronoService extends Service {
	
	public int minutes, books, brochures, magazines, returns;
	public Boolean calcAuto;
	public Time startTime;
	
	private boolean mPaused=false;
	
	
	
	private Timer mTimer = new Timer();
	
	 // Binder given to clients
    private final LocalBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
	
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		minutes=-1;
		books=brochures=magazines=returns=0;
		calcAuto = null;
		startTime = new Time();
		startTime.setToNow();
				
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
				if(mPaused)
					return;
				
				minutes++;	
			
				Message msg = handler.obtainMessage();
				handler.sendMessage(msg);
				
			}
		}, 0, 60000);
		
		return START_NOT_STICKY;
	}
	
	public void stop() {
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(1);
		mTimer.cancel();
		stopSelf();
	}
	
	 
	 public void setPaused(boolean paused) {
		mPaused = paused;		 
	 }
	 public boolean getPaused() {
		 return mPaused;
	 }
	 
	 public void setMinutes(int newMinutes) {
		minutes = newMinutes;
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(1, updateNotify(false));
	 }
	 
	 private Notification updateNotify(boolean showTicker) {
		Notification notification = new Notification(R.drawable.ic_stat_notify_template, showTicker ? getResources().getString(R.string.msg_chrono_service_started) : null, 0);
		Intent notificationIntent = new Intent(ChronoService.this, Chrono.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(ChronoService.this, 0, notificationIntent, 0);
		String title = getResources().getString(R.string.lbl_chrono_current_time)+" "+String.format("%d:%02d", minutes/60, minutes%60);
		notification.setLatestEventInfo(ChronoService.this, title, getResources().getString(R.string.lbl_chrono_service_note), pendingIntent);
		return notification;
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
