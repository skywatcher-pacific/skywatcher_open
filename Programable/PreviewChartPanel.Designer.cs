namespace Programable
{
    partial class PreviewChartPanel
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(PreviewChartPanel));
            this.pictureBoxPreview = new System.Windows.Forms.PictureBox();
            this.statusStripChart = new System.Windows.Forms.StatusStrip();
            this.toolStripStatusLabelMouseOver = new System.Windows.Forms.ToolStripStatusLabel();
            this.listBoxMarkers = new System.Windows.Forms.ListBox();
            this.backgroundWorker1 = new System.ComponentModel.BackgroundWorker();
            this.toolStrip1 = new System.Windows.Forms.ToolStrip();
            this.toolStripButtonDelete = new System.Windows.Forms.ToolStripButton();
            this.toolStripButtonUp = new System.Windows.Forms.ToolStripButton();
            this.toolStripButtonDown = new System.Windows.Forms.ToolStripButton();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxPreview)).BeginInit();
            this.statusStripChart.SuspendLayout();
            this.toolStrip1.SuspendLayout();
            this.SuspendLayout();
            // 
            // pictureBoxPreview
            // 
            this.pictureBoxPreview.Dock = System.Windows.Forms.DockStyle.Fill;
            this.pictureBoxPreview.Location = new System.Drawing.Point(0, 25);
            this.pictureBoxPreview.Name = "pictureBoxPreview";
            this.pictureBoxPreview.Size = new System.Drawing.Size(426, 340);
            this.pictureBoxPreview.SizeMode = System.Windows.Forms.PictureBoxSizeMode.AutoSize;
            this.pictureBoxPreview.TabIndex = 0;
            this.pictureBoxPreview.TabStop = false;
            this.pictureBoxPreview.MouseClick += new System.Windows.Forms.MouseEventHandler(this.pictureBoxPreview_MouseClick);
            this.pictureBoxPreview.MouseDoubleClick += new System.Windows.Forms.MouseEventHandler(this.pictureBoxPreview_MouseDoubleClick);
            this.pictureBoxPreview.MouseMove += new System.Windows.Forms.MouseEventHandler(this.pictureBoxPreview_MouseMove);
            this.pictureBoxPreview.Resize += new System.EventHandler(this.pictureBoxPreview_Resize);
            // 
            // statusStripChart
            // 
            this.statusStripChart.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripStatusLabelMouseOver});
            this.statusStripChart.Location = new System.Drawing.Point(0, 365);
            this.statusStripChart.Name = "statusStripChart";
            this.statusStripChart.Size = new System.Drawing.Size(593, 22);
            this.statusStripChart.TabIndex = 1;
            this.statusStripChart.Text = "statusStrip1";
            // 
            // toolStripStatusLabelMouseOver
            // 
            this.toolStripStatusLabelMouseOver.Name = "toolStripStatusLabelMouseOver";
            this.toolStripStatusLabelMouseOver.Size = new System.Drawing.Size(0, 17);
            // 
            // listBoxMarkers
            // 
            this.listBoxMarkers.AllowDrop = true;
            this.listBoxMarkers.Dock = System.Windows.Forms.DockStyle.Right;
            this.listBoxMarkers.FormattingEnabled = true;
            this.listBoxMarkers.Location = new System.Drawing.Point(426, 25);
            this.listBoxMarkers.Name = "listBoxMarkers";
            this.listBoxMarkers.Size = new System.Drawing.Size(167, 340);
            this.listBoxMarkers.TabIndex = 0;
            this.listBoxMarkers.SelectedValueChanged += new System.EventHandler(this.listBoxMarkers_SelectedValueChanged);
            this.listBoxMarkers.DoubleClick += new System.EventHandler(this.listBoxMarkers_DoubleClick);
            // 
            // toolStrip1
            // 
            this.toolStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripButtonDelete,
            this.toolStripButtonUp,
            this.toolStripButtonDown});
            this.toolStrip1.Location = new System.Drawing.Point(0, 0);
            this.toolStrip1.Name = "toolStrip1";
            this.toolStrip1.Size = new System.Drawing.Size(593, 25);
            this.toolStrip1.TabIndex = 2;
            this.toolStrip1.Text = "toolStrip1";
            // 
            // toolStripButtonDelete
            // 
            this.toolStripButtonDelete.Image = ((System.Drawing.Image)(resources.GetObject("toolStripButtonDelete.Image")));
            this.toolStripButtonDelete.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.toolStripButtonDelete.Name = "toolStripButtonDelete";
            this.toolStripButtonDelete.Size = new System.Drawing.Size(60, 22);
            this.toolStripButtonDelete.Text = "Delete";
            this.toolStripButtonDelete.Click += new System.EventHandler(this.toolStripButtonDelete_Click);
            // 
            // toolStripButtonUp
            // 
            this.toolStripButtonUp.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.toolStripButtonUp.Image = ((System.Drawing.Image)(resources.GetObject("toolStripButtonUp.Image")));
            this.toolStripButtonUp.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.toolStripButtonUp.Name = "toolStripButtonUp";
            this.toolStripButtonUp.Size = new System.Drawing.Size(23, 22);
            this.toolStripButtonUp.Text = "toolStripButton2";
            this.toolStripButtonUp.Click += new System.EventHandler(this.toolStripButtonUp_Click);
            // 
            // toolStripButtonDown
            // 
            this.toolStripButtonDown.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.toolStripButtonDown.Image = ((System.Drawing.Image)(resources.GetObject("toolStripButtonDown.Image")));
            this.toolStripButtonDown.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.toolStripButtonDown.Name = "toolStripButtonDown";
            this.toolStripButtonDown.Size = new System.Drawing.Size(23, 22);
            this.toolStripButtonDown.Text = "toolStripButton3";
            this.toolStripButtonDown.Click += new System.EventHandler(this.toolStripButtonDown_Click);
            // 
            // PreviewChartPanel
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.pictureBoxPreview);
            this.Controls.Add(this.listBoxMarkers);
            this.Controls.Add(this.toolStrip1);
            this.Controls.Add(this.statusStripChart);
            this.Enabled = false;
            this.Name = "PreviewChartPanel";
            this.Size = new System.Drawing.Size(593, 387);
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxPreview)).EndInit();
            this.statusStripChart.ResumeLayout(false);
            this.statusStripChart.PerformLayout();
            this.toolStrip1.ResumeLayout(false);
            this.toolStrip1.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox pictureBoxPreview;
        private System.Windows.Forms.StatusStrip statusStripChart;
        private System.Windows.Forms.ToolStripStatusLabel toolStripStatusLabelMouseOver;
        private System.Windows.Forms.ListBox listBoxMarkers;
        private System.ComponentModel.BackgroundWorker backgroundWorker1;
        private System.Windows.Forms.ToolStrip toolStrip1;
        private System.Windows.Forms.ToolStripButton toolStripButtonDelete;
        private System.Windows.Forms.ToolStripButton toolStripButtonUp;
        private System.Windows.Forms.ToolStripButton toolStripButtonDown;
    }
}
