package com.jwdroid;

import net.londatiga.android.R;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.EditText;

public class Session extends Activity {
	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	
	Long mSessionId;
	
	Integer mMinutes,mBooks,mBrochures,mMagazines,mReturns;
	String mDesc;
	Time mDate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.session);
		
		mSessionId = getIntent().getExtras().getLong("session");
		
		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
		
		Cursor rs = db.rawQuery("SELECT strftime('%s',date),desc,minutes,books,brochures,magazines,returns FROM session WHERE ROWID=?", new String[]{mSessionId.toString()});
		rs.moveToFirst();
		
		mDate = new Time();
		mDate.set(rs.getLong(0)*1000);
		((TextView)findViewById(R.id.title)).setText("Служение "+mDate.format("%d.%m.%y в %H:%M"));
		
		mDesc = rs.getString(1);
		mMinutes = rs.getInt(2);
		mBooks = rs.getInt(3);
		mBrochures = rs.getInt(4);
		mMagazines = rs.getInt(5);
		mReturns = rs.getInt(6);
		
		rs.close();
		
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
				
				db.execSQL("UPDATE session SET desc=?,minutes=?,magazines=?,brochures=?,books=?,returns=? WHERE ROWID=?",
						new Object[] {mDesc, mMinutes, mMagazines, mBrochures, mBooks, mReturns, mSessionId});
				
				
				Intent intent = new Intent(Session.this, Report.class);
				intent.putExtra("month", mDate.format("%Y%m"));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
	}
}
