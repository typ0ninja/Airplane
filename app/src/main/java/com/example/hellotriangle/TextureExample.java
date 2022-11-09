
package com.example.hellotriangle;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

class TextureExample {

		private  final FloatBuffer mVertexBuffer;
		private final FloatBuffer mTexCoordBuffer;
		private final int mProgram;
		private int mPositionHandle;
		private int mMVPMatrixHandle;

	    
	    // number of coordinates per vertex in this array
	    static final int COORDS_PER_VERTEX = 3;
	    // number of texture coordinates per vertex in this array
	    static final int TEX_COORDS_PER_VERTEX = 2;

	    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	    private final int texVertexStride = TEX_COORDS_PER_VERTEX * 4; // 4 bytes per texture coordinate
	    

		private int mTexCoordHandle;
		private int  mTextureDataHandle;
		private int  mTextureUniformHandle;
		      
	    static float vertices[] =
	        {
					-0.5f, -0.5f, 0.0f,
					0.5f,  0.5f, 0.0f,
					-0.5f, 0.5f, 0.0f,
					-0.5f, -0.5f, 0.0f,
					0.5f,  -0.5f, 0.0f,
					0.5f,  0.5f, 0.0f
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

		    
     
		      
	public TextureExample() {
		   
	        mVertexBuffer = GraphicsUtils.allocDBFloat (vertices); // sets up gl buffer for verts
	 	    mTexCoordBuffer = GraphicsUtils.allocDBFloat (vUV);	  // sets up gl buffer for uv coords   
	 	     
	        mProgram = GraphicsUtils.setShaders ("txts.vs", "txts.ps" ); // loads shaders
	         
	        mTextureDataHandle = GraphicsUtils.loadTexture(R.drawable.sullivan); // loads texture image
	}


 

   public void draw(float[] mvpMatrix) { 
	   
       // Add program to OpenGL environment
	   if (mProgram == -1) return;
       
       GLES20.glUseProgram(mProgram);             

       // hook up  vertex array with vertex shader input
       mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
       GraphicsUtils.checkGlError("glGetAttribLocation");
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
       GraphicsUtils.checkGlError("glGetUniformLocation");
       GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
       GraphicsUtils.checkGlError("glUniformMatrix4fv"); 

       mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "uSampler");

       // Set the active texture unit to texture unit 0.
       GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    
       // Bind the texture to this unit.
       GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
    
       // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
       GLES20.glUniform1i(mTextureUniformHandle, 0);

       GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 3);
       GraphicsUtils.checkGlError("glDrawArrays");

       // Disable vertex array
       GLES20.glDisableVertexAttribArray(mPositionHandle);

  }
   
  
}
