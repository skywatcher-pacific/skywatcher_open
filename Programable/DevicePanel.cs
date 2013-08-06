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
    public partial class DevicePanel : UserControl
    {
        public DevicePanel()
        {
            InitializeComponent();

            string[] lPorts = System.IO.Ports.SerialPort.GetPortNames();

            foreach (var port in lPorts)
            {
                string portname = port;

                if (port.Length > 4 && port[3] != '1')
                    portname = port.Substring(0, 4);
                comboBoxPortSelect.Items.Add(portname);
            }
        }

        private void buttonConnect_Click(object sender, EventArgs e)
        {
            if (comboBoxPortSelect.SelectedItem != null)
            {
                var PortNumber = comboBoxPortSelect.SelectedItem.ToString().Replace("COM", "");
                int COM = Convert.ToInt16(PortNumber);

                try
                {
                    if (comboBoxMountSelect.SelectedItem.ToString() == "Skywatcher")
                        Controller.Init(0, COM);
                    //else if (comboBoxMountSelect.SelectedItem.ToString() == "Celestron")
                    //    Controller.Init(1, COM);
                }
                catch (MountControlException ex)
                {
                    textBoxStatus.Text = ex.Message;
                }
                catch (Exception ex)
                {
                    textBoxStatus.Text = ex.Message;
                }
            }
        }

        
    }
}
