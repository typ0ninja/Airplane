package com.example.hellotriangle;

import android.opengl.GLES20;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.Random;

public class LevelMan {

    //block = new Furniture(3f, 3f, 3f);
    private final float[] mBlockMatrix = new float[16];
    private final float[] mBlockTransMatrix = new float[16];
    private final float[] mVentMatrix = new float[16];
    private final float[] mVentTransMatrix = new float[16];
    private final float[] mFanMatrix = new float[16];
    float dGround = 0f;
    float ventRadius = 1f;

    public Furniture furniture[];
    public Vent vents[];
    public Fan aFan;
    public Vector3 planePos;
    public Vector3 groundPos = new Vector3(0, -1, 0);
    public boolean collision;
    public float radius = .5f;
    int ventCount = 5;
    Random rnd = new Random();
    final int blockCount = 20;
    //public float nextFloat ();
 //rnd.setSeed(seed);

    LevelMan(Context context)
    {
        collision = false;
        planePos = new Vector3(0, 0, 0);
        furniture = new Furniture[blockCount];
        vents = new Vent[ventCount];
        aFan = new Fan(context);

        Matrix.setIdentityM(mFanMatrix, 0);

        for(int i = 0; i< blockCount; i++)
        {
            float test = rnd.nextFloat();
            furniture[i] = new Furniture(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat(), 10*rnd.nextFloat(), rnd.nextFloat(), 10*rnd.nextFloat());
        }
        for(int i = 0; i< ventCount; i++)
        {
            vents[i] = new Vent(0, 0, 0);
        }


    }

    public void draw(float[] mvpMatrix)
    {
        SceneObject.setVP(mvpMatrix);
        //aFan.setVP(mvpMatrix);
        Matrix.multiplyMM(mFanMatrix, 0, mvpMatrix, 0, aFan.getModelMatrix(), 0);
        aFan.draw(mFanMatrix);

        for (int i = 0; i<blockCount; i++)
        {
            Vector3 center = furniture[i].getCenter();
            Matrix.setIdentityM(mBlockTransMatrix, 0);
            Matrix.translateM(mBlockTransMatrix, 0, center.X, center.Y, center.Z);
            Matrix.multiplyMM(mBlockMatrix, 0, mvpMatrix, 0, mBlockTransMatrix, 0);

            furniture[i].draw(mBlockMatrix);
        }

        float translate = 0f;
        for (int i = 0; i<ventCount; i++)
        {
            float x = 10.f;
            translate++;
            Matrix.setIdentityM(mVentTransMatrix, 0);
            Matrix.translateM(mVentTransMatrix, 0, x, -.7f, translate*2);
            vents[i].setCenter(x, -.7f, translate*2);
            Matrix.multiplyMM(mVentMatrix, 0, mvpMatrix, 0, mVentTransMatrix, 0);

            vents[i].draw(mVentMatrix);
        }


        //intersect();

        for(Furniture f: furniture)
        {
            Vector3 C1 = f.getCenter();
            C1.X += f.dimensions.X/2;
            C1.Y -= f.dimensions.Y/2;
            C1.Z -= f.dimensions.Z/2;

            Vector3 C2 = f.getCenter();
            C2.X -= f.dimensions.X/2;
            C2.Y += f.dimensions.Y/2;
            C2.Z += f.dimensions.Z/2;

            if (doesCubeIntersectSphere(C1, C2, planePos, radius))
            {
                Log.d("boxCollide", "Collision Happens");
                collision = true;
                //stop plane here
            }
        }
        intersectGround();


    }

    public void updatePlanePos(Vector3 plane)
    {
        planePos = plane;
    }

    public void intersectGround()
    {
        float dGround = -(Vector3.dot(Vector3.up(), groundPos));
        if (Math.abs(Vector3.dot(Vector3.up(), planePos) + dGround) <= radius)
        {
            Log.d("ground Hit", "Ground Hit");
            collision = true;
        }

        /*
        for(Furniture f: furniture)
        {

            if (Math.abs(Vector3.dot(Vector3.up(), planePos) + f.dTop) <= radius)
            {
                Log.d("Top Hit", "Top Hit");
            }

            if (Math.abs(Vector3.dot(Vector3.down(), planePos) + f.dBottom) <= radius)
            {
                Log.d("Bottom Hit", "Bottom Hit");
            }

            if (Math.abs(Vector3.dot(Vector3.left(), planePos) + f.dLeft) <= radius)
            {
                Log.d("Left Hit", "Left Hit");
            }

            if (Math.abs(Vector3.dot(Vector3.right(), planePos) + f.dRight) <= radius)
            {
                Log.d("Right Hit", "Right Hit");
            }

            if (Math.abs(Vector3.dot(Vector3.backward(), planePos) + f.dBack) <= radius)
            {
                Log.d("Back Hit", "Back Hit");
            }


            if (Math.abs(Vector3.dot(Vector3.forward(), planePos) + f.dForward) <= radius)
            {
                Log.d("Front Hit", "Front Hit");
            }


        }
        */

    }//endIntersect


    //from stack overflow: https://stackoverflow.com/questions/4578967/cube-sphere-intersection-test
    float squared(float v) { return v * v; }
    boolean doesCubeIntersectSphere(Vector3 C1, Vector3 C2, Vector3 S, float R)
    {
        float dist_squared = R * R;
        /* assume C1 and C2 are element-wise sorted, if not, do that now */
        if (S.X < C1.X) dist_squared -= squared(S.X - C1.X);
        else if (S.X > C2.X) dist_squared -= squared(S.X - C2.X);
        if (S.Y < C1.Y) dist_squared -= squared(S.Y - C1.Y);
        else if (S.Y > C2.Y) dist_squared -= squared(S.Y - C2.Y);
        if (S.Z < C1.Z) dist_squared -= squared(S.Z - C1.Z);
        else if (S.Z > C2.Z) dist_squared -= squared(S.Z - C2.Z);
        return dist_squared > 0;
    }

    boolean overVent(Vector3 planeLoc)
    {
        boolean underVent = false;
        for(int i = 0; i< ventCount; i++)
        {
            float ventX = vents[i].getCenter().X;
            float ventZ = vents[i].getCenter().Z;

            float planeX = planeLoc.X;
            float planeZ = planeLoc.Z;

            float collider = (float)Math.sqrt((ventX - planeX) * (ventX - planeX) + (ventZ - planeZ) * (ventZ - planeZ));

            if (collider < ventRadius)
            {
                underVent = true;
            }
        }
        return underVent;
    }

}
