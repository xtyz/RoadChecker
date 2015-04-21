package cz.pochoto.roadchecker.listeners;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import cz.pochoto.roadchecker.MainActivity;
import android.location.Location;

public class MyLocationChangeListenerImpl implements MyLocationChangeListener {

	private GoogleMap mMap;		
	private boolean cameraMove = true;

	@Override
	public void onMyLocationChange(Location loc) {
		if(cameraMove){
			try{
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 12.0f));
			}catch(NullPointerException e){
				e.printStackTrace();
			}						
		}
		MainActivity.setSpeed(loc.getSpeed() * 3.6f);
	}

	public void setMap(GoogleMap mMap) {
		this.mMap = mMap;		
	}

}
