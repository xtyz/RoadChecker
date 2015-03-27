package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.utils.LowPassFilter;
import cz.pochoto.roadchecker.utils.SensorUtils;
import cz.pochoto.roadchecker.views.MyGLSurfaceView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.widget.TextView;

public class MySensorEventListenerImpl implements MySensorEventListener {
	private TextView accelerometerLabel, gyroscopeLabel;
	private MyGLSurfaceView glSurfaceView;
	private SensorUtils sensorUtils = new SensorUtils();
	
	private float[] stableR = null;
	private int calibrated = 0;
	private float[] currentR0 = new float[16];
	private float[] currentR = new float[16];
	private float[] inclination = new float[16];
	private float[] stableG = new float[3];
	private float[] oldG = new float[3];
	private float[] currentG = new float[3];	
	private float[] accelerationG = new float[3];
	private float[] geomag = new float[3];
	private float[] orientVals = new float[3];
	private float[] angleChange = new float[3];
	private double stableGValue, currentGValue, accelerationGValue;

	final float pi = (float) Math.PI;
	final float rad2deg = 180 / pi;

	
	public static String endl = "\n";

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

		currentG = LowPassFilter.filter(currentG, oldG).clone();		
		oldG = currentG.clone();
		// pokud je nalezena rotacni matice - pravdepodobne nebude treba bude se
		// rotovat ze souèasného Z na Z 0,0,9

		if (SensorManager.getRotationMatrix(currentR, inclination, currentG,
				geomag)) {		
			
			// prvni spusteni
			if (stableR == null || stableG == null) {
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
			accelerationGValue = Math.abs(currentGValue - stableGValue);					
			
			// kdyz neni rozdil prilis velky, kalibruj jinak znazorni zrychleni
																
				//otoceni accelerationG o  minus orientvals (nejprve po x a pak po y) - promitnuti do 2d xy - ax a ay se budou dat promitnout
				//kolem z ne - magnetometr
				float[] mRotaceX = new float[]{1,0,0,0,
												0,(float)Math.cos(-orientVals[1]),-(float)Math.sin(-orientVals[1]),0,
												0,(float)Math.sin(-orientVals[1]),(float)Math.cos(-orientVals[1]),0,
												0,0,0,1};
				
				float[] mRotaceY = new float[]{(float)Math.cos(-orientVals[2]),0,(float)Math.sin(-orientVals[2]),0,
												0,1,0,0,													
												-(float)Math.sin(-orientVals[2]),0,(float)Math.cos(-orientVals[2]),0,
												0,0,0,1};
					
				float[] pom = new float[4];
				float[] result = new float[4];
				
				
				Matrix.multiplyMV(pom, 0, mRotaceX, 0, new float[]{accelerationG[0],accelerationG[1],accelerationG[2],1}, 0);
				Matrix.multiplyMV(result, 0, mRotaceY, 0, pom, 0);
				float u = result[0];
				float v = result[1];
				//System.out.println(u+"/"+v+"/"+result[2]);
			
				double average = sensorUtils.compute(u, v);
				if (glSurfaceView != null) {
					glSurfaceView.setTrianglePosition(new float[] { u, v,  result[2]});
					glSurfaceView.setSquareScale(average);
					glSurfaceView.setMaxSquareScale(sensorUtils.findDisplacements());
					glSurfaceView.render();
				}
				
				
				
						
			showResults();			

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
	
	private void showResults(){
		String accLabel = "Akcelerometr:"+endl+"C: " + currentGValue + endl+"x: "
				+ currentG[0] + endl+"y: " + currentG[1] + endl+"z: "
				+ currentG[2] + endl+
				"Akcelerace: "+endl+"C: "
				+ accelerationGValue + endl+"x: "
				+ accelerationG[0] + endl+"y: " + accelerationG[1] + endl+"z: "
				+ accelerationG[2];		

		float azimuth = angleChange[0] * rad2deg;
		float pitch = angleChange[1] * rad2deg;
		float roll = angleChange[2] * rad2deg;

		String gyrLabel = "Rotation:" + endl + currentR[0] + " "
				+ currentR[1] + " " + currentR[2] + endl + currentR[4]
				+ " " + currentR[5] + " " + currentR[6] + endl
				+ currentR[8] + " " + currentR[9] + " " + currentR[10]
				+ endl + endl + "Inclination change" + endl
				+ "Azimuth (z): " + azimuth + endl + "Pitch (x): " + pitch
				+ endl + "Roll: (y)" + roll + endl + endl
				+ "Total inclination" + endl + "Azimuth: (z)"
				+ orientVals[0] * rad2deg + endl + "Pitch: (x)"
				+ orientVals[1] * rad2deg + endl + "Roll: (y)"
				+ orientVals[2] * rad2deg + endl;
		
		
		if (accelerometerLabel != null) {
			accelerometerLabel.setText(accLabel);
		}

		if (glSurfaceView != null && glSurfaceView.getTextView() != null) {
			glSurfaceView.getTextView().setText(accLabel);
		}
		
		if(gyroscopeLabel != null){
			gyroscopeLabel.setText(gyrLabel);
		}
		
	}
	
	public void calibration(){
		//natoceni os souradnic
		//normalni - display nahore			
		stableR = currentR.clone();
		stableG = currentG.clone();
		if (glSurfaceView != null) {
			glSurfaceView.setTrianglePosition(new float[3]);
		}
		
	}
	
	public void calibrate(){
		calibration();
	}
	
	@SuppressWarnings("unused")
	private void convertToCalibrationValues(){
		//TODO zkontrolovat remap popripade overit správnost
		if(calibrated == 1){
			//tlacitka dole
			System.out.println("tl dole");
			//SensorManager.remapCoordinateSystem(currentR0, SensorManager.AXIS_X, SensorManager.AXIS_Z, currentR);
			currentG = new float[]{currentG[0],currentG[2], currentG[1]};
			stableG = new float[]{stableG[0],stableG[2], stableG[1]};
			accelerationG = new float[]{accelerationG[0],accelerationG[2], accelerationG[1]};
		}else if(calibrated == 2){
			//tlacitka v pravo
			System.out.println("tl vpravo");
			//SensorManager.remapCoordinateSystem(currentR0, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, currentR);
			currentG = new float[]{currentG[2],-currentG[1], currentG[0]};
			stableG = new float[]{stableG[0],stableG[2], stableG[1]};
			accelerationG = new float[]{accelerationG[2],-accelerationG[1], accelerationG[0]};
		}else if(calibrated == 3){
			// tlacitka v levo
			System.out.println("tl vlevo");
			//SensorManager.remapCoordinateSystem(currentR0, SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X, currentR);
			currentG = new float[]{-currentG[2],-currentG[1], -currentG[0]};
			stableG = new float[]{stableG[0],stableG[2], stableG[1]};
			accelerationG = new float[]{-accelerationG[2],-accelerationG[1], accelerationG[0]};
		}else if(calibrated == 0){
			currentR = currentR0.clone();
			System.out.println("na zadech");
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
