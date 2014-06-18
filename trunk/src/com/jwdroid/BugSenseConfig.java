package com.jwdroid;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;

public class BugSenseConfig {
	
	final static public String APIKEY = "bdd93776";
	
	static public void initAndStartSession(Context context) {
		BugSenseHandler.initAndStartSession(context, APIKEY);
	}

}
