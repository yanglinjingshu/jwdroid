package com.jwdroid;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.londatiga.android.R;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

public class Visit extends FragmentActivity {
	
	private static final int TYPE_FIRST_VISIT = 0;
	private static final int TYPE_RETURN_VISIT = 1;
	private static final int TYPE_STUDY = 2;
	
    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;	
    
    public static final int[] TYPE_ICONS = {R.drawable.first_visit, R.drawable.revisit, R.drawable.study};
	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private Long mTerritoryId, mDoorId, mPersonId, mVisitId;
	
	private String mDesc = "";
	private Integer mCalcAuto = 1;
	private Integer mType = 0;
	private Time mDate = new Time();
	
	private Integer mBooks=0, mBrochures=0, mMagazines=0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.visit);
		
		 Cursor rs;
	    SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
	    
	    mDoorId = getIntent().getExtras().getLong("door");
	    mPersonId = getIntent().getExtras().getLong("person");
	    	    	    
	    rs = db.rawQuery("SELECT name,territory_id FROM door WHERE ROWID=?", new String[] {mDoorId.toString()});
	    rs.moveToFirst();	    
	    ((TextView)findViewById(R.id.door_name)).setText(rs.getString(0));
	    mTerritoryId = rs.getLong(1);
	    rs.close();
	    
	    rs = db.rawQuery("SELECT name FROM person WHERE ROWID=?", new String[] {mPersonId.toString()});
	    rs.moveToFirst();	    
	    ((TextView)findViewById(R.id.person_name)).setText("  •  "+rs.getString(0));
	    rs.close();
	    
	    rs = db.rawQuery("SELECT name FROM territory WHERE ROWID=?", new String[] {mTerritoryId.toString()});
	    rs.moveToFirst();	    
	    ((TextView)findViewById(R.id.territory_name)).setText(rs.getString(0));
	    rs.close();
	    
	    mDate.setToNow();
	    
	    mVisitId = getIntent().getExtras().getLong("visit");
	    if(mVisitId != 0) {
	    	rs = db.rawQuery("SELECT calc_auto,desc,type,strftime('%s',date),magazines,brochures,books FROM visit WHERE ROWID=?", new String[] {mVisitId.toString()});
	    	rs.moveToFirst();
	    	
	    	mCalcAuto = rs.getInt(0);
	    	mDesc = rs.getString(1);
	    	mType = rs.getInt(2);
	    	mDate.set(rs.getLong(3)*1000);
	    	
	    	mMagazines = rs.getInt(4);
	    	mBrochures = rs.getInt(5);
	    	mBooks = rs.getInt(6);
	    	
	    	rs.close();
	    }
	    else {
	    	int visitsCnt = Util.dbFetchInt(db, "SELECT COUNT(*) FROM visit WHERE door_id=?", new String[]{mDoorId.toString()});	    	
	    	if(visitsCnt > 0) {
	    		int studiesCnt = Util.dbFetchInt(db, "SELECT COUNT(*) FROM visit WHERE door_id=? AND type=?", new String[]{mDoorId.toString(), String.valueOf(TYPE_STUDY)});
	    		if(studiesCnt > 0)
	    			mType = TYPE_STUDY;
	    		else
	    			mType = TYPE_RETURN_VISIT;
	    	}
	    }
	    
	    ((EditText)findViewById(R.id.edit_visit_desc)).setText(mDesc);
		((EditText)findViewById(R.id.edit_visit_desc)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mDesc = s.toString();				
				recalcLiterature();
			}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {				
			}			
			@Override
			public void afterTextChanged(Editable s) {				
			}
		});
	    
	    ((Spinner)findViewById(R.id.list_visit_type)).setAdapter(new VisitTypeAdapter(this));    
	    	    
	    ((CheckBox)findViewById(R.id.chk_visit_calc_auto)).setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				mCalcAuto = isChecked ? 1 : 0;
				
				findViewById(R.id.btn_visit_books_less).setEnabled(!isChecked);
				findViewById(R.id.btn_visit_books_more).setEnabled(!isChecked);
				findViewById(R.id.btn_visit_magazines_less).setEnabled(!isChecked);
				findViewById(R.id.btn_visit_magazines_more).setEnabled(!isChecked);
				findViewById(R.id.btn_visit_brochures_less).setEnabled(!isChecked);
				findViewById(R.id.btn_visit_brochures_more).setEnabled(!isChecked);
				
				findViewById(R.id.btn_visit_books_less).setClickable(!isChecked);
				findViewById(R.id.btn_visit_books_more).setClickable(!isChecked);
				findViewById(R.id.btn_visit_magazines_less).setClickable(!isChecked);
				findViewById(R.id.btn_visit_magazines_more).setClickable(!isChecked);
				findViewById(R.id.btn_visit_brochures_less).setClickable(!isChecked);
				findViewById(R.id.btn_visit_brochures_more).setClickable(!isChecked);
				
				if(mCalcAuto == 1)
					recalcLiterature();
			}
		});
	    ((CheckBox)findViewById(R.id.chk_visit_calc_auto)).setChecked(mCalcAuto == 1);
	    
	    ((Spinner)findViewById(R.id.list_visit_type)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mType = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
	    	
		});
	    ((Spinner)findViewById(R.id.list_visit_type)).setSelection(mType);
	    
	    
	    ((TextView)findViewById(R.id.text_visit_books)).setText(mBooks.toString());
	    ((TextView)findViewById(R.id.text_visit_brochures)).setText(mBrochures.toString());
	    ((TextView)findViewById(R.id.text_visit_magazines)).setText(mMagazines.toString());
	    
	    ((ImageButton)findViewById(R.id.btn_visit_books_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				if(mBooks > 0) mBooks--;
				((TextView)findViewById(R.id.text_visit_books)).setText(mBooks.toString());
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_visit_books_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				mBooks++;
				((TextView)findViewById(R.id.text_visit_books)).setText(mBooks.toString());
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_visit_brochures_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				if(mBrochures > 0) mBrochures--;
				((TextView)findViewById(R.id.text_visit_brochures)).setText(mBrochures.toString());
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_visit_brochures_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				mBrochures++;
				((TextView)findViewById(R.id.text_visit_brochures)).setText(mBrochures.toString());
			}
	    });
	    
	    ((ImageButton)findViewById(R.id.btn_visit_magazines_less)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				if(mMagazines > 0) mMagazines--;
				((TextView)findViewById(R.id.text_visit_magazines)).setText(mMagazines.toString());
			}
	    });
	    ((ImageButton)findViewById(R.id.btn_visit_magazines_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				mMagazines++;
				((TextView)findViewById(R.id.text_visit_magazines)).setText(mMagazines.toString());
			}
	    });
	    
	    
	    
	    ((Button)findViewById(R.id.btn_visit_date)).setText(mDate.format("%d.%m.%y"));
	    ((Button)findViewById(R.id.btn_visit_time)).setText(mDate.format("%H:%M"));
	    ((Button)findViewById(R.id.btn_visit_date)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
	    ((Button)findViewById(R.id.btn_visit_time)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}
		});
	    
	    
	    
	    
	    
	    ((Button)findViewById(R.id.title_btn_ok)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mDate.switchTimezone("UTC");
				
				SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
	    		if(mVisitId == 0) {
	    			db.execSQL(	"INSERT INTO visit (territory_id,door_id,person_id,desc,calc_auto,type,date,magazines,brochures,books)" +
	    		    		"VALUES(?,?,?,?,?,?,?,?,?,?)", 
	    		    		new Object[] {mTerritoryId, mDoorId, mPersonId, mDesc, mCalcAuto, mType, mDate.format3339(false), mMagazines, mBrochures, mBooks});
	    			
	    			
	    			int reject = Util.dbFetchInt(db, "SELECT reject FROM person WHERE ROWID=?", new String[]{mPersonId.toString()});
		    		
		    		if(reject == 0) {
			    		String prefKey = "";
			    		int maxType = Util.dbFetchInt(db, "SELECT MAX(type) FROM visit WHERE person_id=?", new String[]{mPersonId.toString()});
			    		switch(maxType) {
			    		case TYPE_FIRST_VISIT:	prefKey="color_visit"; break;
			    		case TYPE_RETURN_VISIT:	prefKey="color_return_visit"; break;
			    		case TYPE_STUDY:	prefKey="color_study"; break;
			    		}
			    		
			    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Visit.this);
						Integer color = Integer.parseInt(prefs.getString(prefKey, "0"));
						int manualColor = Util.dbFetchInt(db, "SELECT manual_color FROM door WHERE ROWID=?", new String[] {mDoorId.toString()});
						if(prefs.getBoolean("autoset_color", true) && color != -1 && manualColor == 0) {
							db.execSQL("UPDATE door SET color1=?,color2=? WHERE ROWID=?", new Object[] {color, color, mDoorId});												
						}
		    		}
	    		}
	    		else {		    		
	    			db.execSQL("UPDATE visit SET desc=?,calc_auto=?,type=?,`date`=?,magazines=?,brochures=?,books=? WHERE ROWID=?", new Object[] {mDesc, mCalcAuto, mType, mDate.format3339(false), mMagazines, mBrochures, mBooks, mVisitId});    			
	    		
	    		}
	    		
	    		Door.updateVisits(Visit.this, mDoorId);
	    		
	    		setResult(1);
    			finish();
			}
		});
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {
							
							@Override
							public void onTimeSet(TimePicker view, int hourOfDay, int minute) {								
								mDate.set(0, minute, hourOfDay, mDate.monthDay, mDate.month, mDate.year);
								((Button)findViewById(R.id.btn_visit_time)).setText(mDate.format("%H:%M"));	
							}
						}, 
						mDate.hour, mDate.minute, true);
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
							
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
								mDate.set(0, mDate.minute, mDate.hour, dayOfMonth, monthOfYear, year);
								((Button)findViewById(R.id.btn_visit_date)).setText(mDate.format("%d.%m.%y"));
							}
						},
                        mDate.year, mDate.month, mDate.monthDay);
        }
        return null;
    }	
	
	
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(mDate.hour, mDate.minute);
                break;
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mDate.year, mDate.month, mDate.monthDay);
                break;
        }
    }  	
    
    private void recalcLiterature() {
    	if(mCalcAuto == 0)
    		return;
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	int brochures = cntDescTemplates(prefs.getString("literature_template_brochure", "--б"));
    	if(mBrochures != brochures) {
    		mBrochures = brochures;
    		((TextView)findViewById(R.id.text_visit_brochures)).setText(mBrochures.toString());
    	}
    	
    	int books = cntDescTemplates(prefs.getString("literature_template_book", "--к"));
    	if(mBooks != books) {
    		mBooks = books;
    		((TextView)findViewById(R.id.text_visit_books)).setText(mBooks.toString());
    	}
    	
    	int magazines = cntDescTemplates(prefs.getString("literature_template_magazine", "--ж"));
    	if(mMagazines != magazines) {
    		mMagazines = magazines;
    		((TextView)findViewById(R.id.text_visit_magazines)).setText(mMagazines.toString());
    	}
    }
    
    private int cntDescTemplates(String str) {
    	int lastIndex = 0;
    	int count = 0;
    	while(lastIndex != -1) {
    		lastIndex = mDesc.indexOf(str, lastIndex);
    		if(lastIndex != -1) {
    			count++;
    			lastIndex++;
    		}
    	}
    	return count;
    }
	
	
	private class VisitTypeAdapter extends BaseAdapter implements SpinnerAdapter {
		
		private String[] texts = {"Обычное","Повторное","Изучение"};
		private int[] icons = {R.drawable.first_visit, R.drawable.revisit, R.drawable.study};
		
		
        private LayoutInflater mInflater;
       
        public VisitTypeAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }		

		@Override
		public int getCount() {			
			return 3;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.spinner_visit_type, null);				
			}
			((ImageView)convertView.findViewById(R.id.spinner_visit_type_icon)).setImageResource(icons[position]);
			((TextView)convertView.findViewById(R.id.spinner_visit_type_text)).setText(texts[position]);
			convertView.setPadding(0, 0, 0, 0);
			return convertView;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.spinner_visit_type, null);				
			}
			((ImageView)convertView.findViewById(R.id.spinner_visit_type_icon)).setImageResource(icons[position]);
			((TextView)convertView.findViewById(R.id.spinner_visit_type_text)).setText(texts[position]);			
			return convertView;
		}
		
	}
}
