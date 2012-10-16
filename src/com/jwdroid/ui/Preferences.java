package com.jwdroid.ui;

import com.jwdroid.R;
import com.jwdroid.R.string;
import com.jwdroid.R.xml;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.title_preferences);
		
		addPreferencesFromResource(R.xml.preferences);
	}
}
