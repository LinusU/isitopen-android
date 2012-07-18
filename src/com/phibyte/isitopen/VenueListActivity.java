package com.phibyte.isitopen;

import android.app.ListActivity;
import android.os.Bundle;

import android.text.format.Time;

import android.content.Intent;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.os.Build;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;

public class VenueListActivity extends ListActivity {
    
    static final String TAG = "Isitopen VenueList";
    
    protected JSONArray data;
    protected String type;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setListAdapter(new Adapter());
        
        new Fetcher().execute(getIntent().getStringExtra("venue_type"));
        
        setupClick();
    }
    
    protected void setupClick() {
        getListView().setOnItemClickListener(
            new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Intent i = new Intent(VenueListActivity.this, VenueActivity.class);
                        i.putExtra("data", data.getJSONObject(position).toString());
                        startActivity(i);
                    } catch(JSONException e) {
                        Log.e(TAG, "JSON Error", e);
                    }
                }
            }
        );
    }
    
    class Adapter extends BaseAdapter {
        
        public boolean hasStableIds() { return false; }
        public boolean areAllItemsEnabled() { return true; }
        public boolean isEnabled() { return true; }
        
        public int getViewTypeCount() { return 1; }
        public int getItemViewType() { return 0; }
        
        public int getCount() { return (data==null?0:data.length()); }
        public Object getItem(int position) { return data.optJSONObject(position); }
        public long getItemId(int position) { return (long) position; }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();
            ViewGroup v = null;
            
            int day = ( (int) now.getDay() + 5 ) % 7;
            
            try {
                
                JSONObject o = data.getJSONObject(position);
                
                if(convertView == null) {
                    v = (ViewGroup) inflater.inflate(R.layout.listitem, null);
                } else {
                    v = (ViewGroup) convertView;
                }
                
                JSONArray hours = o.getJSONArray("hours");
                JSONArray today = hours.getJSONArray(day);
                
                Date tOpen, tClose;
                boolean isOpen, soonClose;
                
                try {
                    tOpen = formatter.parse(today.getString(0));
                    tClose = formatter.parse(today.getString(1).equals("24:00:00")?"00:00:00":today.getString(1));
                    tOpen.setYear(now.getYear());
                    tOpen.setMonth(now.getMonth());
                    tOpen.setDate(now.getDate());
                    tClose.setYear(now.getYear());
                    tClose.setMonth(now.getMonth());
                    tClose.setDate(now.getDate());
                    if(!tClose.after(tOpen)) {
                        tClose.setTime(tClose.getTime() + 86400000);
                    }
                    isOpen = (tOpen.before(now) && tClose.after(now));
                    soonClose = (tClose.getTime() - now.getTime() < 3600000);
                } catch(java.text.ParseException e) {
                    isOpen = false; soonClose = false;
                } catch(JSONException e) {
                    isOpen = false; soonClose = false;
                }
                
                ( (TextView) v.findViewById(R.id.listitem_title) ).setText(o.getString("title"));
                ( (ImageView) v.findViewById(R.id.listitem_status) ).getDrawable().setLevel(isOpen?(soonClose?1:2):0);
                
            } catch(JSONException e) {
                Log.e(TAG, "Malformed JSON", e);
            }
            
            return (View) v;
        }
        
    }
    
    class Fetcher extends AsyncTask<String, Integer, JSONObject> {
        
        protected JSONObject doInBackground(String... types) {
            
            type = types[0];
            
            HttpClient httpclient = new DefaultHttpClient();
            HttpUriRequest request = new HttpGet("http://isitopen.se/data/vaesteraas/" + (type.equals("")?"":type+"/"));
            
            request.setHeader(
                "User-Agent",
                Build.BRAND + " " +
                Build.MODEL + " " +
                "(Android " + Build.VERSION.RELEASE + "; " +
                Build.DISPLAY + ", " +
                Build.DEVICE + ", " +
                Build.BOARD + ")"
            );
            
            String res = null;
            
            try {
                
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                
                if(entity != null) {
                    InputStream instream = entity.getContent();
                    res = stream_to_string(instream);
                    instream.close();
                }
                
            } catch(ClientProtocolException e) {
                Log.e(TAG, "Error fetching data", e);
            } catch(IOException e) {
                Log.e(TAG, "Error fetching data", e);
            }
            
            try {
                return new JSONObject(res);
            } catch(JSONException e) {
                return null;
            }
            
        }
        
        protected void onPostExecute(JSONObject result) {
            
            //findViewById(R.id.wait).setVisibility(View.GONE);
            
            if(result == null) {
                return ;
            }
            
            if(!result.has("data")) {
                return ;
            }
            
            try {
                data = result.getJSONArray("data");
                ((Adapter) getListAdapter()).notifyDataSetChanged();
            } catch(JSONException e) {
                Log.e(TAG, "Malformed JSON", e);
            }
            
        }
        
        private String stream_to_string(InputStream is) {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            
            try {
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading data", e);
            } finally {
                try {
                    is.close();
                } catch(IOException e) {
                    Log.e(TAG, "Error reading data", e);
                }
            }
            
            return sb.toString();
        }
        
    }
    
}
