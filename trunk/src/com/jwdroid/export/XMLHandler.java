package com.jwdroid.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {

	private String mTempVal;
	private String mElementName;
	private List<Map<String,String>> mItems;
	private Map<String,String> mItem;	
	
	public XMLHandler(String elementName) {
		mElementName = elementName;
	}
	
	public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        mTempVal = "";
        if (qName.equalsIgnoreCase("document")) {
        	mItems = new ArrayList<Map<String,String>>();            
        }
        if(qName.equalsIgnoreCase(mElementName)) {
        	mItem = new HashMap<String,String>();
        }
    }
 
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        mTempVal = new String(ch, start, length);
    }
 
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("document")) {
        	
        } else if(qName.equalsIgnoreCase(mElementName)) {
            mItems.add(mItem);
        } else {
            mItem.put(qName, mTempVal);
        }
    }
    
    public List<Map<String,String>> getItems() {
    	return mItems;
    }
}
