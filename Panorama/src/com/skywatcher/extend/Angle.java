package com.skywatcher.extend;

import com.skywatcher.api.AstroMisc;

public class Angle{
	private final String TAG = "Angle";
	
	public final static double RADIAN = 180.0 / Math.PI;
	public final static double RAD1 = Math.PI / 180;
	public final static double RAD30 = Math.PI / 6;
	public final static double RAD60 = Math.PI / 3;
	public final static double RAD90 = Math.PI / 2;
	public final static double RAD180 = Math.PI;
	public final static double RAD270 = Math.PI * 1.5;
	public final static double RAD360 = Math.PI * 2;
	
	private double mRad = 0;
	
	public Angle(double rad)
	{
		this.setRad(rad);
	}

	public static Angle FromRad(double rad){
		return new Angle(rad);
	}
	public static Angle FromDegree(double degreefloat)
	{
		return new Angle(degreefloat / RADIAN);
	}
	public static Angle FromDegree3(double degree, double degreeMin, double degreeSec)
	{
		return FromDegree(degree + degreeMin / 60.0 + degreeSec / 3600.0);
	}
	public static Angle FromHourAngle(double hour, double min, double sec)
	{
		return FromDegree((hour + min / 60.0 + sec / 3600.0) * 360 / 24);
	}
	
	public double getRad() {
		return mRad;
	}
	public void setRad(double rad) {
		this.mRad = rad % RAD360;
		if(this.mRad > RAD180)
			this.mRad = this.mRad - RAD360;
		else if (this.mRad < -RAD180)
			this.mRad = this.mRad + RAD360;		
	}
	
	public double getDegree()
	{
		return this.getRad() * RADIAN;
	}
	public void setDegree(double d)
	{
		setRad(d / RAD360);
	}
	
	@Override
	public String toString()
	{
		return AstroMisc.RadToStr(this.getRad());		
	}
	
}
