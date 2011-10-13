package com.jwdroid;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


abstract public class SimpleArrayAdapter<T> extends BaseAdapter {
	
    protected LayoutInflater mInflater;
    protected ArrayList<T> mItems;
    protected Context mContext;
   
    public SimpleArrayAdapter(Context context, ArrayList<T> items) {
        mInflater = LayoutInflater.from(context);
        mItems = items;
        if(items == null)
        	mItems = new ArrayList<T>();
        mContext = context;        
    }

    public int getCount() {
        return mItems.size();
    }

    public T getItem(int position) {
        return mItems.get(position); 
    }
    
    public int getPositionById(long id) {
    	for (int i = 0; i < getCount(); i++)
        {
            if (getItemId(i) == id)
                return i;
        }
        return -1;
    }
    
    public T getItemById(long id) {
    	int pos = getPositionById(id);
    	if(pos == -1)
    		return null;
    	else
    		return getItem(pos);
    }
    
    public void swapData(ArrayList<T> data) {
    	mItems = data;
    	notifyDataSetChanged();
    }
}