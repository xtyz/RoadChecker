package cz.pochoto.roadchecker.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cz.pochoto.roadchecker.R;
import cz.pochoto.roadchecker.listeners.MySensorEventListener;
import cz.pochoto.roadchecker.opengl.MyGLSurfaceView;

public class GLSurfaceHandler extends AbstractHandler{
	
	private MySensorEventListener mSensorEventListener;

	private MyGLSurfaceView mGLView;
	
	private static View rootView;
	
	public static TextView glSurfaceTextView;

	@Override
	public View getRootView(LayoutInflater inflater, ViewGroup container) {
		if(rootView == null){
			rootView = (RelativeLayout)inflater.inflate(R.layout.fragment_glsurface, container, false);	
		}		
		return rootView;
	}
	
	@Override
	public void init() {
		if(mGLView == null){
			mGLView = (MyGLSurfaceView)rootView.findViewById(R.id.gl_surface_view);			
		}
		
		mGLView.setSensorEventListener(mSensorEventListener);		
	}
	
	public void onPause(){
		if(mGLView != null){
			mGLView.onPause();
		}
	}
	
	public void onResume(){
		if(mGLView != null){
			mGLView.setSensorEventListener(mSensorEventListener);
			mGLView.onResume();
		}
	}
	

	public void setSensorEventListener(
			MySensorEventListener mSensorEventListener) {
		this.mSensorEventListener = mSensorEventListener;
		
	}	
}
