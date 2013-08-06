//---------------------------------------------------------------------------------
//BY DOWNLOADING AND USING, YOU AGREE TO THE FOLLOWING TERMS:
//Copyright (c) 2010 by Pacific Telescope
//LICENSE
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//the MIT License, given here: <http://www.opensource.org/licenses/mit-license.php> 
//---------------------------------------------------------------------------------

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Threading;
using BasicApi;

namespace Programable
{
    public class Marker
    {
        public double Axis1, Axis2;
    }

    /// <summary>
    /// It is a demo program for Skywather's open mount API
    /// A simple mount control proxy. It includes:
    /// 1) Basic mount control Api
    /// 2) Up to date mount status
    /// 3) Bookmark for user to go back to previous point
    /// </summary>
    public static class Controller
    {
        private static bool MarkerUpdated = false;
        private static bool LockUpdated = false;
        private static List<KeyValuePair<DateTime, String>> Logs = null;
        private static List<Marker> Markers = new List<Marker>();
        public static bool IsIdle
        {
            get;
            private set;
        }
        
        private static event EventHandler EventInvoker = delegate {};
        public static event EventHandler StatusUpdated = delegate { };
        public static event EventHandler MarkersChanged = delegate { };
        public static event EventHandler ControllerEnabledUpdated = delegate { };

        private const double RAD1 = BasicMath.RAD1;
        private static Mount pMount = null;

        private static BackgroundWorker StatusUpdater = new BackgroundWorker();
        private static BackgroundWorker GotoWorker = new BackgroundWorker();

        public static void Init(int mount, int com)
        {
            StatusUpdater.DoWork += new DoWorkEventHandler(StatusUpdater_DoWork);
            StatusUpdater.ProgressChanged += new ProgressChangedEventHandler(StatusUpdater_ProgressChanged);
            StatusUpdater.RunWorkerCompleted += new RunWorkerCompletedEventHandler(StatusUpdater_RunWorkerCompleted);
            StatusUpdater.WorkerSupportsCancellation = true;
            StatusUpdater.WorkerReportsProgress = true;

            GotoWorker.DoWork += new DoWorkEventHandler(GotoWorker_DoWork);
            GotoWorker.ProgressChanged += new ProgressChangedEventHandler(GotoWorker_ProgressChanged);
            GotoWorker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(GotoWorker_RunWorkerCompleted);
            GotoWorker.WorkerSupportsCancellation = true;
            GotoWorker.WorkerReportsProgress = true;

            
            pMount = new Mount_Skywatcher();
            pMount.Connect_COM(com);
            pMount.MCInit();

            StatusUpdater.RunWorkerAsync();
            EventInvoker += new EventHandler(Controller_EventInvoker);

            UnLock();
        }

        private static void Controller_EventInvoker(object sender, EventArgs e)
        {
            EventHandler handler = sender as EventHandler;

            foreach (var i in handler.GetInvocationList())
            {
                i.DynamicInvoke();
            }
        }

        private static void GotoWorker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            Trace.WriteLine("Unlock");
            UnLock();
        }
        private static void GotoWorker_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            //throw new NotImplementedException();
        }
        private static void GotoWorker_DoWork(object sender, DoWorkEventArgs e)
        {
            double[] target = e.Argument as double[];
            double axis1 = target[0], axis2 = target[1];

            //Trace.WriteLine(string.Format("Goto {0:f2} {1:f2}", axis1, axis2), "INFO");            

            AxisSlewTo(AXISID.AXIS1, axis1);
            Thread.Sleep(500);
            AxisSlewTo(AXISID.AXIS2, axis2);

            var pAxisPos1 = pMount.MCGetAxisPosition(AXISID.AXIS1) / RAD1;
            var pAxisPos2 = pMount.MCGetAxisPosition(AXISID.AXIS2) / RAD1;
            var pAxisStatus1 = pMount.MCGetAxisStatus(AXISID.AXIS1);
            var pAxisStatus2 = pMount.MCGetAxisStatus(AXISID.AXIS2);

            Thread.Sleep(1000);

            while (!pAxisStatus1.FullStop || !pAxisStatus2.FullStop)
            {
                pAxisPos1 = pMount.MCGetAxisPosition(AXISID.AXIS1) / RAD1;
                pAxisPos2 = pMount.MCGetAxisPosition(AXISID.AXIS2) / RAD1;
                pAxisStatus1 = pMount.MCGetAxisStatus(AXISID.AXIS1);
                pAxisStatus2 = pMount.MCGetAxisStatus(AXISID.AXIS2);

                Thread.Sleep(1000);
            }

            Trace.WriteLine(string.Format("Goto {0:f2} {1:f2} Complete", axis1, axis2), "INFO");
        }

        private static void StatusUpdater_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {

        }
        private static void StatusUpdater_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            UpdateStatus();

            if (MarkerUpdated)
            {
                UpdateMarker();
                MarkerUpdated = false;
            }

            if (LockUpdated)
            {
                UpdateLock();
                LockUpdated = false;
            }
        }
        private static void StatusUpdater_DoWork(object sender, DoWorkEventArgs e)
        {            
            while (!StatusUpdater.CancellationPending)
            {
                try
                {
                    StatusUpdater.ReportProgress(0);
                    Thread.Sleep(500);
                }
                catch { }                                
            }
        }

        private static AXISID ID(int id)
        {
            if (id == 0) return AXISID.AXIS1;
            else if (id == 1) return AXISID.AXIS2;
            throw new Exception();
        }
        private static int _ID(AXISID id)
        {
            return id == AXISID.AXIS1 ? 0 : 1;
        }

        public static void AxisSlew(AXISID id, double degree)
        {
            pMount.MCAxisSlew(id, degree * RAD1);
        }
        public static void AxisSlewTo(AXISID id, double position)
        {
            pMount.MCAxisSlewTo(id, position * RAD1);
        }
        public static void AxisStop(AXISID id)
        {
            pMount.MCAxisStop(id);
        }
        public static void SetAxisPosition(AXISID id, double NewValue)
        {
            pMount.MCSetAxisPosition(id, NewValue * RAD1);
        }

        // Update Automaticlly
        private static double AxisPos1, AxisPos2;
        private static AXISSTATUS AxisStatus1, AxisStatus2;
        private static void UpdateMarker()
        {
            MarkersChanged.Invoke(typeof(Controller), null);
        }
        private static void UpdateStatus()
        {
            AxisPos1 = pMount.MCGetAxisPosition(AXISID.AXIS1) / RAD1;
            AxisPos2 = pMount.MCGetAxisPosition(AXISID.AXIS2) / RAD1;
            AxisStatus1 = pMount.MCGetAxisStatus(AXISID.AXIS1);
            AxisStatus2 = pMount.MCGetAxisStatus(AXISID.AXIS2);

            //EventInvoker.BeginInvoke(StatusUpdated, null, null, null);
            //StatusUpdated.BeginInvoke(typeof(Controller), null, null, null);
            StatusUpdated.Invoke(typeof(Controller), null);
            //System.Diagnostics.Trace.WriteLine(string.Format("Update Status, {0:f2} {1:f2} {2} {3}", AxisPos1, AxisPos2, AxisStatus1.FullStop, AxisStatus2.FullStop),"INFO");
        }
        private static void UpdateLock()
        {
            ControllerEnabledUpdated.Invoke(typeof(Controller), null);
        }

        public static double GetAxisPosition(AXISID id)
        {
            return id == AXISID.AXIS1 ? AxisPos1 : AxisPos2;
        }
        public static AXISSTATUS GetAxisStatus(AXISID id)
        {
            return id == AXISID.AXIS1 ? AxisStatus1 : AxisStatus2;
        }

        public static Mount GetMount()
        {
            return pMount;
        }
        public static void SetSwitch(bool OnOff)
        {
            pMount.MCSetSwitch(OnOff);
        }
        public static void Sleep(int milliseconds)
        {
            System.Threading.Thread.Sleep(milliseconds);
        }

        public static void InsertMarker(int index, Marker m)
        {
            Markers.Insert(index, m);
            MarkerUpdated = true;
        }
        public static void AddMarker(double axis1, double axis2)
        {
            Markers.Add(new Marker { Axis1 = axis1, Axis2 = axis2 });
            MarkerUpdated = true;
        }
        public static void RemoveMarkerAt(int index)
        {
            Markers.RemoveAt(index);
            MarkerUpdated = true;
        }
        public static void RemoveMarker(Marker m)
        {
            Markers.Remove(m);
            MarkerUpdated = true;
        }
        public static void ClearMarker()
        {
            Markers.Clear();
            MarkerUpdated = true;
        }
        public static Marker GetMarker(int index)
        {
            return Markers[index];
        }
        public static void GotoMarker(int index)
        {
            AxisSlewTo(0, Markers[index].Axis1);
            AxisSlewTo(0, Markers[index].Axis2);

            /// Wait Until Mount Full Stop or near enough
            do
            {
                pMount.MCGetAxisStatus(AXISID.AXIS1);
                pMount.MCGetAxisStatus(AXISID.AXIS2);
                System.Threading.Thread.Sleep(500);
            } while (!pMount.AxesStatus[0].FullStop && !pMount.AxesStatus[1].FullStop);
        }
        public static List<Marker> GetMarkers()
        {
            return Markers;
        }

        private static void Lock()
        {
            IsIdle = false;
            LockUpdated = true;
            //ControllerEnabledUpdated.Invoke(typeof(Controller), null);
            //EventInvoker.BeginInvoke(ControllerEnabledUpdated, null, null, null);            
        }
        private static void UnLock()
        {
            IsIdle = true;
            LockUpdated = true;
            //EventInvoker.BeginInvoke(ControllerEnabledUpdated, null, null, null);
            //ControllerEnabledUpdated.Invoke(typeof(Controller), null);
        }
        public static void Goto(double axis1, double axis2)
        {
            Trace.WriteLine("Lock");
            Lock();
            GotoWorker.RunWorkerAsync(new double[] { axis1, axis2 });
            return;
        }
    }
}
