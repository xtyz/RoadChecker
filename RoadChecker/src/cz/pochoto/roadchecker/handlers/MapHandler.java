package cz.pochoto.roadchecker.handlers;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.pochoto.roadchecker.MainActivity;
import cz.pochoto.roadchecker.R;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MapHandler {
	
	private static GoogleMap mMap;	
	
	private boolean cameraMove = true;
	
	public MapHandler(){		
	}
	
	public View getRootView(LayoutInflater inflater, ViewGroup container){
		
		View rootView = (RelativeLayout)inflater.inflate(R.layout.fragment_map, container, false);
		
		initMap();
		
		return rootView;
	}
	
	public void initMap() {			
	    if (mMap == null) {		        	        
	    	mMap = ((MapFragment) MainActivity.fragmentManager.findFragmentById(R.id.location_map)).getMap();
	    
		    mMap.setMyLocationEnabled(true);
		    
		    mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
				
				@Override
				public void onMyLocationChange(Location loc) {
					if(cameraMove){
						try{
							mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 12.0f));
						}catch(NullPointerException e){
							e.printStackTrace();
						}						
					}					
				}
			});	    
	    }
	}

	
	public void addMatker(Double latitude, Double longtitude){
		// For dropping a marker at a point on the Map
	    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longtitude)).title("Aktuální pozice").snippet("Home Address"));
	}
	
	public void destroyMap(){
	    if (mMap != null) {
	        mMap = null;
	    }
	}
	
	
}
