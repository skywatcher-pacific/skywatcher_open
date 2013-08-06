using System;
using System.Diagnostics;
using System.Text;
using System.Threading;

/// TODO: PrepareForGoto Should move to mount control
/// Checked 2/8/2011

namespace BasicApi
{
    public class Mount_Skywatcher : Mount
    {
        // special charactor for communication.
        const char cStartChar_Out = ':';		// Leading charactor of a command 
        const char cStartChar_In = '=';			// Leading charactor of a NORMAL response.
        const char cErrChar = '!';              // Leading charactor of an ABNORMAL response.
        const char cEndChar = (char)13;         // Tailing charactor of command and response.
        const double MAX_SPEED = 500;           //?
        const double LOW_SPEED_MARGIN = (128.0 * CONSTANT.SIDEREALRATE);

        private char dir = '0'; // direction
        // Mount code: 0x00=EQ6, 0x01=HEQ5, 0x02=EQ5, 0x03=EQ3
        //             0x80=GT,  0x81=MF,   0x82=114GT
        //             0x90=DOB
        private long MountCode;
        private long[] StepTimerFreq = new long[2];			// Frequency of stepping timer.
        private long[] PESteps = new long[2];
        private long[] HighSpeedRatio = new long[2];
        //private long[] StepPosition = new long[2];          // Never Used
        private long[] BreakSteps = new long[2];				// Break steps from slewing to stop.
        private long[] LowSpeedGotoMargin = new long[2];		// If slewing steps exceeds this LowSpeedGotoMargin, 
        // GOTO is in high speed slewing.

        private bool IsDCMotor;						// Ture: The motor controller is a DC motor controller. It uses TX/RX line is bus topology.
        // False: The motor controller is a stepper motor controller. TX/RX lines are seperated.
        private bool InstantStop;					// Use InstantStop command for MCAxisStop

        public Mount_Skywatcher()
            : base()
        {

        }
        public override void Connect_COM(int TelescopePort)
        {
            base.Connect_COM(TelescopePort);
            ((SerialConnect_COM)mConnection).hCom.BaudRate = SerialConnect_COM.CBR.CBR_9600;
            ((SerialConnect_COM)mConnection).hCom.Encoding = Encoding.ASCII;
        }

        protected override void SendRequest(AXISID Axis, char Command, string cmdDataStr)
        {
            if (cmdDataStr == null)
                cmdDataStr = "";

            const int BufferSize = 20;
            StringBuilder CommandStr = new StringBuilder(BufferSize);
            CommandStr.Append(cStartChar_Out);                  // 0: Leading char
            CommandStr.Append(Command);                         // 1: Length of command( Source, distination, command char, data )

            // Target Device
            CommandStr.Append(Axis == AXISID.AXIS1 ? '1' : '2');    // 2: Target Axis
            // Copy command data to buffer
            CommandStr.Append(cmdDataStr);

            CommandStr.Append(cEndChar);    // CR Character            

            mConnection.Write(CommandStr.ToString());
        }
        protected override string RecieveResponse()
        {            
            // Receive Response
            // format "::e1\r=020883\r"
            long startticks = DateTime.Now.Ticks;

            StringBuilder mBuffer = new StringBuilder(15);
            bool StartReading = false, EndReading = false;

            int index = 0;
            long interval = 0;
            while (!EndReading)
            {
                index++;
                long curticks = DateTime.Now.Ticks;
                interval = curticks - startticks;

                if ((curticks - startticks) > 10000 * 1000)
                {
                    //Trace.TraceError("Timeout {0} / {1}", mConnection.mBuffer, mBuffer);          
                    throw new TimeoutException();
                }

                string r =mConnection.Read();

                for (int i = 0; i < r.Length; i++)
                {
                    // this code order is important
                    if (r[i] == cStartChar_In || r[i] == cErrChar)
                        StartReading = true;

                    if (StartReading)
                        mBuffer.Append(r[i]);

                    if (r[i] == cEndChar)
                    {
                        if (StartReading)
                        {
                            EndReading = true;
                            break;
                        }
                    }
                }

                Thread.Sleep(1);
            }

            //Trace.TraceInformation("Loop :" + index.ToString() + "Ticks :" + interval);
            return mBuffer.ToString();
        }

        public override void MCInit()
        {
            IsDCMotor = CheckIfDCMotor();
            try
            {
                InquireMotorBoardVersion(AXISID.AXIS1);
            }
            catch
            {
                // try again
                System.Threading.Thread.Sleep(200);
                InquireMotorBoardVersion(AXISID.AXIS1);
            }

            MountCode = MCVersion & 0xFF;

            //// NOTE: Simulator settings, Mount dependent Settings

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
            if (!IsDCMotor)
            {
                //InquirePECPeriod(AXISID.AXIS1);
                //InquirePECPeriod(AXISID.AXIS2);
            }

            // Inquire Axis Position
            Positions[(int)AXISID.AXIS1] = MCGetAxisPosition(AXISID.AXIS1);
            Positions[(int)AXISID.AXIS2] = MCGetAxisPosition(AXISID.AXIS2);

            InitializeMC();

            // These two LowSpeedGotoMargin are calculate from slewing for 5 seconds in 128x sidereal rate
            LowSpeedGotoMargin[(int)AXISID.AXIS1] = (long)(640 * CONSTANT.SIDEREALRATE * FactorRadToStep[(int)AXISID.AXIS1]);
            LowSpeedGotoMargin[(int)AXISID.AXIS2] = (long)(640 * CONSTANT.SIDEREALRATE * FactorRadToStep[(int)AXISID.AXIS2]);

            // Default break steps
            BreakSteps[(int)AXISID.AXIS1] = 3500;
            BreakSteps[(int)AXISID.AXIS2] = 3500;
        }

        public override void MCAxisSlew(AXISID Axis, double Speed)
        {
            // Limit maximum speed
            if (Speed > MAX_SPEED)                  // 3.4 degrees/sec, 800X sidereal rate, is the highest speed.
                Speed = MAX_SPEED;
            else if (Speed < -MAX_SPEED)
                Speed = -MAX_SPEED;

            double InternalSpeed = Speed;
            bool forward = false, highspeed = false;

            // InternalSpeed lower than 1/1000 of sidereal rate?
            if (Math.Abs(InternalSpeed) <= CONSTANT.SIDEREALRATE / 1000.0)
            {
                MCAxisStop(Axis);
                return;
            }

            // Stop motor and set motion mode if necessary.
            PrepareForSlewing(Axis, InternalSpeed);

            if (InternalSpeed > 0.0)
                forward = true;
            else
            {
                InternalSpeed = -InternalSpeed;
                forward = false;
            }

            // TODO: ask the details

            // Calculate and set step period. 
            if (InternalSpeed > LOW_SPEED_MARGIN)
            {						// High speed adjustment
                InternalSpeed = InternalSpeed / (double)HighSpeedRatio[(int)Axis];
                highspeed = true;
            }
            InternalSpeed = 1 / InternalSpeed;							// For using function RadSpeedToInt(), change to unit Senonds/Rad.
            long SpeedInt = RadSpeedToInt(Axis, InternalSpeed);
            if ((MCVersion == 0x010600) || (MCVersion == 0x010601))	// For special MC version.
                SpeedInt -= 3;
            if (SpeedInt < 6) SpeedInt = 6;
            SetStepPeriod(Axis, SpeedInt);

            // Start motion
            // if (AxesStatus[Axis] & AXIS_FULL_STOPPED)				// It must be remove for the latest DC motor board.
            StartMotion(Axis);

            AxesStatus[(int)Axis].SetSlewing(forward, highspeed);
            SlewingSpeed[(int)Axis] = Speed;
        }
        public override void MCAxisSlewTo(AXISID Axis, double TargetPosition)
        {
            // Get current position of the axis.
            var CurPosition = MCGetAxisPosition(Axis);

            // Calculate slewing distance.
            // Note: For EQ mount, Positions[AXIS1] is offset( -PI/2 ) adjusted in UpdateAxisPosition().
            var MovingAngle = TargetPosition - CurPosition;

            // Convert distance in radian into steps.
            var MovingSteps = AngleToStep(Axis, MovingAngle);

            bool forward = false, highspeed = false;

            // If there is no increment, return directly.
            if (MovingSteps == 0)
            {
                return;
            }

            // Set moving direction
            if (MovingSteps > 0)
            {
                dir = '0';
                forward = true;
            }
            else
            {
                dir = '1';
                MovingSteps = -MovingSteps;
                forward = false;
            }

            // Might need to check whether motor has stopped.

            // Check if the distance is long enough to trigger a high speed GOTO.
            if (MovingSteps > LowSpeedGotoMargin[(int)Axis])
            {
                SetMotionMode(Axis, '0', dir);		// high speed GOTO slewing 
                highspeed = true;
            }
            else
            {
                SetMotionMode(Axis, '2', dir);		// low speed GOTO slewing
                highspeed = false;
            }

            SetGotoTargetIncrement(Axis, MovingSteps);
            SetBreakPointIncrement(Axis, BreakSteps[(int)Axis]);
            StartMotion(Axis);

            TargetPositions[(int)Axis] = TargetPosition;
            AxesStatus[(int)Axis].SetSlewingTo(forward, highspeed);
        }
        public override void MCAxisStop(AXISID Axis)
        {
            if (InstantStop)
                TalkWithAxis(Axis, 'L', null);
            else
                TalkWithAxis(Axis, 'K', null);

            AxesStatus[(int)Axis].SetFullStop();
        }
        public override void MCSetAxisPosition(AXISID Axis, double NewValue)
        {
            long NewStepIndex = AngleToStep(Axis, NewValue);
            NewStepIndex += 0x800000;

            string szCmd = longTo6BitHEX(NewStepIndex);
            TalkWithAxis(Axis, 'E', szCmd);

            Positions[(int)Axis] = NewValue;
        }
        public override double MCGetAxisPosition(AXISID Axis)
        {
            string response = TalkWithAxis(Axis, 'j', null);

            long iPosition = BCDstr2long(response);
            iPosition -= 0x00800000;
            Positions[(int)Axis] = StepToAngle(Axis, iPosition);

            return Positions[(int)Axis];
        }
        public override AXISSTATUS MCGetAxisStatus(AXISID Axis)
        {

            var response = TalkWithAxis(Axis, 'f', null);

            if ((response[2] & 0x01) != 0)
            {
                // Axis is running
                if ((response[1] & 0x01) != 0)
                    AxesStatus[(int)Axis].Slewing = true;		// Axis in slewing(AstroMisc speed) mode.
                else
                    AxesStatus[(int)Axis].SlewingTo = true;		// Axis in SlewingTo mode.
            }
            else
            {
                AxesStatus[(int)Axis].FullStop = true;	// FullStop = 1;	// Axis is fully stop.
            }

            if ((response[1] & 0x02) == 0)
                AxesStatus[(int)Axis].SlewingForward = true;	// Angle increase = 1;
            else
                AxesStatus[(int)Axis].SlewingForward = false;

            if ((response[1] & 0x04) != 0)
                AxesStatus[(int)Axis].HighSpeed = true; // HighSpeed running mode = 1;
            else
                AxesStatus[(int)Axis].HighSpeed = false;

            if ((response[3] & 1) == 0)
                AxesStatus[(int)Axis].NotInitialized = true;	// MC is not initialized.
            else
                AxesStatus[(int)Axis].NotInitialized = false;


            return AxesStatus[(int)Axis];
        }

        public override void MCSetSwitch(bool OnOff)
        {
            if (OnOff)
                TalkWithAxis(AXISID.AXIS1, 'O', "1");
            else
                TalkWithAxis(AXISID.AXIS1, 'O', "0");
        }  

        // Skywaterch Helper function
        protected bool IsHEXChar(char tmpChar)
        {
            return ((tmpChar >= '0') && (tmpChar <= '9')) || ((tmpChar >= 'A') && (tmpChar <= 'F'));
        }
        protected long HEX2Int(char HEX)
        {
            long tmp;
            tmp = HEX - 0x30;
            if (tmp > 9)
                tmp -= 7;
            return tmp;
        }
        protected long BCDstr2long(string str)
        {
            // =020782 => 8521474
            try
            {
                long value = 0;
                for (int i = 1; i+1 < str.Length; i += 2)
                {
                    value += (long)(int.Parse(str.Substring(i, 2), System.Globalization.NumberStyles.AllowHexSpecifier) * Math.Pow(16, i - 1));
                }

                // if(D)
                // Log.d(TAG,"BCDstr2long " + response + ","+value);
                return value;
            }
            catch (FormatException e)
            {
                throw new MountControlException(ErrorCode.ERR_INVALID_DATA,
                                "Parse BCD Failed");
            }
            // return Integer.parseInt(response.substring(0, 2), 16)
            // + Integer.parseInt(response.substring(2, 4), 16) * 256
            // + Integer.parseInt(response.substring(4, 6), 16) * 256 * 256;
        }
        protected string longTo6BitHEX(long number)
        {
            // 31 -> 0F0000
            String A = ((int)number & 0xFF).ToString("X").ToUpper();
            String B = (((int)number & 0xFF00) / 256).ToString("X").ToUpper();
            String C = (((int)number & 0xFF0000) / 256 / 256).ToString("X").ToUpper();

            if (A.Length == 1)
                A = "0" + A;
            if (B.Length == 1)
                B = "0" + B;
            if (C.Length == 1)
                C = "0" + C;

            // if (D)
            // Log.d(TAG, "longTo6BitHex " + number + "," + A + "," + B + "," + C);

            return A + B + C;
        }

        // Test if connect to DC motor board.
        private bool CheckIfDCMotor()
        {
            //// Enter critical section to block others conversation with motor controller.
            ////EnterCriticalSection(&csPortBusy);
            ////PortBusy = TRUE;

            //// Enable TX driver.
            //EscapeCommFunction(hCom, SETRTS);
            //// Wait TX enabled
            //Sleep(20);

            //// Clear unsent charactor and any charactor received before this conversation.
            //// Do this after I get control of communication bus.
            //PurgeComm(hCom, PURGE_RXCLEAR | PURGE_TXCLEAR);

            //// Send a Start charactor. 
            //// The Skyatcher motor controller always resets its RX buffer whenever a start charactor is received.
            //WriteFile(hCom, ":", 1, &Count, NULL);

            //// Disable TX driver.
            //// EscapeCommFunction(hCom, CLRRTS);

            //ReadFile(hCom, Buffer, 1, &Count, NULL);

            //if ((Count == 1) && (Buffer[0] == ':'))
            //{
            //    IsDCMotor = TRUE;

            //    // Disable TX driver if it is a DC motor controller.
            //    EscapeCommFunction(hCom, CLRRTS);
            //}
            //else
            //    IsDCMotor = FALSE;

            //// PortBusy = FALSE;
            //// Quit critical section. Enable other conversation.
            //// LeaveCriticalSection(&csPortBusy);

            //SetEvent(hEvent_SerialPortIdled);

            //return IsDCMotor;
            lock (mConnection)
            {
                mConnection.Lock();
                System.Threading.Thread.Sleep(20);

                mConnection.ClearBuffer();
                mConnection.Write(":");

                mConnection.Release();

                var r = mConnection.Read();

                if (r.Length == 1 && r[0] == ':')
                    return true;
                else return false;
            }
        }
        private void PrepareForSlewing(AXISID Axis, double speed)
        {
            bool FirstRun = true;
            char cDirection;

            var axesstatus = MCGetAxisStatus(Axis);
            if (!axesstatus.FullStop)
            {
                if ((axesstatus.SlewingTo) ||											// GOTO in action
                     (axesstatus.HighSpeed) ||										// Currently high speed slewing
                     (Math.Abs(speed) >= LOW_SPEED_MARGIN) ||												// Will be high speed slewing
                     ((axesstatus.SlewingForward) && (speed < 0)) ||					// Different direction
                     (!(axesstatus.SlewingForward) && (speed > 0))						// Different direction
                    )
                {
                    // We need to stop the motor first to change Motion Mode, etc.
                    MCAxisStop(Axis);
                }
                else
                    // Other situatuion, there is no need to set motion mode.
                    return;



                // Wait until the axis stop
                while (true)
                {
                    // Update Mount status, the status of both axes are also updated because _GetMountStatus() includes such operations.
                    axesstatus = MCGetAxisStatus(Axis);

                    // Return if the axis has stopped.
                    if (axesstatus.FullStop)
                        break;

                    Thread.Sleep(100);

                    // If the axis is asked to stop.
                    // if ( (!AxesAskedToRun[Axis] && !(MountStatus & MOUNT_TRACKING_ON)) )		// If AXIS1 or AXIS2 is asked to stop or 
                    //	return ERR_USER_INTERRUPT;

                }

            }
            if (speed > 0.0)
            {
                cDirection = '0';
            }
            else
            {
                cDirection = '1';
                speed = -speed;							// Get absolute value of Speed.
            }

            if (speed > LOW_SPEED_MARGIN)
            {
                SetMotionMode(Axis, '3', cDirection);					// Set HIGH speed slewing mode.
            }
            else
                SetMotionMode(Axis, '1', cDirection);					// Set LOW speed slewing mode.

        }


        /************************ MOTOR COMMAND SET ***************************/
        // Inquire Motor Board Version ":e(*1)", where *1: '1'= CH1, '2'= CH2, '3'= Both.
        protected void InquireMotorBoardVersion(AXISID Axis)
        {
            string response = TalkWithAxis(Axis, 'e', null);

            long tmpMCVersion = BCDstr2long(response);

            MCVersion = ((tmpMCVersion & 0xFF) << 16) | ((tmpMCVersion & 0xFF00)) | ((tmpMCVersion & 0xFF0000) >> 16);

        }
        // Inquire Grid Per Revolution ":a(*2)", where *2: '1'= CH1, '2' = CH2.
        protected void InquireGridPerRevolution(AXISID Axis)
        {
            string response = TalkWithAxis(Axis, 'a', null);

            long GearRatio = BCDstr2long(response);

            // There is a bug in the earlier version firmware(Before 2.00) of motor controller MC001.
            // Overwrite the GearRatio reported by the MC for 80GT mount and 114GT mount.
            if ((MCVersion & 0x0000FF) == 0x80)
            {
                GearRatio = 0x162B97;		// for 80GT mount
            }
            if ((MCVersion & 0x0000FF) == 0x82)
            {
                GearRatio = 0x205318;		// for 114GT mount
            }

            FactorRadToStep[(int)Axis] = GearRatio / (2 * Math.PI);
            FactorStepToRad[(int)Axis] = 2 * Math.PI / GearRatio;
        }
        // Inquire Timer Interrupt Freq ":b1".
        protected void InquireTimerInterruptFreq(AXISID Axis)
        {
            string response = TalkWithAxis(Axis, 'b', null);

            long TimeFreq = BCDstr2long(response);
            StepTimerFreq[(int)Axis] = TimeFreq;

            FactorRadRateToInt[(int)Axis] = (double)(StepTimerFreq[(int)Axis]) / FactorRadToStep[(int)Axis];
        }
        // Inquire high speed ratio ":g(*2)", where *2: '1'= CH1, '2' = CH2.
        protected void InquireHighSpeedRatio(AXISID Axis)
        {
            string response = TalkWithAxis(Axis, 'g', null);

            long highSpeedRatio = BCDstr2long(response);
            HighSpeedRatio[(int)Axis] = highSpeedRatio;
        }
        // Inquire PEC Period ":s(*1)", where *1: '1'= CH1, '2'= CH2, '3'= Both.
        protected void InquirePECPeriod(AXISID Axis)
        {
            string response = TalkWithAxis(Axis, 's', null);

            long PECPeriod = BCDstr2long(response);
            PESteps[(int)Axis] = PECPeriod;
        }
        // Set initialization done ":F3", where '3'= Both CH1 and CH2.
        protected virtual void InitializeMC()
        {
            TalkWithAxis(AXISID.AXIS1, 'F', null);
            TalkWithAxis(AXISID.AXIS2, 'F', null);
        }
        protected void SetMotionMode(AXISID Axis, char func, char direction)
        {
            string szCmd = "" + func + direction;
            TalkWithAxis(Axis, 'G', szCmd);
        }
        protected void SetGotoTargetIncrement(AXISID Axis, long StepsCount)
        {
            string cmd = longTo6BitHEX(StepsCount);

            TalkWithAxis(Axis, 'H', cmd);
        }
        protected void SetBreakPointIncrement(AXISID Axis, long StepsCount)
        {
            string szCmd = longTo6BitHEX(StepsCount);

            TalkWithAxis(Axis, 'M', szCmd);
        }
        protected void SetBreakSteps(AXISID Axis, long NewBrakeSteps)
        {
            string szCmd = longTo6BitHEX(NewBrakeSteps);
            TalkWithAxis(Axis, 'U', szCmd);
        }
        protected void SetStepPeriod(AXISID Axis, long StepsCount)
        {
            string szCmd = longTo6BitHEX(StepsCount);
            TalkWithAxis(Axis, 'I', szCmd);
        }
        protected void StartMotion(AXISID Axis)
        {
            TalkWithAxis(Axis, 'J', null);
        }
    }
}
