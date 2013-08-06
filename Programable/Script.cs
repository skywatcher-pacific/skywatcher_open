using System;
using System.IO;
using System.Windows.Forms;
using System.Threading;
using Microsoft.Scripting.Hosting;
using IronPython.Hosting;
using BasicApi;
using IronPython.Runtime.Types;
using System.Collections.Generic;
using System.Text;

namespace Programable
{
    /// <summary>
    /// A new IronPython Textbox, aim to place the IronPython enviornment in Background    
    /// </summary>
    public partial class Script : UserControl
    {
        private class ProxyStream : Stream
        {
            private MemoryStream stream = null;
            private int p;

            public ProxyStream(int p)
            {
                // TODO: Complete member initialization
                this.p = p;
                stream = new MemoryStream(p);
            }
            public override bool CanRead
            {
                get { return stream.CanRead; }
            }

            public override bool CanSeek
            {
                get { return stream.CanSeek; }
            }

            public override bool CanWrite
            {
                get { return stream.CanWrite; }
            }

            public void clean()
            {
                stream = new MemoryStream(p);
            }

            public override void Flush()
            {
                //stream = new MemoryStream(p);
            }

            public override long Length
            {
                get { return stream.Length; }
            }

            public override long Position
            {
                get
                {
                    return stream.Position;
                }
                set
                {
                    stream.Position = value;
                }
            }

            public override int Read(byte[] buffer, int offset, int count)
            {
                return stream.Read(buffer, offset, count);
            }

            public override long Seek(long offset, SeekOrigin origin)
            {
                return stream.Seek(offset, origin);
            }

            public override void SetLength(long value)
            {
                stream.SetLength(value);
            }

            public override void Write(byte[] buffer, int offset, int count)
            {
                stream.Write(buffer, offset, count);
            }
        }

        private String mShellCommands = null;
        const int mShellLines = 180;
        private bool Locked = false;

        public Script()
        {
            InitializeComponent();

            backgroundWorker1.RunWorkerAsync();
        }

        private void backgroundWorker1_DoWork(object sender, System.ComponentModel.DoWorkEventArgs e)
        {
            // Init the python engine

            ProxyStream InputStream = new ProxyStream(1000);
            ProxyStream OutputStream = new ProxyStream(1000);
            ProxyStream ErrorStream = new ProxyStream(1000);

            ScriptEngine engine = Python.CreateEngine();
            ScriptScope scope = engine.CreateScope();

            StreamReader OutputReader = new StreamReader(OutputStream);
            StreamWriter InputWriter = new StreamWriter(InputStream);

            engine.Runtime.IO.SetInput(InputStream, Encoding.ASCII);
            engine.Runtime.IO.SetOutput(OutputStream, Encoding.ASCII);
            engine.Runtime.IO.SetErrorOutput(ErrorStream, Encoding.ASCII);

            scope.SetVariable("Axis1", AXISID.AXIS1);
            scope.SetVariable("Axis2", AXISID.AXIS2);
            scope.SetVariable("Controller", DynamicHelpers.GetPythonTypeFromType(typeof(Controller)));

            while (true)
            {
                if (Locked)
                {
                    OutputStream.clean();
                    try
                    {
                        backgroundWorker1.ReportProgress(2, mShellCommands);
                        var code = engine.CreateScriptSourceFromString(mShellCommands);
                        code.Execute(scope);
                    }
                    catch (Exception ee)
                    {
                        backgroundWorker1.ReportProgress(1, ee.ToString());
                        //textBox2.AppendText(string.Format("{0}{1}", Environment.NewLine, ee));
                    }


                    Locked = false;

                    OutputStream.Position = 0;
                    backgroundWorker1.ReportProgress(0, OutputReader.ReadToEnd());
                }
            }
        }
        private void backgroundWorker1_ProgressChanged(object sender, System.ComponentModel.ProgressChangedEventArgs e)
        {
            switch (e.ProgressPercentage)
            {
                /// Outuput
                case 0:
                    textBoxOutput.AppendText(Environment.NewLine + e.UserState as String);
                    break;
                /// Exception
                case 1:
                    textBoxOutput.AppendText(Environment.NewLine + e.UserState as String);
                    break;
                /// Input
                case 2:
                    textBoxOutput.AppendText(Environment.NewLine + ">>> " + e.UserState as String);
                    break;
            }


        }
        private void backgroundWorker1_RunWorkerCompleted(object sender, System.ComponentModel.RunWorkerCompletedEventArgs e)
        {

        }

        void Controller_ControllerEnabledUpdated(object sender, EventArgs e)
        {
            this.Enabled = Controller.IsIdle;
        }

        private void textBox1_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (Locked) return;

            /// Enter
            if (e.KeyChar == (char)13)
            {
                if (textBoxInput.Lines.Length > 0)
                {
                    var last_line = textBoxInput.Lines[textBoxInput.Lines.Length - 1];
                    last_line = last_line.TrimEnd();
                    var ind = (last_line.Length - last_line.TrimStart().Length) / 4;

                    /// Add a new line
                    if (last_line.Length > 0 && last_line[last_line.Length - 1] == ':')
                    {
                        textBoxInput.SelectedText = Environment.NewLine + new string('\t', ind + 1).Replace("\t", "    ");
                    }
                    else if (ind > 0)
                    {
                        textBoxInput.SelectedText = Environment.NewLine + new string('\t', ind).Replace("\t", "    ");
                    }
                    else
                    {
                        // Execute                        
                        SendCommand( textBoxInput.Text);
                        
                        textBoxInput.Text = "";
                        
                    }
                }
                else
                {
                    // Execute
                    SendCommand(textBoxInput.Text);                    
                    textBoxInput.Text = "";
                    
                }
            }
            /// Backspace
            else if (e.KeyChar == (char)8)
            {
                if (textBoxInput.SelectedText.Length > 0)
                    textBoxInput.SelectedText = "";
                else if (textBoxInput.SelectionStart > 0)
                {
                    if (textBoxInput.SelectionStart >= 4 && textBoxInput.Text.Substring(textBoxInput.SelectionStart - 4, 4) == "    ")
                    {
                        textBoxInput.SelectionStart -= 4;
                        textBoxInput.SelectionLength = 4;
                        textBoxInput.SelectedText = "";
                    }
                    else
                    {
                        textBoxInput.SelectionStart--;
                        textBoxInput.SelectionLength = 1;
                        textBoxInput.SelectedText = "";
                    }
                    
                }
            }
            /// Tab key
            else if (e.KeyChar == (char)9)
            {
                textBoxInput.SelectedText = "    ";
            }
            /// Other ascii 
            else if (e.KeyChar > 31 && e.KeyChar < 127)
            {
                textBoxInput.SelectedText = new string((char)e.KeyChar, 1);
            }
        }
        public void SendCommand(string cmd)
        {
            mShellCommands = cmd;            
            Locked = true;            
        }
    }
}
;