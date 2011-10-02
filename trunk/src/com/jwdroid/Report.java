package com.jwdroid;

import java.util.ArrayList;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import net.londatiga.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Report extends FragmentActivity implements LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

	static private final int DIALOG_DELETE = 1;
	
	private SessionListAdapter mListAdapter;	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private ListView mListView;
	
	private String mMonth;
	
	private Long mDialogItemId;
	
	public static final String[] MONTHS = {"Январь","Февраль","Март","Апрель","Май","Июнь","Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"}; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
        
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    prefs.registerOnSharedPreferenceChangeListener(this);
        
        mMonth = getIntent().getExtras().getString("month");
        
        final String monthName = MONTHS[Integer.parseInt(mMonth.substring(4,6))-1] + " " + mMonth.substring(0,4);

        ((TextView)findViewById(R.id.title)).setText("Отчет за " + monthName);
        
              
        // Set up territory list
        
        mListView = (ListView)findViewById(R.id.session_list);
            
	    
	  
	    mListAdapter = new SessionListAdapter(this, new ArrayList<SessionItem>());
	    mListAdapter.registerDataSetObserver(new DataSetObserver() {
	    	public void onChanged() {
	    		findViewById(R.id.session_list_empty).setVisibility( mListAdapter.getCount() == 0 ? View.VISIBLE : View.GONE );
	    	}
		});
	    
	    mListView.setAdapter(mListAdapter);   
	    
	    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view,
    				int position, long id) {
    			
    			SessionItem item = mListAdapter.getItem(position);
    			
    			Intent intent = new Intent(Report.this, Session.class);
	    		intent.putExtra("session", item.id);
	    		startActivityForResult(intent,1);	
	    				    			
    		}
		});
	    
	    final QuickAction listActions 	= new QuickAction(this);
		listActions.addActionItem(new ActionItem("Удалить", getResources().getDrawable(R.drawable.ac_trash)));
		listActions.animateTrack(false);
		listActions.setAnimStyle(QuickAction.ANIM_MOVE_FROM_RIGHT);			
    	
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int pos, long id) {
				listActions.show(v, id);
				return true;
			}
		}); 	
		
		listActions.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {				
			@Override
			public void onItemClick(int pos) {
				Bundle args;
				SQLiteDatabase db;
				Cursor rs;
				switch(pos) {
				case 0:	// Название						
					mDialogItemId = listActions.getId();
			  		showDialog(DIALOG_DELETE);
				}
			}
		});
		
		
		final QuickAction sendTypeActions 	= new QuickAction(this);
		sendTypeActions.addActionItem(new ActionItem("SMS", getResources().getDrawable(R.drawable.ac_spechbubble_sq_line)));
		sendTypeActions.addActionItem(new ActionItem("E-mail", getResources().getDrawable(R.drawable.ac_mail)));
		sendTypeActions.animateTrack(false);		    	
	
		sendTypeActions.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {				
			@Override
			public void onItemClick(int pos) {
				Bundle args;
				SQLiteDatabase db;
				Cursor rs;
				SummaryInfo data = getSummaryInfo();
				String text = monthName+"\n\n"+
								"Книги: "+data.books+"\n" +
								"Брошюры: "+data.brochures+"\n" +
								"Часы: "+(data.minutes/60)+"\n" +
								"Журналы: "+data.magazines+"\n" +
								"Повторные: "+data.returns+"\n" +
								"Изучения: "+data.studies;								
								
				switch(pos) {
				case 0:	// SMS						
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel"));
					intent.setType("vnd.android-dir/mms-sms");
					intent.putExtra("sms_body", "Отчет\n\n"+text);
					startActivity(intent);
					break;
				case 1: // E-mail
					intent = new Intent(Intent.ACTION_SEND);
					intent.setType("message/rfc822");
					intent.putExtra(Intent.EXTRA_SUBJECT,"Отчет о проповедническом служении");
					intent.putExtra(Intent.EXTRA_TEXT, text);
					startActivity(Intent.createChooser(intent, null));
					break;
				}
			}
		});
		
	    
		
		findViewById(R.id.title_btn_send).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendTypeActions.show(v);
				
			}
		});
		
		
		
		
		
		
		final QuickAction summaryTypeActions 	= new QuickAction(this);
		summaryTypeActions.addActionItem(new ActionItem("По списку служений", getResources().getDrawable(R.drawable.ac_list_bullets)));
		summaryTypeActions.addActionItem(new ActionItem("По посещениям", getResources().getDrawable(R.drawable.ac_users)));
		summaryTypeActions.animateTrack(false);		    	
	
		summaryTypeActions.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {				
			@Override
			public void onItemClick(int pos) {
				Bundle args;
				SQLiteDatabase db;
				Cursor rs;
				SharedPreferences.Editor editor;
				switch(pos) {
				case 0:	// По списку служений						
					editor = prefs.edit();
					editor.putString("report_calc_type", "1");
					editor.commit();
					break;
				case 1:
					editor = prefs.edit();
					editor.putString("report_calc_type", "2");
					editor.commit();
					break;
				}
			}
		});
		
		findViewById(R.id.btn_summary_type).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				summaryTypeActions.show(v);
			}
		});
	    
	    calcSummary();
	    
	    getSupportLoaderManager().restartLoader(0, null, this);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {    	
	    switch (item.getItemId()) {
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
	    
	    return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		SessionListLoader loader = new SessionListLoader(this, mDbOpenHelper, mMonth);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		ArrayList<SessionItem> data = new ArrayList<SessionItem>();
		while(cursor.moveToNext()) {
			SessionItem item = new SessionItem();
			item.id = cursor.getLong(0);						
			item.date = new Time();				
			item.date.set(cursor.getLong(1)*1000);
			item.minutes = cursor.getInt(2);
			item.books = cursor.getInt(3);
			item.brochures = cursor.getInt(4);
			item.magazines =  cursor.getInt(5);
			item.returns = cursor.getInt(6);
			item.desc = cursor.getString(7);
			
			data.add(item);
		}
		mListAdapter.swapData(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mListAdapter.swapData(new ArrayList<SessionItem>());	
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		getSupportLoaderManager().getLoader(0).forceLoad();
	}
	
	private class SummaryInfo {
		int magazines,brochures,books,returns,studies,minutes;
	}
	
	private SummaryInfo getSummaryInfo() {
		
		SummaryInfo data = new SummaryInfo();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    int calcType = Integer.parseInt(prefs.getString("report_calc_type", "1"));
	    
	    SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
	    
	    if(calcType == 1) { // По хронометру
	    	Cursor rs = db.rawQuery("SELECT SUM(magazines),SUM(brochures),SUM(books),SUM(returns) FROM session WHERE strftime('%Y%m',date)=?", new String[]{mMonth});
	    	rs.moveToFirst();
	    	data.magazines = rs.getInt(0);
	    	data.brochures = rs.getInt(1);
	    	data.books = rs.getInt(2);
	    	data.returns = rs.getInt(3);
	    	rs.close();
    	}
	    else {			  // По посещениям
	    	Cursor rs = db.rawQuery("SELECT SUM(magazines),SUM(brochures),SUM(books) FROM visit WHERE strftime('%Y%m',date)=?", new String[]{mMonth});
	    	rs.moveToFirst();
	    	data.magazines = rs.getInt(0);
	    	data.brochures = rs.getInt(1);
	    	data.books = rs.getInt(2);
	    	rs.close();
	    	data.returns = Util.dbFetchInt(db, "SELECT COUNT(*) FROM visit WHERE type>0 AND strftime('%Y%m',date)=?", new String[]{mMonth});
	    }
	    data.studies = Util.dbFetchInt(db, "SELECT COUNT(DISTINCT person_id) FROM visit WHERE type=2 AND strftime('%Y%m',date)=?", new String[]{mMonth});
	    data.minutes = Util.dbFetchInt(db, "SELECT SUM(minutes) FROM session WHERE strftime('%Y%m',date)=?", new String[]{mMonth});
	   
	    return data;
	}
	
	private void calcSummary() {
		SummaryInfo data = getSummaryInfo();
		
	    String info = "";
        if(data.magazines>0)
        	info += data.magazines+" "+Util.pluralForm(data.magazines, "журнал", "журнала", "журналов");
        if(data.brochures>0) {
        	if(info.length()>0)
        		info += ", ";
        	info += data.brochures+" "+Util.pluralForm(data.brochures, "брошюра", "брошюры", "брошюр");
        }
        if(data.books>0) {
        	if(info.length()>0)
        		info += ", ";
        	info += data.books+" "+Util.pluralForm(data.books, "книга", "книги", "книг");
        }
        if(data.returns>0) {
        	if(info.length()>0)
        		info += ", ";
        	info += data.returns+" "+Util.pluralForm(data.returns, "повторное", "повторных", "повторных");
        }
        if(data.studies>0) {
        	if(info.length()>0)
        		info += ", ";
        	info += data.studies+" "+Util.pluralForm(data.studies, "изучение", "изучения", "изучений");
        }
        if(info.length() == 0)
        	info = "Нет данных";
        ((TextView)findViewById(R.id.summary_info)).setText(info);
        
        ((TextView)findViewById(R.id.summary_minutes)).setText(String.format("%d:%02d", data.minutes/60, data.minutes%60));
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		if(key.equals("report_calc_type")) {
			calcSummary();
		}
	}
	
	
    @Override
    protected Dialog onCreateDialog(int id) {    	
    	Dialog dialog=null;
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View dlgEditLayout = factory.inflate(R.layout.dlg_edit, null);
    	
    	switch(id) {
    		
    	case DIALOG_DELETE:
    		dialog = new AlertDialog.Builder(this) 	
    				.setCancelable(true)
    				.setMessage("Вы действительно хотите удалить это служение?")
    				.setPositiveButton(R.string.btn_ok, null)
    				.setNegativeButton(R.string.btn_cancel, null)
    				.create();
    		break;   	
    	}
    	
    	return dialog;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {    	
    	super.onPrepareDialog(id, dialog);
    	
    	switch(id) {	    	
	    	case DIALOG_DELETE: {		    	
		    	AlertDialog alertDialog = (AlertDialog)dialog;
		    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
							db.execSQL("DELETE FROM `session` WHERE rowid=?", new Long[] { mDialogItemId });					  		
					  		Toast.makeText(Report.this, "Удалено", Toast.LENGTH_SHORT).show();			  		
					  		getSupportLoaderManager().getLoader(0).forceLoad();
					  		calcSummary();
						}
					});
	    		break;
	    	}
    	}
    }
	    	
	
	private class SessionItem {
		Long id;	
		Integer minutes,books,brochures,magazines,returns;
		String desc;
		Time date;		
	}
	
	
	static public class SessionListLoader extends AsyncLoader<Cursor>  {
		
		private AppDbOpenHelper mDbOpenHelper;
		private String mMonth;

		public SessionListLoader(Context context, AppDbOpenHelper db, String month) {
			super(context);			
			mDbOpenHelper = db;
			mMonth = month;
		}		

		@Override
		public Cursor loadInBackground() {
			SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
			Cursor rs = db.rawQuery("SELECT ROWID,strftime('%s',date),minutes,books,brochures,magazines,returns,desc FROM session WHERE strftime('%Y%m',date)=? ORDER BY date DESC", new String[]{mMonth});
			return rs;
		}
	}
	
	
	private static class SessionListAdapter extends SimpleArrayAdapter<SessionItem> {		
	       
        public SessionListAdapter(Context context, ArrayList<SessionItem> items) {
           super(context, items);            
        }

        public long getItemId(int position) {
            return mItems.get(position).id;
        }    

        public View getView(int position, View convertView, ViewGroup parent) {

        	ViewHolder holder;
        	SessionItem item = mItems.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.session_list_item, null);

                holder = new ViewHolder();
                holder.date = (TextView) convertView.findViewById(R.id.session_item_date);
                holder.desc = (TextView) convertView.findViewById(R.id.session_item_desc);
                holder.minutes = (TextView) convertView.findViewById(R.id.session_item_minutes);
                holder.info = (TextView) convertView.findViewById(R.id.session_item_info);
                                
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.date.setText(item.date.format("%d.%m.%y в %H:%M"));
            if(item.desc != null && item.desc.length()>0) {
            	holder.desc.setVisibility(View.VISIBLE);
            	holder.desc.setText(item.desc);
            }
            else 
            	holder.desc.setVisibility(View.GONE);
    		
            holder.minutes.setText(String.format("%d:%02d", item.minutes/60, item.minutes%60));
            
            String info = "";
            if(item.magazines>0)
            	info += item.magazines+" "+Util.pluralForm(item.magazines, "журнал", "журнала", "журналов");
            if(item.brochures>0) {
            	if(info.length()>0)
            		info += ", ";
            	info += item.brochures+" "+Util.pluralForm(item.brochures, "брошюра", "брошюры", "брошюр");
            }
            if(item.books>0) {
            	if(info.length()>0)
            		info += ", ";
            	info += item.books+" "+Util.pluralForm(item.books, "книга", "книги", "книг");
            }
            if(item.returns>0) {
            	if(info.length()>0)
            		info += ", ";
            	info += item.returns+" "+Util.pluralForm(item.returns, "повторное", "повторных", "повторных");
            }
            if(info.length()>0) {
            	holder.info.setVisibility(View.VISIBLE);
            	holder.info.setText(info);
            }
            else
            	holder.info.setVisibility(View.GONE);
        	        	
            return convertView;
        }

        static class ViewHolder {
            TextView date,desc,minutes,info;
        }
        
    }


	
	

}
