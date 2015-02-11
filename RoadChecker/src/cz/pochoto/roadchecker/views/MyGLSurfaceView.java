package cz.pochoto.roadchecker.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

@SuppressLint("ClickableViewAccessibility")
public class MyGLSurfaceView extends GLSurfaceView {

	private final MyGLRenderer mRenderer;
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
	private float mPreviousX;
	private float mPreviousY;
	private TextView textView;

	public MyGLSurfaceView(Context context) {
		super(context);
		mRenderer = new MyGLRenderer();
		init();

	}

	public MyGLSurfaceView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		mRenderer = new MyGLRenderer();
		init();
	}

	public void init() {
		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);
		super.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		// Set the Renderer for drawing on the GLSurfaceView
		setRenderer(mRenderer);
		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// MotionEvent reports input details from the touch screen
		// and other input controls. In this case, you are only
		// interested in events where the touch position changed.

		float x = e.getX();
		float y = e.getY();

		switch (e.getAction()) {
		case MotionEvent.ACTION_MOVE:

			float dx = x - mPreviousX;
			float dy = y - mPreviousY;

			// reverse direction of rotation above the mid-line
			if (y > getHeight() / 2) {
				dx = dx * -1;
			}

			// reverse direction of rotation to left of the mid-line
			if (x < getWidth() / 2) {
				dy = dy * -1;
			}

			mRenderer.setAngle(mRenderer.getAngle()
					+ ((dx + dy) * TOUCH_SCALE_FACTOR)); // = 180.0f / 320
			requestRender();
		}

		mPreviousX = x;
		mPreviousY = y;
		return true;
	}

	public void setPosition(float[] f) {
		mRenderer.setPosition(f);		
		requestRender();
	}

	public void setSensorManager(SensorManager sensorManager) {
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

			sensorManager.registerListener(new SensorEventListener() {

				@Override
				public void onSensorChanged(SensorEvent event) {
					float[] f = event.values;
					
					textView.setText(Math.round(f[0])+"\n"+Math.round(f[1])+"\n"+Math.round(f[2]));
					setPosition(f);
				}

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
				}
			}, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_UI);
		}

	}

	public void setTextView(TextView glSurfaceTextView) {
		this.textView = glSurfaceTextView;
		
	}

}
