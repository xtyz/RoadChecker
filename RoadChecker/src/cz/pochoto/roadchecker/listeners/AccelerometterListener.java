package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.views.MyGLSurfaceView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class AccelerometterListener implements AbstractSensorEventListener {
	private TextView accelerometerLabel, gyroscopeLabel;
	public float x0 = 0, y0 = 0, z0 = 0, u = 0, v = 0;	
	double g, g0, ax, ay, az, dx, dy, dz;
	private MyGLSurfaceView glSurfaceView;
	
	float[] inR = new float[16];
    float[] outR= new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];

    final float pi = (float) Math.PI;
    final float rad2deg = 180/pi;    


	@Override
	public void onSensorChanged(SensorEvent event) {
		// hodnoty x,y,z ze senzoru
		float[] f = event.values;
		float[]rotation = new float[]{0,0,0};
		// hodnoty x,y pro zobrazení ve 2D
		float x, y;
		
		// Gets the value of the sensor that has been changed
		   switch (event.sensor.getType()){  
		   case Sensor.TYPE_ACCELEROMETER:
		    gravity = event.values.clone();
		    break;
		   case Sensor.TYPE_MAGNETIC_FIELD:
		    geomag = event.values.clone();
		    break;
		   }

		try {
			if (accelerometerLabel != null) {
				accelerometerLabel.setText("Akcelerometr \nX-axis: " + f[0]
						+ "\nY-axis: " + f[1] + "\nZ-axis: " + f[2]);
			}

			if (x0 == 0 && y0 == 0 && z0 == 0) {
				// kalibrace na zacatku TODO: udelat vypocet na základe nekolika
				// hodnot - pokud pyt.v. je kolem 9.8, nebo pyt v z pocatku
				// mereni
				x0 = f[0];
				y0 = f[1];
				z0 = f[2];				
			} else {
				g0 = Math.sqrt(x0 * x0 + y0 * y0 + z0 * z0);
				g = Math.sqrt(f[0] * f[0] + f[1] * f[1] + f[2] * f[2]);
				ax = (f[0] - x0);
				ay = (f[1] - y0);
				az = (f[2] - z0);
				if (gyroscopeLabel != null) {
					gyroscopeLabel.setText("Akcelerace: \nCelková: " + (g - g0)
							+ "\nx: " + ax + "\ny: " + ay + "\nz: " + az);
				}
				//kdyz neni rozdil prilis velky, kalibruj
				if(Math.abs(g0 - g) < 0.1 && Math.abs(g0 - g) > -0.1){	
					x0 = f[0];
					y0 = f[1];
					z0 = f[2];
					
				}
				
				
				
				
				
				
			}
			// najdi maximum - která osa vede dolu - dale se provede najiti
			// mnejvetsiho minika - absolutni hodnoty se porovnají - pokud bude
			// |min| vetsi naz max tak je telefon vzhuru nohama			
//			float max = f[0];
//			if (f[1] > max) {
//				max = f[1];
//			}
//			if (f[2] > max) {
//				max = f[2];
//			}
//
//			float min = f[0];
//			if (f[1] < min) {
//				min = f[1];
//			}
//			if (f[2] < min) {
//				min = f[2];
//			}

			// vypocet udelat zvlast pro vsechny osy
//			if (Math.abs(min) > Math.abs(max)) {
//				if (min == f[0]) {
//					// -x tlacitka vlevo
//					u = (int)(Math.sqrt(ay * ay + ax * ax) + 0.5);
//					v = (int)(Math.sqrt(az * az + ax * ax) + 0.5);
//					if(ay > 0){
//						u = -u;
//					}
//					if(az < 0){
//						v = -v;
//					}
//					System.out.println("["+u+";"+v+"]");
//				} else if (min == f[1]) {
//					// -y vzhuru nohama - tlacitka nahore
//				} else if (min == f[2]) {
//					// -z - display dolu
//				}
//			} else {
//				if (max == f[0]) {
//					// x tlacitka v pravo
//					u = (int)(Math.sqrt(ay * ay + ax * ax) + 0.5);
//					v = (int)(Math.sqrt(az * az + ax * ax) + 0.5);
//					if(ay > 0){
//						u = -u;
//					}
//					if(az < 0){
//						v = -v;
//					}
//					System.out.println("["+u+";"+v+"]");
//				} else if (max == f[1]) {
//					// y tlacitka dole
//					u = (int)(Math.sqrt(ax * ax + ay * ay) + 0.5);
//					v = (int)(Math.sqrt(az * az + ay * ay) + 0.5);
//					if(ax > 0){
//						u = -u;
//					}
//					if(az < 0){
//						v = -v;
//					}
//					System.out.println("["+u+";"+v+"]");	
//				} else if (max == f[2]) {
//					// z na zadech					
//					//vypocitat pyt.v. mezi u = ax-az a v=ay-az vypocitat  
//										
//					u = (int)(Math.sqrt(ax * ax + az * az) + 0.5);
//					v = (int)(Math.sqrt(ay * ay + az * az) + 0.5);
//					if(ax < 0){
//						u = -u;
//					}
//					if(ay < 0){
//						v = -v;
//					}
//					System.out.println("["+u+";"+v+"]");				
//					
//					
//				}
//				
//			}	
			
			
			u = (int)(Math.sqrt(ax * ax + az * az) + 0.5);
			v = (int)(Math.sqrt(ay * ay + az * az) + 0.5);
			if(ax < 0){
				u = -u;
			}
			if(ay < 0){
				v = -v;
			}
			System.out.println("["+(Math.sqrt(ax * ax + az * az) + 0.5)+";"+(Math.sqrt(ay * ay + az * az) + 0.5)+"]");	
			if (glSurfaceView != null) {
				glSurfaceView.setPosition(new float[]{u, v});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

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
