/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.hellotriangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Debug;
import android.util.Log;
import android.view.View;

import java.util.LinkedList;
import java.util.Arrays;

import static com.example.hellotriangle.GraphicsUtils.context;


/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private Triangle mTriangle;
    private Square   mSquare;
    private Ground mGround;
    private Airplane mAirplane;
    public LevelMan levelMan;
    float stableAngle = 5f; //In degrees

    float stallCorrection = 0;
    //private Furniture block;

    int numLagFrames = 20;
    //float[] cameraLag = new float[9*numLagFrames];
    int frameCount = 0;

    public LinkedList <CamVals> cameraLag;
    //public volatile float mAccel[] = {0f, 0f, 0f};
    public volatile Vector3 mAccel = new Vector3(0, 0, 0);
    float steepScalar = 1f;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private final float[] mAccelMatrix = new float[16];
    private final float[] mAccelMatrixX = new float[16];
    private final float[] mAccelMatrixY = new float[16];
    private final float[] mAccelMatrixZ = new float[16];

    private final float[] mOffsetUp = new float[16];

    private final float[] mAirOrientationMatrix = new float[16];
    private final float[] mTransMatrix = new float[16];
    private final float[] mAirRotationMatrix = new float[16];
    private final float[] mAirRotationX = new float[16];
    private final float[] mAirRotationY = new float[16];
    private final float[] mAirRotationZ = new float[16];

    private final float[] mAirplaneMatrix = new float[16];
    private final float[] mAirTransMatrix = new float[16];

    private final float[] mTriangleMatrix = new float[16];
    private final float[] mGroundMatrix = new float[16];

    private final float[] mCamOffset = new float[16];
    private final float[] mBlockMatrix = new float[16];
    private final float[] mBlockTransMatrix = new float[16];
    private final float[] mLevelMan = new float[16];
    private final float[] mTransLevelMan = new float[16];
    public volatile float mDx, mDy;
    private float mAngle;
    private float xangle, yangle, zangle, yscalar, zscalar, speed;
    private Vector3 dir, cameraDir, camPos, lookAt, forward, up, right;
    private float xp;
    public float rotateShip;
    float noseDown = 0;
    float pitchSpeed, rollSpeed, yawSpeed;
    Vector3 stableUp;
    //private Context context = ;

    public ObjLoader aModel;
    public MyGLRenderer(Context icontext) {
        super();

        context = icontext;
        GraphicsUtils.context = context;

    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //aModel = new ObjLoader(config, "aFile");

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable( GLES20.GL_DEPTH_TEST );
        GLES20.glDepthFunc( GLES20.GL_LEQUAL );
        GLES20.glDepthMask( true );
        speed = 0f;
        camPos = new Vector3(0, 1, -2);
        lookAt = new Vector3(0f, 5f, 0f);
        dir = new Vector3(0, 0, 1);
        cameraDir = new Vector3(0, 1, -2);
        forward = new Vector3(0, 0, 1);
        up = new Vector3(0, 1, 0);
        right = new Vector3(1, 0, 0);
        xp = 0f;
        yscalar = 0f;
        zscalar = 1f;
        pitchSpeed = 8f;
        rollSpeed = 2f;
        yawSpeed = -2f;

        mTriangle = new Triangle();
        mSquare   = new Square();
        mGround = new Ground();
        mAirplane = new Airplane(context);


        Matrix.setIdentityM(mAirOrientationMatrix, 0); //complete orientation of plane via forward, up, and right
        yprToOrient();

        //block = new Furniture(3f, 3f, 3f);

        cameraLag = new LinkedList<CamVals>();

        Matrix.setIdentityM(mOffsetUp, 0);
        float[] mVectorOffset = {0, 1, 0, 1};
        Matrix.rotateM(mOffsetUp, 0, stableAngle, 1, 0, 0);
        Matrix.multiplyMV(mVectorOffset, 0,mOffsetUp, 0, mVectorOffset, 0);
        stableUp = new Vector3(mVectorOffset[0], mVectorOffset[1],  mVectorOffset[2]);
        stableUp.normalize();
    }

    public void yprToOrient()
    {
        mAirOrientationMatrix[8] = forward.X;
        mAirOrientationMatrix[9] = forward.Y;
        mAirOrientationMatrix[10] = forward.Z;

        mAirOrientationMatrix[4] = up.X;
        mAirOrientationMatrix[5] = up.Y;
        mAirOrientationMatrix[6] = up.Z;

        mAirOrientationMatrix[0] = right.X;
        mAirOrientationMatrix[1] = right.Y;
        mAirOrientationMatrix[2] = right.Z;
    }

    public void orientToYpr()
    {
        forward.X = mAirOrientationMatrix[8];
        forward.Y = mAirOrientationMatrix[9];
        forward.Z = mAirOrientationMatrix[10];
        forward.normalize();

        up.X = mAirOrientationMatrix[4];
        up.Y = mAirOrientationMatrix[5];
        up.Z = mAirOrientationMatrix[6];
        up.normalize();

        right.X = mAirOrientationMatrix[0];
        right.Y = mAirOrientationMatrix[1];
        right.Z = mAirOrientationMatrix[2];
        right.normalize();
    }



    @Override
    public void onDrawFrame(GL10 unused) {
        //camPos = camPos + dir*speed;
        //lookAt = Vector3.subtract(lookAt, Vector3.scalar(dir, speed));
        if (levelMan.overVent(lookAt))
        {
            lookAt.Y += .05f;
        }
        levelMan.updatePlanePos(lookAt);
        //dir = new Vector3((float)Math.cos(xangle) *(float)Math.sin(yangle), (float)Math.sin(xangle), (float)Math.cos(xangle) *(float)Math.cos(yangle));
        //dir = new Vector3(mAccel[0], mAccel[1], mAccel[2]);
        //dir.normalize();

        if (!levelMan.collision)
        {
            lookAt = Vector3.add(lookAt, Vector3.scalar(forward, speed));
        }
        else
        {
            lookAt = new Vector3(0, 10f, 0);
            mDx= 0f;
            mDy = 0f;
            speed = 0f;
            rotateShip = 0f;
            yangle = 0f;
            xangle = 0f;
            zangle = 0f;
            forward = new Vector3(0, 0, 1);
            up = new Vector3(0, 1, 0);
            right = new Vector3(1, 0, 0);
            camPos = new Vector3(0, 6, -2);
            cameraDir = new Vector3(0, 1, -2);
            levelMan.collision = false;
            frameCount = 0;
            cameraLag.clear();

        }






        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        //Matrix.setLookAtM(mViewMatrix, 0, 0, 1, -2, 0, 0, 0, 0f, 1.0f, 0.0f);

        //Matrix.setLookAtM(mViewMatrix, 0, camPos.X, camPos.Y, camPos.Z, lookAt.X, lookAt.Y, lookAt.Z, 0f, 1.0f, 0.0f); //fixed up
        //Matrix.setLookAtM(mViewMatrix, 0, 0, 2, -2, lookAt.X, lookAt.Y, lookAt.Z, up.X, up.Y, up.Z); //filter out camera movement.

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        Matrix.setIdentityM(mTransMatrix, 0);//used for ground
        Matrix.setIdentityM(mAirTransMatrix, 0);//moves plane
        Matrix.setIdentityM(mAirRotationMatrix, 0); //rotates plane
        Matrix.setIdentityM(mAirRotationX, 0);
        Matrix.setIdentityM(mAirRotationY, 0);
        Matrix.setIdentityM(mAirRotationZ, 0);

        Matrix.setIdentityM(mCamOffset, 0);//camOffset
        Matrix.setIdentityM(mBlockTransMatrix, 0);
        Matrix.setIdentityM(mTransLevelMan, 0);

        //mAirplaneMatrix is FINAL airplane stuff
        Matrix.translateM(mAirTransMatrix, 0, lookAt.X, lookAt.Y, lookAt.Z);//plane translation
        Matrix.scaleM(mAirTransMatrix, 0, .03f, .03f, .03f);//plane scale

        turnPlane();
        gravity();

        Matrix.multiplyMM(mAirplaneMatrix, 0, mAirTransMatrix, 0, mAirOrientationMatrix, 0); //mult air into trans and store in plane
        Matrix.multiplyMM(mAirplaneMatrix, 0, mMVPMatrix, 0, mAirplaneMatrix, 0); //mult plane into mvp and store  back in plane

        setCameraPos();
        if(frameCount <= numLagFrames)
        {
            Matrix.setLookAtM(mViewMatrix, 0, 0, 1, -2, 0, 0, 0, 0, 1, 0); //final
        }
        else
        {
            CamVals v = cameraLag.removeLast();
            Matrix.setLookAtM(mViewMatrix, 0, v.camPos.X, v.camPos.Y, v.camPos.Z, v.lookAt.X, v.lookAt.Y, v.lookAt.Z, v.up.X, v.up.Y, v.up.Z); //final
        }
        CamVals v = new CamVals(camPos, lookAt, up);
        cameraLag.addFirst(v);
        frameCount ++;

        //Matrix.setLookAtM(mViewMatrix, 0, camPos.X, camPos.Y, camPos.Z, lookAt.X, lookAt.Y, lookAt.Z, up.X, up.Y, up.Z); //final


        //ground
        //Matrix.translateM(mTransMatrix, 0, 0, 0.0f, 0.f);//ground
        Matrix.multiplyMM(mGroundMatrix, 0, mMVPMatrix, 0, mTransMatrix, 0);

        //block
        //Matrix.multiplyMM(mBlockMatrix, 0, mMVPMatrix, 0, mBlockTransMatrix, 0);
        //levelMan
        //Matrix.multiplyMM(mLevelMan, 0, mMVPMatrix, 0, mTransLevelMan, 0);

        //Matrix.multiplyMM(mTriangleMatrix, 0, mMVPMatrix, 0, mTransMatrix, 0);
        mTriangle.draw(mTriangleMatrix);
        mGround.draw(mGroundMatrix);
        mAirplane.draw(mAirplaneMatrix);
        levelMan.draw(mMVPMatrix);
        //block.draw(mBlockMatrix); //test block



        // Draw square
        //mSquare.draw(mMVPMatrix);

        // Create a rotation for the triangle

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 1000);

        levelMan = new LevelMan(context);
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }


    public void changeSpeed()
    {

        if (mDx < .1 && mDx > -.1)
        {
            speed = 0f;
        }
        else
        {
            speed = mDx*.1f;
        }

    }//end changeSpeed

    public void gravity()//This is old attempt clean up later
    {
        float steepness = Vector3.dot(forward, stableUp);//+1 is pointing up, -1 is pointing down
        speed += -.001*steepness;

        float val2 = (float)Math.sin(stableAngle*(Math.PI/180));

        float maxPitchAngle = speed*12.7f - val2; //assumes a 10 degree rotation for stable up
        Vector3 maxPitch = new Vector3(forward.X, maxPitchAngle, forward.Z);//in rads
        maxPitch.normalize();

        if (forward.Y > maxPitchAngle) {
            float angleDiff = (float) (Math.asin(forward.Y) - Math.asin(maxPitchAngle)); //angle between forward and max pitch along y axis
            angleDiff *= 180/Math.PI;
            Matrix.rotateM(mAirOrientationMatrix, 0, angleDiff, 1, 0, 0); //rotate pitch
        }
    }


    public void turnPlane()
    {
        zangle = mAccel.Z;
        yangle = mAccel.Y;
        xangle = mAccel.X;

        //float steepness = Vector3.dot(forward, stableUp);//+1 is pointing up, -1 is pointing down
        //speed += -.001*steepness;

        //if(speed < 0.02f)
        //{
            //speed = 0.02f;
        //}

        //float val2 = (float)Math.sin(stableAngle*(Math.PI/180));

        //float maxPitchAngle = speed*2.0f - val2; //assumes a 10 degree rotation for stable up
        //Vector3 maxPitch = new Vector3(forward.X, maxPitchAngle, forward.Z);//in rads
        //maxPitch.normalize();
        //float forwardAngle = Vector3.angle(forward, stableUp);//in rads
        //float maxAngle = Vector3.angle(maxPitch, stableUp);//in rads

        //float angleCorrection = 0f;
        //float angleDiff = (float)(Math.abs(forwardAngle - maxAngle));//angle between forward and max pitch angle in rads

        //float t =  angleDiff/((float)(Math.PI)); //normalize to pi because thats the farthest off the nose can be from max pitch if speed = 0;
       // if (t > 1) //CLAMP THEM VALS!!
        //{
            //t = 1;
        //}
        //else if (t < 0)
        //{
            //t = 0;
        //}

        //if(forwardAngle < maxAngle)//make sure to only apply nose correction when its in the bad, backwards due to neg z
        //{
            //angleCorrection = Vector3.dot(maxPitch, forward);
           // angleCorrection = angleDiff*((float)(180/Math.PI);//NOW IN DEGREES!!!!!!!
       // }

        Matrix.rotateM(mAirOrientationMatrix, 0, -mAccel.X*yawSpeed, 0, 1, 0); //rotate yaw
        Matrix.rotateM(mAirOrientationMatrix, 0, -mAccel.X*rollSpeed, 0, 0, 1); //rotate roll
        float pitchAngle = (-mAccel.Y + .707107f)*pitchSpeed;
                                                            //Lerp between pitch angle and angle correction v
        //Matrix.rotateM(mAirOrientationMatrix, 0, pitchAngle*(1-t) + angleCorrection*t, 1, 0, 0); //rotate pitch
        Matrix.rotateM(mAirOrientationMatrix, 0, pitchAngle, 1, 0, 0); //rotate pitch

        //Log.d("Air Rotation Forward", "(" + mAirOrientationMatrix[8] + ", " + mAirOrientationMatrix[9] + ", " + mAirOrientationMatrix[10] + ")");
        //Log.d("Air Rotation Up", "(" + mAirOrientationMatrix[4] + ", " + mAirOrientationMatrix[5] + ", " + mAirOrientationMatrix[6] + ")");
        //Log.d("Air Rotation Right", "(" + mAirOrientationMatrix[0] + ", " + mAirOrientationMatrix[1] + ", " + mAirOrientationMatrix[2] + ")");

        orientToYpr();

    }//end turnPlane

    public void setCameraPos()
    {
        //camPos = Vector3.add(lookAt, dir);
        //mAirplaneMatrix is FINAL airplane stuff
        //lookat = plane loc in vector3 form
        //dir = direction in vector3 form

        Vector3 newPos = new Vector3(lookAt.X, lookAt.Y, lookAt.Z);
        newPos = Vector3.add(newPos,new Vector3(up.X, up.Y, up.Z));
        Vector3 zOffset = new Vector3(forward.X*2, forward.Y*2, forward.Z*2);
        //Log.d("Forward: ", "(" + forward.X + ", " + forward.Y + ", " + forward.Z + ")");
        //Log.d("Up: ", "(" + up.X + ", " + up.Y + ", " + up.Z + ")");
        //Log.d("Right: ", "(" + right.X + ", " + right.Y + ", " + right.Z + ")");

        newPos = Vector3.subtract(newPos,zOffset);
        camPos = newPos;

    }//end setCameraPos

    public void setPosition()
    {

    }//end setPosition

    public Vector3 planePos()
    {
        return lookAt;
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


    public float getXp()
    {
        return xp;
    }




}