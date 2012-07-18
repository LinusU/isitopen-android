package com.phibyte.isitopen;

import com.phibyte.isitopen.VenueListActivity;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.content.Intent;

public class IsitopenActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    }
    
    public void venue(String venueType) {
        Intent intent = new Intent(this, VenueListActivity.class);
        intent.putExtra("venue_type", venueType);
        startActivity(intent);
    }
    
    public void venueRestaurant(View view) { venue("restaurant"); }
    public void venueConvenience(View view) { venue("convenience"); }
    public void venuePharmacy(View view) { venue("pharmacy"); }
    public void venueCafe(View view) { venue("cafe"); }
    public void venueClothing(View view) { venue("clothing"); }
    public void venueRetail(View view) { venue("retail"); }
    public void venueBank(View view) { venue("bank"); }
    
}
