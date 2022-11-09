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

import android.content.Context;
import android.hardware.SensorEventListener;
import android.opengl.GLSurfaceView;
import android.os.Debug;
import android.view.MotionEvent;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView implements SensorEventListener {

    private final MyGLRenderer mRenderer;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int iChange = 0;
    float gravity[] = {0, -9.81f, 0};

    public ObjLoader objLoader;
    public FloatBuffer positions;
    public FloatBuffer normals;
    public FloatBuffer textureCoordinates;
    float numFaces;
    int mBytesPerFloat = 1;



    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
        {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else
        {
            // Sorry, there are no accelerometers on your device.
            // You can't play this game.
        }


        setKeepScreenOn(true);


    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;
    private boolean canXlate;


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // convert x and y to screen independent coordinates

                mRenderer.mDx = (x - (getWidth() / 2.0f)) / (getWidth() / 2.0f);
                mRenderer.mDy = (y - (getHeight() / 2.0f)) / (getHeight() / 2.0f);

                if(mRenderer.mDy > .8)
                {
                    //mRenderer.changeSpeed();
                }

                if(mRenderer.mDy < .8)
                {
                    //mRenderer.turnPlane();
                }

                break;
            default:

        }
        return true;

    }//end onTouchEvent

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        mRenderer.mAccel.X = event.values[0];
        mRenderer.mAccel.Y = event.values[1];
        mRenderer.mAccel.Z = event.values[2];


        if (iChange++ < 6)
        {
            //Log.i("ACCELL :", "  accell :" + mRenderer.mAccel[0] + " " + mRenderer.mAccel[1] + " " + mRenderer.mAccel[2]);
        }

        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        final float alpha = 0.5f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        mRenderer.mAccel.X = event.values[0]/9.81f;// - gravity[0];
        mRenderer.mAccel.Y = (event.values[1]/9.81f);// - gravity[1];
        mRenderer.mAccel.Z = (event.values[2]/9.81f);// - gravity[2];

    }//end onSensorChanged

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }//end onAccuracyChanged

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        super.surfaceCreated(holder);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        super.surfaceDestroyed(holder);
        mSensorManager.unregisterListener(this);

    }
}//end class

