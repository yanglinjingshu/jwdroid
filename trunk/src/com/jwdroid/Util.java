package com.jwdroid;

import java.util.ArrayList;

import android.app.Activity;
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
}
