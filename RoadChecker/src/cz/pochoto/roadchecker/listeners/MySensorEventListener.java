package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.views.MyGLSurfaceView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.TextView;

public class MySensorEventListener implements AbstractSensorEventListener {
	private TextView accelerometerLabel, gyroscopeLabel;
	private MyGLSurfaceView glSurfaceView;
	
	float[] stableR = null;
    /**
     * Rotation
     */
	float[] currentR= new float[16];
    float[] I = new float[16];
    float[] stableG = null;
    float[] currentG = new float[16];
    float[] accelerationG = new float[16];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];
    double stableGValue, currentGValue;

    final float pi = (float) Math.PI;
    final float rad2deg = 180/pi;
	private float[] angleChange = new float[3];    


    @Override
    public void onSensorChanged(SensorEvent event) {

   
     // Z�skan� hodnot ze senzoru
     switch (event.sensor.getType()){  
     case Sensor.TYPE_ACCELEROMETER:
    	 currentG = event.values.clone();     
      break;
     case Sensor.TYPE_MAGNETIC_FIELD:
    	 geomag = event.values.clone();
      break;
     }

     
      // pokud je nalezena rotacni matice - pravdepodobne nebude treba bude se rotovat ze sou�asn�ho Z na Z 0,0,9
     
      if (SensorManager.getRotationMatrix(currentR, I, currentG, geomag)){
    	  //prvni spusteni
    	  if(stableR == null || stableG == null){    		  
    		  stableR = currentR.clone();
    		  stableG = currentG.clone();
    	  }    	  
    	  
    	  //vypocet uhlu o ktery je zarizeni natoceno
    	  SensorManager.getAngleChange(angleChange, currentR, stableR);
    	  //vypocet akcelerace
    	  accelerationG = new float[]{currentG[0]-stableG[0], currentG[1]-stableG[1], currentG[2]-stableG[2]};
    	  
    	  currentGValue = getVectorLenght(currentG);
    	  stableGValue = getVectorLenght(stableG);
    	  
    	//kdyz neni rozdil prilis velky, kalibruj jinak znazorni zrychleni
		if((Math.abs(currentGValue - stableGValue) < 0.1 && Math.abs(currentGValue - stableGValue) > -0.1) || (currentGValue > 9.5 && currentGValue < 9.9)){	
			stableR = currentR.clone();	
			stableG = currentG.clone();
			if(glSurfaceView != null){
				glSurfaceView.setPosition(new float[]{0, 0});
			}
		}else{
			//kdyby se surfaceView nestihl inicializovat
			if(glSurfaceView != null){				
				int u = (int)(Math.sqrt(accelerationG[0] * accelerationG[0] + accelerationG[2] * accelerationG[2]) + 0.5);
				int v = (int)(Math.sqrt(accelerationG[1] * accelerationG[1] + accelerationG[2] * accelerationG[2]) + 0.5);
				if(accelerationG[0] < 0){
					u = -u;
				}
				if(accelerationG[1] < 0){
					v = -v;
				}
				glSurfaceView.setPosition(new float[]{u, v});
	    	
			
			}
		}
		

		// ukaz :)
		if (accelerometerLabel != null) {
			accelerometerLabel.setText("Akcelerometr:\nCelkov� g:"+ currentGValue +" \nx: " + currentG[0]
					+ "\ny: " + currentG[1] + "\nz: " + currentG[2] + "\nAkcelerace: \nCelkov�: " + (currentGValue - stableGValue)
					+ "\nx: " + accelerationG[0] + "\ny: " + accelerationG[1] + "\nz: " + accelerationG[2]);
		}
		
		if(glSurfaceView != null && glSurfaceView.getTextView() != null){
			glSurfaceView.getTextView().setText("Akcelerometr:\nCelkov� g:"+ currentGValue +" \nx: " + currentG[0]
					+ "\ny: " + currentG[1] + "\nz: " + currentG[2] + "\nAkcelerace: \nCelkov�: " + (currentGValue - stableGValue)
					+ "\nx: " + accelerationG[0] + "\ny: " + accelerationG[1] + "\nz: " + accelerationG[2]);
		}
    	  
    	  

       // Re-map coordinates so y-axis comes out of camera
      // SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);

       // Finds the Azimuth and Pitch angles of the y-axis with 
       // magnetic north and the horizon respectively
       SensorManager.getOrientation(currentR, orientVals);
       float azimuth = angleChange[0]*rad2deg;
       float pitch = angleChange[1]*rad2deg;
       float roll = angleChange[2]*rad2deg;

      
       String endl = "\n";
      gyroscopeLabel.setText(
         "Rotation:\n" +
         currentR[0] + " " + currentR[1] + " " + currentR[2] + endl +
         currentR[4] + " " + currentR[5] + " " + currentR[6] + endl +
         currentR[8] + " " + currentR[9] + " " + currentR[10] + endl +endl +
         "Azimuth: " + azimuth + " degrees" + endl + 
         "Pitch: " + pitch + " degrees" + endl +
         "Roll: " + roll + " degrees");
         
      } 
     }   
    
    private double getVectorLenght(float[] f){
    	if(f.length>3){
    		return 0;
    	}
    	return Math.sqrt(f[0] * f[0] + f[1] * f[1] + f[2] * f[2]);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

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