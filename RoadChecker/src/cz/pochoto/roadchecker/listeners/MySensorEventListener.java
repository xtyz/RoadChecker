package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.opengl.MyGLSurfaceView;
import android.hardware.SensorEventListener;

public interface MySensorEventListener extends SensorEventListener {
	
	/**
	 * Sets MyGlSurfaceView to the listener for drawing results
	 * @param myGLSurfaceView
	 */
	public void setSurfaceView(MyGLSurfaceView myGLSurfaceView);

	/**
	 * Calibration af sensor values
	 */
	public void calibration();
	
	/**
	 * Start or stop recording
	 * @return
	 */
	public boolean record();

	/**
	 * Enabled or disabled calibration control
	 * @return
	 */
	public boolean calibrationControl();
}
