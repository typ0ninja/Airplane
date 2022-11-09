package com.example.hellotriangle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import static com.example.hellotriangle.GraphicsUtils.context;

public class Motor extends SceneObject {
	//public final float[] mTransMatrix = new float[16];
	private final float[] mBladesMatrix = new float[16];
	Blades aBlades;

	public Motor(Context context)
	{
		super();

		objLoader = new ObjLoader(context, "motor_3.obj");

		Matrix.translateM(mTransMatrix, 0, 0f, 2.509f, -.367f);//Fan translation

		vColor = new float[objLoader.positions.length];
		setColor(0, .5f, .5f);

		Matrix.setIdentityM(mBladesMatrix, 0);

		aBlades = new Blades(context);

		ByteBuffer vbb = ByteBuffer.allocateDirect(objLoader.positions.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(objLoader.positions);
		mVertexBuffer.position(0);


		ByteBuffer nbb = ByteBuffer.allocateDirect(vColor.length * 4);
		nbb.order(ByteOrder.nativeOrder());
		mColorBuffer = nbb.asFloatBuffer();
		mColorBuffer.put(vColor);
		mColorBuffer.position(0);

		ByteBuffer dlb = ByteBuffer.allocateDirect(objLoader.connect.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(objLoader.connect);
		drawListBuffer.position(0);
	}

	public void draw(float[] mvpMatrix) {
		//TODO: update blade position and orientation here

		aBlades.updateModelMatrix(mModelMatrix, forward, up, right);
		//aMotor.setVP(SceneObject.getVP());
		Matrix.multiplyMM(mBladesMatrix, 0, SceneObject.getVP(), 0, aBlades.getModelMatrix(), 0);
		aBlades.draw(mBladesMatrix);
		//aMotor.draw(mvpMatrix);

		super.draw(mvpMatrix);
	}

	public void updateModelMatrix(float[] root, Vector3 _forward, Vector3 _up, Vector3 _right)
	{
		Matrix.rotateM(mRotMatrix, 0, 1, 0, 1, 0);

		float[] mMotorRotTransMatrix = new float[16];
		Matrix.multiplyMM(mMotorRotTransMatrix, 0, mTransMatrix, 0, mRotMatrix, 0);
		Matrix.multiplyMM(mModelMatrix, 0, root, 0, mMotorRotTransMatrix, 0); //mult air into trans and store in plane

		forward.X = mRotMatrix[8];
		forward.Y = mRotMatrix[9];
		forward.Z = mRotMatrix[10];
		forward.normalize();

		up.X = mRotMatrix[4];
		up.Y = mRotMatrix[5];
		up.Z = mRotMatrix[6];
		up.normalize();

		right.X = mRotMatrix[0];
		right.Y = mRotMatrix[1];
		right.Z = mRotMatrix[2];
		right.normalize();
	}
}
