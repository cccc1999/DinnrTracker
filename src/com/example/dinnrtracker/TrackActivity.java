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
import android.os.PowerManager;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class TrackActivity extends Activity {
	
	//BUG:return button cause crush
	//BUG:crush if press home button
	
	private static final int GPS_TIME_INTERVAL = 1000*5; 	//GPS collect data every 5000ms
	private static final int GPS_DISTANCE= 1000;		//GPS collect data every 1000m
	private static final int HANDLER_DELAY = 1000*5;	//Upload data every 5000ms

	private LocationManager locationManager=null;  
	private LocationListener locationListener=null;   
	private double mLatitude;
	private double mLongitude;
	
	private Switch mSwitch;
	private TextView Status;
	
    private enum State {
        INIT,RESUME
    };
    private State mCurrentState;
	
	private PowerManager mPowerManager = null;
	private PowerManager.WakeLock mWakeLock = null;
	
	private Handler mHandler; 
	private Runnable mRunnable = new Runnable() {
	    @Override
	    public void run() {
	    	uploadData();
            mHandler.postDelayed(this, HANDLER_DELAY);
	    }
	};
		 	 
	private Boolean flag = false;  
  
	@Override  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.activity_track);
		
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		mWakeLock.acquire();
		 		 
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		mSwitch = (Switch) findViewById(R.id.statusswitch);
		Status = (TextView) findViewById(R.id.status);
		
		mSwitch.setChecked(true);
		Status.setText("Current status: Uploading location data.");
		
		mCurrentState = State.INIT;
		
		mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
					locationListener = new MyLocationListener();  
					locationManager.requestLocationUpdates(LocationManager  
							.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE,locationListener);
		        	mHandler.postDelayed(mRunnable, HANDLER_DELAY);
		    		Status.setText("Current status: Uploading location data.");
		        } else {
		        	mHandler.removeCallbacks(mRunnable);
		            if(locationManager != null) {
		            	locationManager.removeUpdates(locationListener);
		            }
		            //locationManager = null;
		    		Status.setText("Current status: Not uploading location data.");
		        }
		    }
		});
	}  
  	
	@Override  
	public void onStart() {
		super.onStart();
		if(mCurrentState == State.INIT){
			flag = displayGpsStatus();  
			if (flag) {
				locationListener = new MyLocationListener();  
				locationManager.requestLocationUpdates(LocationManager  
						.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE,locationListener);
				mHandler = new Handler();
				mHandler.postDelayed(mRunnable, HANDLER_DELAY);
			     
			} else {  
				alertbox("Gps Status!!", "Your GPS is: OFF");  
			}
		}
	}
	
	@Override
	public void onBackPressed(){
	     // do something here and don't write super.onBackPressed()
	    ParseUser.logOut();
		mHandler.removeCallbacks(mRunnable);
        if(locationManager != null) {
        	locationManager.removeUpdates(locationListener);
        }
		mWakeLock.release();
		locationManager=null;
	    Intent loginIntent = new Intent(this, MainActivity.class);
	    startActivity(loginIntent);
	}
	
	
	@Override
	public void onPause(){
		super.onPause();
		mCurrentState = State.RESUME;
		/*mHandler.removeCallbacks(mRunnable);
        if(locationManager != null) {
        	locationManager.removeUpdates(locationListener);
        }*/
        //locationListener = null;
        //locationManager = null;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mHandler.removeCallbacks(mRunnable);
        if(locationManager != null) {
        	locationManager.removeUpdates(locationListener);
        }
		mWakeLock.release();
		locationManager=null;
	}
	
	
/*	
	@Override
	public void onStop(){
		super.onStop();
        if(locationManager != null) {
        	locationManager.removeUpdates(locationListener);
        }
        //locationListener = null;
        //locationManager = null;
	}
*/
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.track, menu);
        return true;
    }
    
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
/*  
	private void obtainLocation(){
		locationListener = new MyLocationListener();  
		locationManager.requestLocationUpdates(LocationManager  
				.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE,locationListener);
		//locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,locationListener);
	}
*/	
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
   
	public void uploadData(){
        // Create a reference to a Firebase location
        Firebase ref = new Firebase("https://dinnrtracker.firebaseio.com/");
		Toast.makeText(getBaseContext(),"Lat: " +  
				mLatitude + " ,Lng: " + mLongitude,  
				Toast.LENGTH_SHORT).show();
        // Write data to Firebase
        Map<String,Double> locationData = new HashMap<String,Double>();
        locationData.put("Latitude", mLatitude);
        locationData.put("Longitude", mLongitude);

        ref.setValue(locationData);
        return;
	}

	/*----------Listener class to get coordinates ------------- */  
	private class MyLocationListener implements LocationListener {  
		@Override  
		public void onLocationChanged(Location loc) {
			mLatitude = loc.getLatitude();
			mLongitude = loc.getLongitude();
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