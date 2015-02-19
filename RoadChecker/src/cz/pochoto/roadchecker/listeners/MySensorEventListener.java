package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.views.MyGLSurfaceView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.TextView;

public class MySensorEventListener implements AbstractSensorEventListener {
	private TextView accelerometerLabel, gyroscopeLabel;
	private MyGLSurfaceView glSurfaceView;
	private String accLabel;

	float[] stableR = null;
	/**
	 * Rotation
	 */
	int calibrated = 0;
	float[] currentR0 = new float[16];
	float[] currentR = new float[16];
	float[] inclination = new float[16];
	float[] stableG = null;
	float[] currentG = new float[16];
	float[] accelerationG = new float[16];
	float[] geomag = new float[3];
	float[] orientVals = new float[3];
	double stableGValue, currentGValue;

	final float pi = (float) Math.PI;
	final float rad2deg = 180 / pi;
	private float[] angleChange = new float[3];

	@Override
	public void onSensorChanged(SensorEvent event) {

		// Získaní hodnot ze senzoru
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			currentG = event.values.clone();
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			geomag = event.values.clone();
			break;
		}

		// pokud je nalezena rotacni matice - pravdepodobne nebude treba bude se
		// rotovat ze souèasného Z na Z 0,0,9

		if (SensorManager.getRotationMatrix(currentR0, inclination, currentG,
				geomag)) {		
			
			currentR = currentR0.clone();
			
			// prvni spusteni
			if (stableR == null || stableG == null) {
				calibrate();
				calibration();
			}
			
			// vypocet uhlu o ktery je zarizeni natoceno
			SensorManager.getOrientation(currentR, orientVals);
			// vypocet zmeny uhlu o ktery je zarizeni natoceno
			SensorManager.getAngleChange(angleChange, currentR, stableR);
			// vypocet akcelerace		
			accelerationG = new float[] { currentG[0] - stableG[0],	currentG[1] - stableG[1], currentG[2] - stableG[2] };

			currentGValue = getVectorLenght(currentG);
			stableGValue = getVectorLenght(stableG);
			
			convertToCalibration();		
			
			// kdyz neni rozdil prilis velky, kalibruj jinak znazorni zrychleni
			if ((Math.abs(currentGValue - stableGValue) < 0.1 && Math
					.abs(currentGValue - stableGValue) > -0.1)
					|| (currentGValue > 9.6 && currentGValue < 9.9)) {
				
				//kalibrace !!!
				//nastavit aby byla akcelerace prevracena furt a ne jen pri kalibraci
				calibration();				
				
			} else {
				
				// kdyby se surfaceView nestihl inicializovat
				if (glSurfaceView != null) {
					// sin Roll (podle y) * akcererace x
					double n = Math.sin(orientVals[2] * rad2deg) * accelerationG[0];
					//sin Pitch (podle x) * akcelerace y
					double m = Math.sin(orientVals[1] * rad2deg) * accelerationG[1];
					int u = (int) (Math.sqrt(accelerationG[0] * accelerationG[0] + n * n) + 0.5);
					int v = (int) (Math.sqrt(accelerationG[1] * accelerationG[1] + m * m) + 0.5);
					if (accelerationG[0] < 0) {
						u = -u;
					}
					if (accelerationG[1] < 0) {
						v = -v;
					}
					
					
					glSurfaceView.setPosition(new float[] { u, v });

				}
			}

			// ukaz :)
			accLabel = "Akcelerometr:\nCelkové g:" + currentGValue + " \nx: "
					+ currentG[0] + "\ny: " + currentG[1] + "\nz: "
					+ currentG[2] + "\nAkcelerace: \nCelková: "
					+ Math.abs(currentGValue - stableGValue) + "\nx: "
					+ accelerationG[0] + "\ny: " + accelerationG[1] + "\nz: "
					+ accelerationG[2];

			if (accelerometerLabel != null) {
				accelerometerLabel.setText(accLabel);
			}

			if (glSurfaceView != null && glSurfaceView.getTextView() != null) {
				glSurfaceView.getTextView().setText(accLabel);
			}

			float azimuth = angleChange[0] * rad2deg;
			float pitch = angleChange[1] * rad2deg;
			float roll = angleChange[2] * rad2deg;

			String endl = "\n";
			gyroscopeLabel.setText("Rotation:" + endl + currentR[0] + " "
					+ currentR[1] + " " + currentR[2] + endl + currentR[4]
					+ " " + currentR[5] + " " + currentR[6] + endl
					+ currentR[8] + " " + currentR[9] + " " + currentR[10]
					+ endl + endl + "Inclination change" + endl
					+ "Azimuth (z): " + azimuth + endl + "Pitch (x): " + pitch
					+ endl + "Roll: (y)" + roll + endl + endl
					+ "Total inclination" + endl + "Azimuth: (z)"
					+ orientVals[0] * rad2deg + endl + "Pitch: (x)"
					+ orientVals[1] * rad2deg + endl + "Roll: (y)"
					+ orientVals[2] * rad2deg + endl);

		}
	}

	private double getVectorLenght(float[] f) {
		if (f.length > 3) {
			return 0;
		}
		return Math.sqrt(f[0] * f[0] + f[1] * f[1] + f[2] * f[2]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
	
	private void calibration(){
		//natoceni os souradnic
		//normalni - display nahore		
			
			stableR = currentR.clone();
			stableG = currentG.clone();
			if (glSurfaceView != null) {
				glSurfaceView.setPosition(new float[2]);
			}
		
	}
	
	public void calibrate(){
		if(currentG[1] > 7){
			//tlacitka dole
			calibrated = 1;
		}else if(currentG[0] > 7){
			//tlacitka v pravo
			calibrated = 2;
		}else if(currentG[0] < -7){
			// tlacitka v levo
			calibrated = 3;
		}
	}
	
	private void convertToCalibration(){
		//TODO upravit vypocet uhlu
		if(calibrated == 1){
			//tlacitka dole
			SensorManager.remapCoordinateSystem(currentR0, SensorManager.AXIS_X, SensorManager.AXIS_Z, currentR);
			currentG = new float[]{currentG[0],currentG[2], currentG[1]};
			accelerationG = new float[]{accelerationG[0],accelerationG[2], accelerationG[1]};
		}else if(calibrated == 2){
			//tlacitka v pravo
			SensorManager.remapCoordinateSystem(currentR0, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, currentR);
			currentG = new float[]{currentG[2],-currentG[1], currentG[0]};
			accelerationG = new float[]{accelerationG[2],-accelerationG[1], accelerationG[0]};
		}else if(calibrated == 3){
			// tlacitka v levo
			SensorManager.remapCoordinateSystem(currentR0, SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X, currentR);
			currentG = new float[]{-currentG[2],-currentG[1], -currentG[0]};
			accelerationG = new float[]{-accelerationG[2],-accelerationG[1], accelerationG[0]};
		}
	}
	
	public TextView getAccelerometerLabel() {
		return accelerometerLabel;
	}

	public void setAccelerometerLabel(TextView accelerometerLabel) {
		this.accelerometerLabel = accelerometerLabel;
	}

	public TextView getGyroscopeLabel() {
		return gyroscopeLabel;
	}

	public void setGyroscopeLabel(TextView gyroscopeLabel) {
		this.gyroscopeLabel = gyroscopeLabel;
	}

	@Override
	public void setSurfaceView(MyGLSurfaceView myGLSurfaceView) {
		this.glSurfaceView = myGLSurfaceView;

	}

}
