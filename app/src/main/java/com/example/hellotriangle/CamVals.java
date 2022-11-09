package com.example.hellotriangle;


public class CamVals {
	public Vector3 camPos;
	public Vector3 lookAt;
	public Vector3 up;

	CamVals(Vector3 _camPos, Vector3 _lookAt, Vector3 _up)
	{
		camPos = _camPos;
		lookAt = _lookAt;
		up = _up;
	}

}
