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
using System.ComponentModel;
using System.IO;
using System.Windows.Forms;
using BasicApi;
using IronPython.Runtime.Types;
using Microsoft.Scripting.Hosting;

namespace Programable
{
    public partial class ScriptPanel : UserControl
    {
        //private ScriptEngine engine;
        //private ScriptScope scope;
        
        public ScriptPanel()
        {
            InitializeComponent();

            //engine = ironTextBoxControl1.engine;
            //scope = ironTextBoxControl1.scope;

            //scope.SetVariable("Axis1", AXISID.AXIS1);
            //scope.SetVariable("Axis2", AXISID.AXIS2);
            //scope.SetVariable( "Controller", DynamicHelpers.GetPythonTypeFromType(typeof(Controller)));            

            Controller.ControllerEnabledUpdated += new EventHandler(Controller_ControllerEnabledUpdated);
        }

        void Controller_ControllerEnabledUpdated(object sender, EventArgs e)
        {
            this.Enabled = Controller.IsIdle;
        }
       
        private void backgroundWorker1_DoWork(object sender, DoWorkEventArgs e)
        {
            //CompiledCode code = e.Argument as CompiledCode;
            //code.Execute(ironTextBoxControl1.scope);
        }
        private void backgroundWorker1_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {

        }
        private void backgroundWorker1_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            //this.Enabled = true;
        }

        private void toolStripButtonEditor_Click(object sender, EventArgs e)
        {
            ScriptEditor editor = new ScriptEditor();
            editor.SetScript(script1);
            editor.Show();
        }            
    }
}
