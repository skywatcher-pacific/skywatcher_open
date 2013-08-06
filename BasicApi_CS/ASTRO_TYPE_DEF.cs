using System;

namespace BasicApi
{
    public class MountControlException : Exception
    {
        private ErrorCode ErrCode;
        private string ErrMessage;
        public MountControlException(ErrorCode err)
        {
            ErrCode = err;
        }
        public MountControlException(ErrorCode err, String message)
        {
            ErrCode = err;
            ErrMessage = message;
        }
    }

    public class MOUNTTYPE
    {
        /// AZ or EQ
        /// Skywatcher or Celestron
        /// Model #
    }

    // if it is a flag, should use const, if it is a state, should use enum

    

    public struct AXISSTATUS
    {
        /// <summary>
        /// 4 different state
        /// 1. FullStop
        /// 2. Slewing
        /// 3. SlewingTo
        /// 4. Notinitialized
        /// </summary>

        public bool FullStop;
        public bool Slewing;
        public bool SlewingTo;
        public bool SlewingForward;
        public bool HighSpeed;
        public bool NotInitialized;        

        public void SetFullStop()
        {
            FullStop = true;
            SlewingTo = Slewing = false;      
        }
        public void SetSlewing(bool forward, bool highspeed)
        {
            FullStop = SlewingTo = false;
            Slewing = true;

            SlewingForward = forward;
            HighSpeed = highspeed;
        }
        public void SetSlewingTo(bool forward, bool highspeed)
        {
            FullStop = Slewing = false;
            SlewingTo = true;

            SlewingForward = forward;
            HighSpeed = highspeed;
        }

        //// Mask for axis status
        //public const long AXIS_FULL_STOPPED = 0x0001;		// 該軸處於完全停止狀態
        //public const long AXIS_SLEWING = 0x0002;			// 該軸處於恒速運行狀態
        //public const long AXIS_SLEWING_TO = 0x0004;		    // 該軸處於運行到指定目標位置的過程中
        //public const long AXIS_SLEWING_FORWARD = 0x0008;	// 該軸正向運轉
        //public const long AXIS_SLEWING_HIGHSPEED = 0x0010;	// 該軸處於高速運行狀態
        //public const long AXIS_NOT_INITIALIZED = 0x0020;    // MC控制器尚未初始化, axis is not initialized.
    }

    

    // 望遠鏡兩軸的代碼
    public enum AXISID { AXIS1 = 0, AXIS2 = 1 };	// ID unsed in ASTRO.DLL for axis 1 and axis 2 of a mount.

    public enum ErrorCode
    {
        ERR_INVALID_ID = 1,			    // 無效的望遠鏡代碼					// Invalid mount ID
        ERR_ALREADY_CONNECTED = 2,	    // 已經連接到另外一個ID的望遠鏡		// Already connected to another mount ID
        ERR_NOT_CONNECTED = 3,		    // 尚未連接到望遠鏡					// Telescope not connected.
        ERR_INVALID_DATA = 4, 		    // 無效或超範圍的資料				// Invalid data, over range etc
        ERR_SERIAL_PORT_BUSY = 5, 	    // 串口忙				            // Serial port is busy.
        ERR_NORESPONSE_AXIS1 = 100,	    // 望遠鏡的主軸沒有回應				// No response from axis1
        ERR_NORESPONSE_AXIS2 = 101,	    // 望遠鏡的次軸沒有回應
        ERR_AXIS_BUSY = 102,			    // 暫時無法執行該操作
        ERR_MAX_PITCH = 103,			    // 目標位置仰角過高
        ERR_MIN_PITCH = 104,			    // 目標位置仰角過低
        ERR_USER_INTERRUPT = 105,	        // 用戶強制終止
        ERR_ALIGN_FAILED = 200,		    // 校準望遠鏡失敗
        ERR_UNIMPLEMENT = 300,		    // 未實現的方法
        ERR_WRONG_ALIGNMENT_DATA = 400,	// The alignment data is incorect.
    };

    //public static class ErrorCode
    //{
    //    public const long ERR_INVALID_ID = 1;			    // 無效的望遠鏡代碼					// Invalid mount ID
    //    public const long ERR_ALREADY_CONNECTED = 2;	    // 已經連接到另外一個ID的望遠鏡		// Already connected to another mount ID
    //    public const long ERR_NOT_CONNECTED = 3;		    // 尚未連接到望遠鏡					// Telescope not connected.
    //    public const long ERR_INVALID_DATA = 4; 		    // 無效或超範圍的資料				// Invalid data, over range etc
    //    public const long ERR_SERIAL_PORT_BUSY = 5; 	    // 串口忙				            // Serial port is busy.
    //    public const long ERR_NORESPONSE_AXIS1 = 100;	    // 望遠鏡的主軸沒有回應				// No response from axis1
    //    public const long ERR_NORESPONSE_AXIS2 = 101;	    // 望遠鏡的次軸沒有回應
    //    public const long ERR_AXIS_BUSY = 102;			    // 暫時無法執行該操作
    //    public const long ERR_MAX_PITCH = 103;			    // 目標位置仰角過高
    //    public const long ERR_MIN_PITCH = 104;			    // 目標位置仰角過低
    //    public const long ERR_USER_INTERRUPT = 105;	        // 用戶強制終止
    //    public const long ERR_ALIGN_FAILED = 200;		    // 校準望遠鏡失敗
    //    public const long ERR_UNIMPLEMENT = 300;		    // 未實現的方法
    //    public const long ERR_WRONG_ALIGNMENT_DATA = 400;	// The alignment data is incorect.
    //}

    public enum MOUNTID
    {
         // Telescope ID, they must be started from 0 and coded continuously.
         ID_CELESTRON_AZ = 0,				// Celestron Alt/Az Mount
         ID_CELESTRON_EQ = 1,				// Celestron EQ Mount
         ID_SKYWATCHER_AZ = 2,			    // Skywatcher Alt/Az Mount
         ID_SKYWATCHER_EQ = 3,			    // Skywatcher EQ Mount
         ID_ORION_EQG = 4,				    // Orion EQ Mount
         ID_ORION_TELETRACK = 5,			// Orion TeleTrack Mount
         ID_EQ_EMULATOR = 6,				// EQ Mount Emulator
         ID_AZ_EMULATOR = 7,				// Alt/Az Mount Emulator
         ID_NEXSTARGT80 = 8,				// NexStarGT-80 mount
         ID_NEXSTARGT114 = 9,				// NexStarGT-114 mount
         ID_STARSEEKER80 = 10,			    // NexStarGT-80 mount
         ID_STARSEEKER114 = 11,			// NexStarGT-114 mount
    }

    //public static class MOUNTID
    //{
    //    // Telescope ID, they must be started from 0 and coded continuously.
    //    public const long ID_CELESTRON_AZ = 0;				// Celestron Alt/Az Mount
    //    public const long ID_CELESTRON_EQ = 1;				// Celestron EQ Mount
    //    public const long ID_SKYWATCHER_AZ = 2;			    // Skywatcher Alt/Az Mount
    //    public const long ID_SKYWATCHER_EQ = 3;			    // Skywatcher EQ Mount
    //    public const long ID_ORION_EQG = 4;				    // Orion EQ Mount
    //    public const long ID_ORION_TELETRACK = 5;			// Orion TeleTrack Mount
    //    public const long ID_EQ_EMULATOR = 6;				// EQ Mount Emulator
    //    public const long ID_AZ_EMULATOR = 7;				// Alt/Az Mount Emulator
    //    public const long ID_NEXSTARGT80 = 8;				// NexStarGT-80 mount
    //    public const long ID_NEXSTARGT114 = 9;				// NexStarGT-114 mount
    //    public const long ID_STARSEEKER80 = 10;			    // NexStarGT-80 mount
    //    public const long ID_STARSEEKER114 = 11;			// NexStarGT-114 mount
    //}


    public static class CONSTANT
    {
        public const double SIDEREALRATE = 2 * Math.PI / 86164.09065;
    }

    public static class BasicMath
    {
        public const double RAD1 = Math.PI / 180;
        public static double AngleDistance(double ang1, double ang2)
        {
            ang1 = UniformAngle(ang1);
            ang2 = UniformAngle(ang2);

            double d = ang2 - ang1;

            return UniformAngle(d);
        }
        public static double UniformAngle(double Source)
        {
            Source = Source % (Math.PI * 2);
            if (Source > Math.PI)
                return Source - 2 * Math.PI;
            if (Source < -Math.PI)
                return Source + 2 * Math.PI;
            return Source;
        }
        
        public static double DegToRad(double Degree) { return (Degree / 180 * Math.PI); }
        public static double RadToDeg(double Rad) { return (Rad / Math.PI * 180.0); }
        public static double RadToMin(double Rad) { return (Rad / Math.PI * 180.0 * 60.0); }
        public static double RadToSec(double Rad) { return (Rad / Math.PI * 180.0 * 60.0 * 60.0); }
                
    }


}
