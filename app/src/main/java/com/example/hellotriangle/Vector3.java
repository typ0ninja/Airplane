package com.example.hellotriangle;

import static java.lang.Math.sqrt;

import android.util.Log;


public class Vector3 {

    float X;
    float Y;
    float Z;

    float length;

    public Vector3()
    {
        X = 0f;
        Y = 0f;
        Z = 0f;
        length = (float) sqrt (X*X + Y*Y + Z*Z);
    }

    public Vector3(float x, float y, float z)
    {
        X = x;
        Y = y;
        Z = z;
        length = (float) sqrt (X*X + Y*Y + Z*Z);
    }

    public float X() //get X
    {
        return X;
    }

    public void X(float x)  //set X
    {
        X = x;
        length = (float) sqrt (X*X + Y*Y + Z*Z);
    }

    public float Y()  //get Y
    {
        return Y;
    }

    public void Y(float y)  //set Y
    {
        Y = y;
        length = (float) sqrt (X*X + Y*Y + Z*Z);
    }

    public float Z()  //get Z
    {
        return Z;
    }

    public void Z(float z)  //set Z
    {
        Z = z;
        length = (float) sqrt (X*X + Y*Y + Z*Z);
    }

    public void set(float x, float y, float z)
    {
        X = x;
        Y = y;
        Z = z;
    }

    public float getLength()
    {
        return length;
    }

    public void normalize()
    {
        if (length != 0 )
        {
            X = (1.0f/length)* X;
            Y = (1.0f/length)* Y;
            Z = (1.0f/length)* Z;
        }
        else
        {
            Log.d("Length = 0", "Vector = " + X + ", " + Y + ", " + Z);
            //put this in a try catch later
            X = 0;
            Y = 0;
            Z = 0;
        }
    }
    public static Vector3 add(Vector3 a, Vector3 b)
    {
        Vector3 out;

        out = new Vector3(a.X + b.X, a.Y + b.Y, a.Z + b.Z);

        return out;
    }

    public static Vector3 subtract(Vector3 a, Vector3 b)
    {
        Vector3 out;

        out = new Vector3(a.X - b.X, a.Y - b.Y, a.Z - b.Z);

        return out;
    }

    public static Vector3 scalar(Vector3 a, float scalar)
    {
        Vector3 out;

        out = new Vector3(a.X * scalar, a.Y * scalar, a.Z * scalar);

        return out;
    }

    public static float dot(Vector3 a, Vector3 b)
    {
        float dotP = 0f;

        dotP = (a.X * b.X) + (a.Y * b.Y) + (a.Z * b.Z);

        return dotP;
    }

    public static Vector3 cross(Vector3 a, Vector3 b)
    {
        Vector3 C;
        float X, Y, Z;
        X = a.X*b.Z - a.Z*b.Y;
        Y = -(a.X*b.Z - a.Z*b.X);
        Z = a.X*b.Y - a.Y*b.X;

        C = new Vector3(X, Y, Z);

        return C;
    }

    public float magnitude()
    {
        float mag = (float)(sqrt(X*X + Y*Y + Z*Z));
        return mag;
    }

    public static float angle(Vector3 a, Vector3 b)
    {
        float angle = (float)(Math.acos(dot(a, b)/(a.magnitude()*b.magnitude())));
        return angle;
    }

    public static Vector3 up()
    {
        Vector3 up = new Vector3(0, 1, 0);
        return up;
    }
    public static Vector3 down()
    {
        Vector3 down = new Vector3(0, -1, 0);
        return down;
    }
    public static Vector3 left()
    {
        Vector3 left = new Vector3(1, 0, 0);
        return left;
    }
    public static Vector3 right()
    {
        Vector3 right = new Vector3(-1, 0, 0);
        return right;
    }
    public static Vector3 forward()
    {
        Vector3 forward = new Vector3(0, 0, 1);
        return forward;
    }
    public static Vector3 backward()
    {
        Vector3 backward = new Vector3(0, 0, -1);
        return backward;
    }
}//end Class Vector3
