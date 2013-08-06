package com.skywatcher.api;

public class MountControlException extends Exception{
	public ErrorCode ErrCode;
	public String ErrMessage;
	public MountControlException(ErrorCode err) {
		this.ErrCode = err;	
	}
	public MountControlException(ErrorCode err, String message)
	{
		this.ErrCode = err;		
		this.ErrMessage = message;		
	}
}
