package com.phibyte.isitopen;

import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class VenueActivity extends Activity {
    
    JSONObject data;
    JSONArray hours;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue);
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        try {
            data = new JSONObject(getIntent().getStringExtra("data"));
            hours = data.getJSONArray("hours");
            ((TextView) findViewById(R.id.venue_title)).setText(data.getString("title"));
        } catch(JSONException e) {
            finish();
            return ;
        }
        
        ViewGroup table = (ViewGroup) findViewById(R.id.venue_table);
        
        for(int i=0; i<hours.length(); i++) {
            
            String day = "", hour = "";
            ViewGroup row = (ViewGroup) inflater.inflate(R.layout.venue_row, null);
            
            try {
                day = DateUtils.getDayOfWeekString(1 + ((i + 8) % 7), DateUtils.LENGTH_LONG);
                hour = hours.isNull(i)?"Closed":(hours.getJSONArray(i).getString(0) + " - " + hours.getJSONArray(i).getString(1));
            } catch(JSONException e) {}
            
            ((TextView) row.findViewById(R.id.venue_hours_day)).setText(day);
            ((TextView) row.findViewById(R.id.venue_hours_hour)).setText(hour);
            
            table.addView(row);
        }
        
    }
    
}
