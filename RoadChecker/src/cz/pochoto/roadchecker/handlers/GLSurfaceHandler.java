package cz.pochoto.roadchecker.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cz.pochoto.roadchecker.R;

public class GLSurfaceHandler {
	public GLSurfaceHandler(){
		
	}
	
	public View getRootView(LayoutInflater inflater, ViewGroup container){
		
		View rootView = (RelativeLayout)inflater.inflate(R.layout.fragment_glsurface, container, false);
		
		
		return rootView;
	}
	
	

	
}
