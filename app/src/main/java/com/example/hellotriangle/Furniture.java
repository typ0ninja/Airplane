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

public class Furniture {

    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mColorBuffer;
    public Vector3 dimensions = new Vector3(.5f, .5f, .5f);
    private Vector3 center = new Vector3(0, 0, 0);

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


    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    float dTop, dBottom, dLeft, dRight, dForward, dBack;// d values for collision

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    static float vertices[] = {
            //starting at bottom left corner going counter clockwise
          0, 0, 0, //bottom left vert
            .5f, 0, 0,
            .5f, 0, .5f,
            0, 0, .5f,
           //top
            0, .5f, 0,
            .5f, .5f, 0,
            .5f, .5f, .5f,
            0, .5f, .5f,

    };

    static float vColor[] = {
            0, 0, 255,
            0, 0, 255,
            0, 0, 255,
            0, 0, 255,

            0, 0, 255,
            0, 0, 255,
            0, 0, 255,
            0, 0, 255
    };

    static short connectivity[] = {

            0, 1, 2,//bottom
            0, 3, 2,

            4, 5, 6,//top
            3, 7, 6,

            3, 4, 0,//left
            3, 7, 4,

            2, 1, 5,//right
            2, 6, 5,

            0, 1, 5,//front
            0, 4, 5,

            3, 2, 6,//back
            3, 7, 6


    };

    public Furniture() {

        // Buffers to be passed to gl*Pointer() functions must be
        // direct, i.e., they must be placed on the native heap
        // where the garbage collector cannot move them.
        //
        // Buffers with multi-byte data types (e.g., short, int,
        // float) must have their byte order set to native order

        //dist between verts is .5
        dimensions = new Vector3(1f, 1f, 1f);
        setCenter(0, 0, 0);
        setHeight(1f);
        setWidth(1f);
        setLength(1f);
        setDval();
        for(int i = 0; i<connectivity.length; i++ )
        {
            connectivity[i]= (short) (connectivity[i]);
        }


        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);


        ByteBuffer nbb = ByteBuffer.allocateDirect(vColor.length * 4);
        nbb.order(ByteOrder.nativeOrder());
        mColorBuffer = nbb.asFloatBuffer();
        mColorBuffer.put(vColor);
        mColorBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(connectivity.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(connectivity);
        drawListBuffer.position(0);


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

    }

    public Furniture(float width, float height, float length, float X, float Y, float Z) {

        // Buffers to be passed to gl*Pointer() functions must be
        // direct, i.e., they must be placed on the native heap
        // where the garbage collector cannot move them.
        //
        // Buffers with multi-byte data types (e.g., short, int,
        // float) must have their byte order set to native order

        //dist between verts is .5
        setCenter(X, Y, Z);
        setHeight(height);
        setWidth(width);
        setLength(length);
        setDval();
        for(int i = 0; i<connectivity.length; i++ )
        {
            connectivity[i]= (short) (connectivity[i]);
        }


        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);


        ByteBuffer nbb = ByteBuffer.allocateDirect(vColor.length * 4);
        nbb.order(ByteOrder.nativeOrder());
        mColorBuffer = nbb.asFloatBuffer();
        mColorBuffer.put(vColor);
        mColorBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(connectivity.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(connectivity);
        drawListBuffer.position(0);


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
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, connectivity.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        MyGLRenderer.checkGlError("glDrawElements");

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }

    public void setHeight(float height)
    {
        //1,4,7,10 bottom
        //13, 16, 19, 22 top
        dimensions.Y = height;
        //bottom
        vertices[1] = -height/2;
        vertices[4] = -height/2;
        vertices[7] = -height/2;
        vertices[10] = -height/2;
        //top
        vertices[13] = height/2;
        vertices[16] = height/2;
        vertices[19] = height/2;
        vertices[22] = height/2;
    }

    public void setWidth(float width)
    {
        dimensions.X = width;
        //left
        vertices[0] = -width/2;
        vertices[9] = -width/2;
        vertices[12] = -width/2;
        vertices[21] = -width/2;
        //right
        vertices[3] = width/2;
        vertices[6] = width/2;
        vertices[15] = width/2;
        vertices[18] = width/2;
    }

    public void setLength(float length)
    {
        dimensions.Z = length;
        //close
        vertices[2] = -length/2;
        vertices[5] = -length/2;
        vertices[14] = -length/2;
        vertices[17] = -length/2;
        //far
        vertices[8] = length/2;
        vertices[11] = length/2;
        vertices[20] = length/2;
        vertices[23] = length/2;
    }

    public void setDval()
    {
        //dTop, dBottom, dLeft, dRight, dForward, dBack;// d values for collision
        Vector3 botPos = center;
        botPos.Y -= dimensions.Y/2;

        Vector3 topPos = center;
        topPos.Y += dimensions.Y/2;

        Vector3 leftPos = center;
        leftPos.X += dimensions.X/2;

        Vector3 rightPos = center;
        rightPos.X -= dimensions.X/2;

        Vector3 frontPos = center;
        frontPos.Z += dimensions.Z/2;

        Vector3 backPos = center;
        backPos.Z -= dimensions.Z/2;

        dBottom = -(Vector3.dot(Vector3.down(), botPos));
        dTop = -(Vector3.dot(Vector3.up(), topPos));
        dLeft = -(Vector3.dot(Vector3.left(), leftPos));
        dRight = -(Vector3.dot(Vector3.right(), rightPos));
        dForward = -(Vector3.dot(Vector3.forward(), frontPos));
        dBack = -(Vector3.dot(Vector3.backward(), backPos));

    }

    public void setCenter(float X, float Y, float Z)
    {
        center = new Vector3(X, Y, Z);
    }

    public Vector3 getCenter()
    {
        return center;
    }


}
