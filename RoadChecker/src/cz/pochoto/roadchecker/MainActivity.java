package cz.pochoto.roadchecker;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;

import cz.pochoto.roadchecker.handlers.GLSurfaceHandler;
import cz.pochoto.roadchecker.handlers.MapHandler;
import cz.pochoto.roadchecker.listeners.MyLocationChangeListenerImpl;
import cz.pochoto.roadchecker.listeners.MySeekBarListener;
import cz.pochoto.roadchecker.listeners.MySensorEventListener;
import cz.pochoto.roadchecker.listeners.MySensorEventListenerImpl;
import cz.pochoto.roadchecker.opengl.MyGLSurfaceView;
import cz.pochoto.roadchecker.utils.RecordUtils;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements ActionBar.TabListener,
		ConnectionCallbacks, OnConnectionFailedListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	public static MapHandler mapHandler;
	public static GLSurfaceHandler glSurfaceHandler;
	
	
	public static FragmentManager fragmentManager;
	public static SensorManager sensorManager;
	
	public static MySensorEventListener mSensorEventListener;
	public static MyLocationChangeListenerImpl mLocationChangeListener;
	
	public static RecordUtils recordUtils;

	public static TextView accelerometerLabel, gyroscopeLabel, glSurfaceTextView;
	public static Button calibrateM, calibrateG;
	public static SeekBar barM, barG;
	public static ActionBar actionBar;
	public static int count = 50;
	public static Location location = null;
	
	

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	protected static ViewPager mViewPager;
	protected static MyGLSurfaceView mGLView;
	private static LocationRequest mLocationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		// init resources		
		recordUtils = new RecordUtils(getApplicationContext());		
		
		mSensorEventListener = new MySensorEventListenerImpl();
		mLocationChangeListener = new MyLocationChangeListenerImpl();
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		registerSensorListeners();
		
		mapHandler = new MapHandler();
		mapHandler.setLocationChangeListener(mLocationChangeListener);
				
		glSurfaceHandler = new GLSurfaceHandler();
		glSurfaceHandler.setSensorEventListener(mSensorEventListener);		
		
		fragmentManager = getFragmentManager();
		
		// Set up the action bar.
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);		
		actionBar.setSubtitle(String.valueOf(location) + " km/h");
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(fragmentManager);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setBackgroundColor(Color.GRAY);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		actionBar.getTabAt(1).select();
	}
	
	/**
	 * Sensor check and registration
	 */
	public static void registerSensorListeners() {
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
						
			sensorManager.registerListener(mSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_UI);
		}
		if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
			
			sensorManager.registerListener(mSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
					SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_calibrate) {
			if(mSensorEventListener.calibrationControl()){
				Toast.makeText(getApplicationContext(), "Kontrola kalibrace ZAPNUTA", Toast.LENGTH_SHORT).show();
				actionBar.getTabAt(0).select();
			}else{
				Toast.makeText(getApplicationContext(), "Kontrola kalibrace VYPNUTA", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				return PlaceholderFragment.newInstance(position + 1);
			case 1:
				return GLSurfaceFragment.newInstance(position + 1);
			case 2:				
				return MyMapFragment.newInstance(position + 1);
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle bundle) {
		mapHandler.init();
	}

	@Override
	public void onConnectionSuspended(int i) {
		// TODO Auto-generated method stub

	}



	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	/**
	 * Method to give signal to sensorListener for record data
	 * @param view button
	 */
	public void rec(View view){
		if(mSensorEventListener != null){
			mSensorEventListener.record();
		}	
	}
	
	/**
	 * Method to give signal to sensorListener for calibration
	 * @param view button
	 */
	public void calibrate(View view){
		if(mSensorEventListener != null){
			mSensorEventListener.calibration();
			Toast.makeText(getApplicationContext(), "Calibrated", Toast.LENGTH_SHORT).show();
		}		
	}

	// fragments

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);			
						
			accelerometerLabel = (TextView) rootView
					.findViewById(R.id.accelerometer);
			gyroscopeLabel = (TextView) rootView.findViewById(R.id.gyroscope);
			if(barM == null){
				barM = (SeekBar)rootView.findViewById(R.id.seekAlpha);
				if(barG == null){
					barM.setProgress(500);
				}else{
					barM.setProgress(barG.getProgress());
				}		
				barM.setOnSeekBarChangeListener(new MySeekBarListener("M"));
			}					
			
			return rootView;
		}
		

		@Override
		public void onPause() {
			super.onPause();
		}
		
		@Override
		public void onResume() {			
			super.onResume();
		}		
		
	}

	/**
	 * A placeholder fragment containing a Map view.
	 */
	public static class MyMapFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static MyMapFragment newInstance(int sectionNumber) {

			MyMapFragment fragment = new MyMapFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			return mapHandler.getRootView(inflater, container);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			mapHandler.init();
			super.onViewCreated(view, savedInstanceState);
		}

		@Override
		public void onDestroyView() {
			mapHandler.destroyMap();
			super.onDestroyView();
		}

	}
	
	/**
	 * A placeholder fragment containing a GLSurface view.
	 */
	public static class GLSurfaceFragment extends Fragment  {
		
		private static final String ARG_SECTION_NUMBER = "section_number";
		

		public static GLSurfaceFragment newInstance(int sectionNumber) {
			GLSurfaceFragment fragment = new GLSurfaceFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = glSurfaceHandler.getRootView(inflater, container);
			
			glSurfaceTextView = (TextView)rootView.findViewById(R.id.gl_surface_text);
			glSurfaceTextView.setTextColor(Color.WHITE);
			
			if(barG == null){
				barG = (SeekBar)rootView.findViewById(R.id.seekAlpha);
				if(barM == null){
					barG.setProgress(500);
				}else{
					barG.setProgress(barM.getProgress());
				}				
				barG.setOnSeekBarChangeListener(new MySeekBarListener("G"));
			}			
			return glSurfaceHandler.getRootView(inflater, container);
		}
		
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			glSurfaceHandler.init();
			super.onViewCreated(view, savedInstanceState);
		}
		
		@Override
		public void onPause(){
			glSurfaceHandler.onPause();
			super.onPause();
		}
		
		public void onResume(){
			glSurfaceHandler.onResume();
			super.onResume();
		}
	}

	/**
	 * Method save current location and show actual speed in the bar
	 * @param loc
	 */
	public static void setLocation(Location loc) {
		location = loc;
		actionBar.setSubtitle(String.valueOf(location.getSpeed()) + " km/h");		
	}

}
