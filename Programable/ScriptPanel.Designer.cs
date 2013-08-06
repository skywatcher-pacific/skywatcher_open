namespace Programable
{
    partial class ScriptPanel
    {
        /// <summary> 
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary> 
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ScriptPanel));
            this.openFileDialog1 = new System.Windows.Forms.OpenFileDialog();
            this.backgroundWorker1 = new System.ComponentModel.BackgroundWorker();
            this.script1 = new Programable.Script();
            this.toolStripOpen = new System.Windows.Forms.ToolStrip();
            this.toolStripButtonEditor = new System.Windows.Forms.ToolStripButton();
            this.toolStripOpen.SuspendLayout();
            this.SuspendLayout();
            // 
            // openFileDialog1
            // 
            this.openFileDialog1.FileName = "openFileDialog1";
            // 
            // backgroundWorker1
            // 
            this.backgroundWorker1.WorkerReportsProgress = true;
            this.backgroundWorker1.WorkerSupportsCancellation = true;
            this.backgroundWorker1.DoWork += new System.ComponentModel.DoWorkEventHandler(this.backgroundWorker1_DoWork);
            this.backgroundWorker1.ProgressChanged += new System.ComponentModel.ProgressChangedEventHandler(this.backgroundWorker1_ProgressChanged);
            this.backgroundWorker1.RunWorkerCompleted += new System.ComponentModel.RunWorkerCompletedEventHandler(this.backgroundWorker1_RunWorkerCompleted);
            // 
            // script1
            // 
            this.script1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.script1.Location = new System.Drawing.Point(0, 25);
            this.script1.Name = "script1";
            this.script1.Size = new System.Drawing.Size(479, 356);
            this.script1.TabIndex = 1;
            // 
            // toolStripOpen
            // 
            this.toolStripOpen.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripButtonEditor});
            this.toolStripOpen.Location = new System.Drawing.Point(0, 0);
            this.toolStripOpen.Name = "toolStripOpen";
            this.toolStripOpen.Size = new System.Drawing.Size(479, 25);
            this.toolStripOpen.TabIndex = 0;
            this.toolStripOpen.Text = "toolStrip1";
            // 
            // toolStripButtonEditor
            // 
            this.toolStripButtonEditor.Image = ((System.Drawing.Image)(resources.GetObject("toolStripButtonEditor.Image")));
            this.toolStripButtonEditor.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.toolStripButtonEditor.Name = "toolStripButtonEditor";
            this.toolStripButtonEditor.Size = new System.Drawing.Size(58, 22);
            this.toolStripButtonEditor.Text = "Editor";
            this.toolStripButtonEditor.Click += new System.EventHandler(this.toolStripButtonEditor_Click);
            // 
            // ScriptPanel
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.script1);
            this.Controls.Add(this.toolStripOpen);
            this.Enabled = false;
            this.Name = "ScriptPanel";
            this.Size = new System.Drawing.Size(479, 381);
            this.toolStripOpen.ResumeLayout(false);
            this.toolStripOpen.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.OpenFileDialog openFileDialog1;
        private System.ComponentModel.BackgroundWorker backgroundWorker1;
        private Script script1;
        private System.Windows.Forms.ToolStrip toolStripOpen;
        private System.Windows.Forms.ToolStripButton toolStripButtonEditor;
    }
}
