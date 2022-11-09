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

public class Fan extends SceneObject{
	private final float[] mMotorMatrix = new float[16];

	Motor aMotor;

	public Fan(Context context)
	{
		super();

		objLoader = new ObjLoader(context, "base.obj");
		vColor = new float[objLoader.positions.length];
		setColor(.5f, .5f, 0);

		Matrix.setIdentityM(mMotorMatrix, 0);

		updateModelMatrix();

		//Matrix.multiplyMM(mBaseMatrix, 0, mMVPMatrix, 0, mBaseMatrix, 0); //mult plane into mvp and store  back in plane

		aMotor = new Motor(context);

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

		aMotor.updateModelMatrix(mModelMatrix, forward, up, right);
		//aMotor.setVP(SceneObject.getVP());
		Matrix.multiplyMM(mMotorMatrix, 0, SceneObject.getVP(), 0, aMotor.getModelMatrix(), 0);
		aMotor.draw(mMotorMatrix);
		//aMotor.draw(mvpMatrix);

		super.draw(mvpMatrix);
	}

	public void updateModelMatrix() {
		Matrix.translateM(mTransMatrix, 0, 10f, 0, 0);//Fan translation
		Matrix.rotateM(mRotMatrix, 0, mRotMatrix, 0, 45f, 0, 1, 0);
		Matrix.scaleM(mTransMatrix, 0, .3f, .3f, .3f);//Fan scale

		Matrix.multiplyMM(mModelMatrix, 0, mRotMatrix, 0, mTransMatrix, 0); //mult air into trans and store in plane

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

	public void updateModelMatrix(float[] root, Vector3 _forward, Vector3 _up, Vector3 _right)
	{

	}

}
