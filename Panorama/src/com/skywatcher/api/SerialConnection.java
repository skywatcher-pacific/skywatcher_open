package com.skywatcher.api;

import java.io.IOException;

public abstract class SerialConnection {
	public String StringSent;
//	public abstract void Close() throws IOException;
	public StringBuffer mBuffer = new StringBuffer();
	public String Encoding = "US-ASCII";	// Default
	
	public void WaitIdle() {}
	public void Lock() {}
	public void Release() {}	
	
	public abstract void Write(String Command) throws IOException;
	public abstract String Read() throws IOException;
	
	public abstract void ClearBuffer();		
}
