package com.skywatcher.PanoramaApp;

import com.skywatcher.PanoramaApp.ShootScenario.ShootScenarioException;
import com.skywatcher.api.AXISID;
import com.skywatcher.api.AstroMisc;
import com.skywatcher.api.Mount;
import com.skywatcher.api.MountControlException;
import com.skywatcher.extend.Angle;
import com.skywatcher.PanoramaApp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingGuideActivity extends Activity {
	public final static int SCENARIO_NEW = 0x2001;
	public static final String GUIDE_SCENARIO = "GuideScenario";

	// Views
	private TextView text_guide, status_bar;
	private Button button_cancel, button_done;
	private ImageButton button_up, button_down, button_left, button_right;

	private Mount mountControl; // 腳架控制

	private int guideType; // Fov or Panorama
	private int step = 0;

	private double top, bottom, left, right; // record

	// Debugging
	private static final String TAG = "SettingGuideActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_guide);
		mountControl = PanoramaAppActivity.getMountControl();

		findViews();
		getGuideType();
		setListeners();
		updateStatusBar();
	}

	private void findViews() {
		text_guide = (TextView) findViewById(R.id.text_guide);
		status_bar = (TextView) findViewById(R.id.status_bar);

		button_cancel = (Button) findViewById(R.id.button_cancel);
		button_done = (Button) findViewById(R.id.button_done);

		button_up = (ImageButton) findViewById(R.id.button_up);
		button_down = (ImageButton) findViewById(R.id.button_down);
		button_left = (ImageButton) findViewById(R.id.button_left);
		button_right = (ImageButton) findViewById(R.id.button_right);
	}

	// set guide type from previous activity
	private void getGuideType() {
		Bundle extras = getIntent().getExtras();
		guideType = extras.getInt(StepByStepActivity.GUIDE_TYPE);

		// 設定一開始顯示的文字
		switch (guideType) {
		case 0:
			text_guide.setText(R.string.target_left_top);
			break;
		case 1:
			text_guide.setText(R.string.move_left_bound);
			break;
		}
	}

	// 設定 Button 的 Action
	private void setListeners() {
		button_cancel.setOnClickListener(cancelListener);
		button_done.setOnClickListener(doneListener);

		button_up.setOnTouchListener(controlListener);
		button_down.setOnTouchListener(controlListener);
		button_left.setOnTouchListener(controlListener);
		button_right.setOnTouchListener(controlListener);
	}

	private void updateStatusBar() {
		try {
			status_bar.setText("Axis1: "
					+ AstroMisc.RadToStr(mountControl
							.MCGetAxisPosition(AXISID.AXIS1))
					+ ", Axis2: "
					+ AstroMisc.RadToStr(mountControl
							.MCGetAxisPosition(AXISID.AXIS2)));
		} catch (MountControlException e) {
			Toast.makeText(this, e.ErrMessage, Toast.LENGTH_SHORT).show();
		}
	}

	private OnClickListener cancelListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private OnClickListener doneListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				switch (guideType) {
				// Fov Step by Step
				case 0:
					switch (step) {
					case 0:
						left = mountControl.MCGetAxisPosition(AXISID.AXIS1);
						top = mountControl.MCGetAxisPosition(AXISID.AXIS2);
						text_guide.setText(R.string.target_right_bottom);
						button_done.setText(R.string.button_done);
						break;
					case 1:
						right = mountControl.MCGetAxisPosition(AXISID.AXIS1);
						bottom = mountControl.MCGetAxisPosition(AXISID.AXIS2);

						// Show the dialog
						AlertDialog dialog = new AlertDialog.Builder(
								SettingGuideActivity.this)
								.setMessage(
										"The Fov is ("
												+ AstroMisc.RadToStr(right
														- left)
												+ ", "
												+ AstroMisc.RadToStr(top
														- bottom) + ")")
								.setPositiveButton("Save",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												SharedPreferences settings = PreferenceManager
														.getDefaultSharedPreferences(SettingGuideActivity.this);
												SharedPreferences.Editor editor = settings
														.edit();
												editor.putString(
														getString(R.string.key_fov_x),
														String.valueOf((int) AstroMisc
																.RadToDeg(right
																		- left)));
												editor.putString(
														getString(R.string.key_fov_y),
														String.valueOf((int) AstroMisc
																.RadToDeg(top
																		- bottom)));
												editor.commit();
												finish();
											}
										})
								.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												finish();
											}
										}).create();

						dialog.show();
						break;
					}
					break;
				// Panorama Step by Step
				case 1:
					switch (step) {
					case 0:
						left = mountControl.MCGetAxisPosition(AXISID.AXIS1);
						text_guide.setText(R.string.move_right_bound);
						break;
					case 1:
						right = mountControl.MCGetAxisPosition(AXISID.AXIS1);
						text_guide.setText(R.string.move_upper_bound);
						break;
					case 2:
						top = mountControl.MCGetAxisPosition(AXISID.AXIS2);
						text_guide.setText(R.string.move_lower_bound);
						button_done.setText(R.string.button_done);
						break;
					case 3:
						bottom = mountControl.MCGetAxisPosition(AXISID.AXIS2);

						ShootScenario scenario = new ShootScenario();
						scenario.setMinAz((int) AstroMisc.RadToDeg(left));
						scenario.setMaxAz((int) AstroMisc.RadToDeg(right));
						scenario.setMinAlt((int) AstroMisc.RadToDeg(bottom));
						scenario.setMaxAlt((int) AstroMisc.RadToDeg(top));
						scenario.setInterlace(false);

						Intent intent = new Intent(SettingGuideActivity.this,
								ScenarioSettingActivity.class);
						intent.putExtra(GUIDE_SCENARIO, scenario); // 將整個劇本傳到下一頁
						startActivity(intent);
						break;
					}
					break;
				}
				step++;

			} catch (MountControlException e) {
				Toast.makeText(SettingGuideActivity.this, e.ErrMessage,
						Toast.LENGTH_SHORT).show();
			} catch (ShootScenarioException e) {
				Toast.makeText(SettingGuideActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, e.getMessage());
			}
		}
	};

	private int PreviousAction = -1;
	private AXISID PreviousAxis = null;
	private static final double speed = AstroMisc.DegToRad(10);

	private OnTouchListener controlListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent motionEvent) {
			try {
				AXISID Axis = AXISID.AXIS1;
				int direction = 1;
				switch (v.getId()) {
				case R.id.button_up:
					Axis = AXISID.AXIS2;
					direction = 1;
					break;
				case R.id.button_down:
					Axis = AXISID.AXIS2;
					direction = -1;
					break;
				case R.id.button_left:
					Axis = AXISID.AXIS1;
					direction = -1;
					break;
				case R.id.button_right:
					Axis = AXISID.AXIS1;
					direction = 1;
					break;
				}

				if (PreviousAxis == Axis
						&& PreviousAction == motionEvent.getAction())
					return false;

				// Should use message to avoid halting UI
				Log.i(TAG, motionEvent.getAction() + ":" + Axis + "," + speed);
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
					mountControl.MCAxisSlew(Axis, direction * speed);
				else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
					mountControl.MCAxisStop(Axis);

				PreviousAction = motionEvent.getAction();
				PreviousAxis = Axis;
				updateStatusBar();
			} catch (MountControlException e) {
				Toast.makeText(SettingGuideActivity.this, e.ErrMessage,
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Failed", e);
				return false;
			}
			return false;
		}
	};
}
