package cz.pochoto.roadchecker.handlers;

import java.util.ArrayList;
import java.util.List;

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
import cz.pochoto.roadchecker.models.Displacement;

public class MapHandler extends AbstractHandler{
	
	private static GoogleMap mMap;
	
	private static View rootView;
	
	private List<Displacement> displacements = new ArrayList<Displacement>();

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
		    mLocationChangeListener.setMapHandler(this);
		    mMap.setOnMyLocationChangeListener(mLocationChangeListener);		    
	    }
	}

	
	public void addMatker(Displacement displacement){
		System.out.println("added " + displacement.getValue());
		displacements.add(displacement);
		showMarkers();
	}
	
	public void showMarkers(){		
		if(mMap != null){
			if(!displacements.isEmpty()){				
				for(Displacement d:displacements){
					if(d.getLoc() != null){
						System.out.println("Marker added " + d.getValue());
						mMap.addMarker(new MarkerOptions().position(new LatLng(d.getLoc().getLatitude(), d.getLoc().getLongitude())).title("Výchylka").snippet(d.getValue()+""));												
					}else{
						System.out.println("not located");
					}
				}
				System.out.println("cleared");
				displacements.clear();
			}
		}
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
