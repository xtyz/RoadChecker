package cz.pochoto.roadchecker.listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class AccelerometterListener implements AbstractSensorEventListener {
	private TextView accelerometerLabel, gyroscopeLabel;
	public float x0 = 0, y0 = 0, z0 = 0;
	float ax, ay, az;

	@Override
	public void onSensorChanged(SensorEvent event) {
		// hodnoty x,y,z ze senzoru
		float[] f = event.values;
		// hodnoty x,y pro zobrazení ve 2D
		float x, y;

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
				double g0 = Math.sqrt(x0 * x0 + y0 * y0 + z0 * z0);
				double g = Math.sqrt(f[0] * f[0] + f[1] * f[1] + f[2] * f[2]);
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
			float max = f[0];
			if (f[1] > max) {
				max = f[1];
			}
			if (f[2] > max) {
				max = f[2];
			}

			float min = f[0];
			if (f[1] < min) {
				min = f[1];
			}
			if (f[2] < min) {
				min = f[2];
			}

			// vypocet udelat zvlast pro vsechny osy
			if (Math.abs(min) > Math.abs(max)) {
				if (min == f[0]) {
					// -x tlacitka vlevo
					int u = (int)(Math.sqrt(ay * ay + ax * ax) + 0.5);
					int v = (int)(Math.sqrt(az * az + ax * ax) + 0.5);
					if(ay > 0){
						u = -u;
					}
					if(az < 0){
						v = -v;
					}
					System.out.println("["+u+";"+v+"]");
				} else if (min == f[1]) {
					// -y vzhuru nohama - tlacitka nahore
				} else if (min == f[2]) {
					// -z - display dolu
				}
			} else {
				if (max == f[0]) {
					// x tlacitka v pravo
					int u = (int)(Math.sqrt(ay * ay + ax * ax) + 0.5);
					int v = (int)(Math.sqrt(az * az + ax * ax) + 0.5);
					if(ay > 0){
						u = -u;
					}
					if(az < 0){
						v = -v;
					}
					System.out.println("["+u+";"+v+"]");
				} else if (max == f[1]) {
					// y tlacitka dole
					int u = (int)(Math.sqrt(ax * ax + ay * ay) + 0.5);
					int v = (int)(Math.sqrt(az * az + ay * ay) + 0.5);
					if(ax > 0){
						u = -u;
					}
					if(az < 0){
						v = -v;
					}
					System.out.println("["+u+";"+v+"]");	
				} else if (max == f[2]) {
					// z na zadech					
					//vypocitat pyt.v. mezi u = ax-az a v=ay-az vypocitat  
										
					int u = (int)(Math.sqrt(ax * ax + az * az) + 0.5);
					int v = (int)(Math.sqrt(ay * ay + az * az) + 0.5);
					if(ax > 0){
						u = -u;
					}
					if(ay < 0){
						v = -v;
					}
					System.out.println("["+u+";"+v+"]");				
					
					
				}
			}
			;

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

}
