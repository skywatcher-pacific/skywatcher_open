package com.skywatcher.PanoramaApp;

import java.util.Date;
import java.util.List;

import com.skywatcher.api.AXISID;
import com.skywatcher.api.Mount;
import com.skywatcher.api.MountControlException;
import com.skywatcher.extend.Angle;
import com.skywatcher.extend.CroodAxis1Axis2;
import com.skywatcher.extend.Panorama;
import com.skywatcher.PanoramaApp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShootActivity extends Activity {
	final private String TAG = "ShootActivity";

	// Views
	private ImageButton button_start, button_stop;
	private Button button_cancel, button_log;
	private CheckBox checkbox_step_by_step;
	private ProgressBar progressBar;
	private TextView status_bar;
	private PreviewChartView view_preview;

	private Mount mountControl; // 腳架控制

	List<CroodAxis1Axis2> points = null; // 所需拍攝的點

	// handler's messages
	private final static int UPDATE_TRIGGER_DELAY_LEFT = 1;
	private final static int UPDATE_STEP_DELAY_LEFT = 2;
	private final static int END_OF_WAIT = 3;

	// Log message
	private StringBuffer logString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.shoot);

		logString = new StringBuffer();
		findViews();
		setListeners();
		setShootPoints();

		mountControl = PanoramaAppActivity.getMountControl();
	}

	private void findViews() {
		button_start = (ImageButton) findViewById(R.id.button_start);
		button_stop = (ImageButton) findViewById(R.id.button_stop);
		button_cancel = (Button) findViewById(R.id.button_cancel);
		button_log = (Button) findViewById(R.id.button_log);

		checkbox_step_by_step = (CheckBox) findViewById(R.id.checkbox_step_by_step);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(100);
		status_bar = (TextView) findViewById(R.id.remaining_time);
		view_preview = (PreviewChartView) findViewById(R.id.preveiw_chart);
	}

	private void setListeners() {
		button_start.setOnClickListener(startListener);
		button_stop.setOnClickListener(stopListener);
		button_cancel.setOnClickListener(cancelListener);
		button_log.setOnClickListener(logListener);
	}

	private void setShootPoints() {
		Bundle extras = getIntent().getExtras();
		ShootScenario ss = extras
				.getParcelable(ScenarioSettingActivity.PREVIEW_SCENARIO);

		// 從設定介面所設的值中，調出來使用
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(ShootActivity.this);
		String fovX = settings.getString(getString(R.string.key_fov_x), null);
		String fovY = settings.getString(getString(R.string.key_fov_y), null);
		String overlap = settings.getString(getString(R.string.key_overlap),
				null);

		String stepDelay = settings.getString(
				getString(R.string.key_step_delay), null);
		String triggerDelay = settings.getString(
				getString(R.string.key_trigger_delay), null);

		Panorama.setStepDelay(Integer.parseInt(stepDelay));
		Panorama.setTriggerDelay(Integer.parseInt(triggerDelay));
		Panorama.setFovX(Angle.FromDegree(Integer.parseInt(fovX)));
		Panorama.setFovY(Angle.FromDegree(Integer.parseInt(fovY)));
		Panorama.setOverlap(Double.parseDouble(overlap) / 100.0);
		Panorama.setPortrait(settings.getBoolean(
				getString(R.string.key_portrait), false));
		points = Panorama.GenerateShotPoint(Angle.FromDegree(ss.getMinAz()),
				Angle.FromDegree(ss.getMaxAz()),
				Angle.FromDegree(ss.getMinAlt()),
				Angle.FromDegree(ss.getMaxAlt()));

		view_preview.setShootPoints(points);

		// 將設定參數寫入log中
		logString
				.append(getString(R.string.setting_fov_x) + ": " + fovX + "\n");
		logString
				.append(getString(R.string.setting_fov_y) + ": " + fovY + "\n");
		logString.append(getString(R.string.setting_overlap) + ": " + overlap
				+ "\n");
		logString.append(getString(R.string.setting_step_delay) + ": "
				+ stepDelay + "\n");
		logString.append(getString(R.string.setting_trigger_delay) + ": "
				+ triggerDelay + "\n");
		logString.append(getString(R.string.setting_portrait) + ": "
				+ settings.getBoolean(getString(R.string.key_portrait), false)
				+ "\n");
	}

	// Refer:
	// http://developer.android.com/guide/appendix/faq/commontasks.html#threading
	// Use threading to handle event
	Thread MainLoop;
	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String second = getString(R.string.second);

			switch (msg.what) {
			case UPDATE_TRIGGER_DELAY_LEFT:
				status_bar.setText(getString(R.string.trigger_delay_left)
						+ msg.getData().getInt("WAIT") + second);
				break;

			case UPDATE_STEP_DELAY_LEFT:
				status_bar.setText(getString(R.string.step_delay_left)
						+ msg.getData().getInt("WAIT") + second);
				break;

			case END_OF_WAIT:
				status_bar.setText("");
				break;
			}
			super.handleMessage(msg);
		}
	};

	// Create runnable for posting
	// Update the Axis Position in Preview Chart
	final Runnable mUpdateAxisPosition = new Runnable() {
		public void run() {
			UpdateAxisPosition();
		}
	};

	final Runnable mUpdateTargetPoint = new Runnable() {
		public void run() {
			UpdateTargetPoint();
		}
	};

	final Runnable mUpdateStartShooting = new Runnable() {
		public void run() {
			UpdateStartShooting();
		}
	};

	final Runnable mUpdateEndShooting = new Runnable() {
		public void run() {
			UpdateEndShooting();
		}
	};

	final Runnable mUpdatePauseButton = new Runnable() {
		public void run() {
			UpdatePauseButton();
		}
	};

	private CroodAxis1Axis2 targetPoint;

	private enum Mode { // Status of Shoot
		PLAY, PAUSE, STOP
	};

	private Mode mode = Mode.STOP;

	private int completeIndex = -1;

	private CroodAxis1Axis2 CurrentPosition;

	// Use only one thread
	private OnClickListener startListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mode == Mode.PLAY) {
				mode = Mode.PAUSE;
				button_start.setImageResource(android.R.drawable.ic_media_play);
				return;
			} else if (mode == Mode.PAUSE) {
				mode = Mode.PLAY;
				button_start
						.setImageResource(android.R.drawable.ic_media_pause);
				return;
			}

			MainLoop = new Thread() {
				public void run() {
					// Start the process
					mHandler.post(mUpdateStartShooting);
					completeIndex = -1;

					mode = Mode.PLAY;
					try {
						for (int i = 0; mode != Mode.STOP && i < points.size(); i++) {
							// Move to Next Position
							targetPoint = points.get(i);
							mountControl.MCAxisSlewTo(AXISID.AXIS1,
									targetPoint.Axis1.getRad());
							mountControl.MCAxisSlewTo(AXISID.AXIS2,
									targetPoint.Axis2.getRad());

							// draw Red color to TargetPoint
							mHandler.post(mUpdateTargetPoint);

							Thread.sleep(500);

							// Wait until stop
							while (true) {
								// draw + to camera's position
								CurrentPosition = new CroodAxis1Axis2(
										new Angle(
												mountControl
														.MCGetAxisPosition(AXISID.AXIS1)),
										new Angle(
												mountControl
														.MCGetAxisPosition(AXISID.AXIS2)));
								mHandler.post(mUpdateAxisPosition);

								if (mountControl.MCGetAxisStatus(AXISID.AXIS1).FullStop
										&& mountControl
												.MCGetAxisStatus(AXISID.AXIS2).FullStop) {
									break;
								}
								Thread.sleep(500);
							}

							// Start Shooting
							mountControl.MCSetSwitch(true);

							// Wait for Trigger Delay
							waitForDelay((int) Panorama.getTriggerDelay(),
									UPDATE_TRIGGER_DELAY_LEFT);

							mountControl.MCSetSwitch(false);

							// log
							logString.append("#" + (i + 1) + ", "
									+ (new Date()) + ", " + CurrentPosition
									+ "\n");

							// Wait for Step Delay
							if (mode == Mode.PLAY) {
								waitForDelay((int) Panorama.getStepDelay(),
										UPDATE_STEP_DELAY_LEFT);
							}

							completeIndex = i;

							// if Step by Step Mode, pause this step
							if (checkbox_step_by_step.isChecked()) {
								mode = Mode.PAUSE;
								mHandler.post(mUpdatePauseButton);
							}

							while (mode == Mode.PAUSE) {
								Thread.sleep(1000);
							}
						}
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (MountControlException e2) {
						e2.printStackTrace();
						PanoramaAppActivity.getBluetoothManager().connectionLost();
						// it is in the thread, cannot raise the dialog directly 
						return;
//						PanoramaAppActivity.PopupReconnectDialog(ShootActivity.this);						
					}

					mHandler.post(mUpdateEndShooting);
				}
			};

			MainLoop.start();
		}
	};

	// The Event may happen in Process
	private void UpdateStartShooting() {
		Log.d(TAG, "StartShooting");
		button_start.setImageResource(android.R.drawable.ic_media_pause);
		progressBar.setProgress(0);
	}

	private void UpdateEndShooting() {
		Log.d(TAG, "EndShooting");
		mode = Mode.STOP;

		// 停止時所不用再畫的東西
		view_preview.setTargetPoint(null);
		view_preview.setCamera(null);

		button_start.setImageResource(android.R.drawable.ic_media_play);
		MainLoop = null;
		progressBar.setProgress(100);
	}

	private void UpdateTargetPoint() {
		Log.d(TAG, "UpdateTargetPoint" + ":" + targetPoint.toString());
		view_preview.setTargetPoint(targetPoint);
		view_preview.setCompleteIndex(completeIndex);
		progressBar.setProgress((completeIndex + 1) * 100 / points.size());
	}

	// MountControl is not thread safe, cannot use in different thread in the
	// same time!
	private void UpdateAxisPosition() {

		Log.d(TAG, "UpdateAxisPosition" + ":" + CurrentPosition);

		view_preview.setCamera(CurrentPosition);
	}

	private void UpdatePauseButton() {
		Log.d(TAG, "UpdatePauseButton");
		button_start.setImageResource(android.R.drawable.ic_media_play);
	}

	private OnClickListener stopListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "ClickStop");
			stop();
		}
	};

	private OnClickListener cancelListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "ClickCancel");
			stop();
			finish(); // back to previous activity
		}
	};

	// 手動 Stop 時要做的事
	private void stop() {
		mode = Mode.STOP;
		if (MainLoop != null) {
			MainLoop.interrupt();
			// Close Thread
			boolean retry = true;
			while (retry) {
				try {
					MainLoop.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
		}
		UpdateEndShooting();
	}

	// Wait for Trigger or Step delay
	private void waitForDelay(int delayTime, int messageType)
			throws InterruptedException {
		Message msg;
		while (delayTime > 0) {
			msg = mHandler.obtainMessage(messageType);
			Bundle b = new Bundle();
			b.putInt("WAIT", delayTime);
			msg.setData(b);
			mHandler.sendMessage(msg);

			delayTime--;
			Thread.sleep(1000);
		}
		mHandler.sendEmptyMessage(END_OF_WAIT);
	}

	private OnClickListener logListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Create the E-mail input dialog
			AlertDialog.Builder alert = new AlertDialog.Builder(
					ShootActivity.this);
			alert.setTitle(R.string.send_to);

			final EditText input = new EditText(ShootActivity.this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

			alert.setView(input);
			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Prepare a intent to send e-mail
							Intent intent = new Intent(
									android.content.Intent.ACTION_SEND);
							intent.setType("text/plain");

							intent.putExtra(android.content.Intent.EXTRA_EMAIL,
									new String[] { input.getText().toString() });
							intent.putExtra(
									android.content.Intent.EXTRA_SUBJECT,
									"Panorama Log");
							intent.putExtra(android.content.Intent.EXTRA_TEXT,
									logString.toString());

							// Send E-mail
							startActivity(Intent.createChooser(intent,
									"Choose Email Client"));
						}
					});
			alert.setNegativeButton("Calcel", null);
			alert.show();
		}
	};
}
