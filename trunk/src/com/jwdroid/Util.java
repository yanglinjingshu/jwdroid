package com.jwdroid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.EditText;

public class Util {
	static public Object[] getViewValues(Activity activity, int[] ids)
	{
		ArrayList<Object> array = new ArrayList<Object>();
		for(int id : ids)
		{
			View view = activity.findViewById(id);
			if(view instanceof EditText) {				
				array.add( ( (EditText)view ).getText() );
			}
		}
		return array.toArray();
	}
	
	
	static public String pluralForm(Context context, int n, String form1, String form2, String form5)
	{
		String locale = context.getResources().getConfiguration().locale.getLanguage();
		if(locale.equals("ru")) {
		    n = Math.abs(n) % 100;
		    int n1 = n % 10;
		    if (n > 10 && n < 20) return form5;
		    if (n1 > 1 && n1 < 5) return form2;
		    if (n1 == 1) return form1;
		    return form5;
		}
		else {
			if(n == 1)
				return form1;
			return form2;
		}
	}
	
	static public String pluralForm(Context context, int n, String[] forms) {
		return pluralForm(context, n, forms[0], forms[1], forms[2]);
	}
	
    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }	
    
    public static Integer dbFetchInt(SQLiteDatabase db, String sql, String[] selectionArgs) {
    	Cursor rs = db.rawQuery(sql, selectionArgs);    	
    	if(!rs.moveToFirst())
    		return null;
    	int retVal = rs.getInt(0);
    	rs.close();
    	return retVal;
    }
    
    public static Long dbFetchLong(SQLiteDatabase db, String sql, String[] selectionArgs) {
    	Cursor rs = db.rawQuery(sql, selectionArgs);
    	if(!rs.moveToFirst())
    		return null;
    	long retVal = rs.getLong(0);
    	rs.close();
    	return retVal;
    }
    
    public static String dbFetchString(SQLiteDatabase db, String sql, String[] selectionArgs) {
    	Cursor rs = db.rawQuery(sql, selectionArgs);
    	if(!rs.moveToFirst())
    		return null;
    	String retVal = rs.getString(0);
    	rs.close();
    	return retVal;
    }
    
}
