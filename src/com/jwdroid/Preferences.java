package com.jwdroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		setTitle("Настройки");
		
		addPreferencesFromResource(R.xml.preferences);
	}
}
