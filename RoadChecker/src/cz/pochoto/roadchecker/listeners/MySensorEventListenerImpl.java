package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.MainActivity;
import cz.pochoto.roadchecker.models.Displacement;
import cz.pochoto.roadchecker.opengl.MyGLSurfaceView;
import cz.pochoto.roadchecker.utils.LowPassFilter;
import cz.pochoto.roadchecker.utils.SensorUtils;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.widget.TextView;

public class MySensorEventListenerImpl implements MySensorEventListener {
	private TextView accelerometerLabel, gyroscopeLabel;
	private MyGLSurfaceView glSurfaceView;
	private SensorUtils xySensorUtils = new SensorUtils();
	private SensorUtils zSensorUtils = new SensorUtils();
	
	private float[] stableR = null;
	private boolean recording = false;
	private int records = 0;
	private float[] currentR = new float[16];
	private float[] inclination = new float[16];
	private float[] stableG = new float[3];
	private float[] oldG = new float[3];
	private float[] currentG = new float[3];
	private float[] accelerationG = new float[3];
	private float[] geomag = new float[3];
	private float[] orientVals = new float[3];
	private float[] angleChange = new float[3];
	
	private float[] mRotationX = new float[16];
	private float[] mRotationY = new float[16];
	private float[] mTransformation = new float[16];
	private double stableGValue, currentGValue, accelerationGValue;
	
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

		if (SensorManager.getRotationMatrix(currentR, inclination, currentG, geomag)) {		
			
			// prvni spusteni
			if (stableR == null || stableG == null) {
				calibration();
			}
			
			// vypocet zmeny uhlu o ktery je zarizeni natoceno
			SensorManager.getAngleChange(angleChange, currentR, stableR);
			// vypocet akcelerace		
			accelerationG = new float[] { currentG[0] - stableG[0],	currentG[1] - stableG[1], currentG[2] - stableG[2] };

			currentGValue = getVectorLenght(currentG);			
			accelerationGValue = Math.abs(currentGValue - stableGValue);	
																			
			float[] result = new float[4];
			float[] currentGK = new float[4];			
			
			Matrix.multiplyMV(result, 0, mTransformation, 0, new float[]{accelerationG[0],accelerationG[1],accelerationG[2],1}, 0);			
			Matrix.multiplyMV(currentGK, 0, mTransformation, 0, new float[]{currentG[0],currentG[1],currentG[2],1}, 0);			
			
			xySensorUtils.compute(getVectorLenght(new float[]{result[0],result[1]}));
			zSensorUtils.compute(Math.abs(result[2]));
			
			double xyMean = xySensorUtils.getFinalMean();				
			double zMean = zSensorUtils.getFinalMean();
			double xyDeviation = xySensorUtils.getFinalDeviation();
			double zDeviation = zSensorUtils.getFinalDeviation();
				
			if (glSurfaceView != null) {
				glSurfaceView.setTrianglePosition(new float[] { result[0], result[1], result[2]});
				glSurfaceView.setSquareScale(xyMean);
				glSurfaceView.setAvgZSquareScale(zMean);
				if(xySensorUtils.getDisplacement()!=0)glSurfaceView.setMaxSquareScale(xySensorUtils.getDisplacement());
				glSurfaceView.render();
			}
			
			Displacement lastDisplacement = zSensorUtils.getMaxDisplacement();
			double lastMaxDisplacement = 0;
			float speed = 0;
			
			if(lastDisplacement != null){
				lastMaxDisplacement = lastDisplacement.getValue();
				if(MainActivity.mapHandler != null){
					MainActivity.mapHandler.addMatker(lastDisplacement);
				}
			}			
			
			if(MainActivity.location != null){
				speed = MainActivity.location.getSpeed();				
			}			
			
			if(recording){
				// velikost xy;velikost xy kaliblováno; xy prumer; xy odchylka; z; z kalibrováno; z prumer; mez; z odchylka; alfa; rychlost
				MainActivity.recordUtils.addValue(getVectorLenght(new float[]{accelerationG[0],accelerationG[1]})+";"+getVectorLenght(new float[]{result[0],result[1]})+";"+xyMean+";"+xyDeviation+";"+Math.abs(accelerationG[2])+";"+Math.abs(result[2])+";"+zMean+";"+(zMean+1)+";"+zSensorUtils.getDisplacement()+";"+lastMaxDisplacement+";"+zDeviation+";"+LowPassFilter.ALPHA+";"+speed);
			}
									
			showResults(currentGK);			
		}
	}

	public void calibration(){				
		stableR = currentR.clone();
		stableG = currentG.clone();
		stableGValue = getVectorLenght(stableG);
		// vypocet uhlu o ktery je zarizeni natoceno
		SensorManager.getOrientation(currentR, orientVals);
		//otoceni accelerationG o  minus orientvals (nejprve po x a pak po y) - promitnuti do 2d xy - ax a ay se budou dat promitnout
		//kolem z ne - magnetometr
	
		Matrix.setIdentityM(mTransformation, 0);
		Matrix.setIdentityM(mRotationX, 0);
		Matrix.setIdentityM(mRotationY, 0);
				
		Matrix.setRotateM(mRotationX, 0, Math.round(-Math.toDegrees(orientVals[1])), 1f, 0f, 0f);		
		Matrix.setRotateM(mRotationY, 0, Math.round(Math.toDegrees(orientVals[2])), 0f, 1f, 0f);	
		
		Matrix.multiplyMM(mTransformation, 0, mRotationX, 0, mRotationY, 0);
		
		if (glSurfaceView != null) {
			glSurfaceView.setTrianglePosition(new float[3]);
		}		
	}

	private double getVectorLenght(float[] f) {
		if (f.length == 3 || f.length == 4) {
			return Math.sqrt(f[0] * f[0] + f[1] * f[1] + f[2] * f[2]);
		}
		if (f.length == 2){
			return Math.sqrt(f[0] * f[0] + f[1] * f[1]);
		}
		return 0;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		switch (accuracy) {
		case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:			
			break;
		case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:			
			break;
		case SensorManager.SENSOR_STATUS_ACCURACY_LOW:			
			break;
		case SensorManager.SENSOR_STATUS_UNRELIABLE:			
			break;
		case SensorManager.SENSOR_STATUS_NO_CONTACT:			
			break;
		}
	}
	
	private void showResults(float[] result){
		String accLabel = "Akcelerometr:"+endl+"C: " + currentGValue + endl+"x: "
				+ currentG[0] + endl+"y: " + currentG[1] + endl+"z: "
				+ currentG[2] + endl+
				"Akcelerometr kalibrováno:"+endl+"C: " + getVectorLenght(result) + endl+"x: "
				+ result[0] + endl+"y: " + result[1] + endl+"z: "
				+ result[2] + endl+
				"Akcelerace: "+endl+"C: "
				+ accelerationGValue + endl+"x: "
				+ accelerationG[0] + endl+"y: " + accelerationG[1] + endl+"z: "
				+ accelerationG[2];		

		String gyrLabel = 
				"Zmìna náklonu:" + endl
				+ "Azimut (z): " 
				+ Math.toDegrees(angleChange[0]) + endl + "Pitch (x): " 
				+ Math.toDegrees(angleChange[1]) + endl + "Roll: (y)" 
				+ Math.toDegrees(angleChange[2]) + endl 
				+ "Kalibrovaný náklon:" + endl + "Azimut: (z)"
				+ Math.toDegrees(orientVals[0]) + endl + "Pitch: (x)"
				+ Math.toDegrees(orientVals[1]) + endl + "Roll: (y)"
				+ Math.toDegrees(orientVals[2]) + endl + endl
				+"Low-Pass: "+LowPassFilter.ALPHA + endl;
		
		
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
	
	public void calibrate(){
		calibration();
	}
	
	@Override
	public boolean record() {
		if(recording){
			MainActivity.recordUtils.stopRecord();
			recording = false;			
		}else{
			MainActivity.recordUtils.startRecord("_"+(records++));			
			MainActivity.recordUtils.addValue("velikost_xy;velikost_xy(kalibrováno);xy_prùmìr;xy_smìrodatná_odchylka;velikost_z;velikost_z(kalibrováno);z_prùmìr;mez;nad limit;max;z_smìrodatná_odchylka;alpha;rychlost");
			recording = true;
		}
		return recording;
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
