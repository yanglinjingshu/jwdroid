package com.jwdroid;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class Help extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.help);
        WebView mWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setDefaultTextEncodingName("utf-8");

        String locale = getResources().getConfiguration().locale.getLanguage();
		if(locale.equals("ru")) 
			mWebView.loadUrl("http://jwdroid.googlecode.com/svn/trunk/help-ru.html");
		else if(locale.equals("es"))
			mWebView.loadUrl("http://jwdroid.googlecode.com/svn/trunk/help-es.html");
    else
			mWebView.loadUrl("http://jwdroid.googlecode.com/svn/trunk/help-en.html");


	}
}
