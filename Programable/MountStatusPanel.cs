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
    public partial class MountStatusPanel : UserControl
    {

        public MountStatusPanel()
        {
            InitializeComponent();

            Controller.StatusUpdated += new EventHandler(Controller_StatusUpdated);
        }

        void Controller_StatusUpdated(object sender, EventArgs e)
        {
            var RAD1 = BasicMath.RAD1;
            var Axis1Status = Controller.GetAxisStatus(AXISID.AXIS1);
            var Axis2Status = Controller.GetAxisStatus(AXISID.AXIS2);
            var axis1position = Controller.GetAxisPosition(AXISID.AXIS1);
            var axis2position = Controller.GetAxisPosition(AXISID.AXIS2);

            var mMount = Controller.GetMount();

            string[] builder = new string[10];
            builder[0] = (string.Format("{0,10}{1,10}{2,10}", "", "Axis1", "Axis2"));
            builder[1] = (string.Format("{0,10}{1,10:F}{2,10:F}", "Position", axis1position, axis2position));
            builder[2] = "";
            builder[3] = (string.Format("{0,10}{1,10:F}{2,10:F}", "PositionC", mMount.Positions[0] / RAD1, mMount.Positions[1] / RAD1));
            builder[4] = (string.Format("{0,10}{1,10:F}{2,10:F}", "TPositionC", mMount.TargetPositions[0] / RAD1, mMount.TargetPositions[1] / RAD1));
            builder[5] = (string.Format("{0,10}{1,10:F}{2,10:F}", "Speed", mMount.SlewingSpeed[0], mMount.SlewingSpeed[1]));
            builder[6] = "";
            builder[7] = (string.Format("{0,-10}{1,-10}{2,-10}{3,-10}{4,-10}{5,-10}", "FullStop", "SlewingTo", "Slewing", "Forward", "HighSpeed", "NotInit"));
            builder[8] = (string.Format("{0,-10}{1,-10}{2,-10}{3,-10}{4,-10}{5,-10}", Axis1Status.FullStop, Axis1Status.SlewingTo, Axis1Status.Slewing, Axis1Status.SlewingForward, Axis1Status.HighSpeed, Axis1Status.NotInitialized));
            builder[9] = (string.Format("{0,-10}{1,-10}{2,-10}{3,-10}{4,-10}{5,-10}", Axis2Status.FullStop, Axis2Status.SlewingTo, Axis2Status.Slewing, Axis2Status.SlewingForward, Axis2Status.HighSpeed, Axis2Status.NotInitialized));

            textBoxStatus.Lines = builder;
        }
    }
}
