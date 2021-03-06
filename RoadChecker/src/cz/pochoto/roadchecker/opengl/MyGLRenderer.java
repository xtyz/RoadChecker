package cz.pochoto.roadchecker.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cz.pochoto.roadchecker.models.Square;
import cz.pochoto.roadchecker.models.Triangle;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class must
 * override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "MyGLRenderer";
	private Triangle mTriangle;
	private Square mSquare;
	
	private static float SLOW_DOWN = 0.0001f;
	private static float LIMIT = 0.1f;

	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	private final float[] mScaleMatrix = new float[16];
	private final float[] mModelMatrix = new float[16];

	private float mAngle;
	private float[] f = { 0, 0 };
	private float scale, maxScale;
	private float averageZ;

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.setIdentityM(mScaleMatrix, 0);
		mTriangle = new Triangle();
		mSquare = new Square();
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		float[] scratchTriangle = new float[16];
		float[] scratchSquare = new float[16];

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.setIdentityM(mScaleMatrix, 0);
		// Draw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		// Set the camera position (View matrix)
		Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

		Matrix.setIdentityM(mScaleMatrix, 0);
		
		// Draw average XY square		
		Matrix.scaleM(mScaleMatrix, 0, scale, scale, scale);
		Matrix.multiplyMM(scratchSquare, 0, mMVPMatrix, 0, mScaleMatrix, 0);
		mSquare.setColorBlue();
		mSquare.draw(scratchSquare);
		
		
		Matrix.setIdentityM(mScaleMatrix, 0);
		
		// Draw displacement Z square		
		Matrix.scaleM(mScaleMatrix, 0, 0.2f, maxScale, 0f);
		Matrix.translateM(mScaleMatrix, 0, -5f, 0, 0);
		Matrix.multiplyMM(scratchSquare, 0, mMVPMatrix, 0, mScaleMatrix, 0);
		mSquare.setColorRed();
		mSquare.draw(scratchSquare);

		
		Matrix.setIdentityM(mScaleMatrix, 0);
		
		// Draw current Z square		
		Matrix.scaleM(mScaleMatrix, 0, 0.2f, f[2], 0f);
		Matrix.translateM(mScaleMatrix, 0, -5f, 0, 0);
		Matrix.multiplyMM(scratchSquare, 0, mMVPMatrix, 0, mScaleMatrix, 0);
		mSquare.setColorGreen();
		mSquare.draw(scratchSquare);
		
				
		Matrix.setIdentityM(mScaleMatrix, 0);
		
		// Draw average Z square		
		Matrix.scaleM(mScaleMatrix, 0, 0.2f, averageZ, 0f);
		Matrix.translateM(mScaleMatrix, 0, -5f, 0, 0);
		Matrix.multiplyMM(scratchSquare, 0, mMVPMatrix, 0, mScaleMatrix, 0);
		mSquare.setColorBlue();
		mSquare.draw(scratchSquare);		
		
		
		// Draw triangle XY
		Matrix.translateM(mModelMatrix, 0, f[0] / 10f, -f[1] / 10f, f[2] / 10f);
		mTriangle.setTriangleCoords(f);
		Matrix.multiplyMM(scratchTriangle, 0, mMVPMatrix, 0, mModelMatrix, 0);
		mTriangle.draw(scratchTriangle);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		
		GLES20.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);

	}

	/**
	 * Utility method for compiling a OpenGL shader.
	 *
	 * <p>
	 * <strong>Note:</strong> When developing shaders, use the checkGlError()
	 * method to debug shader coding errors.
	 * </p>
	 *
	 * @param type
	 *            - Vertex or fragment shader type.
	 * @param shaderCode
	 *            - String containing the shader code.
	 * @return - Returns an id for the shader.
	 */
	public static int loadShader(int type, String shaderCode) {

		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	/**
	 * Utility method for debugging OpenGL calls. Provide the name of the call
	 * just after making it:
	 *
	 * <pre>
	 * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
	 * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
	 * </pre>
	 *
	 * If the operation is not successful, the check throws an error.
	 *
	 * @param glOperation
	 *            - Name of the OpenGL call to check.
	 */
	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}

	/**
	 * Returns the rotation angle of the triangle shape (mTriangle).
	 *
	 * @return - A float representing the rotation angle.
	 */
	public float getAngle() {
		return mAngle;
	}

	/**
	 * Sets the rotation angle of the triangle shape (mTriangle).
	 */
	public void setAngle(float angle) {
		mAngle = angle;
	}

	/**
	 * Sets position for triangle
	 * @param f
	 */
	public void setTrianglePosition(float[] f) {
		if (f[0] == 0 && f[1] == 0) {
			if (this.f[0] > LIMIT && this.f[0] < -LIMIT && this.f[1] > LIMIT && this.f[1] < -LIMIT) {
				if (this.f[0] > 0) {
					this.f[0] = this.f[0] - SLOW_DOWN;					
				} else {
					this.f[0] = this.f[0] + SLOW_DOWN;
				}
				if (this.f[1] > 0) {
					this.f[1] = this.f[1] - SLOW_DOWN;
				} else {
					this.f[1] = this.f[1] + SLOW_DOWN;
				}
			}else{
				this.f[0] = 0;
				this.f[1] = 0;
			}
		}
		this.f = f;
	}
	
	/**
	 * Sets average value for XY square
	 * @param scale
	 */
	public void setXYSquareScale(double scale){
		this.scale = (float)scale;
	}

	/**
	 * Sets displacement value for Z square
	 * @param scale
	 */
	public void setDisplacementSquareScale(double scale) {
		this.maxScale = (float)scale;
		
	}

	/**
	 * Sets average value for Z square
	 * @param averageZ
	 */
	public void setAvgZSquareScale(double averageZ) {
		this.averageZ = (float)averageZ;		
	}

}