package com.example.gpsdemo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

public class MainActivity extends Activity {

	/*MAP*/
	private GoogleMap map;
	private Marker markerMe;
	
	/*GPS*/
	private LocationManager locationmgr;
	private String provider;
	
	/*lon lat*/
	public double lon;
	public double lat;
	/*Text View*/
	private TextView textoutput;
	private String gpssta;
	private String locsta;
	
	/*Control Value*/
	private boolean isopen = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("GPS Service not open!");
	        builder.setMessage("Use this app need to use GPS service" +
	        				   "\nNeed to open 'GPS SERVICE'"+
	        				   "\n\nWe find you not open it" +
	        				   "\nAre you open this service?"); 
	        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialogInterface, int i) {
	                MainActivity.this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	                MainActivity.this.finish();
	            }
	        });
	        builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialogInterface, int i) {
	            	MainActivity.this.finish();
	            }});
	        builder.create().show();
	        return;
	    }
	    else{
			textoutput = (TextView)findViewById(R.id.textoutput);
			gpssta = "Search";
			locsta = "Status:Ok";
			putmap();
			if (initLocationProvider()) {
		        whereami();
		    }else{
		    	textoutput.setText("Remind:Please open GPS");
		    }
	    }
	}
	
	private void putmap(){
		if(map == null){
			map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			if(map != null){
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			}
		}
	}

	private boolean initLocationProvider() {//choose GPS
		locationmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		if (locationmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
			isopen = true;
			return true;
		}
		return false;
	}
//////////////////////////////////////////////////////
	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {//GPS Manager
	   
		public void onGpsStatusChanged(int event) {
	        switch (event) {
	            case GpsStatus.GPS_EVENT_STARTED:
	            	gpssta = "STARTED";
	            	break;
	            case GpsStatus.GPS_EVENT_STOPPED:
	            	gpssta = "STOPPED";
	            	break;
	            case GpsStatus.GPS_EVENT_FIRST_FIX:
	            	gpssta = "FIRST_FIX";
	            	break;
	            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	            	break;
	       }
	    }
	};


	LocationListener locationListener = new LocationListener(){//LOCATION Manager
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		} 
		
		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
		}	 
		public void onProviderEnabled(String provider) {
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		  switch (status) {
		      case LocationProvider.OUT_OF_SERVICE:
		    	  locsta = "Status:Out of service";
		    	  break;
		      case LocationProvider.TEMPORARILY_UNAVAILABLE:
		    	  locsta = "Status:Unavailable";
		    	  break;
		      case LocationProvider.AVAILABLE:
		    	  locsta = "Status:Available";
		    	  break;
		  }
		}
	};
/////////////////////////////////////////////////////
	
//////////////////////////////////////////////////////
	private void whereami(){ 
		Location location = locationmgr.getLastKnownLocation(provider);//To get GPS location last time.
		updateWithNewLocation(location);
		//Open GPS loctaionManager
		locationmgr.addGpsStatusListener(gpsListener);
		int minTime = 15000;//ms
		int minDist = 5;//meter
		locationmgr.requestLocationUpdates(provider, minTime, minDist,locationListener);
	}
    
	private void showme(double lat, double lng){//Show my location
		if (markerMe != null) {
			markerMe.remove();
		}
		MarkerOptions markerOpt = new MarkerOptions();
		markerOpt.position(new LatLng(lat, lng));
		markerOpt.title("I'm Here");
		markerMe = map.addMarker(markerOpt);
	}
		
	private void focusonme(double lat, double lng){//Let Camera to catch where I am.
		 CameraPosition camPosition = new CameraPosition.Builder()
		    .target(new LatLng(lat, lng))
		    .zoom(16)
		    .build();
		 map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
	}
    
	private void updateWithNewLocation(Location location) {//To catch where I am.
		String where = "";
		if (location != null) {
			//Longitude
			double lng = location.getLongitude();
			//Latitude
			double lat = location.getLatitude();
			//"Me"
			showme(lat, lng);
			focusonme(lat, lng);
			where = "Longitude: " + lng + 
					"\nLatitude: " + lat + 
					"\nGPS: " + gpssta+
					"\n" + locsta;
		}
		else{
			where = "Searching Your Location";
		}
		textoutput.setText(where);
	}
	
	protected void onDestroy() {//Leave service, close LocationManager
		if(isopen == true){
			locationmgr.removeUpdates(locationListener);
		}
	    super.onDestroy();
	}
}
