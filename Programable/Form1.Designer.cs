namespace Programable
{
    partial class Form1
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

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.splitContainer1 = new System.Windows.Forms.SplitContainer();
            this.groupBox4 = new System.Windows.Forms.GroupBox();
            this.previewChartPanel1 = new Programable.PreviewChartPanel();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.devicePanel1 = new Programable.DevicePanel();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.mountControlPanel1 = new Programable.MountControlPanel();
            this.axisControlPanel1 = new Programable.AxisControlPanel();
            this.groupBox5 = new System.Windows.Forms.GroupBox();
            this.userControlScript1 = new Programable.ScriptPanel();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.mountStatusPanel1 = new Programable.MountStatusPanel();
            this.splitContainer1.Panel1.SuspendLayout();
            this.splitContainer1.Panel2.SuspendLayout();
            this.splitContainer1.SuspendLayout();
            this.groupBox4.SuspendLayout();
            this.groupBox3.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.groupBox5.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.SuspendLayout();
            // 
            // splitContainer1
            // 
            this.splitContainer1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainer1.Location = new System.Drawing.Point(0, 0);
            this.splitContainer1.Name = "splitContainer1";
            this.splitContainer1.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // splitContainer1.Panel1
            // 
            this.splitContainer1.Panel1.Controls.Add(this.groupBox4);
            this.splitContainer1.Panel1.Controls.Add(this.groupBox3);
            this.splitContainer1.Panel1.Controls.Add(this.groupBox1);
            // 
            // splitContainer1.Panel2
            // 
            this.splitContainer1.Panel2.Controls.Add(this.groupBox5);
            this.splitContainer1.Panel2.Controls.Add(this.groupBox2);
            this.splitContainer1.Size = new System.Drawing.Size(1177, 813);
            this.splitContainer1.SplitterDistance = 447;
            this.splitContainer1.TabIndex = 4;
            // 
            // groupBox4
            // 
            this.groupBox4.Controls.Add(this.previewChartPanel1);
            this.groupBox4.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox4.Location = new System.Drawing.Point(0, 88);
            this.groupBox4.Name = "groupBox4";
            this.groupBox4.Size = new System.Drawing.Size(796, 359);
            this.groupBox4.TabIndex = 8;
            this.groupBox4.TabStop = false;
            this.groupBox4.Text = "Preview";
            // 
            // previewChartPanel1
            // 
            this.previewChartPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.previewChartPanel1.Enabled = false;
            this.previewChartPanel1.Location = new System.Drawing.Point(3, 16);
            this.previewChartPanel1.Name = "previewChartPanel1";
            this.previewChartPanel1.Size = new System.Drawing.Size(790, 340);
            this.previewChartPanel1.TabIndex = 3;
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.devicePanel1);
            this.groupBox3.Dock = System.Windows.Forms.DockStyle.Top;
            this.groupBox3.Location = new System.Drawing.Point(0, 0);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(796, 88);
            this.groupBox3.TabIndex = 7;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "Connection";
            // 
            // devicePanel1
            // 
            this.devicePanel1.AutoSize = true;
            this.devicePanel1.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.devicePanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.devicePanel1.Location = new System.Drawing.Point(3, 16);
            this.devicePanel1.Name = "devicePanel1";
            this.devicePanel1.Size = new System.Drawing.Size(790, 69);
            this.devicePanel1.TabIndex = 6;
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.mountControlPanel1);
            this.groupBox1.Controls.Add(this.axisControlPanel1);
            this.groupBox1.Dock = System.Windows.Forms.DockStyle.Right;
            this.groupBox1.Location = new System.Drawing.Point(796, 0);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(381, 447);
            this.groupBox1.TabIndex = 5;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Mount Control";
            // 
            // mountControlPanel1
            // 
            this.mountControlPanel1.AutoSize = true;
            this.mountControlPanel1.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.mountControlPanel1.Enabled = false;
            this.mountControlPanel1.Location = new System.Drawing.Point(68, 42);
            this.mountControlPanel1.Name = "mountControlPanel1";
            this.mountControlPanel1.Size = new System.Drawing.Size(215, 223);
            this.mountControlPanel1.TabIndex = 2;
            // 
            // axisControlPanel1
            // 
            this.axisControlPanel1.AutoSize = true;
            this.axisControlPanel1.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.axisControlPanel1.AxisPos1 = 0D;
            this.axisControlPanel1.AxisPos2 = 0D;
            this.axisControlPanel1.Enabled = false;
            this.axisControlPanel1.Location = new System.Drawing.Point(68, 271);
            this.axisControlPanel1.Name = "axisControlPanel1";
            this.axisControlPanel1.Size = new System.Drawing.Size(216, 70);
            this.axisControlPanel1.TabIndex = 4;
            // 
            // groupBox5
            // 
            this.groupBox5.Controls.Add(this.userControlScript1);
            this.groupBox5.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox5.Location = new System.Drawing.Point(0, 0);
            this.groupBox5.Name = "groupBox5";
            this.groupBox5.Size = new System.Drawing.Size(796, 362);
            this.groupBox5.TabIndex = 4;
            this.groupBox5.TabStop = false;
            this.groupBox5.Text = "Script";
            // 
            // userControlScript1
            // 
            this.userControlScript1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.userControlScript1.Font = new System.Drawing.Font("Lucida Sans", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.userControlScript1.Location = new System.Drawing.Point(3, 16);
            this.userControlScript1.Name = "userControlScript1";
            this.userControlScript1.Size = new System.Drawing.Size(790, 343);
            this.userControlScript1.TabIndex = 1;
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.mountStatusPanel1);
            this.groupBox2.Dock = System.Windows.Forms.DockStyle.Right;
            this.groupBox2.Location = new System.Drawing.Point(796, 0);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(381, 362);
            this.groupBox2.TabIndex = 3;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Mount Status";
            // 
            // mountStatusPanel1
            // 
            this.mountStatusPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.mountStatusPanel1.Location = new System.Drawing.Point(3, 16);
            this.mountStatusPanel1.Name = "mountStatusPanel1";
            this.mountStatusPanel1.Size = new System.Drawing.Size(375, 343);
            this.mountStatusPanel1.TabIndex = 2;
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1177, 813);
            this.Controls.Add(this.splitContainer1);
            this.Name = "Form1";
            this.Text = "Form1";
            this.splitContainer1.Panel1.ResumeLayout(false);
            this.splitContainer1.Panel2.ResumeLayout(false);
            this.splitContainer1.ResumeLayout(false);
            this.groupBox4.ResumeLayout(false);
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.groupBox5.ResumeLayout(false);
            this.groupBox2.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private ScriptPanel userControlScript1;
        private Programable.MountControlPanel mountControlPanel1;
        private PreviewChartPanel previewChartPanel1;
        private System.Windows.Forms.SplitContainer splitContainer1;
        private AxisControlPanel axisControlPanel1;
        private MountStatusPanel mountStatusPanel1;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.GroupBox groupBox2;
        private DevicePanel devicePanel1;
        private System.Windows.Forms.GroupBox groupBox4;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.GroupBox groupBox5;
    }
}

