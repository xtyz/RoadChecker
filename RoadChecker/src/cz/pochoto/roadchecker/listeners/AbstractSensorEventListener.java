package cz.pochoto.roadchecker.listeners;

import android.hardware.SensorEventListener;
import android.widget.TextView;

public interface AbstractSensorEventListener extends SensorEventListener {
	public TextView getAccelerometerLabel();

	public void setAccelerometerLabel(TextView accelerometerLabel);

	public TextView getGyroscopeLabel();

	public void setGyroscopeLabel(TextView gyroscopeLabel);
}
