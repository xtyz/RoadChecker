package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.views.MyGLSurfaceView;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public interface MySensorEventListener extends SensorEventListener {
	public TextView getAccelerometerLabel();

	public void setAccelerometerLabel(TextView accelerometerLabel);

	public TextView getGyroscopeLabel();

	public void setGyroscopeLabel(TextView gyroscopeLabel);

	public void setSurfaceView(MyGLSurfaceView myGLSurfaceView);

	public void calibrate();
}
