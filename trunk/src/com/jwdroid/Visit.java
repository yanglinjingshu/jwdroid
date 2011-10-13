package com.jwdroid;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.londatiga.android.R;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

public class Visit extends FragmentActivity {
	
	public static final int TYPE_NA = 0;
	public static final int TYPE_FIRST_VISIT = 1;
	public static final int TYPE_RETURN_VISIT = 2;
	public static final int TYPE_STUDY = 3;
	
    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;
    static final int DIALOG_TIP_TEMPLATES = 2;	
    
    public static final int[] TYPE_ICONS = {R.drawable.visit_na, R.drawable.first_visit, R.drawable.revisit, R.drawable.study};
	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private Long mTerritoryId, mDoorId, mPersonId, mVisitId;
	
	private String mDesc = "";
	private Integer mCalcAuto = 1;
	private Integer mType = TYPE_FIRST_VISIT;
	private Time mDate = new Time();
	
	private Integer mBooks=0, mBrochures=0, mMagazines=0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.visit);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("tip_literature_templates", false)) {
			Editor editor = prefs.edit();
			editor.putBoolean("tip_literature_templates", true);
			editor.commit();
			
			showDialog(DIALOG_TIP_TEMPLATES);
		}
		
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
	    if(mTerritoryId != 0)
	    	((TextView)findViewById(R.id.person_name)).setText("  •  "+rs.getString(0));
	    else
	    	((TextView)findViewById(R.id.person_name)).setText(rs.getString(0));
	    rs.close();
	    
	    if(mTerritoryId != 0) {
		    rs = db.rawQuery("SELECT name FROM territory WHERE ROWID=?", new String[] {mTerritoryId.toString()});
		    rs.moveToFirst();	    
		    ((TextView)findViewById(R.id.territory_name)).setText(rs.getString(0));
		    rs.close();
	    }
	    else
	    	((TextView)findViewById(R.id.territory_name)).setText(R.string.title_people);
	    
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
	    	int visitsCnt = Util.dbFetchInt(db, "SELECT COUNT(*) FROM visit WHERE door_id=? AND person_id=? AND type!=?", new String[]{mDoorId.toString(), mPersonId.toString(), String.valueOf(TYPE_NA)});	    	
	    	if(visitsCnt > 0) {
	    		int studiesCnt = Util.dbFetchInt(db, "SELECT COUNT(*) FROM visit WHERE door_id=? AND person_id=? AND type=?", new String[]{mDoorId.toString(), mPersonId.toString(), String.valueOf(TYPE_STUDY)});
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
			public void onItemSelected(AdapterView<?> adapterView, View arg1,
					int position, long arg3) {
				mType = (Integer)adapterView.getItemAtPosition(position);
				findViewById(R.id.visit_desc_block).setVisibility(mType == TYPE_NA ? View.GONE : View.VISIBLE);
				findViewById(R.id.literature_block).setVisibility(mType == TYPE_NA ? View.GONE : View.VISIBLE);
				if(mType == TYPE_NA) { 
					((EditText)findViewById(R.id.edit_visit_desc)).setText("");
					mBooks = mBrochures = mMagazines = 0;
				}

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
	    
	    
	    
	    ((Button)findViewById(R.id.btn_visit_date)).setText( DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(mDate.toMillis(true))));
	    ((Button)findViewById(R.id.btn_visit_time)).setText( DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(mDate.toMillis(true))));
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
								mDate.normalize(true);
								((Button)findViewById(R.id.btn_visit_time)).setText( DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(mDate.toMillis(true))));	
							}
						}, 
						mDate.hour, mDate.minute, android.text.format.DateFormat.is24HourFormat(this));
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
							
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
								mDate.set(0, mDate.minute, mDate.hour, dayOfMonth, monthOfYear, year);
								mDate.normalize(true);
								((Button)findViewById(R.id.btn_visit_date)).setText( DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(mDate.toMillis(true))));
							}
						},
                        mDate.year, mDate.month, mDate.monthDay);
                
            case DIALOG_TIP_TEMPLATES:
            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            	
            	String text = String.format(getResources().getString(R.string.msg_tip_literature_templates),
            						prefs.getString("literature_template_magazine", getResources().getString(R.string.pref_template_magazine_default)),
            						prefs.getString("literature_template_brochure", getResources().getString(R.string.pref_template_brochure_default)), 
            						prefs.getString("literature_template_book", getResources().getString(R.string.pref_template_book_default)),
            						prefs.getString("literature_template_magazine", getResources().getString(R.string.pref_template_magazine_default)));
            	
            	return new AlertDialog.Builder(this)
            		   .setTitle(R.string.title_tip)
        			   .setCancelable(true)
        			   .setMessage(text)
        			   .setPositiveButton(R.string.btn_ok, null).create();        		
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
    	
    	int brochures = cntDescTemplates(prefs.getString("literature_template_brochure", getResources().getString(R.string.pref_template_brochure_default)));
    	if(mBrochures != brochures) {
    		mBrochures = brochures;
    		((TextView)findViewById(R.id.text_visit_brochures)).setText(mBrochures.toString());
    	}
    	
    	int books = cntDescTemplates(prefs.getString("literature_template_book", getResources().getString(R.string.pref_template_book_default)));
    	if(mBooks != books) {
    		mBooks = books;
    		((TextView)findViewById(R.id.text_visit_books)).setText(mBooks.toString());
    	}
    	
    	int magazines = cntDescTemplates(prefs.getString("literature_template_magazine", getResources().getString(R.string.pref_template_magazine_default)));
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
				
		private int[] icons = {R.drawable.visit_na, R.drawable.first_visit, R.drawable.revisit, R.drawable.study};
			
		
        private LayoutInflater mInflater;
        private Context mContext;
       
        public VisitTypeAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mContext = context;
        }		

		@Override
		public int getCount() {			
			return 4;
		}

		@Override
		public Object getItem(int position) {			
			return position;
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
			((TextView)convertView.findViewById(R.id.spinner_visit_type_text)).setText(mContext.getResources().getStringArray(R.array.visit_types)[position]);
			convertView.setPadding(0, 0, 0, 0);
			return convertView;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.spinner_visit_type, null);				
			}
			((ImageView)convertView.findViewById(R.id.spinner_visit_type_icon)).setImageResource(icons[position]);
			((TextView)convertView.findViewById(R.id.spinner_visit_type_text)).setText(mContext.getResources().getStringArray(R.array.visit_types)[position]);			
			return convertView;
		}
		
	}
}
