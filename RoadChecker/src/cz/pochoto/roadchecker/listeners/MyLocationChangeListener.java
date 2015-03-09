package cz.pochoto.roadchecker.listeners;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;

public interface MyLocationChangeListener extends OnMyLocationChangeListener {
	public void setMap(GoogleMap mMap);
}
