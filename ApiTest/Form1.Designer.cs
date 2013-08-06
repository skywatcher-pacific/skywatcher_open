namespace ApiTest
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
            this.components = new System.ComponentModel.Container();
            this.buttonUp = new System.Windows.Forms.Button();
            this.buttonDown = new System.Windows.Forms.Button();
            this.buttonRight = new System.Windows.Forms.Button();
            this.buttonLeft = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.label2 = new System.Windows.Forms.Label();
            this.label1 = new System.Windows.Forms.Label();
            this.numericUpDownSetAxis2 = new System.Windows.Forms.NumericUpDown();
            this.numericUpDownSetAxis1 = new System.Windows.Forms.NumericUpDown();
            this.buttonSet = new System.Windows.Forms.Button();
            this.textBoxOutput = new System.Windows.Forms.TextBox();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.buttonTrigger = new System.Windows.Forms.Button();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.numericUpDownGotoAxis2 = new System.Windows.Forms.NumericUpDown();
            this.numericUpDownGotoAxis1 = new System.Windows.Forms.NumericUpDown();
            this.buttonGoto = new System.Windows.Forms.Button();
            this.comboBoxMountSelect = new System.Windows.Forms.ComboBox();
            this.Connection = new System.Windows.Forms.GroupBox();
            this.textBoxStatus = new System.Windows.Forms.TextBox();
            this.label7 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.comboBoxPortSelect = new System.Windows.Forms.ComboBox();
            this.buttonConnect = new System.Windows.Forms.Button();
            this.groupBox1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownSetAxis2)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownSetAxis1)).BeginInit();
            this.groupBox2.SuspendLayout();
            this.groupBox3.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownGotoAxis2)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownGotoAxis1)).BeginInit();
            this.Connection.SuspendLayout();
            this.SuspendLayout();
            // 
            // buttonUp
            // 
            this.buttonUp.Location = new System.Drawing.Point(95, 14);
            this.buttonUp.Name = "buttonUp";
            this.buttonUp.Size = new System.Drawing.Size(75, 23);
            this.buttonUp.TabIndex = 0;
            this.buttonUp.Text = "Axis2 -";
            this.buttonUp.UseVisualStyleBackColor = true;
            this.buttonUp.MouseDown += new System.Windows.Forms.MouseEventHandler(this.buttonDirection_MouseDown);
            this.buttonUp.MouseUp += new System.Windows.Forms.MouseEventHandler(this.buttonDirrect_MouseUp);
            // 
            // buttonDown
            // 
            this.buttonDown.Location = new System.Drawing.Point(95, 108);
            this.buttonDown.Name = "buttonDown";
            this.buttonDown.Size = new System.Drawing.Size(75, 23);
            this.buttonDown.TabIndex = 1;
            this.buttonDown.Text = "Axis2 +";
            this.buttonDown.UseVisualStyleBackColor = true;
            this.buttonDown.MouseDown += new System.Windows.Forms.MouseEventHandler(this.buttonDirection_MouseDown);
            this.buttonDown.MouseUp += new System.Windows.Forms.MouseEventHandler(this.buttonDirrect_MouseUp);
            // 
            // buttonRight
            // 
            this.buttonRight.Location = new System.Drawing.Point(176, 61);
            this.buttonRight.Name = "buttonRight";
            this.buttonRight.Size = new System.Drawing.Size(75, 23);
            this.buttonRight.TabIndex = 2;
            this.buttonRight.Text = "Axis1 +";
            this.buttonRight.UseVisualStyleBackColor = true;
            this.buttonRight.MouseDown += new System.Windows.Forms.MouseEventHandler(this.buttonDirection_MouseDown);
            this.buttonRight.MouseUp += new System.Windows.Forms.MouseEventHandler(this.buttonDirrect_MouseUp);
            // 
            // buttonLeft
            // 
            this.buttonLeft.Location = new System.Drawing.Point(14, 61);
            this.buttonLeft.Name = "buttonLeft";
            this.buttonLeft.Size = new System.Drawing.Size(75, 23);
            this.buttonLeft.TabIndex = 3;
            this.buttonLeft.Text = "Axis1 -";
            this.buttonLeft.UseVisualStyleBackColor = true;
            this.buttonLeft.MouseDown += new System.Windows.Forms.MouseEventHandler(this.buttonDirection_MouseDown);
            this.buttonLeft.MouseUp += new System.Windows.Forms.MouseEventHandler(this.buttonDirrect_MouseUp);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.label2);
            this.groupBox1.Controls.Add(this.label1);
            this.groupBox1.Controls.Add(this.numericUpDownSetAxis2);
            this.groupBox1.Controls.Add(this.numericUpDownSetAxis1);
            this.groupBox1.Controls.Add(this.buttonSet);
            this.groupBox1.Enabled = false;
            this.groupBox1.Location = new System.Drawing.Point(288, 84);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(203, 72);
            this.groupBox1.TabIndex = 4;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "SetAxisPosition";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(7, 51);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(32, 13);
            this.label2.TabIndex = 8;
            this.label2.Text = "Axis2";
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(7, 18);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(32, 13);
            this.label1.TabIndex = 7;
            this.label1.Text = "Axis1";
            // 
            // numericUpDownSetAxis2
            // 
            this.numericUpDownSetAxis2.Location = new System.Drawing.Point(45, 47);
            this.numericUpDownSetAxis2.Maximum = new decimal(new int[] {
            360,
            0,
            0,
            0});
            this.numericUpDownSetAxis2.Name = "numericUpDownSetAxis2";
            this.numericUpDownSetAxis2.Size = new System.Drawing.Size(65, 20);
            this.numericUpDownSetAxis2.TabIndex = 6;
            // 
            // numericUpDownSetAxis1
            // 
            this.numericUpDownSetAxis1.Location = new System.Drawing.Point(45, 14);
            this.numericUpDownSetAxis1.Maximum = new decimal(new int[] {
            360,
            0,
            0,
            0});
            this.numericUpDownSetAxis1.Name = "numericUpDownSetAxis1";
            this.numericUpDownSetAxis1.Size = new System.Drawing.Size(65, 20);
            this.numericUpDownSetAxis1.TabIndex = 5;
            // 
            // buttonSet
            // 
            this.buttonSet.Location = new System.Drawing.Point(116, 46);
            this.buttonSet.Name = "buttonSet";
            this.buttonSet.Size = new System.Drawing.Size(75, 23);
            this.buttonSet.TabIndex = 3;
            this.buttonSet.Text = "Set";
            this.buttonSet.UseVisualStyleBackColor = true;
            this.buttonSet.Click += new System.EventHandler(this.buttonSet_Click);
            // 
            // textBoxOutput
            // 
            this.textBoxOutput.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.textBoxOutput.Location = new System.Drawing.Point(12, 257);
            this.textBoxOutput.Multiline = true;
            this.textBoxOutput.Name = "textBoxOutput";
            this.textBoxOutput.Size = new System.Drawing.Size(717, 297);
            this.textBoxOutput.TabIndex = 5;
            // 
            // timer1
            // 
            this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.buttonTrigger);
            this.groupBox2.Controls.Add(this.buttonRight);
            this.groupBox2.Controls.Add(this.buttonLeft);
            this.groupBox2.Controls.Add(this.buttonUp);
            this.groupBox2.Controls.Add(this.buttonDown);
            this.groupBox2.Enabled = false;
            this.groupBox2.Location = new System.Drawing.Point(8, 84);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(274, 142);
            this.groupBox2.TabIndex = 7;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Control";
            // 
            // buttonTrigger
            // 
            this.buttonTrigger.Location = new System.Drawing.Point(95, 61);
            this.buttonTrigger.Name = "buttonTrigger";
            this.buttonTrigger.Size = new System.Drawing.Size(75, 23);
            this.buttonTrigger.TabIndex = 7;
            this.buttonTrigger.Text = "Trigger Off";
            this.buttonTrigger.UseVisualStyleBackColor = true;
            this.buttonTrigger.Click += new System.EventHandler(this.buttonTrigger_Click);
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.label3);
            this.groupBox3.Controls.Add(this.label4);
            this.groupBox3.Controls.Add(this.numericUpDownGotoAxis2);
            this.groupBox3.Controls.Add(this.numericUpDownGotoAxis1);
            this.groupBox3.Controls.Add(this.buttonGoto);
            this.groupBox3.Enabled = false;
            this.groupBox3.Location = new System.Drawing.Point(288, 157);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(203, 72);
            this.groupBox3.TabIndex = 9;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "GotoAxisPosition";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(7, 51);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(32, 13);
            this.label3.TabIndex = 8;
            this.label3.Text = "Axis2";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(7, 18);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(32, 13);
            this.label4.TabIndex = 7;
            this.label4.Text = "Axis1";
            // 
            // numericUpDownGotoAxis2
            // 
            this.numericUpDownGotoAxis2.Location = new System.Drawing.Point(45, 47);
            this.numericUpDownGotoAxis2.Maximum = new decimal(new int[] {
            360,
            0,
            0,
            0});
            this.numericUpDownGotoAxis2.Name = "numericUpDownGotoAxis2";
            this.numericUpDownGotoAxis2.Size = new System.Drawing.Size(65, 20);
            this.numericUpDownGotoAxis2.TabIndex = 6;
            // 
            // numericUpDownGotoAxis1
            // 
            this.numericUpDownGotoAxis1.Location = new System.Drawing.Point(45, 14);
            this.numericUpDownGotoAxis1.Maximum = new decimal(new int[] {
            360,
            0,
            0,
            0});
            this.numericUpDownGotoAxis1.Name = "numericUpDownGotoAxis1";
            this.numericUpDownGotoAxis1.Size = new System.Drawing.Size(65, 20);
            this.numericUpDownGotoAxis1.TabIndex = 5;
            // 
            // buttonGoto
            // 
            this.buttonGoto.Location = new System.Drawing.Point(116, 46);
            this.buttonGoto.Name = "buttonGoto";
            this.buttonGoto.Size = new System.Drawing.Size(75, 23);
            this.buttonGoto.TabIndex = 3;
            this.buttonGoto.Text = "Goto";
            this.buttonGoto.UseVisualStyleBackColor = true;
            this.buttonGoto.Click += new System.EventHandler(this.buttonGoto_Click);
            // 
            // comboBoxMountSelect
            // 
            this.comboBoxMountSelect.Enabled = false;
            this.comboBoxMountSelect.FormattingEnabled = true;
            this.comboBoxMountSelect.Items.AddRange(new object[] {
            "Skywatcher"});
            this.comboBoxMountSelect.Location = new System.Drawing.Point(6, 39);
            this.comboBoxMountSelect.Name = "comboBoxMountSelect";
            this.comboBoxMountSelect.Size = new System.Drawing.Size(170, 21);
            this.comboBoxMountSelect.TabIndex = 10;
            this.comboBoxMountSelect.Text = "Skywatcher";
            // 
            // Connection
            // 
            this.Connection.Controls.Add(this.textBoxStatus);
            this.Connection.Controls.Add(this.label7);
            this.Connection.Controls.Add(this.label6);
            this.Connection.Controls.Add(this.label5);
            this.Connection.Controls.Add(this.comboBoxPortSelect);
            this.Connection.Controls.Add(this.buttonConnect);
            this.Connection.Controls.Add(this.comboBoxMountSelect);
            this.Connection.Location = new System.Drawing.Point(2, 7);
            this.Connection.Name = "Connection";
            this.Connection.Size = new System.Drawing.Size(489, 71);
            this.Connection.TabIndex = 11;
            this.Connection.TabStop = false;
            this.Connection.Text = "Connection";
            // 
            // textBoxStatus
            // 
            this.textBoxStatus.Location = new System.Drawing.Point(380, 37);
            this.textBoxStatus.Name = "textBoxStatus";
            this.textBoxStatus.Size = new System.Drawing.Size(100, 20);
            this.textBoxStatus.TabIndex = 15;
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(377, 16);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(37, 13);
            this.label7.TabIndex = 14;
            this.label7.Text = "Status";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(179, 16);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(26, 13);
            this.label6.TabIndex = 13;
            this.label6.Text = "Port";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(7, 16);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(37, 13);
            this.label5.TabIndex = 9;
            this.label5.Text = "Mount";
            // 
            // comboBoxPortSelect
            // 
            this.comboBoxPortSelect.FormattingEnabled = true;
            this.comboBoxPortSelect.Location = new System.Drawing.Point(182, 39);
            this.comboBoxPortSelect.Name = "comboBoxPortSelect";
            this.comboBoxPortSelect.Size = new System.Drawing.Size(110, 21);
            this.comboBoxPortSelect.TabIndex = 12;
            // 
            // buttonConnect
            // 
            this.buttonConnect.Location = new System.Drawing.Point(298, 37);
            this.buttonConnect.Name = "buttonConnect";
            this.buttonConnect.Size = new System.Drawing.Size(75, 23);
            this.buttonConnect.TabIndex = 11;
            this.buttonConnect.Text = "Connect";
            this.buttonConnect.UseVisualStyleBackColor = true;
            this.buttonConnect.Click += new System.EventHandler(this.buttonConnect_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(741, 582);
            this.Controls.Add(this.Connection);
            this.Controls.Add(this.groupBox3);
            this.Controls.Add(this.textBoxOutput);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.groupBox2);
            this.Name = "Form1";
            this.Text = "Skywatcher Sample Control Program";
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownSetAxis2)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownSetAxis1)).EndInit();
            this.groupBox2.ResumeLayout(false);
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownGotoAxis2)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownGotoAxis1)).EndInit();
            this.Connection.ResumeLayout(false);
            this.Connection.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button buttonUp;
        private System.Windows.Forms.Button buttonDown;
        private System.Windows.Forms.Button buttonRight;
        private System.Windows.Forms.Button buttonLeft;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.NumericUpDown numericUpDownSetAxis2;
        private System.Windows.Forms.NumericUpDown numericUpDownSetAxis1;
        private System.Windows.Forms.Button buttonSet;
        private System.Windows.Forms.TextBox textBoxOutput;
        private System.Windows.Forms.Timer timer1;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.Button buttonTrigger;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.NumericUpDown numericUpDownGotoAxis2;
        private System.Windows.Forms.NumericUpDown numericUpDownGotoAxis1;
        private System.Windows.Forms.Button buttonGoto;
        private System.Windows.Forms.ComboBox comboBoxMountSelect;
        private System.Windows.Forms.GroupBox Connection;
        private System.Windows.Forms.Button buttonConnect;
        private System.Windows.Forms.ComboBox comboBoxPortSelect;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.TextBox textBoxStatus;
        private System.Windows.Forms.Label label7;
    }
}

