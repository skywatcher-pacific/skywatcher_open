package com.skywatcher.extend;

public class CroodAxis1Axis2{
	public CroodAxis1Axis2(Angle axis1, Angle axis2)
	{
		Axis1 = axis1; 
		Axis2 = axis2;
	}
	public Angle Axis1, Axis2;
	
	@Override
	public String toString()
	{
		return "(" + Axis1.toString() + "," + Axis2.toString() + ")";
	}
}
