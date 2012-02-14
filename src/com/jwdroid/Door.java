package com.jwdroid;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import net.londatiga.android.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Selection;
import android.text.format.Time;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FrameLayout.LayoutParams;

public class Door extends FragmentActivity {
	
	private static final String TAG = "JWGroupList";
	
	private static final int MENU_DELETE = Menu.FIRST + 1;
		
	private static final int DIALOG_EDIT_PERSON = 1;
	private static final int DIALOG_DELETE = 2;
	private static final int DIALOG_COLOR = 3;
	private static final int DIALOG_DELETE_PERSON = 4;
	private static final int DIALOG_DELETE_DOOR = 5;
	
	private static final int LOADER_PERSON = 1;
	private static final int LOADER_VISIT = 2;
	
	private static final int ID_PANEL_LISTVIEW = 1;
	
	
	static final int[] COLORS = {R.color.gray, R.color.blue, R.color.green, R.color.yellow, R.color.red, R.color.lightgray, R.color.lightblue, R.color.lightgreen, R.color.lightyellow, R.color.lightred};
	static final int[] COLORS_LIGHT = {R.color.gray_light, R.color.blue_light, R.color.green_light, R.color.yellow_light, R.color.red_light, R.color.lightgray_light, R.color.lightblue_light, R.color.lightgreen_light, R.color.lightyellow_light, R.color.lightred_light};
	static final int[] DRAWABLES = {R.drawable.btn_colored_gray, R.drawable.btn_colored_blue, R.drawable.btn_colored_green, R.drawable.btn_colored_yellow, R.drawable.btn_colored_red, R.drawable.btn_colored_lightgray, R.drawable.btn_colored_lightblue, R.drawable.btn_colored_lightgreen, R.drawable.btn_colored_lightyellow, R.drawable.btn_colored_lightred};
	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private Long mTerritoryId, mDoorId;
	private Long mViewPersonId;
	
	private Long mDialogItemId;
	
	private HorizontalPanelsView mPanelsView;
	
	private HashMap<Long,Integer> mPersonIndexes;
	private HashMap<Integer,Long> mPersonIds; 
	
	private boolean mCreatingNewPerson = false;
	private boolean mEmptyCreateRequested = false;
	
	private int mRememberedActiveViewGroup = 0, mRememberedActiveViewGroupScroll = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    if(savedInstanceState != null) {
	    	mEmptyCreateRequested = savedInstanceState.getBoolean("emptyCreateRequested", false);
	    	mRememberedActiveViewGroup = savedInstanceState.getInt("rememberedActiveViewGroup", 0);
	    	mRememberedActiveViewGroupScroll = savedInstanceState.getInt("rememberedActiveViewGroupScroll", 0);
	    }
	    
	    
	    Cursor rs;
	    SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
	    
	    mDoorId = getIntent().getExtras().getLong("door");
	    if(getIntent().hasExtra("person"))
	    	mViewPersonId = getIntent().getExtras().getLong("person");
	    
	    setContentView(R.layout.door);
	    
	    rs = db.rawQuery("SELECT name,territory_id,color1,color2 FROM door WHERE ROWID=?", new String[] {mDoorId.toString()});
	       rs.moveToFirst();	    
	    ((TextView)findViewById(R.id.door_name)).setText(rs.getString(0));
	    mTerritoryId = rs.getLong(1);
	    ((TriangleView)findViewById(R.id.title_color1)).setColor( getResources().getColor(COLORS[rs.getInt(2)]) );
	    ((TriangleView)findViewById(R.id.title_color2)).setColor( getResources().getColor(COLORS[rs.getInt(3)]) );
	    rs.close();
	    
	    if(mTerritoryId != 0) {
		    rs = db.rawQuery("SELECT name FROM territory WHERE ROWID=?", new String[] {mTerritoryId.toString()});
		    rs.moveToFirst();	    
		    ((TextView)findViewById(R.id.territory_name)).setText(rs.getString(0));
		    rs.close();
		    
		    findViewById(R.id.title_people).setVisibility(View.GONE);
	    } else {
	    	findViewById(R.id.title_btn_back).setVisibility(View.GONE);
	    }
	  
	    mPanelsView = new HorizontalPanelsView(this);
	    mPanelsView.setBackgroundColor(Color.parseColor("#eeeeee"));
	    mPanelsView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    mPanelsView.setHorizontalScrollBarEnabled(true);
	    mPanelsView.setScrollbarFadingEnabled(false);
	    mPanelsView.setHorizontalFadingEdgeEnabled(false);
	    
	    mPanelsView.setOnActiveChangedListener(new HorizontalPanelsView.OnActiveChangedListener() {			
			@Override
			public void onActiveChanged(int newActive) {
				mRememberedActiveViewGroup = newActive;				
			}
		});
	    
	    ((LinearLayout)findViewById(R.id.door)).addView(mPanelsView);
	    
	    
	    
	   ((Button)findViewById(R.id.title_btn_add)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if( mPersonIds.containsKey(mPanelsView.getActivePos()) ) {				
					Intent intent = new Intent(Door.this, Visit.class);
		    		intent.putExtra("door", mDoorId);
		    		intent.putExtra("visit", 0);
		    		intent.putExtra("person", mPersonIds.get(mPanelsView.getActivePos()));
		    		startActivityForResult(intent, 1);
				}
				else {
					mCreatingNewPerson = true;
					showDialog(DIALOG_EDIT_PERSON);
				}
			}
		});
	   
	   ((Button)findViewById(R.id.title_btn_color)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				showDialog(DIALOG_COLOR);				
			}
		});
	   
	   
	   ((LinearLayout)findViewById(R.id.title_btn_back)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	    
	    
	    updateContent();
	    
	    	    
	    if(mPersonIds.keySet().size() == 0 && !mEmptyCreateRequested) {
	    	mEmptyCreateRequested = true;
    		mCreatingNewPerson = true;
    		showDialog(DIALOG_EDIT_PERSON);
    	}
	    
	    if(mViewPersonId != null) {
	    	mPanelsView.setActiveViewGroup(mPersonIndexes.get(mViewPersonId));
	    }

	}
	
	@Override
    protected void onPause() {    
    	super.onPause();
    	
    	mDbOpenHelper.close();
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		
		outState.putBoolean("emptyCreateRequested", mEmptyCreateRequested);
		
		outState.putInt("rememberedActiveViewGroup", mRememberedActiveViewGroup);
		
		if(mPanelsView.getViewGroupsCount() > 0 && mPanelsView.getViewGroupAt( mPanelsView.getActivePos() ).findViewById(ID_PANEL_LISTVIEW) != null)
			mRememberedActiveViewGroupScroll = ((ListView)mPanelsView.getViewGroupAt( mPanelsView.getActivePos()).findViewById(ID_PANEL_LISTVIEW)).getFirstVisiblePosition();
		outState.putInt("rememberedActiveViewGroupScroll", mRememberedActiveViewGroupScroll);
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	if(mTerritoryId != 0) 
    		getMenuInflater().inflate(R.menu.door, menu);
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}	
    
    public boolean onOptionsItemSelected(MenuItem item) {		
	    switch (item.getItemId()) {
	    case R.id.menu_delete:
	    	showDialog(DIALOG_DELETE_DOOR);
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
	    }
	    
	    return false;
	}    
    
    
    @Override
    protected Dialog onCreateDialog(int id) {    	
    	Dialog dialog=null;
    	AlertDialog.Builder builder;
		LayoutInflater factory = LayoutInflater.from(this);
    	switch(id) {
    	case DIALOG_EDIT_PERSON:
    		
            final View view = factory.inflate(R.layout.dlg_person_desc, null);
            final EditText edit = (EditText)view.findViewById(R.id.edit_dlgedit_text);
            
            final String maleLetter = getResources().getString(R.string.lbl_male_letter);
			final String femaleLetter = getResources().getString(R.string.lbl_female_letter);
            
            view.findViewById(R.id.btn_person_male).setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View view) {					
					final ArrayList<String> items = new ArrayList<String>();
					for(int i=10;i<=80;i+=5)
						items.add(maleLetter+i);
					String[] strings = new String[15];					
					items.toArray(strings);
					new AlertDialog.Builder(Door.this)
						.setTitle(R.string.msg_choose_age)
						.setItems(strings, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int selectedItem) {
						        
						    	String text = edit.getText().toString();
						    	Pattern p = Pattern.compile("^((?:"+maleLetter+"|"+femaleLetter+")\\d+)?\\s*(.+)?$");
								Matcher m = p.matcher(text);
								if(m.matches() && m.group(2) != null) 
									text = items.get(selectedItem)+" "+m.group(2);						
								else
									text = items.get(selectedItem);
								edit.setText(text);
								Editable editable = edit.getText();
								Selection.setSelection(editable, editable.length());
						    	
						    }
						})
						.create()
						.show();
				}
			});
            
            view.findViewById(R.id.btn_person_female).setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View view) {					
					final ArrayList<String> items = new ArrayList<String>();
					for(int i=10;i<=80;i+=5)
						items.add(femaleLetter+i);
					String[] strings = new String[15];					
					items.toArray(strings);
					new AlertDialog.Builder(Door.this)
						.setTitle(R.string.msg_choose_age)
						.setItems(strings, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int selectedItem) {
						        
						    	String text = edit.getText().toString();
								Pattern p = Pattern.compile("^((?:"+maleLetter+"|"+femaleLetter+")\\d+)?\\s*(.+)?$");
								Matcher m = p.matcher(text);
								if(m.matches() && m.group(2) != null) 
									text = items.get(selectedItem)+" "+m.group(2);						
								else
									text = items.get(selectedItem);
								edit.setText(text);
								Editable editable = edit.getText();
								Selection.setSelection(editable, editable.length());
						    	
						    }
						})
						.create()
						.show();
				}
			});
            
    		dialog = new AlertDialog.Builder(this)
    					.setTitle(R.string.msg_person)
    					.setView(view)
    					.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								Editable editable = ((EditText)view.findViewById(R.id.edit_dlgedit_text)).getText();
								
								SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
								
								if(mCreatingNewPerson) {
									int activeViewGroup = mPanelsView.getActivePos();
									db.execSQL("INSERT INTO person (door_id,name) VALUES(?,?)", new Object[] {mDoorId, editable.toString()});
									updateContent();
									
									Intent intent = new Intent(Door.this, Visit.class);
						    		intent.putExtra("door", mDoorId);
						    		intent.putExtra("visit", 0);
						    		intent.putExtra("person", mPersonIds.get(activeViewGroup));
						    		startActivityForResult(intent, 1);
						    		
						    		mCreatingNewPerson = false;
								}
								else {					    		
									db.execSQL("UPDATE person SET name=? WHERE ROWID=?", new Object[] { editable.toString(), mPersonIds.get(mPanelsView.getActivePos()) });
										
									updateVisits(Door.this, mDoorId);
									updateContent();
								}
														
						}
					})
					.setNegativeButton(R.string.btn_cancel, null).create();    		
    		break;
    		
    	case DIALOG_DELETE:
    		builder = new AlertDialog.Builder(this);    	
    		builder.setCancelable(true)
    			   .setMessage(R.string.msg_delete_visit)
    			   .setPositiveButton(R.string.btn_ok, null)
    			   .setNegativeButton(R.string.btn_cancel, null);
    		dialog = builder.create();
    		break;
    		
    	case DIALOG_DELETE_PERSON:
    		builder = new AlertDialog.Builder(this);    	
    		builder.setCancelable(true)
    			   .setMessage(R.string.msg_delete_person)
    			   .setPositiveButton(R.string.btn_ok, null)
    			   .setNegativeButton(R.string.btn_cancel, null);
    		dialog = builder.create();
    		break;
    		
    	case DIALOG_COLOR:
    		
    		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
    	    Cursor rs = db.rawQuery("SELECT name,color1,color2 FROM door WHERE ROWID=?", new String[] {mDoorId.toString()});
    	    rs.moveToFirst();
    	    Integer color1 = rs.getInt(1);
    	    Integer color2 = rs.getInt(2);
    	    
    		ColorPicker colorPicker = new ColorPicker(this, color1, color2);
    		
    		colorPicker.setOkListener( new ColorPicker.OnOkListener() {
    			@Override
    			public void onOk(int newColor1, int newColor2) {
    				SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
					db.execSQL("UPDATE `door` SET color1=?,color2=?,manual_color=1 WHERE ROWID=?", new Object[] { new Integer(newColor1), new Integer(newColor2), mDoorId });
					
					((TriangleView)findViewById(R.id.title_color1)).setColor( getResources().getColor(COLORS[newColor1]) );
				    ((TriangleView)findViewById(R.id.title_color2)).setColor( getResources().getColor(COLORS[newColor2]) );
    			}
    		});
    		
    		dialog = colorPicker.getDialog();
    		break;
    		
    	case DIALOG_DELETE_DOOR:
    		builder = new AlertDialog.Builder(this);    	
    		builder.setCancelable(true)
    			   .setMessage(R.string.msg_delete_object)
    			   .setPositiveButton(R.string.btn_ok, null)
    			   .setNegativeButton(R.string.btn_cancel, null);
    		dialog = builder.create();
    		break;
    	}
    	
    	return dialog;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {    
    	super.onPrepareDialog(id, dialog);
    	
    	switch(id) {
    	case DIALOG_EDIT_PERSON:
    		if(mPersonIds.containsKey(mPanelsView.getActivePos())) {
	    		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
	    		Cursor rs = db.rawQuery("SELECT name FROM person WHERE ROWID=?", new String[] {mPersonIds.get(mPanelsView.getActivePos()).toString()});
	    		rs.moveToNext();
	    		((EditText)dialog.findViewById(R.id.edit_dlgedit_text)).setText(rs.getString(0));
	    		rs.close();
    		}
    		else {
    			((EditText)dialog.findViewById(R.id.edit_dlgedit_text)).setText("");
    		}
    		break;
    		
    	case DIALOG_DELETE: {	    	
	    	AlertDialog alertDialog = (AlertDialog)dialog;
	    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
				  		db.execSQL("DELETE FROM `visit` WHERE rowid=?", new Long[] { mDialogItemId });
				  		updateVisits(Door.this, mDoorId);
				  		Toast.makeText(Door.this, R.string.msg_visit_deleted, Toast.LENGTH_SHORT).show();			  		
				  		updateContent();
					}
				});
	    	alertDialog.setButton(alertDialog.BUTTON_NEGATIVE, null, new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int which) {
						   dialog.cancel();
					}
				});
	    	break;
    	}
	    	
    	case DIALOG_DELETE_PERSON:  {
    		if(mPersonIds.containsKey(mPanelsView.getActivePos())) {
	    	
    			final Long personId = mPersonIds.get(mPanelsView.getActivePos());
    			AlertDialog alertDialog = (AlertDialog)dialog;
		    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
					  		db.execSQL("DELETE FROM `visit` WHERE person_id=?", new Long[] { personId });
					  		db.execSQL("DELETE FROM `person` WHERE ROWID=?", new Long[] { personId });
					  		updateVisits(Door.this, mDoorId);
					  		Toast.makeText(Door.this, R.string.msg_person_deleted, Toast.LENGTH_SHORT).show();			  		
					  		updateContent();
					  		
					  		if(mTerritoryId == 0) {
					  			db.execSQL("DELETE FROM door WHERE ROWID=?", new Object[]{mDoorId});
					  			finish();
					  		}
						}
					});
    		}
	    	break;
    	}
	    	
    	case DIALOG_DELETE_DOOR: {		    	
	    	AlertDialog alertDialog = (AlertDialog)dialog;
	    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
						db.execSQL("DELETE FROM `door` WHERE rowid=?", new Long[] { mDoorId });
				  		db.execSQL("DELETE FROM `person` WHERE door_id=?", new Long[] { mDoorId });
				  		db.execSQL("DELETE FROM `visit` WHERE door_id=?", new Long[] { mDoorId });					  		
				  		Toast.makeText(Door.this, R.string.msg_object_deleted, Toast.LENGTH_SHORT).show();			  		
				  		finish();
					}
				});
    		break;
    	}
    	}
    	
    }
    
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		switch(v.getId()) {
		case R.id.btn_person_male:
			menu.add(0, 0, 0, getResources().getString(R.string.lbl_male_letter) + "10");
			break;
		}
	}
    
    
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  
		switch(item.getItemId()) {
		  	
	  	case MENU_DELETE:
	  		mDialogItemId = info.id;
	  		showDialog(DIALOG_DELETE);
	  		break;	
		}
		
	  
	  return true;
	  
	}
	


	public void updateContent() {
		
		if(mPanelsView.getViewGroupsCount() > 0 && mPanelsView.getViewGroupAt( mPanelsView.getActivePos() ).findViewById(ID_PANEL_LISTVIEW) != null)
			mRememberedActiveViewGroupScroll = ((ListView)mPanelsView.getViewGroupAt( mPanelsView.getActivePos() ).findViewById(ID_PANEL_LISTVIEW)).getFirstVisiblePosition();
		
		LayoutParams lp;
		LinearLayout.LayoutParams llp;
		RelativeLayout.LayoutParams rlp;
		Display display = getWindowManager().getDefaultDisplay();
    	int width = display.getWidth();
    	float density = getResources().getDisplayMetrics().density;
			
    	HashMap<Long,ArrayList<VisitItem>> items = new HashMap<Long,ArrayList<VisitItem>>();
    		
		mPanelsView.removeViewGroups();
		
		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
		Cursor rs = db.rawQuery("SELECT ROWID,name,reject FROM person WHERE door_id=?", new String[] {mDoorId.toString()});
				
		mPersonIndexes = new HashMap<Long,Integer>();
		mPersonIds = new HashMap<Integer,Long>();
		int c=0;
	    while(rs.moveToNext()) {
	    	long person_id = rs.getLong(0);
	    	String name = rs.getString(1);    	
	    	final int reject = rs.getInt(2);
	    	
	    	mPersonIds.put(c,person_id);
	    	mPersonIndexes.put(person_id, c);
	    	c++;
	    	
	    	items.put(person_id, new ArrayList<VisitItem>());
	    
	    	LinearLayout curPerson = new LinearLayout(this);
	    	curPerson.setLayoutParams(new LayoutParams(width, LayoutParams.WRAP_CONTENT));
	    	curPerson.setOrientation(LinearLayout.VERTICAL);
	    	
	    	mPanelsView.addViewGroup(curPerson); 
	    	
	    	LinearLayout personNameLayout = new LinearLayout(this);
	    	personNameLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));	    	
	    	personNameLayout.setPadding((int)(10*density), (int)(5*density), (int)(10*density), (int)(5*density));
	    	personNameLayout.setBackgroundDrawable( new GradientDrawable(Orientation.TOP_BOTTOM, new int[] {0xFFFFFFFF, 0xFFDDDDDD}));
	    	personNameLayout.setGravity(Gravity.CENTER);
	    	curPerson.addView(personNameLayout);
	    	
	    	
    		TriangleView arrowLeft = new TriangleView(this);
    		arrowLeft.setLayoutParams( new LayoutParams((int)(7*density), (int)(8*density)) );
    		arrowLeft.setOrientation(TriangleView.LEFT);
    		arrowLeft.setColor(0xffaaaaaa);
    		if(rs.isFirst()) arrowLeft.setVisibility(View.INVISIBLE);    		
    		personNameLayout.addView(arrowLeft);   
    	
	    	Button personNameView = new Button(this);
	    	llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)(32*density));
	    	llp.setMargins((int)(7*density), 0, (int)(7*density), 0);
	    	personNameView.setLayoutParams( llp );	    	
	    	personNameView.setText(name);
	    	personNameView.setShadowLayer(1, 1, 1, Color.WHITE);
	    	personNameView.setMinWidth((int)(50*density));
	    	if(reject == 1) {
	    		//personNameView.setPaintFlags(personNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
	    		personNameView.setTextColor(0xFFAAAAAA);
	    	}

	    	personNameView.setTypeface(Typeface.create((String)null, Typeface.BOLD));	    	
	    	personNameView.setBackgroundResource(R.drawable.btn_person);
	    	personNameView.setGravity(Gravity.CENTER);
	    					
			final QuickAction personActions 	= new QuickAction(this);
			personActions.addActionItem(new ActionItem(getResources().getString(R.string.action_person_change), getResources().getDrawable(R.drawable.ac_pencil)));
			personActions.addActionItem(new ActionItem(getResources().getString(R.string.action_person_delete), getResources().getDrawable(R.drawable.ac_trash)));
			if(mTerritoryId != 0)
				personActions.addActionItem(new ActionItem(reject == 0 ? getResources().getString(R.string.action_person_reject) : getResources().getString(R.string.action_person_nreject), getResources().getDrawable(R.drawable.ac_cancel)));			
			personActions.animateTrack(false);
			personActions.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {				
				@Override
				public void onItemClick(int pos) {
					switch(pos) {
					case 0:	// Изменить
						showDialog(DIALOG_EDIT_PERSON);
						break;	
					case 1: // Удалить
						showDialog(DIALOG_DELETE_PERSON);
						break;
					case 2:	// Отказ
						SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
						db.execSQL("UPDATE person SET reject=? WHERE ROWID=?", new Object[] { Integer.toString(reject == 0 ? 1 : 0), mPersonIds.get(mPanelsView.getActivePos()) });
						
						updateColor(Door.this, mDoorId);

						
						updateContent();
						updateVisits(Door.this, mDoorId);
						break;	
					}
				}
			});
	    	
	    	personNameView.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
			       personActions.show(v);					
				}
			});
	    	personNameLayout.addView(personNameView);
	    	
	    	TriangleView arrowRight = new TriangleView(this);
    		arrowRight.setLayoutParams( new LayoutParams((int)(7*density), (int)(8*density)) );
    		arrowRight.setOrientation(TriangleView.RIGHT);
    		arrowRight.setColor(0xffaaaaaa);
    		if(rs.isLast()) arrowRight.setVisibility(View.INVISIBLE);    		
    		personNameLayout.addView(arrowRight);    	
	    	
	    	View v = new View(this);
	    	v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1));	    	
	    	v.setBackgroundColor(0xFF999999);
	    	curPerson.addView(v);
	    	
	    	
	    	final ListView listView = new ListView(this);
	    	listView.setLayoutParams(new LayoutParams(width, LayoutParams.WRAP_CONTENT));		
	    	listView.setId(ID_PANEL_LISTVIEW);
	    	listView.setBackgroundColor(Color.WHITE);
	    	listView.setDivider(new ColorDrawable(0xFFCCCCCC));
	    	listView.setDividerHeight(1);
	    	curPerson.addView(listView);
	    	
	    	final QuickAction listActions 	= new QuickAction(this);
			listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_visit_delete), getResources().getDrawable(R.drawable.ac_trash)));		
			listActions.animateTrack(false);
			listActions.setAnimStyle(QuickAction.ANIM_MOVE_FROM_RIGHT);			
	    	
			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
					switch(pos) {
					case 0:	// Удалить						
						mDialogItemId = listActions.getId();
				  		showDialog(DIALOG_DELETE);
						break;						
					}					
				}
			});
	    	
	    	//registerForContextMenu(listView);
	    	
	    	
	    	listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	    		@Override
	    		public void onItemClick(AdapterView<?> parent, View view,
	    				int position, long id) {
	    			
	    			Intent intent = new Intent(Door.this, Visit.class);
		    		intent.putExtra("door", mDoorId);
		    		intent.putExtra("visit", id);
		    		intent.putExtra("person", mPersonIds.get(mPanelsView.getActivePos()));
		    		startActivityForResult(intent, 1);
		    		
	    			
	    		}
			});
	    	
	    	v = new View(this);
	    	v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, (int)(10*density)));	    	
	    	v.setBackgroundDrawable( new GradientDrawable(Orientation.TOP_BOTTOM, new int[] {0xFFCCCCCC, 0xFFEEEEEE}) );
	    	curPerson.addView(v);
	    	
	    }
	    
	    rs.close();

	    rs = db.rawQuery("SELECT ROWID, person_id, strftime(\"%s\",date), desc, type, brochures,magazines,books FROM visit WHERE door_id=? ORDER BY person_id ASC, `date` DESC", new String[] {mDoorId.toString()});
	    			    
		while(rs.moveToNext()) {
	    	
			VisitItem item = new VisitItem();
			item.id = rs.getLong(0);
	    	item.personId = rs.getLong(1);	    	
	    	item.date = new Time();
	    	item.date.set(rs.getLong(2)*1000);	    	
    		item.desc = rs.getString(3);
    		item.type = rs.getInt(4);
    		item.brochures = rs.getInt(5);
    		item.magazines = rs.getInt(6);
    		item.books = rs.getInt(7);
    		
    		items.get(item.personId).add(item);
    		
        	
        }
		
		for(int i=0;i<mPanelsView.getViewGroupsCount();i++) {
			((ListView)mPanelsView.getViewGroupAt(i).findViewById(ID_PANEL_LISTVIEW)).setAdapter( new DoorAdapter(this, items.get( mPersonIds.get(i) )) );
		}
			
			
		// Последний экран заглушки
	    
		if(mTerritoryId != 0) {
			LinearLayout curPerson = new LinearLayout(this);
	    	curPerson.setLayoutParams(new LayoutParams(width, LayoutParams.WRAP_CONTENT));
	    	curPerson.setGravity(Gravity.CENTER);
	    	curPerson.addView(View.inflate(this, R.layout.door_empty, null));
	    	mPanelsView.addViewGroup(curPerson);
		}
		
    	
    	
    	
    	mPanelsView.setActiveViewGroup(mRememberedActiveViewGroup);
		mRememberedActiveViewGroup = mPanelsView.getActivePos();		
		
		if(mPanelsView.getViewGroupsCount() > 0 && mPanelsView.getViewGroupAt( mPanelsView.getActivePos() ).findViewById(ID_PANEL_LISTVIEW) != null)
			((ListView)mPanelsView.getViewGroupAt(mRememberedActiveViewGroup).findViewById(ID_PANEL_LISTVIEW)).setSelection(mRememberedActiveViewGroupScroll);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,resultCode,data);
				
		if(resultCode == 1) {
			updateContent();
			SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
			Cursor rs = db.rawQuery("SELECT color1,color2 FROM door WHERE ROWID=?", new String[] {mDoorId.toString()});
		    rs.moveToFirst();	    
		    ((TriangleView)findViewById(R.id.title_color1)).setColor( getResources().getColor(COLORS[rs.getInt(0)]) );
		    ((TriangleView)findViewById(R.id.title_color2)).setColor( getResources().getColor(COLORS[rs.getInt(1)]) );
		    rs.close();
		}
	}
	
	
	
	public static void updateVisits(Context context, Long doorId) {
		
		AppDbOpenHelper dbOpenHelper = new AppDbOpenHelper(context);
		
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor rs = db.rawQuery("SELECT date,desc,person.name,person.reject FROM visit LEFT JOIN person ON person.ROWID=visit.person_id WHERE visit.door_id=? AND visit.type!=? ORDER BY date DESC LIMIT 1", new String[] {doorId.toString(),String.valueOf(Visit.TYPE_NA)});
		if(rs.getCount() == 0) {
			db.execSQL("UPDATE door SET last_date=NULL,last_desc=NULL,last_person_name=NULL,last_person_reject=NULL WHERE ROWID=?", new Object[] {doorId});
		}
		else {
			rs.moveToNext();
			db.execSQL("UPDATE door SET last_date=?,last_desc=?,last_person_name=?,last_person_reject=? WHERE ROWID=?", new Object[] {rs.getString(0),rs.getString(1),rs.getString(2),rs.getString(3),doorId});			
		}
		rs.close();
		
		db.execSQL("UPDATE door SET last_modified_date=(SELECT date FROM visit WHERE door_id=door.ROWID ORDER BY date DESC LIMIT 1) WHERE ROWID=?", new Object[] {doorId});
		
		Long territoryId = Util.dbFetchLong(db, "SELECT territory_id FROM door WHERE ROWID=?", new String[] {doorId.toString()});
		
		
		db.execSQL("UPDATE door SET visits_num=(SELECT COUNT(*) FROM visit WHERE door_id=?) WHERE ROWID=?", new Object[] {doorId,doorId});
		db.execSQL("UPDATE territory SET modified=(SELECT date FROM visit WHERE territory_id=territory.ROWID ORDER BY date DESC LIMIT 1) WHERE ROWID=?", new Object[] {territoryId});
		
		updateColor(context, doorId);
	}
	
	public static void updateColor(Context context, Long doorId) {
				
		AppDbOpenHelper dbOpenHelper = new AppDbOpenHelper(context);		
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				
		int manualColor = Util.dbFetchInt(db, "SELECT manual_color FROM door WHERE ROWID=?", new String[] {doorId.toString()});
		
		if(!prefs.getBoolean("autoset_color", true) || manualColor != 0) {
			return;
		}		
				
		Integer prefColorReject = Integer.parseInt(prefs.getString("color_reject", "4"));
		
		int personsCnt = Util.dbFetchInt(db, "SELECT COUNT(*) FROM person WHERE door_id=?", new String[] {doorId.toString()});
		
		if(personsCnt == 0) {
			db.execSQL("UPDATE door SET color1=0,color2=0 WHERE ROWID=?", new Object[] {doorId});
			return;
		}
		
		if(prefColorReject != -1) {
			int nonRejectsCnt = Util.dbFetchInt(db, "SELECT COUNT(*) FROM person WHERE door_id=? AND reject=0", new String[] {doorId.toString()});
			if(nonRejectsCnt == 0) {
				db.execSQL("UPDATE door SET color1=?,color2=? WHERE ROWID=?", new Object[] {prefColorReject, prefColorReject, doorId});
				if(context instanceof Door) {
					((TriangleView) ((Door)context).findViewById(R.id.title_color1)).setColor( context.getResources().getColor(COLORS[prefColorReject]) );
					((TriangleView) ((Door)context).findViewById(R.id.title_color2)).setColor( context.getResources().getColor(COLORS[prefColorReject]) );
				}
				return;
			} 
		}
		
		String prefKey = "";
		Integer maxType = Util.dbFetchInt(db, "SELECT MAX(visit.type) FROM visit LEFT JOIN person ON person.ROWID=visit.person_id WHERE person.reject=0 AND visit.door_id=?", new String[]{doorId.toString()});		
		switch(maxType) {
		case Visit.TYPE_FIRST_VISIT:	prefKey="color_visit"; break;
		case Visit.TYPE_RETURN_VISIT:	prefKey="color_return_visit"; break;
		case Visit.TYPE_STUDY:			prefKey="color_study"; break;
		}
		if(prefKey != "") {
			Integer prefColor = Integer.parseInt(prefs.getString(prefKey, "0"));
			if(prefColor != -1) {
				db.execSQL("UPDATE door SET color1=?,color2=? WHERE ROWID=?", new Object[] {prefColor, prefColor, doorId});
				if(context instanceof Door) {
					((TriangleView) ((Door)context).findViewById(R.id.title_color1)).setColor( context.getResources().getColor(COLORS[prefColor]) );
					((TriangleView) ((Door)context).findViewById(R.id.title_color2)).setColor( context.getResources().getColor(COLORS[prefColor]) );
				}
				return;
			}
		}
		
		db.execSQL("UPDATE door SET color1=0,color2=0 WHERE ROWID=?", new Object[] {doorId});
		if(context instanceof Door) {
			((TriangleView) ((Door)context).findViewById(R.id.title_color1)).setColor( context.getResources().getColor(COLORS[0]) );
			((TriangleView) ((Door)context).findViewById(R.id.title_color2)).setColor( context.getResources().getColor(COLORS[0]) );
		}
	}

	
	
	
	////////////////////////////////////
	
	private static class VisitItem {
		long id,personId;
		int type,brochures,magazines,books;
		Time date;
		String desc;
	}
	
	
	private static class DoorAdapter extends BaseAdapter {
		
        private LayoutInflater mInflater;
        ArrayList<VisitItem> mItems;
        Context mContext;
       
        public DoorAdapter(Context context, ArrayList<VisitItem> items) {
            mInflater = LayoutInflater.from(context);
            mItems = items;
            mContext = context;
        }

        public int getCount() {
            return mItems.size();
        }

        public Object getItem(int position) {
            return mItems.get(position); 
        }

        public long getItemId(int position) {
            return mItems.get(position).id;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

        	ViewHolder holder;
        	VisitItem item = mItems.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.visit_item, null);

                holder = new ViewHolder();
                
                holder.typeIcon = (ImageView) convertView.findViewById(R.id.visit_item_type_icon);
                holder.desc = (TextView) convertView.findViewById(R.id.visit_item_desc);
                holder.date = (TextView) convertView.findViewById(R.id.visit_item_date);
                holder.info = (TextView) convertView.findViewById(R.id.visit_item_info);

                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.typeIcon.setImageResource(Visit.TYPE_ICONS[item.type]);
            
            
            holder.desc.setText(item.desc);
            if(item.desc.length() == 0)
            	holder.desc.setVisibility(View.GONE);
            else 
            	holder.desc.setVisibility(View.VISIBLE);
            
            Date date = new Date(item.date.toMillis(true));
            holder.date.setText( DateFormat.getDateInstance(DateFormat.SHORT).format(date)+", "+DateFormat.getTimeInstance(DateFormat.SHORT).format(date));
            
            
            StringBuilder info = new StringBuilder();
            
            if(item.brochures > 0)
            	info.append(item.brochures+" "+Util.pluralForm(mContext, item.brochures, mContext.getResources().getStringArray(R.array.plural_brochures)));
            if(item.magazines > 0) {
            	if(info.length()>0) info.append(", ");
            	info.append(item.magazines+" "+Util.pluralForm(mContext, item.magazines, mContext.getResources().getStringArray(R.array.plural_magazines)));
            }
            if(item.books > 0) {
            	if(info.length()>0) info.append(", ");
            	info.append(item.books+" "+Util.pluralForm(mContext, item.books, mContext.getResources().getStringArray(R.array.plural_books)));
            }
            
            if(info.length() > 0) {
            	holder.info.setVisibility(View.VISIBLE);            
            	holder.info.setText(info.toString());
            }
            else
            	holder.info.setVisibility(View.GONE);
            
            if(item.type == Visit.TYPE_NA)
            	holder.date.setTextColor(0xffbbbbbb);
            else
            	holder.date.setTextColor(Color.BLACK);
            
            
            	
        	
            return convertView;
        }

        static class ViewHolder {
            TextView date,desc,info;
            ImageView typeIcon;
        }
        
    }
	

}
