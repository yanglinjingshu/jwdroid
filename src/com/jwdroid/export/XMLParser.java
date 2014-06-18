package com.jwdroid.export;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

public class XMLParser {
	
	public static List<Map<String,String>> parse(String elementName, InputStream is) {
		List<Map<String,String>> list = null;
        try {
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            XMLHandler saxHandler = new XMLHandler(elementName);
            xmlReader.setContentHandler(saxHandler);
            xmlReader.parse(new InputSource(is));
            list = saxHandler.getItems();
 
        } catch (Exception ex) {
            Log.d("XML", "XMLParser: parse() failed");
        }
 
        return list;
	}

}
