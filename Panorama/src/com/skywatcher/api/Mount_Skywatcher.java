package com.skywatcher.api;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Mount_Skywatcher extends Mount {
	// special charactor for communication.
	final char cStartChar_Out = ':'; // Leading charactor of a command
	final char cStartChar_In = '='; // Leading charactor of a NORMAL response.
	final char cErrChar = '!'; // Leading charactor of an ABNORMAL response.
	final char cEndChar = (char) 13; // Tailing charactor of command and
	// response.
	final double MAX_SPEED = 500; // ?
	final double LOW_SPEED_MARGIN = (128.0 * AstroMisc.SIDEREALRATE);

	private char dir = '0'; // direction
	// Mount code: 0x00=EQ6, 0x01=HEQ5, 0x02=EQ5, 0x03=EQ3
	// 0x80=GT, 0x81=MF, 0x82=114GT
	// 0x90=DOB
	private long MountCode;
	private long[] StepTimerFreq = new long[2]; // Frequency of stepping timer.
	private long[] PESteps = new long[2];
	private long[] HighSpeedRatio = new long[2];
	// private long[] StepPosition = new long[2]; // Never Used
	private long[] BreakSteps = new long[2]; // Break steps from slewing to
	// stop.
	private long[] LowSpeedGotoMargin = new long[2]; // If slewing steps exceeds
	// this
	// LowSpeedGotoMargin,
	// GOTO is in high speed slewing.

	private boolean IsDCMotor; // Ture: The motor controller is a DC motor
	// controller. It uses TX/RX line is bus
	// topology.
	// False: The motor controller is a stepper motor controller. TX/RX lines
	// are seperated.
	private boolean InstantStop; // Use InstantStop command for MCAxisStop

	public Mount_Skywatcher() {
		super();
	}

	@Override
	protected void SendRequest(AXISID Axis, char Command, String cmdDataStr)
			throws IOException {
		// TODO Auto-generated method stub
		if (cmdDataStr == null)
			cmdDataStr = "";

		final int BufferSize = 20;
		StringBuilder CommandStr = new StringBuilder(BufferSize);
		CommandStr.append(cStartChar_Out); // 0: Leading char
		CommandStr.append(Command); // 1: Length of command( Source,
		// distination, command char, data )

		// Target Device
		CommandStr.append(Axis == AXISID.AXIS1 ? '1' : '2'); // 2: Target Axis
		// Copy command data to buffer
		CommandStr.append(cmdDataStr);

		CommandStr.append(cEndChar); // CR Character

		mConnection.Write(CommandStr.toString());
	}

	@Override
	protected String RecieveResponse() throws IOException, TimeoutException {
		// Receive Response
		// format "::e1\r=020883\r"
		long startticks = System.currentTimeMillis();

		StringBuilder mBuffer = new StringBuilder(15);
		boolean StartReading = false, EndReading = false;

		while (!EndReading) {
			long curticks = System.currentTimeMillis();

			if ((curticks - startticks) > 1000) {
				// var write = string.Format("Timeout {0} / {1}",
				// mConnection.mBuffer, mBuffer);
				// Console.WriteLine(write);
				// Throw if cannot get response				
				throw new TimeoutException("Cannot get response in Time. Dump Buffer: " + mConnection.mBuffer);
			}

			// Throw if connection failed
			String r = mConnection.Read();

			for (int i = 0; i < r.length(); i++) {
				// this code order is important
				if (r.charAt(i) == cStartChar_In || r.charAt(i) == cErrChar)
					StartReading = true;

				if (StartReading)
					mBuffer.append(r.charAt(i));

				if (r.charAt(i) == cEndChar) {
					if (StartReading) {
						EndReading = true;
						break;
					}
				}
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return mBuffer.toString();
	}

	@Override
	public void MCOpenTelescopeConnection() throws MountControlException {
		// TODO Auto-generated method stub
		IsDCMotor = CheckIfDCMotor();
		try {
			InquireMotorBoardVersion(AXISID.AXIS1);
		} catch (Exception e) {
			// try again
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			InquireMotorBoardVersion(AXISID.AXIS1);
		}

		MountCode = MCVersion & 0xFF;

		// // NOTE: Simulator settings, Mount dependent Settings

		// Inquire Gear Rate
		InquireGridPerRevolution(AXISID.AXIS1);
		InquireGridPerRevolution(AXISID.AXIS2);

		// Inquire motor timer interrup frequency
		InquireTimerInterruptFreq(AXISID.AXIS1);
		InquireTimerInterruptFreq(AXISID.AXIS2);

		// Inquire motor high speed ratio
		InquireHighSpeedRatio(AXISID.AXIS1);
		InquireHighSpeedRatio(AXISID.AXIS2);

		// Inquire PEC period
		// DC motor controller does not support PEC
		if (!IsDCMotor) {
			// InquirePECPeriod(AXISID.AXIS1);
			// InquirePECPeriod(AXISID.AXIS2);
		}

		// Inquire Axis Position
		Positions[AXISID.AXIS1.id] = MCGetAxisPosition(AXISID.AXIS1);
		Positions[AXISID.AXIS2.id] = MCGetAxisPosition(AXISID.AXIS2);

		InitializeMC();

		// These two LowSpeedGotoMargin are calculate from slewing for 5 seconds
		// in 128x sidereal rate
		LowSpeedGotoMargin[AXISID.AXIS1.id] = (long) (640 * AstroMisc.SIDEREALRATE * FactorRadToStep[AXISID.AXIS1.id]);
		LowSpeedGotoMargin[AXISID.AXIS2.id] = (long) (640 * AstroMisc.SIDEREALRATE * FactorRadToStep[AXISID.AXIS2.id]);

		// Default break steps
		BreakSteps[AXISID.AXIS1.id] = 3500;
		BreakSteps[AXISID.AXIS2.id] = 3500;
	}

	@Override
	public void MCAxisSlew(AXISID Axis, double Speed)
			throws MountControlException {
		// TODO Auto-generated method stub
		// Limit maximum speed
		if (Speed > MAX_SPEED) // 3.4 degrees/sec, 800X sidereal rate, is the
			// highest speed.
			Speed = MAX_SPEED;
		else if (Speed < -MAX_SPEED)
			Speed = -MAX_SPEED;

		double InternalSpeed = Speed;
		boolean forward = false, highspeed = false;

		// InternalSpeed lower than 1/1000 of sidereal rate?
		if (Math.abs(InternalSpeed) <= AstroMisc.SIDEREALRATE / 1000.0) {
			MCAxisStop(Axis);
			return;
		}

		// Stop motor and set motion mode if necessary.
		PrepareForSlewing(Axis, InternalSpeed);

		if (InternalSpeed > 0.0)
			forward = true;
		else {
			InternalSpeed = -InternalSpeed;
			forward = false;
		}

		// TODO: ask the details

		// Calculate and set step period.
		if (InternalSpeed > LOW_SPEED_MARGIN) { // High speed adjustment
			InternalSpeed = InternalSpeed / (double) HighSpeedRatio[Axis.id];
			highspeed = true;
		}
		InternalSpeed = 1 / InternalSpeed; // For using function
		// RadSpeedToInt(), change to unit
		// Senonds/Rad.
		long SpeedInt = RadSpeedToInt(Axis, InternalSpeed);
		if ((MCVersion == 0x010600) || (MCVersion == 0x010601)) // For special
			// MC version.
			SpeedInt -= 3;
		if (SpeedInt < 6)
			SpeedInt = 6;
		SetStepPeriod(Axis, SpeedInt);

		// Start motion
		// if (AxesStatus[Axis] & AXIS_FULL_STOPPED) // It must be remove for
		// the latest DC motor board.
		StartMotion(Axis);

		AxesStatus[Axis.id].SetSlewing(forward, highspeed);
		SlewingSpeed[Axis.id] = Speed;
	}

	@Override
	public void MCAxisSlewTo(AXISID Axis, double TargetPosition)
			throws MountControlException {
		// TODO Auto-generated method stub
		// Get current position of the axis.
		double CurPosition = MCGetAxisPosition(Axis);

		// Calculate slewing distance.
		// Note: For EQ mount, Positions[AXIS1] is offset( -PI/2 ) adjusted in
		// UpdateAxisPosition().
		double MovingAngle = TargetPosition - CurPosition;

		// Convert distance in radian into steps.
		long MovingSteps = AngleToStep(Axis, MovingAngle);

		boolean forward = false, highspeed = false;

		// If there is no increment, return directly.
		if (MovingSteps == 0) {
			return;
		}

		// Set moving direction
		if (MovingSteps > 0) {
			dir = '0';
			forward = true;
		} else {
			dir = '1';
			MovingSteps = -MovingSteps;
			forward = false;
		}

		// Might need to check whether motor has stopped.

		// Check if the distance is long enough to trigger a high speed GOTO.
		if (MovingSteps > LowSpeedGotoMargin[Axis.id]) {
			SetMotionMode(Axis, '0', dir); // high speed GOTO slewing
			highspeed = true;
		} else {
			SetMotionMode(Axis, '2', dir); // low speed GOTO slewing
			highspeed = false;
		}

		SetGotoTargetIncrement(Axis, MovingSteps);
		SetBreakPointIncrement(Axis, BreakSteps[Axis.id]);
		StartMotion(Axis);

		TargetPositions[Axis.id] = TargetPosition;
		AxesStatus[Axis.id].SetSlewingTo(forward, highspeed);
	}

	@Override
	public void MCAxisStop(AXISID Axis) throws MountControlException {
		// TODO Auto-generated method stub
		if (InstantStop)
			TalkWithAxis(Axis, 'L', null);
		else
			TalkWithAxis(Axis, 'K', null);

		AxesStatus[Axis.id].SetFullStop();
	}

	@Override
	public void MCSetAxisPosition(AXISID Axis, double NewValue)
			throws MountControlException {
		// TODO Auto-generated method stub
		long NewStepIndex = AngleToStep(Axis, NewValue);
		NewStepIndex += 0x800000;

		String szCmd = longTo6BitHEX(NewStepIndex);
		TalkWithAxis(Axis, 'E', szCmd);

		Positions[Axis.id] = NewValue;
	}

	@Override
	public double MCGetAxisPosition(AXISID Axis) throws MountControlException {
		// TODO Auto-generated method stub
		String response = TalkWithAxis(Axis, 'j', null);

		long iPosition = BCDstr2long(response);
		iPosition -= 0x00800000;
		Positions[Axis.id] = StepToAngle(Axis, iPosition);

		return Positions[Axis.id];
	}

	@Override
	public AXISSTATUS MCGetAxisStatus(AXISID Axis) throws MountControlException {
		// TODO Auto-generated method stub
		String response = TalkWithAxis(Axis, 'f', null);

		if ((response.charAt(2) & 0x01) != 0) {
			// Axis is running
			if ((response.charAt(1) & 0x01) != 0)
				AxesStatus[Axis.id].Slewing = true; // Axis in slewing(constant
			// speed) mode.
			else
				AxesStatus[Axis.id].SlewingTo = true; // Axis in SlewingTo mode.
		} else {
			AxesStatus[Axis.id].FullStop = true; // FullStop = 1; // Axis is
			// fully stop.
		}

		if ((response.charAt(1) & 0x02) == 0)
			AxesStatus[Axis.id].SlewingForward = true; // Angle increase = 1;
		else
			AxesStatus[Axis.id].SlewingForward = false;

		if ((response.charAt(1) & 0x04) != 0)
			AxesStatus[Axis.id].HighSpeed = true; // HighSpeed running mode = 1;
		else
			AxesStatus[Axis.id].HighSpeed = false;

		if ((response.charAt(3) & 1) == 0)
			AxesStatus[Axis.id].NotInitialized = true; // MC is not initialized.
		else
			AxesStatus[Axis.id].NotInitialized = false;

		return AxesStatus[Axis.id];
	}

	@Override
	public void MCSetSwitch(boolean OnOff) throws MountControlException
	{
		if (OnOff)
            TalkWithAxis(AXISID.AXIS1, 'O', "1");
        else
            TalkWithAxis(AXISID.AXIS1, 'O', "0");
	}
	
	// Skywaterch Helper function
	protected boolean IsHEXChar(char tmpChar) {
		return ((tmpChar >= '0') && (tmpChar <= '9'))
				|| ((tmpChar >= 'A') && (tmpChar <= 'F'));
	}

	protected long HEX2Int(char HEX) {
		long tmp;
		tmp = HEX - 0x30;
		if (tmp > 9)
			tmp -= 7;
		return tmp;
	}

	private long BCDstr2long(String response) throws MountControlException {
		// =020782 => 8521474
		try {
			long value = 0;
			for (int i = 1; i+1 < response.length(); i += 2) {
				value += Integer.parseInt(response.substring(i, i + 2), 16)
						* Math.pow(16, i - 1);//
			}

			// if(D)
			// Log.d(TAG,"BCDstr2long " + response + ","+value);
			return value;
		} catch (NumberFormatException e) {
			throw new MountControlException(ErrorCode.ERR_INVALID_DATA,
					"Parse BCD Failed");
		}
		// return Integer.parseInt(response.substring(0, 2), 16)
		// + Integer.parseInt(response.substring(2, 4), 16) * 256
		// + Integer.parseInt(response.substring(4, 6), 16) * 256 * 256;
	}

	private String longTo6BitHEX(long number) {
		// 31 -> 0F0000
		String A = Integer.toHexString((int) number & 0xFF).toUpperCase();
		String B = Integer.toHexString(((int) number & 0xFF00) / 256)
				.toUpperCase();
		String C = Integer.toHexString(((int) number & 0xFF0000) / 256 / 256)
				.toUpperCase();

		if (A.length() == 1)
			A = "0" + A;
		if (B.length() == 1)
			B = "0" + B;
		if (C.length() == 1)
			C = "0" + C;

		// if (D)
		// Log.d(TAG, "longTo6BitHex " + number + "," + A + "," + B + "," + C);

		return A + B + C;

	}

	// Test if connect to DC motor board.
	private boolean CheckIfDCMotor() {
		// // Enter critical section to block others conversation with motor
		// controller.
		// //EnterCriticalSection(&csPortBusy);
		// //PortBusy = TRUE;

		// // Enable TX driver.
		// EscapeCommFunction(hCom, SETRTS);
		// // Wait TX enabled
		// Sleep(20);

		// // Clear unsent charactor and any charactor received before this
		// conversation.
		// // Do this after I get control of communication bus.
		// PurgeComm(hCom, PURGE_RXCLEAR | PURGE_TXCLEAR);

		// // Send a Start charactor.
		// // The Skyatcher motor controller always resets its RX buffer
		// whenever a start charactor is received.
		// WriteFile(hCom, ":", 1, &Count, NULL);

		// // Disable TX driver.
		// // EscapeCommFunction(hCom, CLRRTS);

		// ReadFile(hCom, Buffer, 1, &Count, NULL);

		// if ((Count == 1) && (Buffer[0] == ':'))
		// {
		// IsDCMotor = TRUE;

		// // Disable TX driver if it is a DC motor controller.
		// EscapeCommFunction(hCom, CLRRTS);
		// }
		// else
		// IsDCMotor = FALSE;

		// // PortBusy = FALSE;
		// // Quit critical section. Enable other conversation.
		// // LeaveCriticalSection(&csPortBusy);

		// SetEvent(hEvent_SerialPortIdled);

		// return IsDCMotor;
//		synchronized (mConnection) {
//			try {
//				mConnection.Lock();
//				Thread.sleep(20);
//
//				mConnection.ClearBuffer();
//				mConnection.Write(":");
//
//				mConnection.Release();
//
//				String r = mConnection.Read();
//
//				if (r.length() == 1 && r.charAt(0) == ':')
//					return true;
//				else
//					return false;
//			} catch (Exception e) {
//				return false;
//			}
//		}
		return true;
	}

	private void PrepareForSlewing(AXISID Axis, double speed)
			throws MountControlException {
		boolean FirstRun = true;
		char cDirection;

		AXISSTATUS axesstatus = MCGetAxisStatus(Axis);
		if (!axesstatus.FullStop) {
			if ((axesstatus.SlewingTo) || // GOTO in action
					(axesstatus.HighSpeed) || // Currently high speed slewing
					(Math.abs(speed) >= LOW_SPEED_MARGIN) || // Will be high
																// speed slewing
					((axesstatus.SlewingForward) && (speed < 0)) || // Different
																	// direction
					(!(axesstatus.SlewingForward) && (speed > 0)) // Different
																	// direction
			) {
				// We need to stop the motor first to change Motion Mode, etc.
				MCAxisStop(Axis);
			} else
				// Other situatuion, there is no need to set motion mode.
				return;

			// Wait until the axis stop
			while (true) {
				// Update Mount status, the status of both axes are also updated
				// because _GetMountStatus() includes such operations.
				axesstatus = MCGetAxisStatus(Axis);

				// Return if the axis has stopped.
				if (axesstatus.FullStop)
					break;

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// If the axis is asked to stop.
				// if ( (!AxesAskedToRun[Axis] && !(MountStatus &
				// MOUNT_TRACKING_ON)) ) // If AXIS1 or AXIS2 is asked to stop
				// or
				// return ERR_USER_INTERRUPT;

			}

		}
		if (speed > 0.0) {
			cDirection = '0';
		} else {
			cDirection = '1';
			speed = -speed; // Get absolute value of Speed.
		}

		if (speed > LOW_SPEED_MARGIN) {
			SetMotionMode(Axis, '3', cDirection); // Set HIGH speed slewing
													// mode.
		} else
			SetMotionMode(Axis, '1', cDirection); // Set LOW speed slewing mode.

	}

	/************************
	 * MOTOR COMMAND SET
	 * 
	 * @throws MountControlException
	 *             *
	 **************************/
	// Inquire Motor Board Version ":e(*1)", where *1: '1'= CH1, '2'= CH2, '3'=
	// Both.
	protected void InquireMotorBoardVersion(AXISID Axis)
			throws MountControlException {
		String response = TalkWithAxis(Axis, 'e', null);

		long tmpMCVersion = BCDstr2long(response);

		MCVersion = ((tmpMCVersion & 0xFF) << 16) | ((tmpMCVersion & 0xFF00))
				| ((tmpMCVersion & 0xFF0000) >> 16);

	}

	// Inquire Grid Per Revolution ":a(*2)", where *2: '1'= CH1, '2' = CH2.
	protected void InquireGridPerRevolution(AXISID Axis)
			throws MountControlException {
		String response = TalkWithAxis(Axis, 'a', null);

		long GearRatio = BCDstr2long(response);

		// There is a bug in the earlier version firmware(Before 2.00) of motor
		// controller MC001.
		// Overwrite the GearRatio reported by the MC for 80GT mount and 114GT
		// mount.
		if ((MCVersion & 0x0000FF) == 0x80) {
			GearRatio = 0x162B97; // for 80GT mount
		}
		if ((MCVersion & 0x0000FF) == 0x82) {
			GearRatio = 0x205318; // for 114GT mount
		}

		FactorRadToStep[Axis.id] = GearRatio / (2 * Math.PI);
		FactorStepToRad[Axis.id] = 2 * Math.PI / GearRatio;
	}

	// Inquire Timer Interrupt Freq ":b1".
	protected void InquireTimerInterruptFreq(AXISID Axis)
			throws MountControlException {
		String response = TalkWithAxis(Axis, 'b', null);

		long TimeFreq = BCDstr2long(response);
		StepTimerFreq[Axis.id] = TimeFreq;

		FactorRadRateToInt[Axis.id] = (double) (StepTimerFreq[Axis.id])
				/ FactorRadToStep[Axis.id];
	}

	// Inquire high speed ratio ":g(*2)", where *2: '1'= CH1, '2' = CH2.
	protected void InquireHighSpeedRatio(AXISID Axis)
			throws MountControlException {
		String response = TalkWithAxis(Axis, 'g', null);

		long highSpeedRatio = BCDstr2long(response);
		HighSpeedRatio[Axis.id] = highSpeedRatio;
	}

	// Inquire PEC Period ":s(*1)", where *1: '1'= CH1, '2'= CH2, '3'= Both.
	protected void InquirePECPeriod(AXISID Axis) throws MountControlException {
		String response = TalkWithAxis(Axis, 's', null);

		long PECPeriod = BCDstr2long(response);
		PESteps[Axis.id] = PECPeriod;
	}

	// Set initialization done ":F3", where '3'= Both CH1 and CH2.
	protected void InitializeMC() throws MountControlException {
		TalkWithAxis(AXISID.AXIS1, 'F', null);
		TalkWithAxis(AXISID.AXIS2, 'F', null);
	}

	protected void SetMotionMode(AXISID Axis, char func, char direction)
			throws MountControlException {
		String szCmd = "" + func + direction;
		TalkWithAxis(Axis, 'G', szCmd);
	}

	protected void SetGotoTargetIncrement(AXISID Axis, long StepsCount)
			throws MountControlException {
		String cmd = longTo6BitHEX(StepsCount);

		TalkWithAxis(Axis, 'H', cmd);
	}

	protected void SetBreakPointIncrement(AXISID Axis, long StepsCount)
			throws MountControlException {
		String szCmd = longTo6BitHEX(StepsCount);

		TalkWithAxis(Axis, 'M', szCmd);
	}

	protected void SetBreakSteps(AXISID Axis, long NewBrakeSteps)
			throws MountControlException {
		String szCmd = longTo6BitHEX(NewBrakeSteps);
		TalkWithAxis(Axis, 'U', szCmd);
	}

	protected void SetStepPeriod(AXISID Axis, long StepsCount)
			throws MountControlException {
		String szCmd = longTo6BitHEX(StepsCount);
		TalkWithAxis(Axis, 'I', szCmd);
	}

	protected void StartMotion(AXISID Axis) throws MountControlException {
		TalkWithAxis(Axis, 'J', null);
	}

}
