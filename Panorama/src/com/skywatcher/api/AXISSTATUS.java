package com.skywatcher.api;

public class AXISSTATUS {
	public boolean FullStop;
	public boolean Slewing;
	public boolean SlewingTo;
	public boolean SlewingForward;
	public boolean HighSpeed;
	public boolean NotInitialized;

	public AXISSTATUS()
	{
		FullStop = false;
		Slewing = false;
		SlewingTo = false;
		SlewingForward= false;
		HighSpeed = false;
		NotInitialized = true;
	}
	
	public void SetFullStop() {
		this.FullStop = true;
		this.Slewing = this.SlewingTo = false;
	}
	public void SetSlewing(boolean forward, boolean highspeed)
	{
		this.FullStop = this.SlewingTo = false;
		this.Slewing=  true;
		
		this.SlewingForward = forward;
		this.HighSpeed = highspeed;
	}
	public void SetSlewingTo(boolean forward, boolean highspeed)
	{
		this.FullStop = this.Slewing = false;
		this.SlewingTo =true;
		
		this.SlewingForward = forward;
		this.HighSpeed = highspeed;
	}
}
