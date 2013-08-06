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
using System.Drawing;
using System.Windows.Forms;
using BasicApi;

namespace Programable
{
    public partial class PreviewChartPanel : UserControl
    {
        private Bitmap BitmapBackground = null;         // background only update while neccessary        
        private Bitmap BitmapBuffer = null;             //                                 

        private const int MarginWidth = 10;
        private const int MarginHeight = 10;
        private Marker SelectedMarker = null;
        private int SelectedIndex
        {
            get
            {
                return listBoxMarkers.SelectedIndex;
            }
            set
            {
                listBoxMarkers.SelectedIndex = value;
            }
        }
        private int Count{
            get { return listBoxMarkers.Items.Count; }
        }

        public PreviewChartPanel()
        {
            InitializeComponent();

            Controller.MarkersChanged += new EventHandler(Controller_MarkersChanged);
            Controller.ControllerEnabledUpdated += new EventHandler(Controller_ControllerEnabledUpdated);
            Controller.StatusUpdated += new EventHandler(Controller_StatusUpdated);

            BitmapBackground = new Bitmap(pictureBoxPreview.Width, pictureBoxPreview.Height);
            BitmapBuffer = new Bitmap(pictureBoxPreview.Width, pictureBoxPreview.Height);
        }

        private void UpdateProcessChartBackground()
        {
            using (Graphics g = Graphics.FromImage(BitmapBackground))
            {
                g.FillRectangle(Brushes.White, 0, 0, BitmapBackground.Width, BitmapBackground.Height);

                var Markers = Controller.GetMarkers();
                for (int i = 0; i < Markers.Count; i++)
                {
                    Point pt = ConvertPoint(Markers[i].Axis1, Markers[i].Axis2);
                    g.FillEllipse(Brushes.Gray, pt.X - 10, pt.Y - 10, 20, 20);
                    g.DrawString(i.ToString(), new Font("Arial", 10, FontStyle.Regular), Brushes.Black, pt.X - 10, pt.Y + 10);
                }
            }
        }
        private void UpdateProcessChartForeground()
        {
            using (Graphics g = Graphics.FromImage(BitmapBuffer))
            {
                var Markers = Controller.GetMarkers();
                /// Copy Background
                g.DrawImage(BitmapBackground, 0, 0);

                /// Draw the selected marker
                if (SelectedMarker != null)
                {
                    Point SelectedPoint = ConvertPoint(SelectedMarker.Axis1, SelectedMarker.Axis2);
                    g.FillEllipse(Brushes.Green, SelectedPoint.X - 10, SelectedPoint.Y - 10, 20, 20);
                }

                Point CurrentPos = ConvertPoint(Controller.GetAxisPosition(AXISID.AXIS1), Controller.GetAxisPosition(AXISID.AXIS2));
                g.FillEllipse(Brushes.Red, CurrentPos.X - 5, CurrentPos.Y - 2, 10, 4);
                g.FillEllipse(Brushes.Red, CurrentPos.X - 2, CurrentPos.Y - 5, 4, 10);
            }
        }        
        private void UpdateProcessChart()
        {
            pictureBoxPreview.Image = BitmapBuffer;
        }

        void Controller_StatusUpdated(object sender, EventArgs e)
        {
            UpdateProcessChartForeground();
            UpdateProcessChart();
        }
        void Controller_ControllerEnabledUpdated(object sender, EventArgs e)
        {
            this.Enabled = Controller.IsIdle;
        }
        void Controller_MarkersChanged(object sender, EventArgs e)
        {
            var Markers = Controller.GetMarkers();
            listBoxMarkers.Items.Clear();

            for (int i = 0; i < Markers.Count; i++)
            {
                listBoxMarkers.Items.Add(string.Format("{0}: ({1:f2},{2:f2})", i, Markers[i].Axis1, Markers[i].Axis2));
                if (Markers[i] == SelectedMarker)
                    listBoxMarkers.SelectedIndex = i;
            }

            UpdateProcessChartBackground();
            UpdateProcessChartForeground();
            UpdateProcessChart();
        }               
        
        private double[] ConvertAxis(int x, int y)
        {
            if (x < MarginWidth || x > pictureBoxPreview.Width - MarginWidth)
                return null;
            if (y < MarginHeight || y > pictureBoxPreview.Height - MarginHeight)
                return null;

            x = x - MarginWidth;
            y = y - MarginHeight;

            int GWidth = pictureBoxPreview.Width - MarginWidth * 2;
            int GHeight = pictureBoxPreview.Height - MarginHeight * 2;

            double Axis1 = (x * 360.0 / GWidth) - 180;
            double Axis2 = ((y * 180.0 / GHeight) - 90) * (-1);

            return new double[] { Axis1, Axis2 };
        }
        private Point ConvertPoint(double axis1, double axis2)
        {
            int GWidth = pictureBoxPreview.Width - MarginWidth * 2;
            int GHeight = pictureBoxPreview.Height - MarginHeight * 2;

            int x = (int)((axis1 + 180) * GWidth / 360);
            int y = (int)((90 - axis2) * GHeight / 180);

            x = x % GWidth;

            return new Point(x + MarginWidth, y + MarginHeight);
        }
        private Marker FindMarker(int x, int y)
        {
            var Markers = Controller.GetMarkers();
            for (int i = 0; i < Markers.Count; i++)
            {
                var p = Markers[i];
                var point = ConvertPoint(p.Axis1, p.Axis2);
                if (Math.Pow(x - point.X, 2) + Math.Pow(y - point.Y, 2) < 100)
                    return p;
            }
            return null;
        }
        
        private void pictureBoxPreview_MouseMove(object sender, MouseEventArgs e)
        {
            double[] axispos = ConvertAxis(e.X, e.Y); ;
            if (axispos != null)
                toolStripStatusLabelMouseOver.Text = string.Format("Pos: ({0:f2},{1:f2})", axispos[0] , axispos[1] );
        }
        private void pictureBoxPreview_Resize(object sender, EventArgs e)
        {
            BitmapBackground = new Bitmap(pictureBoxPreview.Width, pictureBoxPreview.Height);
            BitmapBuffer = new Bitmap(pictureBoxPreview.Width, pictureBoxPreview.Height);
            UpdateProcessChartBackground();
            UpdateProcessChartForeground();
            UpdateProcessChart();
        }        
        private void pictureBoxPreview_MouseClick(object sender, MouseEventArgs e)
        {
            var NearByMarker = FindMarker(e.X, e.Y);
            if (NearByMarker != null)
            {
                var Markers = Controller.GetMarkers();
                listBoxMarkers.SelectedIndex = Markers.FindIndex(a=>a==NearByMarker);
                //SelectedMarker = NearByMarker;
                //UpdateProcessChartForeground();
                //pictureBoxPreview.Image = BitmapBuffer;
                //ListBox
            }
        }        
        private void pictureBoxPreview_MouseDoubleClick(object sender, MouseEventArgs e)
        {            
            var NearByMarker = FindMarker(e.X, e.Y);
            if (NearByMarker != null)
            {
                // Goto a marker
                SelectedMarker = NearByMarker;
                UpdateProcessChartForeground();
                UpdateProcessChart();

                Controller.Goto(NearByMarker.Axis1, NearByMarker.Axis2);
            }
            else
            {
                // Add a new marker
                var pt = ConvertAxis(e.X, e.Y);
                if (pt != null)
                {
                    Controller.AddMarker(pt[0], pt[1]);
                    UpdateProcessChartForeground();
                    UpdateProcessChart();
                }
            }
        }       

        private void listBoxMarkers_SelectedValueChanged(object sender, EventArgs e)
        {
            
            if (listBoxMarkers.SelectedIndex >= 0)
            {
                var Markers = Controller.GetMarkers();
                SelectedMarker = Markers[listBoxMarkers.SelectedIndex];

                UpdateProcessChartForeground();
                UpdateProcessChart();
            }
        }
        private void listBoxMarkers_DoubleClick(object sender, EventArgs e)
        {
            var Markers = Controller.GetMarkers();

            SelectedMarker = Markers[listBoxMarkers.SelectedIndex];
            
            UpdateProcessChartForeground();
            UpdateProcessChart();

            Controller.Goto(SelectedMarker.Axis1, SelectedMarker.Axis2);
        }

        private void toolStripButtonDelete_Click(object sender, EventArgs e)
        {
            if (SelectedMarker != null)
            {
                Controller.RemoveMarker(SelectedMarker);
                
                UpdateProcessChartForeground();
                UpdateProcessChart();

                SelectedMarker = null;                
            }
        }

        private void toolStripButtonUp_Click(object sender, EventArgs e)
        {
            if (SelectedMarker != null && SelectedIndex > 0)
            {
                int index = SelectedIndex;
                Controller.RemoveMarker(SelectedMarker);
                Controller.InsertMarker(index - 1, SelectedMarker);
            }
        }

        private void toolStripButtonDown_Click(object sender, EventArgs e)
        {
            if (SelectedMarker != null && SelectedIndex < Count - 1)
            {
                int index = SelectedIndex;
                Controller.RemoveMarker(SelectedMarker);
                Controller.InsertMarker(index + 1, SelectedMarker);                
            }
        }
    }
}
