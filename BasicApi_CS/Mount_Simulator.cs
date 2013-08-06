using System;
using System.Collections.Generic;
using System.Text;

namespace BasicApi
{
    public class Mount_Simulator: Mount
    {
        protected override void SendRequest(AXISID Axis, char Command, string cmdDataStr)
        {
            throw new NotImplementedException();
        }

        protected override string RecieveResponse()
        {
            throw new NotImplementedException();
        }

        public override void MCInit()
        {
         //   throw new NotImplementedException();
        }

        public override void MCAxisSlew(AXISID Axis, double Speed)
        {
            //throw new NotImplementedException();
        }

        double[] Pos = new double[] { 0, 0 };
        public override void MCAxisSlewTo(AXISID Axis, double TargetPosition)
        {
            Pos[(int)Axis] = TargetPosition;
        }

        public override void MCAxisStop(AXISID Axis)
        {
            //throw new NotImplementedException();
        }

        public override void MCSetAxisPosition(AXISID Axis, double NewValue)
        {
            Pos = new double[] { 0, 0 };
            //throw new NotImplementedException();
        }

        public override double MCGetAxisPosition(AXISID Axis)
        {
            return Pos[(int)Axis];
        }

        public override AXISSTATUS MCGetAxisStatus(AXISID Axis)
        {
            return new AXISSTATUS { FullStop = true };
        }

        public override void MCSetSwitch(bool OnOff)
        {
            //throw new NotImplementedException();
        }
    }
}
