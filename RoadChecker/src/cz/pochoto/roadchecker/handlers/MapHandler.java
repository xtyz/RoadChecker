package cz.pochoto.roadchecker.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.pochoto.roadchecker.MainActivity;
import cz.pochoto.roadchecker.R;
import cz.pochoto.roadchecker.listeners.MyLocationChangeListenerImpl;

public class MapHandler extends AbstractHandler{
	
	private static GoogleMap mMap;
	
	private static View rootView;

	private MyLocationChangeListenerImpl mLocationChangeListener;
	
	@Override
	public View getRootView(LayoutInflater inflater, ViewGroup container) {
		if(rootView == null){
			rootView = (RelativeLayout)inflater.inflate(R.layout.fragment_map, container, false);	
		}		
		return rootView;
	}
	
	@Override
	public void init() {			
	    if (mMap == null) {		        	        
	    	mMap = ((MapFragment) MainActivity.fragmentManager.findFragmentById(R.id.location_map)).getMap();
	    
		    mMap.setMyLocationEnabled(true);
		    
		    mLocationChangeListener.setMap(mMap);
		    
		    mMap.setOnMyLocationChangeListener(mLocationChangeListener);	    
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

	public void setLocationChangeListener(
			MyLocationChangeListenerImpl mLocationChangeListener) {
		this.mLocationChangeListener = mLocationChangeListener;
		
	}
	
	
}
