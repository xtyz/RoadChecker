package cz.pochoto.roadchecker;

import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import cz.pochoto.roadchecker.handlers.MapHandler;
import cz.pochoto.roadchecker.listeners.AbstractSensorEventListener;
import cz.pochoto.roadchecker.listeners.AccelerometterListener;
import cz.pochoto.roadchecker.views.MyGLSurfaceView;

public class MainActivity extends Activity implements ActionBar.TabListener,
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	public static MapHandler mapHandler;
	public static FragmentManager fragmentManager;
	public static SensorManager sensorManager;
	public static AbstractSensorEventListener acceletometerListener;

	public static TextView accelerometerLabel, gyroscopeLabel;
	public static int count = 50;
	
	

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	protected static ViewPager mViewPager;
	protected static MyGLSurfaceView mGLView;

	private static GoogleApiClient mGoogleApiClient;
	private static LocationRequest mLocationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buildGoogleApiClient();
		
		acceletometerListener = new AccelerometterListener();

		mapHandler = new MapHandler();
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		fragmentManager = getFragmentManager();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);		

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
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
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
				return MyMapFragment.newInstance(position + 1);
			default:
				return GLSurfaceFragment.newInstance(position + 1);
			}
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
		mapHandler.initMap();
	}

	@Override
	public void onConnectionSuspended(int i) {
		// TODO Auto-generated method stub

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	protected void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onLocationChanged(Location loc) {

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
			
			acceletometerListener.setAccelerometerLabel(accelerometerLabel);
			acceletometerListener.setGyroscopeLabel(gyroscopeLabel);
			registerSensorListeners();
			
			return rootView;
		}
		
		@Override
		public void onPause() {
			//kdyz se neodregistruje, bezi na pozadi.. toho se necha vyuzit :)
			sensorManager.unregisterListener(acceletometerListener);
			super.onPause();
		}
		
		@Override
		public void onResume() {
			registerSensorListeners();
			super.onResume();
		}
		
		private static void registerSensorListeners() {

			if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
							
				sensorManager.registerListener(acceletometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_UI);
			}

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
			mapHandler.initMap();
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
			View rootView = inflater.inflate(R.layout.fragment_glsurface, container,
					false);
			
			mGLView = (MyGLSurfaceView)rootView.findViewById(R.id.gl_surface_view);
			TextView glSurfaceTextView = (TextView)rootView.findViewById(R.id.gl_surface_text);
			glSurfaceTextView.setTextColor(Color.WHITE);
			mGLView.setSensorManager(sensorManager);
			mGLView.setTextView(glSurfaceTextView);
			return rootView;
		}
		
		@Override
		public void onPause(){
			if(mGLView != null){
				mGLView.onPause();
			}
			super.onPause();
		}
		
		public void onResume(){
			if(mGLView != null){
				mGLView.onResume();
			}
			super.onResume();
		}
	}

}
