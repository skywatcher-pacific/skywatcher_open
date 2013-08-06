using System;
using System.Collections.Generic;
using System.Text;
using AstroLib;

namespace AstroApi
{
    public static class AstroMisc
    {
        public const double SIDEREALRATE = 2 * Math.PI / 86164.09065;
        public const double RADIAN = 180.0 / Math.PI;
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
        public static VECTOR AstroCoordToVector(CartesianCrood AnglePair)
        {
            return new VECTOR
            {
                X = Math.Cos(AnglePair.Phi) * Math.Cos(AnglePair.Theta),
                Y = Math.Sin(AnglePair.Phi) * Math.Cos(AnglePair.Theta),
                Z = Math.Sin(AnglePair.Theta)
            };
        }
        public static CartesianCrood VectorToAstroCoord(VECTOR vector)
        {
            double fNormal = Math.Sqrt(vector.X * vector.X +
                vector.Y * vector.Y +
                vector.Z * vector.Z);

            // VECTOR is struct, in c#, it is call by value type
            double x = vector.X / fNormal;
            double y = vector.Y / fNormal;
            double z = vector.Z / fNormal;

            return new CartesianCrood
            {
                Phi = Math.Atan2(y, x),
                Theta = Math.Asin(z)
            };
        }
        public static double DegToRad(double Degree) { return (Degree / 180 * Math.PI); }
        public static double RadToDeg(double Rad) { return (Rad / Math.PI * 180.0); }
        public static double RadToMin(double Rad) { return (Rad / Math.PI * 180.0 * 60.0); }
        public static double RadToSec(double Rad) { return (Rad / Math.PI * 180.0 * 60.0 * 60.0); }
        public static double AngularSeparation(CartesianCrood Pos1, CartesianCrood Pos2)
        {
            double CosD12 = Math.Sin(Pos1.Theta) * Math.Sin(Pos2.Theta) + Math.Cos(Pos1.Theta) * Math.Cos(Pos2.Theta) * Math.Cos(Pos1.Phi - Pos2.Phi);
            if (CosD12 > 1.0) CosD12 = 1.0;
            else if (CosD12 < -1.0) CosD12 = -1.0;

            return Math.Acos(CosD12);
        }
        public static double GetSiderialTime(DateTime LocalTime, double TimeZoneInHour, bool isDayLightSaving)
        {
            int year = LocalTime.Year;
            int month = LocalTime.Month;
            int day = LocalTime.Day;
            int hour = LocalTime.Hour;
            int minute = LocalTime.Minute;
            int second = LocalTime.Second;
            int millisecod = LocalTime.Millisecond;

            if (month <= 2)
            {
                year -= 1;
                month += 12;
            }

            int A = LocalTime.Year / 100;
            int B = 2 - A + A / 4;

            double JD = Math.Floor(365.25 * (year + 4716)) + Math.Floor(30.6001 * (month + 1)) + day + B - 1524.5;
            double D = JD - 2451545.0;

            double GMST = 18.697374558 + 24.06570982441908 * D;
            GMST = GMST % 24;

            double Offset = 1.00273790935 * (hour + minute / 60.0 + second / 3600.0 + millisecod / 3600.0 / 1000.0);
            double SiderealTime = GMST + Offset;

            if (isDayLightSaving)
            {
                SiderealTime = SiderealTime - 1.00273790935 * (TimeZoneInHour + 1);
            }
            else
            {
                SiderealTime = SiderealTime - 1.00273790935 * (TimeZoneInHour);
            }
            SiderealTime = 2 * Math.PI * SiderealTime / 24.0;

            return SiderealTime;
        }
        public static double GetLocalSiderialTime(CroodLonLat Location, DateTime LocalTime, int TimeZoneInHour, bool IsDayLightSaving)
        {
            var ST = GetSiderialTime(LocalTime, TimeZoneInHour, IsDayLightSaving);
            return ST - Location.Lon;
        }
        /// Compute Ra Dec
        public static CroodAzAlt FromRaDecToAzAlt(CroodLonLat Location, CroodRaDec RaDec, Angle LST)
        {
            Angle Ha = LST - RaDec.Ra;

            double Az = Math.Atan2(Math.Sin(Ha), Math.Cos(Ha)
                * Math.Sin(Location.Lat) - Math.Tan(RaDec.Dec) * Math.Cos(Location.Lat));
            double Alt = Math.Asin(Math.Sin(Location.Lat) * Math.Sin(RaDec.Dec)
                + Math.Cos(Location.Lat) * Math.Cos(RaDec.Dec) * Math.Cos(Ha));

            return new CroodAzAlt(Az + Math.PI, Alt);
        }
        public static CroodRaDec FromAzAltToRaDec(CroodLonLat Location, CroodAzAlt AzAlt, Angle LST)
        {
            Angle HA = Math.Atan2(Math.Sin(AzAlt.Az + Math.PI), Math.Cos(AzAlt.Az + Math.PI) * Math.Sin(Location.Lat) + Math.Tan(AzAlt.Alt) * Math.Cos(Location.Lat));
            Angle DEC = Math.Asin(Math.Sin(Location.Lat) * Math.Sin(AzAlt.Alt) - Math.Cos(Location.Lat) * Math.Cos(AzAlt.Alt) * Math.Cos(AzAlt.Az + Math.PI));
            Angle RA = LST - HA;
            return new CroodRaDec(RA, DEC);
        }

    }
}
