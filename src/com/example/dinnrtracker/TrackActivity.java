package com.example.dinnrtracker;

import java.util.HashMap;
import java.util.Map;

import com.firebase.client.Firebase;
import com.parse.ParseUser;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

public class TrackActivity extends Activity {
	
	//BUG:return button cause crush
	//BUG:crush if press home button
	
	private static final int GPS_TIME_INTERVAL = 1000*5; 	//GPS collect data every 5000ms
	private static final int GPS_DISTANCE= 1000;		//GPS collect data every 1000m
	private static final int HANDLER_DELAY = 1000*5;	//Collect data every 5000ms

	private LocationManager locationManager=null;  
	private LocationListener locationListener=null;   
	private double mLatitude;
	private double mLongitude;
	 	 
	private Boolean flag = false;  
  
	@Override  
	public void onCreate(Bundle savedInstanceState) {
		 
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.activity_track);  
		 		 
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);  
	}  
  
	@Override  
	public void onResume() {
		super.onResume();
		flag = displayGpsStatus();  
		if (flag) {  
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
			        public void run() {
			            obtainLocation();
			            // Read data and react to changes
			            /*ref.addValueEventListener(new ValueEventListener() {

			                @Override
			                public void onDataChange(DataSnapshot snap) {
			                    System.out.println(snap.getName() + " -> " + snap.getValue());
			                }

			                @Override public void onCancelled() { }
			            });*/
			            handler.postDelayed(this, HANDLER_DELAY);
			        }
			    }, HANDLER_DELAY);
		     
		} else {  
			alertbox("Gps Status!!", "Your GPS is: OFF");  
		}    
	}
	@Override
	public void onPause(){
		super.onPause();
        if(locationManager != null) {
        	locationManager.removeUpdates(locationListener);
        }
        locationListener = null;
        locationManager = null;
	}
	@Override
	public void onStop(){
		super.onStop();
        if(locationManager != null) {
        	locationManager.removeUpdates(locationListener);
        }
        locationListener = null;
        locationManager = null;
	}
	
	/*
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
	    ParseUser.logOut();
	    Intent loginIntent = new Intent(this, MainActivity.class);
	    startActivity(loginIntent);
	    return true;
    }*/
  
	/*----Method to Check GPS is enable or disable ----- */  
	private Boolean displayGpsStatus() {  
		ContentResolver contentResolver = getBaseContext()  
				.getContentResolver();  
		boolean gpsStatus = Settings.Secure  
				.isLocationProviderEnabled(contentResolver,   
						LocationManager.GPS_PROVIDER);  
		if (gpsStatus){  
			return true;  
		} else {  
			return false;  
		}  
	}  
  
	private void obtainLocation(){
		locationListener = new MyLocationListener();  
		locationManager.requestLocationUpdates(LocationManager  
				.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE,locationListener);  
	}
	
	/*----------Method to create an AlertBox ------------- */  
	protected void alertbox(String title, String mymessage) {  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		builder.setMessage("Your Device's GPS is Disable")  
		.setCancelable(false)  
		.setTitle("** Gps Status **")  
		.setPositiveButton("Gps On",  
				new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {
				// finish the current activity  
				// AlertBoxAdvance.this.finish();  
				Intent myIntent = new Intent(  
						Settings.ACTION_SECURITY_SETTINGS);  
				startActivity(myIntent);  
				dialog.cancel();  
			}  
		})  
		.setNegativeButton("Cancel",  
				new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {  
				// cancel the dialog box  
				dialog.cancel();  
			}  
		});  
		AlertDialog alert = builder.create();  
		alert.show();  
	}  
   
	/*----------Listener class to get coordinates ------------- */  
	private class MyLocationListener implements LocationListener {  
		@Override  
		public void onLocationChanged(Location loc) {
			mLatitude = loc.getLatitude();
			mLongitude = loc.getLongitude();
			Toast.makeText(getBaseContext(),"Location changed : Lat: " +  
					loc.getLatitude()+ " Lng: " + loc.getLongitude(),  
					Toast.LENGTH_SHORT).show();
			uploadData();
            //String longitude = "Longitude: " +loc.getLongitude();    
            //String latitude = "Latitude: " +loc.getLatitude();  
        }
		
		public void uploadData(){
            // Create a reference to a Firebase location
            Firebase ref = new Firebase("https://dinnrtracker.firebaseio.com/");

            // Write data to Firebase
            Map<String,Double> locationData = new HashMap<String,Double>();
            locationData.put("Latitude", mLatitude);
            locationData.put("Longitude", mLongitude);

            ref.setValue(locationData);
            return;
		}
  
		@Override  
		public void onProviderDisabled(String provider) {  
            // TODO Auto-generated method stub           
        }  
  
        @Override  
        public void onProviderEnabled(String provider) {  
            // TODO Auto-generated method stub           
        }  
  
        @Override  
        public void onStatusChanged(String provider,   
		int status, Bundle extras) {  
            // TODO Auto-generated method stub           
        }  
    }  
}  