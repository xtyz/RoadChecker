package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.MainActivity;
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
	
	private float[] mRotationX;
	private float[] mRotationY;
	private double stableGValue, currentGValue, accelerationGValue;

	final float pi = (float) Math.PI;
	final float rad2deg = 180 / pi;

	
	public static String endl = "\n";

	@Override
	public void onSensorChanged(SensorEvent event) {

		// Z�skan� hodnot ze senzoru
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

		if (SensorManager.getRotationMatrix(currentR, inclination, currentG,
				geomag)) {		
			
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
																			
			
				
			float[] pom = new float[4];
			float[] result = new float[4];
			float[] currentGK = new float[4];
			
			
			Matrix.multiplyMV(pom, 0, mRotationX, 0, new float[]{accelerationG[0],accelerationG[1],accelerationG[2],1}, 0);
			Matrix.multiplyMV(result, 0, mRotationY, 0, pom, 0);
					
			pom = new float[4];
			
			Matrix.multiplyMV(pom, 0, mRotationX, 0, new float[]{currentG[0],currentG[1],currentG[2],1}, 0);
			Matrix.multiplyMV(currentGK, 0, mRotationY, 0, pom, 0);
			
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
			
			if(recording){
				// velikost xy;velikost xy kaliblov�no; xy prumer; xy odchylka; z; z kalibrov�no; z prumer; mez; z odchylka; alfa; rychlost
				MainActivity.recordUtils.addValue(getVectorLenght(new float[]{accelerationG[0],accelerationG[1]})+";"+getVectorLenght(new float[]{result[0],result[1]})+";"+xyMean+";"+xyDeviation+";"+Math.abs(accelerationG[2])+";"+Math.abs(result[2])+";"+zMean+";0.0;"+zSensorUtils.getDisplacement()+";"+zDeviation+";"+LowPassFilter.ALPHA+";"+MainActivity.speed);
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
		mRotationX = new float[]{1,0,0,0,
										0,(float)Math.cos(orientVals[1]),-(float)Math.sin(orientVals[1]),0,
										0,(float)Math.sin(orientVals[1]),(float)Math.cos(orientVals[1]),0,
										0,0,0,1};
			
		mRotationY = new float[]{(float)Math.cos(-orientVals[2]),0,(float)Math.sin(-orientVals[2]),0,
										0,1,0,0,													
										-(float)Math.sin(-orientVals[2]),0,(float)Math.cos(-orientVals[2]),0,
										0,0,0,1};
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
				"Akcelerometr Kalibrov�no:"+endl+"C: " + getVectorLenght(result) + endl+"x: "
				+ result[0] + endl+"y: " + result[1] + endl+"z: "
				+ result[2] + endl+
				"Akcelerace: "+endl+"C: "
				+ accelerationGValue + endl+"x: "
				+ accelerationG[0] + endl+"y: " + accelerationG[1] + endl+"z: "
				+ accelerationG[2];		

		double azimuth = Math.toDegrees(angleChange[0]);
		double pitch = Math.toDegrees(angleChange[1]);
		double roll = Math.toDegrees(angleChange[2]);

		String gyrLabel = 
				"Inclination change" + endl
				+ "Azimuth (z): " + azimuth + endl + "Pitch (x): " + pitch
				+ endl + "Roll: (y)" + roll + endl + endl
				+ "Total inclination" + endl + "Azimuth: (z)"
				+ orientVals[0] * rad2deg + endl + "Pitch: (x)"
				+ orientVals[1] * rad2deg + endl + "Roll: (y)"
				+ orientVals[2] * rad2deg + endl + endl
				+"Low-Pass: "+LowPassFilter.ALPHA + endl
				+"Speed: "+MainActivity.speed;
		
		
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
			MainActivity.recordUtils.addValue("velikost_xy;velikost_xy(kalibrov�no);xy_pr�m�r;xy_sm�rodatn�_odchylka;velikost_z;velikost_z(kalibrov�no);z_pr�m�r;mez;nad limit;z_sm�rodatn�_odchylka;alpha;rychlost");
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
