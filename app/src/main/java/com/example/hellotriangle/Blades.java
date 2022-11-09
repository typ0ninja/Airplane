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

public class Blades extends SceneObject {

	public Blades(Context context)
	{
		super();
		objLoader = new ObjLoader(context, "fanBlades_3.obj");

		vColor = new float[objLoader.positions.length];
		setColor(.5f, 0, .5f);

		Matrix.translateM(mTransMatrix, 0, 0f, 0f, 1.45f);//Fan translation
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

	public void updateModelMatrix(float[] root, Vector3 _forward, Vector3 _up, Vector3 _right)
	{
		Matrix.rotateM(mRotMatrix, 0, -20, 0, 0, 1);

		float[] mBladeRotTransMatrix = new float[16];
		Matrix.multiplyMM(mBladeRotTransMatrix, 0, mTransMatrix, 0, mRotMatrix, 0);
		Matrix.multiplyMM(mModelMatrix, 0, root, 0, mBladeRotTransMatrix, 0); //mult air into trans and store in plane

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

