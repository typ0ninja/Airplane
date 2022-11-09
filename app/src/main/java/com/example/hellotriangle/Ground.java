
package com.example.hellotriangle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

class Ground {
	private final FloatBuffer mVertexBuffer;
	//private final FloatBuffer mColorBuffer;

	private final FloatBuffer mTexCoordBuffer;

	private final String vertexShaderCode =
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


	private final String fragmentShaderCode =
			"precision mediump float;" +
					"varying vec4 vertex_color;      \n" +
					"void main() {" +
					"  gl_FragColor = vertex_color;" +
					"}";


	//private final ShortBuffer drawListBuffer;
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;
	private static int gridWidth = 100;
	private static int gridHeight = 100;
	private int gDim = gridWidth;
	private int mTexCoordHandle;
	private int mTextureDataHandle;
	private int mTextureUniformHandle;
	private boolean purpSquare = true;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;
	// number of texture coordinates per vertex in this array
	static final int TEX_COORDS_PER_VERTEX = 2;

	private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	private final int texVertexStride = TEX_COORDS_PER_VERTEX * 4; // 4 bytes per texture coordinate
	float color[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};

	static float[] vertices = new float[(gridHeight) * (gridWidth) * 6 * 3];//4 verts per square, 3 coords per vert
	//static float[] vertices = new float[18*gridWidth];//4 verts per square, 3 coords per vert

	static float[] vColor = new float[(gridHeight) * (gridWidth) * 4 * 3];
	static short[] connectivity = new short[2 * 3 * gridHeight * gridWidth]; //2 connections per square * 3 verts * height * width

	static float vUV[] = new float[(gridHeight) * (gridWidth) * 2 * 6];

/*
	    static float vertices[] = //generate this in nested for loop form
	        {
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f,  0.0f,
            0.0f, -0.5f,  0.0f,
            0.0f, -0.5f, -0.5f,

            -0.5f, -0.5f, 0.f,
            -0.5f, -0.5f,  0.5f,
            0.0f, -0.5f,  0.5f,
            0.0f, -0.5f, 0.f,

            0.f, -0.5f, -0.5f,
            0.f, -0.5f,  0.f,
            0.5f, -0.5f,  0.f,
            0.5f, -0.5f, -0.5f,

            0.f, -0.5f, 0.f,
            0.f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f, -0.5f, 0.f
	         };



	    static float vColor[] =
	        {
            0.4f, 0.4f, 0.8f,
            0.4f, 0.4f, 0.8f,
            0.4f, 0.4f, 0.8f,
            0.4f, 0.4f, 0.8f,

            0.3f, 0.2f, 0.1f,
            0.3f, 0.2f, 0.1f,
            0.3f, 0.2f, 0.1f,
            0.3f, 0.2f, 0.1f,

            0.3f, 0.2f, 0.1f,
            0.3f, 0.2f, 0.1f,
            0.3f, 0.2f, 0.1f,
            0.3f, 0.2f, 0.1f,

            0.4f, 0.4f, 0.8f,
            0.4f, 0.4f, 0.8f,
            0.4f, 0.4f, 0.8f,
            0.4f, 0.4f, 0.8f

	         };


	      static short connectivity[] =
	          {
	             0, 2, 1,
	             0, 3, 2,

	             4, 5, 6,
	             4, 7, 6,

	             8, 9, 10,
	             8, 11, 10,

	             12, 15, 14,
	             12, 14, 13
	          };      
	      */

	/*
	    static float vertices[] =
			    {
					    -0.5f, -0.5f, 0.0f,
					    0.5f,  0.5f, 0.0f,
					    -0.5f, 0.5f, 0.0f,
					    -0.5f, -0.5f, 0.0f,
					    0.5f,  -0.5f, 0.0f,
					    0.5f,  0.5f, 0.0f

					    //0, 3 shared and 1,5 shared
			    };


	    static float vUV[] =
			    {
					    0.0f, 1.0f,
					    1.0f, 0.0f,
					    0.0f, 0.0f,
					    0.0f, 1.0f,
					    1.0f, 1.0f,
					    1.0f, 0.0f,
			    };
	      */
	public Ground() {

		// Buffers to be passed to gl*Pointer() functions must be
		// direct, i.e., they must be placed on the native heap
		// where the garbage collector cannot move them.
		//
		// Buffers with multi-byte data types (e.g., short, int,
		// float) must have their byte order set to native order

		//dist between verts is .5
		genTexCoord();
		genVert();


	   /*
	   for (int i = 0; i<48; i++)
	   {
	   	Log.d("array", Float.toString(vertices[i]));
	   }
*/

	   /*
	   ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
*/
		/*
		ByteBuffer nbb = ByteBuffer.allocateDirect(vColor.length * 4);
		nbb.order(ByteOrder.nativeOrder());
		mColorBuffer = nbb.asFloatBuffer();
		mColorBuffer.put(vColor);
		mColorBuffer.position(0);

		 */


		/*
        ByteBuffer dlb = ByteBuffer.allocateDirect(connectivity.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(connectivity);
        drawListBuffer.position(0);
		*/
      /*
      // prepare shaders and OpenGL program
      int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                                                 vertexShaderCode);
      int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                   fragmentShaderCode);

      mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
      GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
      MyGLRenderer.checkGlError("glAttachShader");
      GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
      MyGLRenderer.checkGlError("glAttachShader");
      GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
      MyGLRenderer.checkGlError("glLinkProgram");
*/

		mVertexBuffer = GraphicsUtils.allocDBFloat(vertices); // sets up gl buffer for verts
		mTexCoordBuffer = GraphicsUtils.allocDBFloat(vUV);      // sets up gl buffer for uv coords

		//mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program

		mProgram = GraphicsUtils.setShaders("txts.vs", "txts.ps"); // loads shaders

		mTextureDataHandle = GraphicsUtils.loadTexture(R.drawable.moonsurface); // loads texture image

	   /*
	   GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
	   MyGLRenderer.checkGlError("glAttachShader");
	   GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
	   MyGLRenderer.checkGlError("glAttachShader");
	   GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
	   MyGLRenderer.checkGlError("glLinkProgram");
	*/


	}

	public void airplane() {

		for (int i = 0; i < gridHeight; i++)//height
		{
			for (int j = 0; j < gridWidth; j++)//width
			{

			}//end inner for
		}//end outter for
	}

	public void resetLoc() {
		genVert();
	}

	public void genVert()
	{
		for (int i = 0; i < gridHeight; i++)//height
		{
			for (int j = 0; j < gridWidth; j++)//width
			{
				//generate verts

				float x = (float) i - gridWidth/2.0f;
				float y = -.5f;
				float z = (float) j - gridHeight/2.0f;

				int offset = (i * gridWidth * 18 + j * 18);
				//+ (gridWidth/2)
				vertices[offset + 0] = x; //x
				vertices[offset + 1] = y; //y
				vertices[offset + 2] = z; //z

				vertices[offset + 3] = x + 1f;
				vertices[offset + 4] = y;
				vertices[offset + 5] = z + 1f;

				vertices[offset + 6] = x;
				vertices[offset + 7] = y;
				vertices[offset + 8] = z + 1f;

				vertices[offset + 9] = x;
				vertices[offset + 10] = y;
				vertices[offset + 11] = z;

				vertices[offset + 12] = x + 1f;
				vertices[offset + 13] = y;
				vertices[offset + 14] = z;

				vertices[offset + 15] = x + 1f;
				vertices[offset + 16] = y;
				vertices[offset + 17] = z + 1f;
				//+ (gridWidth/2)
			   /*
			            -0.5f, -0.5f, 0.0f,
					   0.5f,  0.5f, 0.0f,
					   -0.5f, 0.5f, 0.0f,
					   -0.5f, -0.5f, 0.0f,
					   0.5f,  -0.5f, 0.0f,
					   0.5f,  0.5f, 0.0f

			   //0, 3 shared and 1,5 shared
			   */

			   /*
				float x = (float) (.5f * i - (.5 * gridHeight / 2));
				float y = -.5f;
				float z = (float) (.5f * j - (.5 * gridWidth / 2));

				int offset = (i * gridWidth * 18 + j * 18);


				vertices[offset + 0] = x; //x
				vertices[offset + 1] = y; //y
				vertices[offset + 2] = z; //z

				vertices[offset + 3] = x + .5f;
				vertices[offset + 4] = y;
				vertices[offset + 5] = z + .5f;

				vertices[offset + 6] = x;
				vertices[offset + 7] = y;
				vertices[offset + 8] = z + .5f;

				vertices[offset + 9] = x;
				vertices[offset + 10] = y;
				vertices[offset + 11] = z;

				vertices[offset + 12] = x + .5f;
				vertices[offset + 13] = y;
				vertices[offset + 14] = z;

				vertices[offset + 15] = x + .5f;
				vertices[offset + 16] = y;
				vertices[offset + 17] = z + .5f;
*/

			}//end inner for
		}//end outer for
	}

	public void genTexCoord()
	{

		for (int i = 0; i < gridHeight; i++)//height
		{
			for (int j = 0; j < gridWidth; j++)//width
			{
				float x = (float) (1 * i - (1 * gridHeight / 2));

				float y = (float) (1 * j - (1 * gridWidth / 2));

				int offset = (i * gridWidth * 12 + j * 12);
				/*
				        0.0f, 1.0f,
					    1.0f, 0.0f,
					    0.0f, 0.0f,
					    0.0f, 1.0f,
					    1.0f, 1.0f,
					    1.0f, 0.0f,
				 */

				vUV[offset + 0] = 0;
				vUV[offset + 1] = 1;

				vUV[offset + 2] = 1f;
				vUV[offset + 3] = 0;

				vUV[offset + 4] = 0;
				vUV[offset + 5] = 0;

				vUV[offset + 6] = 0;
				vUV[offset + 7] = 1;

				vUV[offset + 8] = 1f;
				vUV[offset + 9] = 1;

				vUV[offset + 10] = 1;
				vUV[offset + 11] = 0;

				//vUV;
			}
		}

	}

   public void draw(float[] mvpMatrix) { 
	   
       // Add program to OpenGL environment
	   // Add program to OpenGL environment
	   if (mProgram == -1) return;

       GLES20.glUseProgram(mProgram);
       
       // Associate color array with vertex shader input
	   /*
       mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
       GLES20.glEnableVertexAttribArray(mColorHandle);
       GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                                    GLES20.GL_FLOAT, false,
                                    vertexStride, mColorBuffer);
       */

       // hook up  vertex array with vertex shader input
       mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
       MyGLRenderer.checkGlError("glGetAttribLocation");
       GLES20.glEnableVertexAttribArray(mPositionHandle);
       GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                    GLES20.GL_FLOAT, false,
                                    vertexStride, mVertexBuffer);


	   mTexCoordHandle = glGetAttribLocation(mProgram, "aTexPos");
	   glEnableVertexAttribArray(mTexCoordHandle);
	   glVertexAttribPointer(mTexCoordHandle, TEX_COORDS_PER_VERTEX,
			   GL_FLOAT, false,
			   texVertexStride, mTexCoordBuffer);

       // send matrix to vertex shader
       mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
       MyGLRenderer.checkGlError("glGetUniformLocation");
       GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
       MyGLRenderer.checkGlError("glUniformMatrix4fv");

       mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "uSampler");

	   // Set the active texture unit to texture unit 0.
	   GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

	   // Bind the texture to this unit.
	   GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

	   // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
	   GLES20.glUniform1i(mTextureUniformHandle, 0);

       // Draw the ground
       //GLES20.glDrawElements(GLES20.GL_TRIANGLES, connectivity.length,
       //                      GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
       //MyGLRenderer.checkGlError("glDrawElements");

	   GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 3);
	   GraphicsUtils.checkGlError("glDrawArrays");

       // Disable vertex array
       GLES20.glDisableVertexAttribArray(mPositionHandle);

  }
   
  
}
