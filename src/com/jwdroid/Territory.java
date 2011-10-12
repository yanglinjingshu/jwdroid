package com.jwdroid;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.jwdroid.TriangleButton.TriangleButtonContextMenuInfo;

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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.TextUtils.TruncateAt;
import android.text.format.Time;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FrameLayout.LayoutParams;

public class Territory extends FragmentActivity implements LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String TAG = "JWGroupList";
	
	private static final int MENU_CHANGE_DOOR_NAME = Menu.FIRST + 1;
		
	private static final int DIALOG_ADD_SINGLE = 1;
	private static final int DIALOG_DELETE = 2;
	private static final int DIALOG_CHANGE_NAME = 3;
	private static final int DIALOG_COLOR = 4;
	private static final int DIALOG_ARRANGE = 5;
	private static final int DIALOG_SET_POSITION = 6;
	private static final int DIALOG_ADD_MULTIPLE = 7;
	private static final int DIALOG_OFFER_ARRANGE = 8;
	
	private static final int ID_PANEL_LISTVIEW = 100000;
	private static final int ID_PANEL_TABLE = 100001;
	
	private static final int DISPLAY_LIST = 1;
	private static final int DISPLAY_TABLE = 2;
	
	private static final int ARRANGE_DIR_UP = 1;
	private static final int ARRANGE_DIR_DOWN = 2;
	private static final int ARRANGE_DIR_RIGHT = 3;
	private static final int ARRANGE_DIR_LEFT = 4;	
	
	private Cursor mListCursor;	
	private AppDbOpenHelper mDbOpenHelper = new AppDbOpenHelper(this);
	private Long mTerritoryId;
	
	private HorizontalPanelsView mPanelsView;
	
	private int mRememberedActiveViewGroup = 0, mRememberedActiveViewGroupScroll = 0;
	private int mDisplayMode = DISPLAY_LIST;
	
	private ColorPicker mColorPicker;
	
	private Long mDialogItemId;
	
		/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    prefs.registerOnSharedPreferenceChangeListener(this);
	    
	    mColorPicker = new ColorPicker(this, 0, 0);
	    
	    if(savedInstanceState != null) {
	    	mRememberedActiveViewGroup = savedInstanceState.getInt("rememberedActiveViewGroup", 0);
	    	mRememberedActiveViewGroupScroll = savedInstanceState.getInt("rememberedActiveViewGroupScroll", 0);
	    	mDisplayMode = savedInstanceState.getInt("displayMode", DISPLAY_LIST);
	    }
	    
	    Cursor rs;
	    SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
	    
	    mTerritoryId = getIntent().getExtras().getLong("territory");
	    
	    setContentView(R.layout.territory);
	    
	    rs = db.rawQuery("SELECT name FROM territory WHERE ROWID=?", new String[] {mTerritoryId.toString()});
	    rs.moveToFirst();
	    ((TextView)findViewById(R.id.territory_name)).setText(rs.getString(0));
	  
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
	    
	    
	    ((LinearLayout)findViewById(R.id.territory)).addView(mPanelsView);
	    
	    
	    
	    final QuickAction addActions 	= new QuickAction(this);
		addActions.addActionItem(new ActionItem(getResources().getString(R.string.action_add_single_object), getResources().getDrawable(R.drawable.ac_single)));		
		addActions.addActionItem(new ActionItem(getResources().getString(R.string.action_add_many_objects), getResources().getDrawable(R.drawable.ac_grid)));
		addActions.animateTrack(false);	
    	
		
		addActions.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {				
			@Override
			public void onItemClick(int pos) {
				switch(pos) {
				case 0:	// Один						
					showDialog(DIALOG_ADD_SINGLE);
					break;
				case 1:	// Неколько						
					showDialog(DIALOG_ADD_MULTIPLE);
					break;	
				}
			}
		});
	    
	    ((Button)findViewById(R.id.title_btn_add)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				addActions.show(v);
			}
		});
	    
	    ((Button)findViewById(R.id.title_btn_display_mode)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mDisplayMode = mDisplayMode == DISPLAY_LIST ? DISPLAY_TABLE : DISPLAY_LIST;
				v.setBackgroundResource(mDisplayMode == DISPLAY_LIST ? R.drawable.title_btn_struct : R.drawable.title_btn_list);
				getSupportLoaderManager().getLoader(0).forceLoad();
			}
		});
	    findViewById(R.id.title_btn_display_mode).setBackgroundResource(mDisplayMode == DISPLAY_LIST ? R.drawable.title_btn_struct : R.drawable.title_btn_list);
	    
	    getSupportLoaderManager().restartLoader(0, null, this);
	    

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		
		outState.putInt("rememberedActiveViewGroup", mRememberedActiveViewGroup);
		
		if(mPanelsView.getViewGroupsCount() > 0 && mPanelsView.getViewGroupAt( mPanelsView.getActivePos() ).findViewById(ID_PANEL_LISTVIEW) != null)
			mRememberedActiveViewGroupScroll = ((ListView)mPanelsView.getViewGroupAt( mPanelsView.getActivePos()).findViewById(ID_PANEL_LISTVIEW)).getFirstVisiblePosition();
		outState.putInt("rememberedActiveViewGroupScroll", mRememberedActiveViewGroupScroll);
		
		outState.putInt("displayMode", mDisplayMode);
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {		
    	getMenuInflater().inflate(R.menu.territory, menu);
		return true;
	}	
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(R.id.menu_group_table, mDisplayMode == DISPLAY_TABLE);
    	return super.onPrepareOptionsMenu(menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {		
    	SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();	    
    	
	    switch (item.getItemId()) {
	    case R.id.menu_arrange:
	    	showDialog(DIALOG_ARRANGE);
	    	break;
	    	
	    case R.id.menu_shift_down:
	    	db.execSQL("UPDATE door SET row=row+1 WHERE territory_id=? AND group_id=?", new Object[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
	    	getSupportLoaderManager().getLoader(0).forceLoad();
	    	break;
	    case R.id.menu_shift_up:
	    	db.execSQL("UPDATE door SET row=row-1 WHERE territory_id=? AND group_id=?", new Object[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
	    	getSupportLoaderManager().getLoader(0).forceLoad();
	    	break;
	    case R.id.menu_shift_right:
	    	db.execSQL("UPDATE door SET col=col+1 WHERE territory_id=? AND group_id=?", new Object[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
	    	getSupportLoaderManager().getLoader(0).forceLoad();
	    	break;
	    case R.id.menu_shift_left:
	    	db.execSQL("UPDATE door SET col=col-1 WHERE territory_id=? AND group_id=?", new Object[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
	    	getSupportLoaderManager().getLoader(0).forceLoad();
	    	break;
	    	
	    case R.id.menu_info:
	    	Intent intent = new Intent(this, TerritoryInfo.class);
	    	intent.putExtra("territory", mTerritoryId);
	    	startActivity(intent);
	    	break;
	    	
	    case R.id.menu_preferences:
	    	intent = new Intent(this, Preferences.class);
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
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View dlgEditLayout = factory.inflate(R.layout.dlg_edit, null);
    	
    	switch(id) {
    	case DIALOG_ADD_SINGLE:   
    		 ((TextView)dlgEditLayout.findViewById(R.id.lbl_dlgedit_note)).setText(R.string.dlg_add_single_object_note);            
     		dialog = new AlertDialog.Builder(this)
     					.setTitle(R.string.msg_object_name)
     					.setView(dlgEditLayout)
     					.setPositiveButton(R.string.btn_ok, null)
     					.setNegativeButton(R.string.btn_cancel, null).create(); 
    		break;
    		
    	case DIALOG_DELETE:
    		dialog = new AlertDialog.Builder(this) 	
    				.setCancelable(true)
    				.setMessage(R.string.msg_delete_object)
    				.setPositiveButton(R.string.btn_ok, null)
    				.setNegativeButton(R.string.btn_cancel, null)
    				.create();
    		break;    		
    		
    	case DIALOG_CHANGE_NAME:
            ((TextView)dlgEditLayout.findViewById(R.id.lbl_dlgedit_note)).setText(R.string.dlg_add_single_object_note);            
    		dialog = new AlertDialog.Builder(this)
    					.setTitle(R.string.msg_object_name)
    					.setView(dlgEditLayout)
    					.setPositiveButton(R.string.btn_ok, null)
    					.setNegativeButton(R.string.btn_cancel, null).create();    		
    		
    		break;
    		
    		
    	case DIALOG_COLOR:
    		dialog = mColorPicker.getDialog();    		
    		break;
    		
    	case DIALOG_ARRANGE:
    		final View dlgArrangeLayout = factory.inflate(R.layout.dlg_arrange, null);
    		
    		ArrayAdapter adapter = new ArrayAdapter(this,    				
    			android.R.layout.simple_spinner_item, new SimpleArrayItem[] {
    				new SimpleArrayItem(ARRANGE_DIR_DOWN, getResources().getString(R.string.li_top_down)),
    				new SimpleArrayItem(ARRANGE_DIR_UP, getResources().getString(R.string.li_bottom_up))
    		});
    		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    		((Spinner)dlgArrangeLayout.findViewById(R.id.spinner_arrange_direction_vertical)).setAdapter(adapter);
    		
    		adapter = new ArrayAdapter(this,    				
        			android.R.layout.simple_spinner_item, new SimpleArrayItem[] {
        				new SimpleArrayItem(ARRANGE_DIR_RIGHT, getResources().getString(R.string.li_left_right)),
        				new SimpleArrayItem(ARRANGE_DIR_LEFT, getResources().getString(R.string.li_right_left))
        		});
        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	((Spinner)dlgArrangeLayout.findViewById(R.id.spinner_arrange_direction_horizontal)).setAdapter(adapter);
        		
    		dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.menu_territory_arrange)
				.setView(dlgArrangeLayout)
				.setPositiveButton(R.string.btn_ok, null)
				.setNegativeButton(R.string.btn_cancel, null).create(); 
    		break;
    		
    	case DIALOG_SET_POSITION: 
    		final View dlgSetPositionLayout = factory.inflate(R.layout.dlg_2edits, null);
    		((TextView)dlgSetPositionLayout.findViewById(R.id.lbl_left)).setText(R.string.lbl_set_row);
    		((TextView)dlgSetPositionLayout.findViewById(R.id.lbl_right)).setText(R.string.lbl_set_col);
    		dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.menu_set_position)
				.setView(dlgSetPositionLayout)
				.setPositiveButton(R.string.btn_ok, null)
				.setNegativeButton(R.string.btn_cancel, null).create(); 
    		break;
    		
    	case DIALOG_ADD_MULTIPLE: 
    		final View dlgAddMultipleLayout = factory.inflate(R.layout.dlg_2edits, null);
    		((TextView)dlgAddMultipleLayout.findViewById(R.id.lbl_left)).setText(R.string.lbl_numbers_from);
    		((TextView)dlgAddMultipleLayout.findViewById(R.id.lbl_right)).setText(R.string.lbl_numbers_to);
    		dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.action_add_many_objects)
				.setView(dlgAddMultipleLayout)
				.setPositiveButton(R.string.btn_ok, null)
				.setNegativeButton(R.string.btn_cancel, null).create(); 
    		break;
    		
    	case DIALOG_OFFER_ARRANGE:
    		dialog = new AlertDialog.Builder(this) 	
    				.setCancelable(true)
    				.setMessage(R.string.msg_arrange_after_add)
    				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {					
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						showDialog(DIALOG_ARRANGE);
    					}
    				})
    				.setNegativeButton(R.string.btn_no, null)
    				.create();
    		break;   
    	
    	}	 	
    	
    	return dialog;
    }
    
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {    	
    	super.onPrepareDialog(id, dialog);
    	
    	switch(id) {
	    	case DIALOG_ADD_SINGLE: {
	    		final AlertDialog editDialog = (AlertDialog)dialog;
	    		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();	    
	    		((EditText)editDialog.findViewById(R.id.edit_dlgedit_text)).setText("");
	    		editDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String name = ((EditText)editDialog.findViewById(R.id.edit_dlgedit_text)).getText().toString();
						SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
						
						Integer orderNum = Util.dbFetchInt(db,"SELECT MAX(order_num) FROM door WHERE territory_id=? AND group_id=?", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
						if(orderNum == null)
							orderNum = 0;
						else
							orderNum++;
						
						Integer row = Util.dbFetchInt(db,"SELECT MAX(row) FROM door WHERE territory_id=? AND group_id=?", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
						Integer col = 0;
						if(row == null)
							row = 0;
						else
							col = Util.dbFetchInt(db,"SELECT MAX(col)+1 FROM door WHERE territory_id=? AND group_id=? AND row=?", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos()), String.valueOf(row)});
			    		
						int cols = 6;				    		
			    		if(row>0) 
			    			cols = Util.dbFetchInt(db, "SELECT MAX(col)+1 FROM door WHERE territory_id=? AND group_id=?", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
			    		
			    		if(col >= cols) {
			    			col = 0;
			    			row++;
			    		}
			    		
			    		db.execSQL(	"INSERT INTO door (territory_id, group_id, order_num, col, row, name, color1, color2, visits_num, last_person_name, last_date, last_desc)" +
			    		"VALUES(?,?,?,?,?,?,0,0,0,null,null,null)", new Object[] {mTerritoryId, mPanelsView.getActivePos(), orderNum, col, row, name});
			    		getSupportLoaderManager().getLoader(0).forceLoad();	
					}
	    		});
	    		break;
	    	}
	    	case DIALOG_ADD_MULTIPLE: {
	    		final AlertDialog alertDialog = (AlertDialog)dialog;
	    		
	    		((EditText)dialog.findViewById(R.id.edit_left)).setText("");
	    		((EditText)dialog.findViewById(R.id.edit_right)).setText("");
	    		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							int from = Integer.parseInt(((EditText)alertDialog.findViewById(R.id.edit_left)).getText().toString());							
				    		int to = Integer.parseInt(((EditText)alertDialog.findViewById(R.id.edit_right)).getText().toString());
				    		
				    		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

				    		Integer maxRow = Util.dbFetchInt(db,"SELECT MAX(row) FROM door WHERE territory_id=? AND group_id=?", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
				    		Integer maxCol = 0;
				    		if(maxRow == null)
				    			maxRow = 0;
				    		else
				    			maxCol = Util.dbFetchInt(db,"SELECT MAX(col)+1 FROM door WHERE territory_id=? AND group_id=? AND row=?", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos()), String.valueOf(maxRow)});
				    		
				    		Integer orderNum = Util.dbFetchInt(db,"SELECT MAX(order_num) FROM door WHERE territory_id=? AND group_id=?", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
				    		if(orderNum == null)
				    			orderNum = 0;
				    		else
				    			orderNum++;
				    		
				    		int cols = 6;				    		
				    		if(maxRow>0) 
				    			cols = Util.dbFetchInt(db, "SELECT MAX(col)+1 FROM door WHERE territory_id=? AND group_id=?", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});			    			
				    		
				    		int col = maxCol;
				    		int row = maxRow;
				    		for(int i=from;i<=to;i++) {
				    			
				    			if(col >= cols) {
				    				col = 0;
				    				row++;
				    			}
				    			
				    			db.execSQL(	"INSERT INTO door (territory_id, group_id, order_num, col, row, name, color1, color2, visits_num, last_person_name, last_date, last_desc)" +
							    		"VALUES(?,?,?,?,?,?,0,0,0,null,null,null)", new Object[] {mTerritoryId, mPanelsView.getActivePos(), orderNum, col, row, String.valueOf(i)});
				    			
				    			orderNum++;		
				    			col++;
				    			
				    		}
				    						    		
				    		showDialog(DIALOG_OFFER_ARRANGE);
				    		getSupportLoaderManager().getLoader(0).forceLoad();
				    		
				    		
						}
						catch(Exception e) {
							Toast.makeText(Territory.this, R.string.msg_error, Toast.LENGTH_SHORT);
							Log.e(TAG, e.getMessage());
						}
					}	    			
	    		});
	    		break;
	    	}
	    	
	    	
	    	case DIALOG_DELETE: {		    	
		    	AlertDialog alertDialog = (AlertDialog)dialog;
		    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
							db.execSQL("DELETE FROM `door` WHERE rowid=?", new Long[] { mDialogItemId });
					  		db.execSQL("DELETE FROM `person` WHERE door_id=?", new Long[] { mDialogItemId });
					  		db.execSQL("DELETE FROM `visit` WHERE door_id=?", new Long[] { mDialogItemId });					  		
					  		Toast.makeText(Territory.this, R.string.msg_object_deleted, Toast.LENGTH_SHORT).show();			  		
					  		getSupportLoaderManager().getLoader(0).forceLoad();
						}
					});
	    		break;
	    	}
	    	case DIALOG_CHANGE_NAME: {
	    		final AlertDialog editDialog = (AlertDialog)dialog;
	    		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();	
	    		Cursor rs = db.rawQuery("SELECT name FROM door WHERE ROWID=?", new String[] {mDialogItemId.toString()});
	    		rs.moveToNext();
	    		((EditText)editDialog.findViewById(R.id.edit_dlgedit_text)).setText(rs.getString(0));
	    		editDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newName = ((EditText)editDialog.findViewById(R.id.edit_dlgedit_text)).getText().toString();
						SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
						db.execSQL("UPDATE `door` SET name=? WHERE ROWID=?", new Object[] { newName, mDialogItemId });
						
						if(mDisplayMode == DISPLAY_LIST) {
							ListView list = (ListView)mPanelsView.getActiveViewGroup().findViewById(ID_PANEL_LISTVIEW);
							TerritoryAdapter adapter = (TerritoryAdapter)list.getAdapter();
							((DoorItem)adapter.getItem(adapter.getPositionById(mDialogItemId))).name = newName;
							adapter.notifyDataSetChanged();					
						}
						if(mDisplayMode == DISPLAY_TABLE) {
							TableLayout table = (TableLayout)mPanelsView.getActiveViewGroup().findViewById(ID_PANEL_TABLE);
							TriangleButton btn = (TriangleButton)table.findViewWithTag(mDialogItemId);							
							btn.setText(newName);							
						}
						
					}
				});
	    		rs.close();
	    		break;
	    	}
    		
	    	case DIALOG_COLOR: {	    		
	    		SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();	
	    		Cursor rs = db.rawQuery("SELECT name,color1,color2 FROM door WHERE ROWID=?", new String[] {mDialogItemId.toString()});
	    	    rs.moveToFirst();
	    	    String name = rs.getString(0);
	    	    Integer color1 = rs.getInt(1);
	    	    Integer color2 = rs.getInt(2);
	    	    rs.close();
	    	    
	    	    mColorPicker.setColors(color1,color2);
	    		
	    		mColorPicker.setOkListener( new ColorPicker.OnOkListener() {
	    			@Override
	    			public void onOk(int newColor1, int newColor2) {
	    				SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
						db.execSQL("UPDATE `door` SET color1=?,color2=?,manual_color=1 WHERE ROWID=?", new Object[] { new Integer(newColor1), new Integer(newColor2), mDialogItemId });
						
						if(mDisplayMode == DISPLAY_LIST) {
							ListView list = (ListView)mPanelsView.getActiveViewGroup().findViewById(ID_PANEL_LISTVIEW);
							TerritoryAdapter adapter = (TerritoryAdapter)list.getAdapter();
							int pos = adapter.getPositionById(mDialogItemId);
							((DoorItem)adapter.getItem(pos)).color1 = newColor1;
							((DoorItem)adapter.getItem(pos)).color2 = newColor2;
							adapter.notifyDataSetChanged();						
						}
						if(mDisplayMode == DISPLAY_TABLE) {
							TableLayout table = (TableLayout)mPanelsView.getActiveViewGroup().findViewById(ID_PANEL_TABLE);
							TriangleButton btn = (TriangleButton)table.findViewWithTag(mDialogItemId);							
							btn.setBackgroundResource(Door.DRAWABLES[newColor1]);
							if(newColor1 == newColor2)
								btn.setColor(0);
							else {
								btn.setColor(table.getContext().getResources().getColor(Door.COLORS_LIGHT[newColor2]));
								btn.setColorStroke(table.getContext().getResources().getColor(Door.COLORS[newColor2]));		
							}
						}
	    			}
	    		});
	    		break;
	    	}
	    	
	    	case DIALOG_ARRANGE: {
	    		final AlertDialog alertDialog = (AlertDialog)dialog;
	    		((DialogArrangeLayout)alertDialog.findViewById(R.id.dlg_arrange)).prepare(mDbOpenHelper, mTerritoryId, mPanelsView.getActivePos());
	    		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Integer cols = 0;
						try {
							cols = Integer.parseInt(((EditText)alertDialog.findViewById(R.id.edit_arrange_cols)).getText().toString());
						}
						catch(Exception e) {
							Toast.makeText(Territory.this, R.string.err_invalid_input, Toast.LENGTH_SHORT).show();
							return;
						}
						if(cols == 0) {
							Toast.makeText(Territory.this, R.string.err_no_cols_value, Toast.LENGTH_SHORT).show();
							return;
						}
						
						int directionVertical = ( (SimpleArrayItem)  ((Spinner)alertDialog.findViewById(R.id.spinner_arrange_direction_vertical)).getSelectedItem() ).id;
						int directionHorizontal = ( (SimpleArrayItem)  ((Spinner)alertDialog.findViewById(R.id.spinner_arrange_direction_horizontal)).getSelectedItem() ).id;
						
						SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();						
						Cursor rs = db.rawQuery("SELECT ROWID FROM door WHERE territory_id=? AND group_id=? ORDER BY order_num ASC", new String[] {mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos())});
												
						Integer col = (directionHorizontal == ARRANGE_DIR_RIGHT ? 0 : cols-1), 
								row = (directionVertical == ARRANGE_DIR_DOWN ? 0 : (int)Math.ceil((float)rs.getCount()/cols)-1);
						
						while(rs.moveToNext()) {			
							db.execSQL("UPDATE door SET col=?,row=? WHERE ROWID=?", new Object[] {col,row,rs.getInt(0)});
							Log.i(TAG, ""+row+","+col);
							
							if(directionHorizontal == ARRANGE_DIR_RIGHT)	
								col++;
							else
								col--;
							
							if(col >= cols && directionHorizontal == ARRANGE_DIR_RIGHT ||
							   col < 0 && directionHorizontal == ARRANGE_DIR_LEFT) {
								if(directionVertical == ARRANGE_DIR_DOWN)	
									row++;
								else
									row--;
								
								if(directionHorizontal == ARRANGE_DIR_RIGHT)	
									col = 0;
								else
									col = cols-1;
							}
						}
						
						rs.close();
						
						getSupportLoaderManager().getLoader(0).forceLoad();
					}
				});
	    		break;
	    	}
	    	
	    	case DIALOG_SET_POSITION: {
	    		final AlertDialog alertDialog = (AlertDialog)dialog;
	    		
	    		final SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();						
				Cursor rs = db.rawQuery("SELECT row,col FROM door WHERE ROWID=?", new String[] {mDialogItemId.toString()});
				rs.moveToFirst();
				final int row = rs.getInt(0);
				final int col = rs.getInt(1);
				rs.close();
	    		
	    		((EditText)dialog.findViewById(R.id.edit_left)).setText(String.valueOf(row+1));
	    		((EditText)dialog.findViewById(R.id.edit_right)).setText(String.valueOf(col+1));
	    		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {						
						try {
							Integer newRow = Integer.parseInt(((EditText)alertDialog.findViewById(R.id.edit_left)).getText().toString());
							newRow--;
							Integer newCol = Integer.parseInt(((EditText)alertDialog.findViewById(R.id.edit_right)).getText().toString());
							newCol--;
							
							Cursor rs = db.rawQuery("SELECT ROWID FROM door WHERE territory_id=? AND group_id=? AND row=? AND col=?", new String[]{
									mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos()), newRow.toString(), newCol.toString() 
							});
							if(rs.moveToFirst())
								db.execSQL("UPDATE door SET row=?,col=? WHERE ROWID=?", new Object[] {row,col,rs.getLong(0)});				
							rs.close();
							db.execSQL("UPDATE door SET row=?,col=? WHERE ROWID=?", new Object[] {newRow,newCol,mDialogItemId});
							
							getSupportLoaderManager().getLoader(0).forceLoad();
						}
						catch(Exception e) {							
						}
					}
	    		});
	    		break;
	    	}
	    	
	    	
	    	
	    	
	    		
    	}
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		switch(v.getId()) {

		}
		
		if(v instanceof TriangleButton) {	// Кнопка жилого объекта
			getMenuInflater().inflate(R.menu.territory_item_table, menu);
			
		}
	}
    
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuInfo info = (ContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {		  		
		}
			
		if(info instanceof TriangleButtonContextMenuInfo) {
			SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
			Cursor rs;
			TriangleButton btn = (TriangleButton) ((TriangleButtonContextMenuInfo)info).getView();
			
			final Long id = (Long)btn.getTag();
			rs = db.rawQuery("SELECT row,col FROM door WHERE ROWID=?", new String[] {id.toString()});
			rs.moveToFirst();
			final Integer row[] = {rs.getInt(0),0};
			final Integer col[] = {rs.getInt(1),1};
			rs.close();
			
			Runnable doMove = new Runnable() {
				public void run() {
					SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
					Cursor rs = db.rawQuery("SELECT ROWID FROM door WHERE territory_id=? AND group_id=? AND row=? AND col=?", new String[]{
							mTerritoryId.toString(), String.valueOf(mPanelsView.getActivePos()), row[1].toString(), col[1].toString() 
					});
					if(rs.moveToFirst())
						db.execSQL("UPDATE door SET row=?,col=? WHERE ROWID=?", new Object[] {row[0],col[0],rs.getLong(0)});				
					rs.close();
					db.execSQL("UPDATE door SET row=?,col=? WHERE ROWID=?", new Object[] {row[1],col[1],id});	
				}				
			};
			
			switch(item.getItemId()) {
			
			case R.id.menu_shift_down:
				row[1] = row[0]+1;
				col[1] = col[0];
				doMove.run();			
				getSupportLoaderManager().getLoader(0).forceLoad();
				break;
			case R.id.menu_shift_up:
				row[1] = row[0]-1;
				col[1] = col[0];
				doMove.run();			
				getSupportLoaderManager().getLoader(0).forceLoad();
				break;
			case R.id.menu_shift_right:
				row[1] = row[0];
				col[1] = col[0]+1;
				doMove.run();			
				getSupportLoaderManager().getLoader(0).forceLoad();
				break;
			case R.id.menu_shift_left:
				row[1] = row[0];
				col[1] = col[0]-1;
				doMove.run();			
				getSupportLoaderManager().getLoader(0).forceLoad();
				break;
				
			case R.id.menu_set_position:
				mDialogItemId = id;
				showDialog(DIALOG_SET_POSITION);
				break;
			}			
			
		}
	  
		return true;
	  
	}

	@Override
	public Loader onCreateLoader(int arg0, Bundle arg1) {
		TerritoryLoader loader = new TerritoryLoader(this, mDbOpenHelper, mTerritoryId);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader loader, Cursor cursor) {
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(mPanelsView.getViewGroupsCount() > 0 && mPanelsView.getViewGroupAt( mPanelsView.getActivePos() ).findViewById(ID_PANEL_LISTVIEW) != null)
			mRememberedActiveViewGroupScroll = ((ListView)mPanelsView.getViewGroupAt( mPanelsView.getActivePos() ).findViewById(ID_PANEL_LISTVIEW)).getFirstVisiblePosition();
		
		Display display = getWindowManager().getDefaultDisplay();
		float density = getResources().getDisplayMetrics().density;
    	int width = display.getWidth();
    	
    	mPanelsView.removeViewGroups();
    	
    	HashMap<Integer,ArrayList<DoorItem>> items = new HashMap<Integer,ArrayList<DoorItem>>();
	    
		while(cursor.moveToNext()) {
	    	
			DoorItem item = new DoorItem();
	    	
			item.id = cursor.getLong(0);			
			item.groupId = cursor.getInt(1);			
			item.orderNum = cursor.getInt(2);
			item.col = cursor.getInt(3);
			item.row = cursor.getInt(4);
			item.name = cursor.getString(5);
    		item.color1 = cursor.getInt(6);
    		item.color2 = cursor.getInt(7);
    		item.visitsNum = cursor.getInt(8);
    		item.lastPersonName = cursor.getString(9);
    		item.lastDate = new Time();
    		item.lastDate.set(cursor.getLong(10)*1000);    		
    		item.lastDesc = cursor.getString(11);
    		item.lastPersonReject = cursor.getInt(12);
    		
    		if(!items.containsKey(item.groupId))
    			items.put(item.groupId, new ArrayList<DoorItem>());
    		
    		items.get(item.groupId).add(item);
	    	
    		LinearLayout curGroup = (LinearLayout)mPanelsView.getViewGroupAt(item.groupId);
    		
    		
	    	if(curGroup == null) {
	    		
	    		curGroup = new LinearLayout(this);
		    	curGroup.setLayoutParams(new LayoutParams(width, LayoutParams.WRAP_CONTENT));
		    	curGroup.setOrientation(LinearLayout.VERTICAL);
		    	
		    	if(mDisplayMode == DISPLAY_LIST) {
			    	
			    	final ListView listView = new ListView(this);
			    	
			    	listView.setLayoutParams(new LayoutParams(width, LayoutParams.WRAP_CONTENT));		
			    	listView.setId(ID_PANEL_LISTVIEW);
			    	listView.setBackgroundColor(Color.WHITE);		
			    	listView.setDivider(new ColorDrawable(0xFFCCCCCC));
			    	listView.setDividerHeight(1);
	
			    	curGroup.addView(listView);
			    	
			    	final QuickAction listActions 	= new QuickAction(this);
					listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_change_name), getResources().getDrawable(R.drawable.ac_pencil)));
					listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_change_color), getResources().getDrawable(R.drawable.ac_color)));
					listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_move_up), getResources().getDrawable(R.drawable.ac_up)));
					listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_move_down), getResources().getDrawable(R.drawable.ac_down)));				
					listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_delete), getResources().getDrawable(R.drawable.ac_trash)));
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
							Bundle args;
							SQLiteDatabase db;
							Cursor rs;
							switch(pos) {
							case 0:	// Заголовок						
								mDialogItemId = listActions.getId();
						  		showDialog(DIALOG_CHANGE_NAME);
								break;		
							case 1:	// Цвет						
								mDialogItemId = listActions.getId();
						  		showDialog(DIALOG_COLOR);
								break;	
							case 2: // Выше			
								long id = listActions.getId();
								db = mDbOpenHelper.getWritableDatabase();
								rs = db.rawQuery("SELECT group_id,order_num FROM door WHERE ROWID=?", new String[] {String.valueOf(id)});
								rs.moveToFirst();
								int groupId = rs.getInt(0);
								int orderNum = rs.getInt(1);
								if(orderNum > 1) {
									rs = db.rawQuery("SELECT ROWID FROM door WHERE territory_id=? AND group_id=? AND order_num=?", new String[] {mTerritoryId.toString(), String.valueOf(groupId), String.valueOf(orderNum-1)});									
									if(rs.getCount() > 0) {
										rs.moveToFirst();
										long otherId = rs.getLong(0);									
										db.execSQL("UPDATE door SET order_num=? WHERE ROWID=?", new Object[] {String.valueOf(orderNum), String.valueOf(otherId)});
									}
									db.execSQL("UPDATE door SET order_num=? WHERE ROWID=?", new Object[] {String.valueOf(orderNum-1), String.valueOf(id)});
									getSupportLoaderManager().getLoader(0).forceLoad();
								}
								rs.close();
								break;
							case 3: // Ниже								
								db = mDbOpenHelper.getWritableDatabase();
								rs = db.rawQuery("SELECT group_id,order_num FROM door WHERE ROWID=?", new String[] {String.valueOf(listActions.getId())});
								rs.moveToFirst();
								groupId = rs.getInt(0);
								orderNum = rs.getInt(1);
								rs = db.rawQuery("SELECT ROWID FROM door WHERE territory_id=? AND group_id=? AND order_num>? ORDER BY order_num ASC LIMIT 1", new String[] {mTerritoryId.toString(), String.valueOf(groupId), String.valueOf(orderNum)});
								if(rs.getCount() > 0) {
									rs.moveToFirst();
									long otherId = rs.getLong(0);
									db.execSQL("UPDATE door SET order_num=? WHERE ROWID=?", new Object[] {String.valueOf(orderNum+1), String.valueOf(listActions.getId())});									
									db.execSQL("UPDATE door SET order_num=? WHERE ROWID=?", new Object[] {String.valueOf(orderNum), String.valueOf(otherId)});
								}
								getSupportLoaderManager().getLoader(0).forceLoad();
								rs.close();
								break;
							case 4:	// Удалить						
								mDialogItemId = listActions.getId();
						  		showDialog(DIALOG_DELETE);
								break;	
							}
						}
					});
			    	
			    	View v = new View(this);
			    	v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1));	    	
			    	v.setBackgroundColor(0xFFCCCCCC);
			    	curGroup.addView(v);
			    	
			    	listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
			    		@Override
			    		public void onItemClick(AdapterView<?> parent, View view,
			    				int position, long id) {
			    			
			    			Intent intent = new Intent(Territory.this, Door.class);
				    		intent.putExtra("territory", mTerritoryId);
				    		intent.putExtra("door", id);
				    		startActivityForResult(intent,1);	
				    				    			
			    		}
					});   	
			    	
		    	}
		    	
		    	else if(mDisplayMode == DISPLAY_TABLE) {
		    		ScrollView scroll = new ScrollView(this);
		    		curGroup.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		    		
		    		LinearLayout cont = new LinearLayout(this);
		    		scroll.addView(cont, new ScrollView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		    		cont.setGravity(Gravity.CENTER_HORIZONTAL);
		    		
		    		TableLayout table = new TableLayout(this);
		    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		    		//lp.gravity = Gravity.CENTER_HORIZONTAL;
		    		lp.topMargin = (int)(10 * density);
		    		table.setLayoutParams(lp);
		    		table.setId(ID_PANEL_TABLE);
		    		
		    		cont.addView(table);
		    		
		    		
		    	}
		    	
		    	
		    	mPanelsView.addViewGroup(curGroup);
    		}
	    	
    		
    		
        }
		
		
		if(mDisplayMode == DISPLAY_LIST)
			for(int i=0;i<mPanelsView.getViewGroupsCount();i++) 
				((ListView)mPanelsView.getViewGroupAt(i).findViewById(ID_PANEL_LISTVIEW)).setAdapter( new TerritoryAdapter(this, items.get(i), sharedPref) );
		
		if(mDisplayMode == DISPLAY_TABLE) {
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int buttonSize = Integer.parseInt(prefs.getString("table_button_size", "2"));
			
			for(int iPanel=0;iPanel<mPanelsView.getViewGroupsCount();iPanel++) {
				TableLayout table = (TableLayout)mPanelsView.getViewGroupAt(iPanel).findViewById(ID_PANEL_TABLE);
				
				HashMap<Integer,HashMap<Integer,DoorItem>> tableContent = new HashMap<Integer,HashMap<Integer,DoorItem>>();
				
				int rowsFrom = 0, rowsTo = 0, colsFrom = 0, colsTo = 0;
				for(Iterator<DoorItem> iter = items.get(iPanel).iterator(); iter.hasNext();) {
					DoorItem i = iter.next();
					if(!tableContent.containsKey(i.row))
						tableContent.put(i.row, new HashMap<Integer,DoorItem>());
					tableContent.get(i.row).put(i.col, i);
					
					if(rowsTo < i.row)		rowsTo =  i.row;
					if(rowsFrom > i.row)	rowsFrom = i.row;
					if(colsTo < i.col)		colsTo = i.col;
					if(colsFrom > i.col)	colsFrom = i.col;
				}
				
				for(int row = rowsFrom; row<=rowsTo; row++) {
					TableRow rowView = new TableRow(this);
					rowView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
					table.addView(rowView);
										
					for(int col = colsFrom; col<=colsTo; col++) {
						
						if(!tableContent.containsKey(row) || !tableContent.get(row).containsKey(col)) {
							View v = new View(this);
							TableRow.LayoutParams 	lp = new TableRow.LayoutParams((int)(45*density), (int)(45*density));
							if(buttonSize == 1)		lp = new TableRow.LayoutParams((int)(35*density), (int)(35*density));
							if(buttonSize == 3)		lp = new TableRow.LayoutParams((int)(55*density), (int)(55*density));
							v.setLayoutParams(lp);
							rowView.addView(v);
							continue;
						}
						DoorItem item = tableContent.get(row).get(col);
						
						
												
						TriangleButton btn = new TriangleButton(this);
						btn.setTag((Long)item.id);
						TableRow.LayoutParams 	lp = new TableRow.LayoutParams((int)(45*density), (int)(45*density));
						if(buttonSize == 1)		lp = new TableRow.LayoutParams((int)(35*density), (int)(35*density));
						if(buttonSize == 3)		lp = new TableRow.LayoutParams((int)(55*density), (int)(55*density));
						lp.setMargins((int)(3*density), (int)(3*density), (int)(3*density), (int)(3*density));
						btn.setLayoutParams(lp);
						btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
						btn.setTextColor(Color.WHITE);
						btn.setTypeface(Typeface.create((String)null, Typeface.BOLD));
						btn.setShadowLayer(1, 1, 1, Color.BLACK);
						btn.setText(item.name);
						btn.setEllipsize(TruncateAt.END);
						//btn.setHorizontallyScrolling(true);
						btn.setBackgroundResource(Door.DRAWABLES[item.color1]);	
						if(item.color1 != item.color2) {
							btn.setColor(getResources().getColor(Door.COLORS_LIGHT[item.color2]));
							btn.setColorStroke(getResources().getColor(Door.COLORS[item.color2]));
						}
						
						final QuickAction listActions 	= new QuickAction(this);
						listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_change_name), getResources().getDrawable(R.drawable.ac_pencil)));
						listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_change_color), getResources().getDrawable(R.drawable.ac_color)));
						listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_move), getResources().getDrawable(R.drawable.ac_move)));			
						listActions.addActionItem(new ActionItem(getResources().getString(R.string.action_object_delete), getResources().getDrawable(R.drawable.ac_trash)));
						listActions.animateTrack(false);
						listActions.setAnimStyle(QuickAction.ANIM_MOVE_FROM_RIGHT);			
				    	
						btn.setOnLongClickListener(new View.OnLongClickListener() {

							@Override
							public boolean onLongClick(View v) {
								listActions.show(v, (Long)v.getTag());
								return false;
							}
						}); 	
						
						listActions.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {				
							@Override
							public void onItemClick(int pos) {
								Bundle args;
								switch(pos) {
								case 0:	// Заголовок						
									mDialogItemId = listActions.getId();
							  		showDialog(DIALOG_CHANGE_NAME);
									break;		
								case 1:	// Цвет						
									mDialogItemId = listActions.getId();
							  		showDialog(DIALOG_COLOR);
									break;	
								case 2:	// Переместить
									registerForContextMenu(listActions.getView());
									listActions.getView().showContextMenu();
									unregisterForContextMenu(listActions.getView());
									break;
								case 3:	// Удалить						
									mDialogItemId = listActions.getId();
							  		showDialog(DIALOG_DELETE);
									break;	
								}
							}
						});
						
						btn.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(Territory.this, Door.class);
					    		intent.putExtra("territory", mTerritoryId);
					    		intent.putExtra("door", (Long)v.getTag());
					    		startActivityForResult(intent,1);								
							}
						});
						
						rowView.addView(btn);
						
					}
				}
			}
		}
		
		
		// Последний экран заглушки    	
	    
		LinearLayout curGroupScroll = new LinearLayout(this);
    	curGroupScroll.setLayoutParams(new LayoutParams(width, LayoutParams.WRAP_CONTENT));
    	curGroupScroll.setGravity(Gravity.CENTER);
    	curGroupScroll.addView(View.inflate(this, R.layout.group_empty, null));
    	curGroupScroll.setBackgroundColor(Color.parseColor("#eeeeee"));
    	mPanelsView.addViewGroup(curGroupScroll);
		
		
		mPanelsView.setActiveViewGroup(mRememberedActiveViewGroup);
		mRememberedActiveViewGroup = mPanelsView.getActivePos();		
		
		if(mPanelsView.getViewGroupsCount() > 0 && mPanelsView.getViewGroupAt( mPanelsView.getActivePos() ).findViewById(ID_PANEL_LISTVIEW) != null)
			((ListView)mPanelsView.getViewGroupAt(mRememberedActiveViewGroup).findViewById(ID_PANEL_LISTVIEW)).setSelection(mRememberedActiveViewGroupScroll);				
		
		
	}

	@Override
	public void onLoaderReset(Loader arg0) {
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		getSupportLoaderManager().getLoader(0).forceLoad();
	}
	

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if(key.equals("table_button_size") && mDisplayMode == DISPLAY_TABLE) {
			getSupportLoaderManager().getLoader(0).forceLoad();
		}
		
	}
	
	
	
	private static class DoorItem {
		long id;
		int groupId,orderNum,col,row;
		String name;
		int color1,color2,visitsNum;
		String lastPersonName,lastDesc;
		int lastPersonReject;
		Time lastDate;
	}
	
	
	private static class TerritoryAdapter extends SimpleArrayAdapter<DoorItem> implements SharedPreferences.OnSharedPreferenceChangeListener {
		        
        private int mTextRowsCnt = 0;
        
        public TerritoryAdapter(Context context, ArrayList<DoorItem> items, SharedPreferences prefs) {
           super(context,items);
           
           mTextRowsCnt = Integer.parseInt(prefs.getString("list_text_rows", "2"));
           prefs.registerOnSharedPreferenceChangeListener(this);
           
        }
        

        public long getItemId(int position) {
            return mItems.get(position).id;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

        	ViewHolder holder;
        	DoorItem item = mItems.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.door_item_list, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.door_item_name);
                holder.num_visits = (TextView) convertView.findViewById(R.id.door_item_num_visits);
                holder.desc = (TextView) convertView.findViewById(R.id.door_item_desc);
                holder.color1 = convertView.findViewById(R.id.door_item_color1);
                holder.color2 = convertView.findViewById(R.id.door_item_color2);
                                
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.name.setText(item.name);
        	
        	if(item.visitsNum > 0) {
        		holder.num_visits.setText(item.visitsNum+" "+Util.pluralForm(mContext, item.visitsNum, mContext.getResources().getStringArray(R.array.plural_visits)));
        		String snippet = "<s><b>"+DateFormat.getDateInstance(DateFormat.SHORT).format( new Date(item.lastDate.toMillis(true)) );
        		if(item.lastPersonName != null && item.lastPersonName.length() > 0)
        			snippet += ", "+item.lastPersonName;
        		snippet += "</b></s>: "+item.lastDesc;
        		holder.desc.setText( Html.fromHtml(snippet) );        		
        		holder.desc.setTextColor( item.lastPersonReject == 1 ? 0xFFAAAAAA : 0xFF000000 );
        	}
        	else {
        		holder.num_visits.setText("");
        		holder.desc.setText("");
        	}
        	
        	holder.desc.setLines(mTextRowsCnt);
        	
        	
        	
        	holder.color1.setBackgroundColor( mContext.getResources().getColor(Door.COLORS[item.color1]) );
        	holder.color2.setBackgroundColor( mContext.getResources().getColor(Door.COLORS[item.color2]) );
        	        	
            return convertView;
        }

        static class ViewHolder {
            TextView name,num_visits;
            TextView desc;
            View color1,color2;
        }

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if(key.equals("list_text_rows")) {
				mTextRowsCnt = Integer.parseInt(sharedPreferences.getString("list_text_rows", "2"));
				notifyDataSetChanged();
			}
			
		}
        
    } 
	
	
	
	

	
	
	
	
	static public class TerritoryLoader extends AsyncLoader<Cursor>  {
		
		private AppDbOpenHelper mDbOpenHelper;
		private Long mTerritoryId;

		public TerritoryLoader(Context context, AppDbOpenHelper db, Long territoryId) {
			super(context);			
			mDbOpenHelper = db;
			mTerritoryId = territoryId;
		}		

		@Override
		public Cursor loadInBackground() {
			
			SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
			Cursor rs = db.rawQuery("SELECT ROWID,group_id,order_num,col,row,name,color1,color2,visits_num,last_person_name,strftime('%s',last_date),last_desc,last_person_reject FROM door WHERE territory_id=? ORDER BY group_id, order_num ASC", new String[] {mTerritoryId.toString()});
			
			return rs;
		}
	}
	
	
	
	
	

}
