package com.skywatcher.api;

// should add more exception
// talkwithaxis will cause some delay of control
// still have some bugs

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public abstract class Mount {
	protected static SerialConnection mConnection = null;
	protected long MCVersion = 0;

	public MOUNTID MountID;
	public boolean IsEQMount = false;

	public double[] Positions = new double[2];
	public double[] TargetPositions = new double[2];
	public double[] SlewingSpeed = new double[2];
	public AXISSTATUS[] AxesStatus = new AXISSTATUS[2];

	public Mount() {
		mConnection = null;
		MCVersion = 0;
		IsEQMount = false;

		Positions[0] = 0;
		Positions[1] = 0;
		TargetPositions[0] = 0;
		TargetPositions[1] = 0;
		SlewingSpeed[0] = 0;
		SlewingSpeed[1] = 0;
		AxesStatus[0] = new AXISSTATUS();
		AxesStatus[1] = new AXISSTATUS();
	}

	
//	protected void finalize() throws Throwable {		
//		Log.e("BluetoothManager","Called finalize");
//		
//		super.finalize();
//		mConnection.Close();
//	}

	public void Connect_Bluetooth(BluetoothSocket socket) throws IOException {
		mConnection = new SerialConnection_Bluetooth(socket);
	}

	public MOUNTTYPE DetectMount() {
		return null;
	}

	public String TalkWithAxis(AXISID Axis, char Command, String cmdDataStr) throws MountControlException {
		synchronized (mConnection) {
			for (int i = 0; i < 2; i++) {
				try {
					mConnection.ClearBuffer();
					mConnection.WaitIdle();
					mConnection.Lock();

					SendRequest(Axis, Command, cmdDataStr);

					mConnection.Release();

					return RecieveResponse();
				} catch (TimeoutException e) {
					// cannot get legal response in time, try to resend 
					e.printStackTrace();					
					
				} catch (IOException e) {
					// the connection is lost
					e.printStackTrace();
					throw new MountControlException(ErrorCode.ERR_NOT_CONNECTED, e.getMessage());
				}
			}
			if(Axis == AXISID.AXIS1)
				throw new MountControlException(ErrorCode.ERR_NORESPONSE_AXIS1);
			else 
				throw new MountControlException(ErrorCode.ERR_NORESPONSE_AXIS2);
		}
	}

	protected abstract void SendRequest(AXISID axis, char command, String cmdDataStr) throws IOException;

	protected abstract String RecieveResponse() throws IOException, TimeoutException;

	public abstract void MCOpenTelescopeConnection() throws MountControlException;

	public abstract void MCAxisSlew(AXISID Axis, double Speed) throws MountControlException;

	public abstract void MCAxisSlewTo(AXISID Axis, double TargetPosition) throws MountControlException;

	public abstract void MCAxisStop(AXISID Axis) throws MountControlException;

	public abstract void MCSetAxisPosition(AXISID Axis, double NewValue) throws MountControlException;

	public abstract double MCGetAxisPosition(AXISID Axis) throws MountControlException;

	public abstract AXISSTATUS MCGetAxisStatus(AXISID Axis) throws MountControlException;

	public abstract void MCSetSwitch(boolean OnOff) throws MountControlException;

	protected double[] FactorRadToStep = new double[2];

	protected long AngleToStep(AXISID Axis, double AngleInRad) {
		return (long) (AngleInRad * FactorRadToStep[Axis.id]);
	}

	protected double[] FactorStepToRad = new double[2];

	protected double StepToAngle(AXISID Axis, long Steps) {
		return Steps * FactorStepToRad[Axis.id];
	}

	protected double[] FactorRadRateToInt = new double[2];

	protected long RadSpeedToInt(AXISID Axis, double RateInRad) {

		return (long) (RateInRad * FactorRadRateToInt[Axis.id]);
	}
}
