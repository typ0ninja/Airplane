
package com.example.hellotriangle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import static java.security.AccessController.getContext;

class Airplane extends SceneObject {

	public Airplane(Context context) {
		super();

		// Buffers to be passed to gl*Pointer() functions must be
		// direct, i.e., they must be placed on the native heap
		// where the garbage collector cannot move them.
		//
		// Buffers with multi-byte data types (e.g., short, int,
		// float) must have their byte order set to native order

		//dist between verts is .5
		//direction = new Vector3(0, 0, 1);


		objLoader = new ObjLoader(context, "ship_obj_5.obj");
		vColor = new float[objLoader.positions.length];
		setColor(.8f, 0, 0);

		ByteBuffer vbb = ByteBuffer.allocateDirect(objLoader.positions.length * 4 * 3);
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

}
