package com.example.hellotriangle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.transition.Scene;
import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

public class SceneObject {
	public FloatBuffer mVertexBuffer;
	public FloatBuffer mColorBuffer;
	public float[] mModelMatrix = new float[16];
	public final float[] mTransMatrix = new float[16];
	public final float[] mRotMatrix = new float[16];

	Vector3 forward = Vector3.forward();
	Vector3 up = Vector3.up();
	Vector3 right = Vector3.right();

	static private final float[] vp = new float[16];

	public ObjLoader objLoader;

	public final String vertexShaderCode =
			// This matrix member variable provides a hook to manipulate
			// the coordinates of the objects that use this vertex shader
			"uniform mat4 uMVPMatrix;" +
					"attribute vec4 vPosition;" +
					"attribute vec4 vColor;" +
					"varying vec4 vertex_color;      \n" +
					"void main() {" +
					// the matrix must be included as a modifier of gl_Position
					"  gl_Position = uMVPMatrix * vPosition;" +
					"  vertex_color = vColor;" +
					"}";


	public final String fragmentShaderCode =
			"precision mediump float;" +
					"varying vec4 vertex_color;      \n" +
					"void main() {" +
					"  gl_FragColor = vertex_color;" +
					"}";

	public ShortBuffer drawListBuffer;
	public int mProgram;
	public int mPositionHandle;
	public int mColorHandle;
	public int mMVPMatrixHandle;
	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;

	public final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

	float vColor[];

	public SceneObject()
	{

		Matrix.setIdentityM(mTransMatrix, 0);
		Matrix.setIdentityM(mRotMatrix, 0);

		// prepare shaders and OpenGL program
		int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		Matrix.setIdentityM(mModelMatrix, 0);

		mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
		MyGLRenderer.checkGlError("glAttachShader");
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
		MyGLRenderer.checkGlError("glAttachShader");
		GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
		MyGLRenderer.checkGlError("glLinkProgram");
	}

	public void draw(float[] mvpMatrix) {
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		// Associate color array with vertex shader input
		mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
		GLES20.glEnableVertexAttribArray(mColorHandle);
		GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, mColorBuffer);


		// hook up  vertex array with vertex shader input
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		MyGLRenderer.checkGlError("glGetAttribLocation");
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, mVertexBuffer);

		// send matrix to vertex shader
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");


		// Draw the ground
		/*
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, base.connect.length,
				GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
		MyGLRenderer.checkGlError("glDrawElements");
		*/

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, objLoader.positions.length / 3);
		GraphicsUtils.checkGlError("glDrawArrays");

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}

	public float[] getModelMatrix()
	{
		return mModelMatrix;
	}

	//Update the model matrix without referencing the parent's root matrix
	public void updateModelMatrix() {

	}

	//Update the model matrix with references to the parent's root matrix
	public void updateModelMatrix(float[] root, Vector3 _forward, Vector3 _up, Vector3 _right)
	{

	}

	public static void setVP(float[] aVP)
	{
		Matrix.setIdentityM(vp, 0);
		Matrix.multiplyMM(vp, 0, vp, 0, aVP, 0);

		//Log.d("Matrix mult test: ", ""+vp.equals(aVP));
	}

	public static float[] getVP()
	{
		return vp;
	}

	public void setColor(float r, float g, float b)
	{
		for(int i=0; i<vColor.length-2; i+=3)
		{
			vColor[i] = r;
			vColor[i+1] = g;
			vColor[i+2] = b;
		}
	}
}
