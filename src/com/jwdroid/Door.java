package com.jwdroid;

import java.util.ArrayList;
import java.util.HashMap;

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
import android.text.format.Time;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
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
	    
	    rs = db.rawQuery("SELECT name FROM territory WHERE ROWID=?", new String[] {mTerritoryId.toString()});
	    rs.moveToFirst();	    
	    ((TextView)findViewById(R.id.territory_name)).setText(rs.getString(0));
	    rs.close();
	  
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
		getMenuInflater().inflate(R.menu.door, menu);
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
    protected Dialog onCreateDialog(int id) {    	
    	Dialog dialog=null;
    	AlertDialog.Builder builder;
		LayoutInflater factory = LayoutInflater.from(this);
    	switch(id) {
    	case DIALOG_EDIT_PERSON:
    		
            final View view = factory.inflate(R.layout.dlg_edit, null);
            ((TextView)view.findViewById(R.id.lbl_dlgedit_note)).setVisibility(View.GONE);
            
    		dialog = new AlertDialog.Builder(this)
    					.setTitle("Введите описание жильца:")
    					.setView(view)
    					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {

								int error = 0;
								
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
					.setNegativeButton("Отмена", null).create();    		
    		break;
    		
    	case DIALOG_DELETE:
    		builder = new AlertDialog.Builder(this);    	
    		builder.setCancelable(true)
    			   .setMessage("Вы действительно хотите удалить это посещение?")
    			   .setPositiveButton(R.string.btn_ok, null)
    			   .setNegativeButton(R.string.btn_cancel, null);
    		dialog = builder.create();
    		break;
    		
    	case DIALOG_DELETE_PERSON:
    		builder = new AlertDialog.Builder(this);    	
    		builder.setCancelable(true)
    			   .setMessage("Вы действительно хотите удалить жильца и все его посещения?")
    			   .setPositiveButton(R.string.btn_ok, null)
    			   .setNegativeButton(R.string.btn_cancel, null);
    		dialog = builder.create();
    		break;
    		
    	case DIALOG_COLOR:
    		
    		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
    	    Cursor rs = db.rawQuery("SELECT name,color1,color2 FROM door WHERE ROWID=?", new String[] {mDoorId.toString()});
    	    rs.moveToFirst();
    	    String name = rs.getString(0);
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
    		break;
    		
    	case DIALOG_DELETE:    		    	
	    	AlertDialog alertDialog = (AlertDialog)dialog;
	    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
				  		db.execSQL("DELETE FROM `visit` WHERE rowid=?", new Long[] { mDialogItemId });
				  		updateVisits(Door.this, mDoorId);
				  		Toast.makeText(Door.this, "Посещение удалено", Toast.LENGTH_SHORT).show();			  		
				  		updateContent();
					}
				});
	    	alertDialog.setButton(alertDialog.BUTTON_NEGATIVE, null, new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int which) {
						   dialog.cancel();
					}
				});
	    	break;
	    	
    	case DIALOG_DELETE_PERSON:   
    		if(mPersonIds.containsKey(mPanelsView.getActivePos())) {
	    	
    			final Long personId = mPersonIds.get(mPanelsView.getActivePos());
		    	alertDialog = (AlertDialog)dialog;
		    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
					  		db.execSQL("DELETE FROM `visit` WHERE person_id=?", new Long[] { personId });
					  		db.execSQL("DELETE FROM `person` WHERE ROWID=?", new Long[] { personId });
					  		updateVisits(Door.this, mDoorId);
					  		Toast.makeText(Door.this, "Жилец удален", Toast.LENGTH_SHORT).show();			  		
					  		updateContent();
						}
					});
    		}
	    	break;
    	}
    	
    }
    
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		switch(v.getId()) {
			case ID_PANEL_LISTVIEW:
				menu.setHeaderTitle("Посещение");
				menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.menu_delete);				
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
	    	if(reject == 1) {
	    		//personNameView.setPaintFlags(personNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
	    		personNameView.setTextColor(0xFFAAAAAA);
	    	}

	    	personNameView.setTypeface(Typeface.create((String)null, Typeface.BOLD));	    	
	    	personNameView.setBackgroundResource(R.drawable.btn_person);
	    	personNameView.setGravity(Gravity.CENTER);
	    					
			final QuickAction personActions 	= new QuickAction(this);
			personActions.addActionItem(new ActionItem("Изменить", getResources().getDrawable(R.drawable.ac_pencil)));
			personActions.addActionItem(new ActionItem("Удалить", getResources().getDrawable(R.drawable.ac_trash)));
			personActions.addActionItem(new ActionItem(reject == 0 ? "Отказ" : "Не отказ", getResources().getDrawable(R.drawable.ac_cancel)));			
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
						
						if(reject == 0) {
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Door.this);
							Integer color = Integer.parseInt(prefs.getString("color_reject", "4"));
							int manualColor = Util.dbFetchInt(db, "SELECT manual_color FROM door WHERE ROWID=?", new String[] {mDoorId.toString()});
							if(prefs.getBoolean("autoset_color", true) && color != -1 && manualColor == 0) {
								db.execSQL("UPDATE door SET color1=?,color2=? WHERE ROWID=?", new Object[] {color, color, mDoorId});
								((TriangleView)findViewById(R.id.title_color1)).setColor( getResources().getColor(COLORS[color]) );
							    ((TriangleView)findViewById(R.id.title_color2)).setColor( getResources().getColor(COLORS[color]) );							
							}
						}

						
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
			listActions.addActionItem(new ActionItem("Удалить", getResources().getDrawable(R.drawable.ac_trash)));		
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
	    
		LinearLayout curPerson = new LinearLayout(this);
    	curPerson.setLayoutParams(new LayoutParams(width, LayoutParams.WRAP_CONTENT));
    	curPerson.setGravity(Gravity.CENTER);
    	curPerson.addView(View.inflate(this, R.layout.door_empty, null));
    	mPanelsView.addViewGroup(curPerson);
		
    	
    	
    	
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
		Cursor rs = db.rawQuery("SELECT date,desc,person.name,person.reject FROM visit LEFT JOIN person ON person.ROWID=visit.person_id WHERE visit.door_id=? ORDER BY date DESC LIMIT 1", new String[] {doorId.toString()});
		if(rs.getCount() == 0) {
			db.execSQL("UPDATE door SET last_date=NULL,last_desc=NULL,last_person_name=NULL,last_person_reject=NULL WHERE ROWID=?", new Object[] {doorId});
		}
		else {
			rs.moveToNext();
			db.execSQL("UPDATE door SET last_date=?,last_desc=?,last_person_name=?,last_person_reject=? WHERE ROWID=?", new Object[] {rs.getString(0),rs.getString(1),rs.getString(2),rs.getString(3),doorId});			
		}
		rs.close();
		
		Long territoryId = Util.dbFetchLong(db, "SELECT territory_id FROM door WHERE ROWID=?", new String[] {doorId.toString()});
		
		
		db.execSQL("UPDATE door SET visits_num=(SELECT COUNT(*) FROM visit WHERE door_id=?) WHERE ROWID=?", new Object[] {doorId,doorId});
		db.execSQL("UPDATE territory SET modified=(SELECT date FROM visit WHERE territory_id=territory.ROWID ORDER BY date DESC LIMIT 1) WHERE ROWID=?", new Object[] {territoryId});
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
       
        public DoorAdapter(Context context, ArrayList<VisitItem> items) {
            mInflater = LayoutInflater.from(context);
            mItems = items;
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
            holder.date.setText(item.date.format("%d.%m.%y в %H:%M"));
            
            
            StringBuilder info = new StringBuilder();
            if(item.brochures > 0)
            	info.append(item.brochures+" "+Util.pluralForm(item.brochures, "брошюра", "брошюры", "брошюр"));
            if(item.magazines > 0) {
            	if(info.length()>0) info.append(", ");
            	info.append(item.magazines+" "+Util.pluralForm(item.magazines, "журнал", "журнала", "журналов"));
            }
            if(item.books > 0) {
            	if(info.length()>0) info.append(", ");
            	info.append(item.books+" "+Util.pluralForm(item.books, "книга", "книги", "книг"));
            }
            
            if(info.length() > 0) {
            	holder.info.setVisibility(View.VISIBLE);            
            	holder.info.setText(info.toString());
            }
            else
            	holder.info.setVisibility(View.GONE);
        	
            return convertView;
        }

        static class ViewHolder {
            TextView date,desc,info;
            ImageView typeIcon;
        }
        
    }
	

}
