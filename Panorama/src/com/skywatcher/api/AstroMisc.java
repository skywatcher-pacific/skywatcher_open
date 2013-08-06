package com.skywatcher.api;

public class AstroMisc {
	public static final double RADIAN = 180.0 / Math.PI;
	public static final double SIDEREALRATE = 2 * Math.PI / 86164.09065;
	
	public static String RadToStr(double rad)
	{
		double deg = rad * RADIAN;
		return String.format("%3.2f", deg);
	}

	public static double DegToRad(double Degree) {
		// TODO Auto-generated method stub
		return (Degree / 180 * Math.PI);
	}
	
	public static double RadToDeg(double rad) {
		return rad * 180 / Math.PI;
	}

	public static double UniformAngle(double targetPosition) {
		// TODO Auto-generated method stub
		targetPosition = targetPosition % (Math.PI * 2);
         if (targetPosition > Math.PI)
             return targetPosition - 2 * Math.PI;
         if (targetPosition < -Math.PI)
             return targetPosition + 2 * Math.PI;
         return targetPosition;
	}

	public static double AngleDistance(double ang1, double ang2) {
		// TODO Auto-generated method stub
		  ang2 = UniformAngle(ang2);

          double d = ang2 - ang1;

          return UniformAngle(d);
	}
}
