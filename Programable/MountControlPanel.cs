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
    public partial class MountControlPanel : UserControl
    {
        public MountControlPanel()
        {
            InitializeComponent();

            Controller.ControllerEnabledUpdated += new EventHandler(Controller_ControllerEnabledUpdated);
        }

        void Controller_ControllerEnabledUpdated(object sender, EventArgs e)
        {
            this.Enabled = Controller.IsIdle;
        }

        private double Speed
        {
            get
            {
                return Convert.ToDouble(numericUpDownSpeed.Value);
            }
            set
            {
                numericUpDownSpeed.Value = Convert.ToDecimal(value);
            }
        }

        private void buttonUp_MouseDown(object sender, MouseEventArgs e)
        {
            Controller.AxisSlew(AXISID.AXIS2, Speed);
        }
        private void buttonUp_MouseUp(object sender, MouseEventArgs e)
        {
            Controller.AxisStop(AXISID.AXIS2);
        }
        private void buttonDown_MouseDown(object sender, MouseEventArgs e)
        {
            Controller.AxisSlew(AXISID.AXIS2, -Speed);
        }
        private void buttonDown_MouseUp(object sender, MouseEventArgs e)
        {
            Controller.AxisStop(AXISID.AXIS2);
        }
        private void buttonLeft_MouseDown(object sender, MouseEventArgs e)
        {
            Controller.AxisSlew(AXISID.AXIS1, -Speed);
        }
        private void buttonLeft_MouseUp(object sender, MouseEventArgs e)
        {
            Controller.AxisStop(AXISID.AXIS1);
        }
        private void buttonRight_MouseDown(object sender, MouseEventArgs e)
        {
            Controller.AxisSlew(AXISID.AXIS1, Speed);
        }
        private void buttonRight_MouseUp(object sender, MouseEventArgs e)
        {
            Controller.AxisStop(AXISID.AXIS1);
        }

        private void buttonTrigger_MouseDown(object sender, MouseEventArgs e)
        {
            Controller.SetSwitch(true);
        }
        private void buttonTrigger_MouseUp(object sender, MouseEventArgs e)
        {
            Controller.SetSwitch(false);
        }
    }
}
