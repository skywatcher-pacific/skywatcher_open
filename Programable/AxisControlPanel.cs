
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
using System.Windows.Forms;
using BasicApi;

namespace Programable
{
    public partial class AxisControlPanel : UserControl
    {        
        public AxisControlPanel()
        {
            InitializeComponent();

            Controller.ControllerEnabledUpdated += new EventHandler(Controller_ControllerEnabledUpdated);
        }

        void Controller_ControllerEnabledUpdated(object sender, EventArgs e)
        {
            this.Enabled = Controller.IsIdle;
        }

        public double AxisPos1
        {
            get
            {
                return Convert.ToDouble(numericUpDownAxis1.Value);
            }
            set
            {
                numericUpDownAxis1.Value = Convert.ToDecimal(value / RAD1);
            }
        }
        public double AxisPos2
        {
            get
            {
                return Convert.ToDouble(numericUpDownAxis2.Value);
            }
            set
            {
                numericUpDownAxis2.Value = Convert.ToDecimal(value / RAD1);
            }
        }
        private double RAD1 = BasicMath.RAD1;
        private void buttonSetAxis1_Click(object sender, EventArgs e)
        {            
            Controller.SetAxisPosition(AXISID.AXIS1, AxisPos1);
        }
        private void buttonGotoAxis1_Click(object sender, EventArgs e)
        {
            Controller.AxisSlewTo(AXISID.AXIS1, AxisPos1);            
        }
        private void buttonSetAxis2_Click(object sender, EventArgs e)
        {
            Controller.SetAxisPosition(AXISID.AXIS2, AxisPos2);            
        }
        private void buttonGotoAxis2_Click(object sender, EventArgs e)
        {
            Controller.AxisSlewTo(AXISID.AXIS2, AxisPos2);
        }        
    }
}
