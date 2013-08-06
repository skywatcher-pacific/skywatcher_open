using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.IO;

namespace Programable
{
    public partial class ScriptEditor : Form
    {
        public ScriptEditor()
        {
            InitializeComponent();
        }

        private void toolStripButtonOpen_Click(object sender, EventArgs e)
        {
            openFileDialog1.ShowDialog();
            using (StreamReader reader = new StreamReader(openFileDialog1.FileName))
            {
                textBoxCode.Text = reader.ReadToEnd();
            }
        }

        private void toolStripButtonSave_Click(object sender, EventArgs e)
        {
            saveFileDialog1.ShowDialog();
            using (StreamWriter writer = new StreamWriter(saveFileDialog1.FileName))
            {
                writer.Write(textBoxCode.Text);
            }
        }

        private void toolStripButtonRun_Click(object sender, EventArgs e)
        {
            if (mScript != null)
                mScript.SendCommand(textBoxCode.Text);
        }

        internal void SetScript(Script script1)
        {
            mScript = script1;
        }

        public Script mScript { get; set; }
    }
}
