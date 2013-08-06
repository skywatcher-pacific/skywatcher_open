package com.skywatcher.PanoramaApp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skywatcher.api.AXISID;
import com.skywatcher.api.AstroMisc;
import com.skywatcher.api.Mount;
import com.skywatcher.api.MountControlException;
import com.skywatcher.PanoramaApp.R;

public class MainControlActivity extends Activity {

	private LinearLayout mainLayout; // 最上層的layout
	Button button_home;
	private ImageButton button_up, button_down, button_left, button_right;
	private Button button_shot, button_high_speed, button_low_speed,
			button_set_home;
	private TextView view_status; // Status Bar
	private Mount mountControl; // 腳架控制
	private BluetoothManager mManager;
	private double home_axis1, home_axis2; // 目前初始位置的座標
	private double speed; // 移動速度

	private HandlerThread handlerThread; // 快門使用的thread
	private int shotSecond; // 按下快門後剩下的秒數

	// Debugging
	private static final String TAG = "MainControlActivity";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_control);

		findViews();
		setListeners();

		setHome(0.0, 0.0);
		speed = HIGH_SPEED;
	}

	@Override
	protected void onResume() {
		mountControl = PanoramaAppActivity.getMountControl();
		mManager = PanoramaAppActivity.getBluetoothManager();

		// 若已連線則所有元件設為enable，反之則設 disable
		if (mManager.getState() == BluetoothManager.STATE_CONNECTED) {
			setAllEnabled(mainLayout, true);
			setStatusBar(true);
		} else {
			setAllEnabled(mainLayout, false);
			setStatusBar(false);
		}
		super.onResume();
	}

	// 連結畫面元件與程式內的控制
	private void findViews() {
		mainLayout = (LinearLayout) findViewById(R.id.main_control_LinearLayout);

		button_home = (Button) findViewById(R.id.button_home);
		button_up = (ImageButton) findViewById(R.id.button_up);
		button_down = (ImageButton) findViewById(R.id.button_down);
		button_left = (ImageButton) findViewById(R.id.button_left);
		button_right = (ImageButton) findViewById(R.id.button_right);

		button_shot = (Button) findViewById(R.id.button_shot);
		button_high_speed = (Button) findViewById(R.id.button_high_speed);
		button_low_speed = (Button) findViewById(R.id.button_low_speed);
		button_set_home = (Button) findViewById(R.id.button_set_home);

		view_status = (TextView) findViewById(R.id.view_status);
	}

	// 設定 Button 的 Action
	private void setListeners() {
		button_home.setOnClickListener(clickListener);

		button_up.setOnTouchListener(controlListener);
		button_down.setOnTouchListener(controlListener);
		button_left.setOnTouchListener(controlListener);
		button_right.setOnTouchListener(controlListener);

		button_shot.setOnClickListener(clickListener);
		button_high_speed.setOnClickListener(clickListener);
		button_low_speed.setOnClickListener(clickListener);
		button_set_home.setOnClickListener(clickListener);
	}

	private final double HIGH_SPEED = AstroMisc.DegToRad(15);
	private final double LOW_SPEED = AstroMisc.DegToRad(1);

	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				switch (v.getId()) {
				case R.id.button_home:
					mountControl.MCAxisSlewTo(AXISID.AXIS1, home_axis1);
					mountControl.MCAxisSlewTo(AXISID.AXIS2, home_axis2);
					break;

				case R.id.button_shot:
					v.setEnabled(false);
					mountControl.MCSetSwitch(true);

					// 從設定介面所設的值中，調出來使用 (Trigger Delay)
					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(MainControlActivity.this);
					String triggerDelay = settings.getString(
							getString(R.string.key_trigger_delay), "3");
					shotSecond = Integer.parseInt(triggerDelay);

					// 使用thread來倒數秒數
					handlerThread = new HandlerThread("shot");
					handlerThread.start();
					new Handler(handlerThread.getLooper()).post(new Runnable() {
						@Override
						public void run() {
							atFrontOfQueue();
						}
					});
					break;

				case R.id.button_high_speed:
					speed = HIGH_SPEED;
					break;

				case R.id.button_low_speed:
					speed = LOW_SPEED;
					break;

				case R.id.button_set_home:
					setHome(mountControl.MCGetAxisPosition(AXISID.AXIS1),
							mountControl.MCGetAxisPosition(AXISID.AXIS2));
					Toast
							.makeText(
									MainControlActivity.this,
									"Home is ("
											+ AstroMisc.RadToStr(home_axis1)
											+ ", "
											+ AstroMisc.RadToStr(home_axis2)
											+ ") now.", Toast.LENGTH_LONG)
							.show();
					break;
				}
				setStatusBar(true);

			} catch (MountControlException e) {
				Toast.makeText(MainControlActivity.this, e.ErrMessage,
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};
	private int PreviousAction = -1;
	private AXISID PreviousAxis = null;

	private OnTouchListener controlListener = new OnTouchListener() {
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
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					Log.i(TAG, "AxisSlew:" + Axis + "," + speed);
					mountControl.MCAxisSlew(Axis, direction * speed);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					Log.i(TAG, "AxisStop:" + Axis + "," + speed);
					mountControl.MCAxisStop(Axis);
				}

				PreviousAction = motionEvent.getAction();
				PreviousAxis = Axis;
				setStatusBar(true);
			} catch (MountControlException e) {
				if (e.ErrMessage != null) {
					Toast.makeText(MainControlActivity.this, e.ErrMessage,
							Toast.LENGTH_SHORT).show();				
				}
				else {
					Toast.makeText(MainControlActivity.this, "Some thing wrong", Toast.LENGTH_SHORT).show();
				}
				Log.e(TAG, "Failed", e);
			}

			return false;
		}
	};

	/**
	 * Set all descendant enable or disable.
	 * 
	 * @param vg
	 *            Like LinearLayout...
	 * @param enabled
	 *            enable or disable.
	 */
	public static void setAllEnabled(ViewGroup vg, boolean enabled) {
		for (int i = 0; i < vg.getChildCount(); i++) {
			if (vg.getChildAt(i) instanceof ViewGroup)
				setAllEnabled((ViewGroup) vg.getChildAt(i), enabled);
			else
				vg.getChildAt(i).setEnabled(enabled);
		}
	}

	// 顯示目前的角度讀數, 或未連線時顯示未連線
	private void setStatusBar(boolean connected) {
		if (!connected) {
			view_status.setText(R.string.title_not_connected);
			return;
		}

		try {
			view_status.setText("Axis1: "
					+ AstroMisc.RadToStr(mountControl
							.MCGetAxisPosition(AXISID.AXIS1))
					+ ", Axis2: "
					+ AstroMisc.RadToStr(mountControl
							.MCGetAxisPosition(AXISID.AXIS2)));
		} catch (MountControlException e) {
			Toast.makeText(MainControlActivity.this, e.ErrMessage,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private void setHome(double axis1, double axis2) {
		home_axis1 = axis1;
		home_axis2 = axis2;
	}

	private void atFrontOfQueue() {
		new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new Runnable() {
			@Override
			public void run() {
				if (shotSecond <= 0) {
					try {
						mountControl.MCSetSwitch(false);
						handlerThread.getLooper().quit();
						handlerThread.join();
						button_shot.setText(R.string.button_shot);
						button_shot.setEnabled(true);
					} catch (MountControlException e) {
						e.printStackTrace();
						mManager.connectionLost();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return;
				}
				button_shot.setText(getString(R.string.button_shot_wait)
						+ shotSecond + getString(R.string.button_shot_second));
				--shotSecond;
				delayed();
			}
		});
	}

	private void delayed() {
		new Handler(handlerThread.getLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				atFrontOfQueue();
			}
		}, 1000);
	}
}