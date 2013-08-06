using System;
using System.Windows.Forms;
using BasicApi;

namespace ApiTest
{
    public partial class Form1 : Form
    {
        Mount mount;
        public Form1()
        {
            /// Cannot Parse BCDString
            /// Didn't get response string
            InitializeComponent();

            string[] lPorts = System.IO.Ports.SerialPort.GetPortNames();

            foreach (var port in lPorts)
            {
                string portname = port;
                /// handel
                /// https://connect.microsoft.com/VisualStudio/feedback/details/236183/system-io-ports-serialport-getportnames-error-with-bluetooth
                if (port.Length > 4 && port[3] != '1')
                    portname = port.Substring(0, 4);
                comboBoxPortSelect.Items.Add(portname);
            }


        }

        private void buttonDirection_MouseDown(object sender, MouseEventArgs e)
        {
            
            var obj = sender as Button;

            if (obj == buttonUp)
                mount.MCAxisSlew(AXISID.AXIS2, -10);
            else if (obj == buttonDown)
                mount.MCAxisSlew(AXISID.AXIS2, 10);
            else if (obj == buttonLeft)
                mount.MCAxisSlew(AXISID.AXIS1, -10);
            else if (obj == buttonRight)
                mount.MCAxisSlew(AXISID.AXIS1, 10);

        }

        private void buttonDirrect_MouseUp(object sender, MouseEventArgs e)
        {
            var obj = sender as Button;

            if (obj == buttonUp)
                mount.MCAxisStop(AXISID.AXIS2);
            else if (obj == buttonDown)
                mount.MCAxisStop(AXISID.AXIS2);
            else if (obj == buttonLeft)
                mount.MCAxisStop(AXISID.AXIS1);
            else if (obj == buttonRight)
                mount.MCAxisStop(AXISID.AXIS1);

        }
        private double RAD1 = Math.PI / 180.0;
        private void buttonSet_Click(object sender, EventArgs e)
        {
            double axis1 = Convert.ToInt32(numericUpDownSetAxis1.Value);
            double axis2 = Convert.ToInt32(numericUpDownSetAxis2.Value);

            mount.MCSetAxisPosition(AXISID.AXIS1, axis1 * RAD1);
            mount.MCSetAxisPosition(AXISID.AXIS2, axis2 * RAD1);
        }

        private void buttonGoto_Click(object sender, EventArgs e)
        {
            double axis1 = Convert.ToInt32(numericUpDownGotoAxis1.Value);
            double axis2 = Convert.ToInt32(numericUpDownGotoAxis2.Value);

            mount.MCAxisSlewTo(AXISID.AXIS1, axis1 * RAD1);
            mount.MCAxisSlewTo(AXISID.AXIS2, axis2 * RAD1);
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            var Axis1Status = mount.MCGetAxisStatus(AXISID.AXIS1);
            var Axis2Status = mount.MCGetAxisStatus(AXISID.AXIS2);
            var axis1position = mount.MCGetAxisPosition(AXISID.AXIS1) / RAD1;
            var axis2position = mount.MCGetAxisPosition(AXISID.AXIS2) / RAD1;

            string[] builder = new string[9];
            builder[0] = (string.Format("{0,10}{1,10:F}{2,10:F}", "Position", axis1position, axis2position));
            builder[1] = "";
            builder[2] = (string.Format("{0,10}{1,10:F}{2,10:F}", "PositionC", mount.Positions[0] / RAD1, mount.Positions[1] / RAD1));
            builder[3] = (string.Format("{0,10}{1,10:F}{2,10:F}", "TPositionC", mount.TargetPositions[0] / RAD1, mount.TargetPositions[1] / RAD1));
            builder[4] = (string.Format("{0,10}{1,10:F}{2,10:F}", "Speed", mount.SlewingSpeed[0] / RAD1, mount.SlewingSpeed[1] / RAD1));
            builder[5] = "";
            builder[6] = (string.Format("{0,-15}{1,-15}{2,-15}{3,-15}{4,-15}{5,-15}", "FullStop", "SlewingTo", "Slewing", "Forward", "HighSpeed", "NotInit"));
            builder[7] = (string.Format("{0,-15}{1,-15}{2,-15}{3,-15}{4,-15}{5,-15}", Axis1Status.FullStop, Axis1Status.SlewingTo, Axis1Status.Slewing, Axis1Status.SlewingForward, Axis1Status.HighSpeed, Axis1Status.NotInitialized));
            builder[8] = (string.Format("{0,-15}{1,-15}{2,-15}{3,-15}{4,-15}{5,-15}", Axis2Status.FullStop, Axis2Status.SlewingTo, Axis2Status.Slewing, Axis2Status.SlewingForward, Axis2Status.HighSpeed, Axis2Status.NotInitialized));

            textBoxOutput.Lines = builder;
        }

        bool TriggerOnOff = false;
        private void buttonTrigger_Click(object sender, EventArgs e)
        {
            TriggerOnOff = !TriggerOnOff;
            mount.MCSetSwitch(TriggerOnOff);
            buttonTrigger.Text = TriggerOnOff ? "Trigger On" : "Trigger Off";
        }

        private void buttonConnect_Click(object sender, EventArgs e)
        {
            try
            {             
                mount = new Mount_Skywatcher();

                var PortNumber = comboBoxPortSelect.SelectedItem.ToString().Replace("COM", "");
                int COM = Convert.ToInt16(PortNumber);

                mount.Connect_COM(COM);                

                mount.MCOpenTelescopeConnection();

                groupBox1.Enabled = groupBox2.Enabled = groupBox3.Enabled = true;
                timer1.Start();
            }
            catch (MountControlException exception)
            {
                textBoxStatus.Text = exception.Message;
            }
            catch(Exception exception)
            {
                textBoxStatus.Text = exception.Message;
            }
        }

    }
}
