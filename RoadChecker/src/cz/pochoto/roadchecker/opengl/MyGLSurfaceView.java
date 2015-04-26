package cz.pochoto.roadchecker.opengl;

import cz.pochoto.roadchecker.listeners.MySensorEventListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Custom implementation of {@link android.opengl.GLSurfaceView}
 * @author Tomáš Pochobradský
 *
 */
@SuppressLint("ClickableViewAccessibility")
public class MyGLSurfaceView extends GLSurfaceView {

	private final MyGLRenderer mRenderer;
	private MySensorEventListener sensorEventListener;

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

	private void init() {
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
		return true;
	}
	
	@Override
	public void onResume() {
		this.sensorEventListener.setSurfaceView(this);
		super.onResume();
	}

	public void setTrianglePosition(float[] f) {
		mRenderer.setTrianglePosition(f);
	}
	
	/**
	 * {@link cz.pochoto.roadchecker.opengl.MyGLRenderer#setXYSquareScale(double)}
	 * @param scale
	 */
	public void setXYSquareScale(double scale){
		mRenderer.setXYSquareScale(scale);
	}

	public void setSensorEventListener(
			MySensorEventListener sensorEventListener) {
		this.sensorEventListener = sensorEventListener;
		this.sensorEventListener.setSurfaceView(this);
		
	}
	
	/**
	 * {@link  android.opengl.GLSurfaceView#requestRender()}	
	 */
	public void render() {
		requestRender();		
	}

	/**
	 * {@link cz.pochoto.roadchecker.opengl.MyGLRenderer#setDisplacementSquareScale(double)}
	 * @param scale
	 */
	public void setDisplacementSquareScale(double scale) {
		this.mRenderer.setDisplacementSquareScale(scale);		
	}

	/**
	 * {@link cz.pochoto.roadchecker.opengl.MyGLRenderer#setAvgZSquareScale(double)}
	 * @param scale
	 */
	public void setAvgZSquareScale(double scale) {
		this.mRenderer.setAvgZSquareScale(scale);
		
	}

}
