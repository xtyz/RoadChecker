package cz.pochoto.roadchecker.listeners;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import cz.pochoto.roadchecker.MainActivity;
import cz.pochoto.roadchecker.handlers.MapHandler;
import android.location.Location;

public class MyLocationChangeListenerImpl implements MyLocationChangeListener {

	private GoogleMap mMap;		
	private boolean cameraMove = true;
	private MapHandler mapHandler;

	@Override
	public void onMyLocationChange(Location loc) {
		if(cameraMove){
			try{
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 16.0f));
			}catch(NullPointerException e){
				e.printStackTrace();
			}						
		}		
		loc.setSpeed(loc.getSpeed() * 3.6f);
		MainActivity.setLocation(loc);
		mapHandler.showMarkers();		
	}

	public void setMap(GoogleMap mMap) {
		this.mMap = mMap;		
	}

	public void setMapHandler(MapHandler mapHandler) {
		this.mapHandler = mapHandler;		
	}

}
