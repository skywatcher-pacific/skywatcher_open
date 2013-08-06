using System;
using System.Diagnostics;
using System.IO;
using System.IO.Ports;

/// Notes:
/// 1. Use exception instead of ErrCode because there is no dll import issue we need to handle and 
/// the exception code stlye will much easier to maintain.
/// 2. Need to confirm the mapping between SerialPort class in C# and DCB class in C++, such as CTS.
/// 3. Rename UpdateAxisPosition and UpdateAxesStatus to GetAxisPosition and GetAxesStatus to hide the details
/// 4. LastSlewingIsPositive has been merge with AxesStatus.SLEWING_FORWARD
///
/// 5. While bluetooth connection fail, user should apply Connect_COM and try to connect again
/// RTSEnable may not be accepcted 
/// http://blog.csdn.net/solond/archive/2008/03/04/2146446.aspx
/// 6. It looks like Skywatcher mounts response time is 1.5x longer than Celestron's mount


namespace BasicApi
{

    /// <summary>
    /// Define the abstract interface of a Mount, includes:
    /// 1) Connection via Serial Port    
    /// 2) Protocol 
    /// 3) Basic Mount control interface 
    /// LV0. 
    /// TalkWithAxis
    /// LV1. 
    /// DetectMount // Not implement yet
    /// MCOpenTelescopeConnection
    /// MCCloseTelescopeConnection
    /// MCAxisSlew
    /// MCAxisSlewTo
    /// MCAxisStop
    /// MCSetAxisPosition
    /// MCGetAxisPosition
    /// MCGetAxisStatus
    /// </summary>
    /// Checked 2/7/2011
    public abstract class Mount
    {
        /// The abstract Serial connection instance 
        /// it is static because all connection shared the same serial connection
        /// and connection should be lock between differnct thread
        protected static SerialConnection mConnection = null;
        protected long MCVersion = 0;   // 馬達控制器的版本號

        public MOUNTID MountID = 0;     // Mount Id
        public bool IsEQMount = false;      // the physical meaning of mount (Az or EQ)

        /// ************ Motion control related **********************
        /// They are variables represent the mount's status, but not grantee always updated.        
        /// 1) The Positions are updated with MCGetAxisPosition and MCSetAxisPosition
        /// 2) The TargetPositions are updated with MCAxisSlewTo        
        /// 3) The SlewingSpeed are updated with MCAxisSlew
        /// 4) The AxesStatus are updated updated with MCGetAxisStatus, MCAxisSlewTo, MCAxisSlew
        /// Notes:
        /// 1. Positions may not represent the mount's position while it is slewing, or user manually update by hand
        public double[] Positions = new double[2] { 0, 0 };		    // 托架的軸坐標位置，以弧度爲單位
        public double[] TargetPositions = new double[2] { 0, 0 };	// 目標位置，以弧度爲單位
        public double[] SlewingSpeed = new double[2] { 0, 0 };		// 以弧度/秒為單位的運行速度                
        public AXISSTATUS[] AxesStatus = new AXISSTATUS[2];             // 托架的兩軸狀態，應通過AxesStatus[AXIS1]和AxesStatus[AXIS2]引用

        public Mount()
        {
            mConnection = null;
            MCVersion = 0;
            IsEQMount = false;

            Positions[0] = 0;
            Positions[1] = 0;
            TargetPositions[0] = 0;
            TargetPositions[1] = 0;
            SlewingSpeed[0] = 0;
            SlewingSpeed[1] = 0;
            AxesStatus[0] = new AXISSTATUS { FullStop = false, NotInitialized = true, HighSpeed = false, Slewing = false, SlewingForward = false, SlewingTo = false };
            AxesStatus[1] = new AXISSTATUS { FullStop = false, NotInitialized = true, HighSpeed = false, Slewing = false, SlewingForward = false, SlewingTo = false };
        }
        ~Mount()
        {
            if (mConnection != null)
                mConnection.Close();
        }
        /// <summary>
        /// Build a connection to mount via COM
        /// </summary>
        /// <param name="TelescopePort">the COM port number for connection</param>
        /// Raise IOException
        public virtual void Connect_COM(int TelescopePort)
        {
            // May raise IOException 
            //var hCom = new SerialPort(string.Format("\\$device\\COM{0}", TelescopePort));
            var hCom = new SerialPort(string.Format("COM{0}", TelescopePort));

            // Origional Code in C++
            //// Set communication parameter.
            //GetCommState(hCom, &dcb);
            //dcb.BaudRate = CBR_9600;
            //dcb.fOutxCtsFlow = FALSE;
            //dcb.fOutxDsrFlow = FALSE;
            //dcb.fDtrControl = DTR_CONTROL_DISABLE;
            //dcb.fDsrSensitivity = FALSE;
            //dcb.fTXContinueOnXoff = TRUE;
            //dcb.fOutX = FALSE;
            //dcb.fInX = FALSE;
            //dcb.fErrorChar = FALSE;
            //dcb.fNull = FALSE;
            //dcb.fRtsControl = RTS_CONTROL_DISABLE;
            //dcb.fAbortOnError = FALSE;
            //dcb.ByteSize = 8;
            //dcb.fParity = NOPARITY;
            //dcb.StopBits = ONESTOPBIT;
            //SetCommState(hCom, &dcb);

            //// Communication overtime parameter
            //GetCommTimeouts(hCom, &TimeOuts);
            //TimeOuts.ReadIntervalTimeout = 30;			// Maxim interval between two charactors, set according to Celestron's hand control.
            //TimeOuts.ReadTotalTimeoutAstroMisc = 500;	// Timeout for reading operation.
            //TimeOuts.ReadTotalTimeoutMultiplier = 2;	// DOUBLE the reading timeout
            //TimeOuts.WriteTotalTimeoutAstroMisc = 30;	// Write timeout
            //TimeOuts.WriteTotalTimeoutMultiplier = 2;	// DOUBLE the writing timeout
            //SetCommTimeouts(hCom, &TimeOuts);

            //// Set RTS to high level, this will disable TX driver in iSky.
            //EscapeCommFunction(hCom, CLRRTS);

            // Set communication parameter
            hCom.BaudRate = SerialConnect_COM.CBR.CBR_9600;
            // fOutxCTSFlow
            // fOutxDsrFlow
            hCom.DtrEnable = false;
            // fDsrSensitivity            
            hCom.Handshake = Handshake.RequestToSendXOnXOff;
            // fOutX
            // fInX
            // fErrorChar
            // fNull
            hCom.RtsEnable = false;
            // fAboveOnError
            hCom.Parity = Parity.None;
            hCom.DataBits = 8;
            hCom.StopBits = StopBits.One;

            hCom.ReadTimeout = 1000;
            hCom.WriteTimeout = 60;

            hCom.Open();
            mConnection = new SerialConnect_COM(hCom);
        }
        public virtual MOUNTTYPE DetectMount() { return null; }     // Return the mount's info

        /// <summary>
        /// One communication between mount and client
        /// </summary>
        /// <param name="Axis">The target of command</param>
        /// <param name="Command">The comamnd char set</param>
        /// <param name="cmdDataStr">The data need to send</param>
        /// <returns>The response string from mount</returns>
        protected virtual String TalkWithAxis(AXISID Axis, char Command, string cmdDataStr)
        {
            /// Lock the serial connection
            /// It grantee there is only one thread entering this function in one time
            /// ref: http://msdn.microsoft.com/en-us/library/ms173179.aspx
            /// TODO: handle exception
            lock (mConnection)
            {
                for (int i = 0; i < 2; i++)
                {
                    /// The General Process for SerialPort COM
                    /// 1. Prepare Command Str by protocol
                    /// 2. Wait CTS if need
                    /// 3. Set RTS
                    /// 4. Send Command
                    /// 5. Receive Response
                    /// 6. Clear RTS

                    // prepare to communicate
                    try
                    {
                        mConnection.ClearBuffer();
                        mConnection.WaitIdle();
                        mConnection.Lock();

                        // send the request
                        SendRequest(Axis, Command, cmdDataStr);

                        // Release the line, so the mount can send response
                        mConnection.Release();

                        //Trace.TraceInformation("Send command successful");
                        // receive the response
                        return RecieveResponse();
                    }
                    catch (TimeoutException e)
                    {
                        Trace.TraceError("Timeout, need Resend the Command");                        
                    }
                    catch (IOException e)
                    {
                        Trace.TraceError("Connnection Lost");
                        throw new MountControlException(ErrorCode.ERR_NOT_CONNECTED, e.Message);
                    }
                }
                //Trace.TraceError("Timeout, stop send");
                if (Axis == AXISID.AXIS1)
                    throw new MountControlException(ErrorCode.ERR_NORESPONSE_AXIS1);
                else
                    throw new MountControlException(ErrorCode.ERR_NORESPONSE_AXIS2);
            }

        }

        /// <summary>
        /// 
        /// </summary>
        /// <exception cref="IOException">Throw when</exception>
        /// <param name="Axis"></param>
        /// <param name="Command"></param>
        /// <param name="cmdDataStr"></param>
        protected abstract void SendRequest(AXISID Axis, char Command, string cmdDataStr);
        /// <summary>
        /// Receive the response
        /// </summary>
        /// <exception cref="IOException"></exception>
        /// <exception cref="TimeOutException"></exception>
        /// <returns></returns>
        protected abstract string RecieveResponse();

        /// ************** The Mount Control Interface *****************
        public abstract void MCInit();

        // Mount dependent motion control functions 
        public abstract void MCAxisSlew(AXISID Axis, double rad);
        public abstract void MCAxisSlewTo(AXISID Axis, double pos);
        public abstract void MCAxisStop(AXISID Axis);
        // Unit: radian
        public abstract void MCSetAxisPosition(AXISID Axis, double pos);
        public abstract double MCGetAxisPosition(AXISID Axis);
        // Support Mount Status;        
        public abstract AXISSTATUS MCGetAxisStatus(AXISID Axis);
        public abstract void MCSetSwitch(bool OnOff);

        // 將弧度角度轉換為"步"
        protected double[] FactorRadToStep = new double[] { 0, 0 }; 				// 將弧度數值乘以該系數即可得到馬達板的位置數值（24位數則丟棄最高字節即可）
        protected long AngleToStep(AXISID Axis, double AngleInRad)
        {
            return (long)(AngleInRad * FactorRadToStep[(int)Axis]);
        }

        // 將"步"轉換為弧度角度
        protected double[] FactorStepToRad = new double[] { 0, 0 };                 // 將馬達板的位置數值(需處理符號問題后)乘以該系數即可得到弧度數值
        protected double StepToAngle(AXISID Axis, long Steps)
        {
            return Steps * FactorStepToRad[(int)Axis];
        }

        // 將弧度/秒的速度轉換為設定速度所用的整數
        protected double[] FactorRadRateToInt = new double[] { 0, 0 };			    // 將弧度/秒數值乘以該系數即可得到馬達板所使用的設定速度的32位整數
        protected long RadSpeedToInt(AXISID Axis, double RateInRad)
        {
            return (long)(RateInRad * FactorRadRateToInt[(int)Axis]);
        }

    }
}
