package com.skywatcher.PanoramaApp;

import java.io.IOException;

import com.skywatcher.api.Mount;
import com.skywatcher.api.MountControlException;
import com.skywatcher.api.Mount_Skywatcher;
import com.skywatcher.PanoramaApp.R; //import com.skywatchertelescope.remote.Mount_Skywatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class PanoramaAppActivity extends TabActivity {
	/** Called when the activity is first created. */

	// Layout Views
	private TextView title_right; // 在畫面右上角(Title的右方) 顯示連線狀態

	// 拖架控制
	private static Mount mountControl;
	private static BluetoothManager mManager;

	// Name of the connected device
	private String mConnectedDeviceName = null;

	// Reconnect Dialog
	static AlertDialog reconnectDialog;
	static ProgressDialog connectDialog;

	// Debugging
	private static final String TAG = "PanoramaAppActivity";
	private static final boolean D = true;
	
	private BluetoothAdapter mBluetoothAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 更改Title為可顯示連線狀態的layout
		setCustomTitle();

		// 設置主畫面tab內容
		setTabs();

		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// 初始化 MountControl 類別,並指定Bluetooth Event的Handler
		mManager = new BluetoothManager(this, mHandler);
		// mountControl = new Mount_Skywatcher();
		mountControl = new Mount_Skywatcher();
	}

	
	@Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        // Otherwise, setup the chat session
//        } else {
////            if (mChatService == null) setupChat();
//        }
    }
	
	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
//		if (mManager != null) {
//			// Only if the state is STATE_NONE, do we know that we haven't
//			// started already
//			if (mManager.getState() == BluetoothManager.STATE_NONE) {
//				// Start the Bluetooth chat services
//				mManager.reconnect();
//			}
//		}
//		mountControl = PanoramaAppActivity.getMountControl();
//		mManager = PanoramaAppActivity.getBluetoothManager();
//
//		// 若已連線則所有元件設為enable，反之則設 disable
//		if (mManager.getState() == BluetoothManager.STATE_CONNECTED) {
//			setAllEnabled(mainLayout, true);
//			setStatusBar(true);
//		} else {
//			setAllEnabled(mainLayout, false);
//			setStatusBar(false);
//		}
		super.onResume();
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mManager != null)
			mManager.stop();

		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	public static Mount getMountControl() {
		return mountControl;
	}

	public static BluetoothManager getBluetoothManager() {
		return mManager;
	}

	private void setCustomTitle() {
		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		((TextView) findViewById(R.id.title_left_text))
				.setText(R.string.app_name);
		title_right = (TextView) findViewById(R.id.title_right_text);
		title_right.setText(R.string.title_not_connected);
	}

	private void setTabs() {
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// 設置主控制介面的 tab
		intent = new Intent().setClass(this, MainControlActivity.class);
		spec = tabHost.newTabSpec("main_control").setIndicator(
				res.getString(R.string.tab_main_control),
				res.getDrawable(R.drawable.ic_tab_main_control)).setContent(
				intent);
		tabHost.addTab(spec);

		// 設置快選介面的 tab
		intent = new Intent().setClass(this, QuickSelectActivity.class);
		spec = tabHost.newTabSpec("qucik_select").setIndicator(
				res.getString(R.string.tab_quick_select),
				res.getDrawable(R.drawable.ic_tab_quick_select)).setContent(
				intent);
		tabHost.addTab(spec);

		// 設置逐步設置介面的 tab
		intent = new Intent().setClass(this, StepByStepActivity.class);
		spec = tabHost.newTabSpec("step_by_step").setIndicator(
				res.getString(R.string.tab_step_by_step),
				res.getDrawable(R.drawable.ic_tab_step_by_step)).setContent(
				intent);
		tabHost.addTab(spec);

		// 設置設定介面的 tab
		intent = new Intent().setClass(this, SettingActivity.class);
		spec = tabHost.newTabSpec("setting").setIndicator(
				res.getString(R.string.tab_setting),
				res.getDrawable(R.drawable.ic_tab_setting)).setContent(intent);
		tabHost.addTab(spec);

		// 設定預設tab (App一開啟時)
		tabHost.setCurrentTab(0);
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothManager.MESSAGE_RECONNECTED:
				doReconnected();
				break;
			case BluetoothManager.MESSAGE_CONNECTED:
				doConnected();
				break;
			case BluetoothManager.MESSAGE_CONNECTION_LOST:
				doConnectLost();
				PopupReconnectDialog(PanoramaAppActivity.this);
				// mManager.requestReconnect();
				break;
			case BluetoothManager.MESSAGE_CONNECTION_FAILED:
				doConnectionFailed();
				break;
			case BluetoothManager.MESSAGE_CONNECTION_STOP:
				doConnectionStop();
				break;
			// case BluetoothManager.MESSAGE_REQUEST_RECONNECT:
			// PopupReconnectDialog(PanoramaAppActivity.this);
			// break;
			// case BluetoothManager.MESSAGE_REQUEST_CONNECT:
			// String address = (String) msg.obj;
			// PopupConnectDialog(PanoramaAppActivity.this, address);
			// break;
			case BluetoothManager.MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				doStateChange(msg.arg1);
				break;
			case BluetoothManager.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(
						BluetoothManager.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case BluetoothManager.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(BluetoothManager.TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		private void doConnectionStop() {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			if (reconnectDialog != null)
				reconnectDialog.dismiss();

			if (connectDialog != null)
				connectDialog.dismiss();
		}

		private void doConnectionFailed() {
			// TODO Auto-generated method stub
			if (reconnectDialog != null)
				reconnectDialog.dismiss();

			if (connectDialog != null)
				connectDialog.dismiss();
		}
	};

	private void doReconnected() {
		// TODO Auto-generated method stub
		try {
			mountControl.Connect_Bluetooth(mManager.getSocket());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Bluetooth Connect Failed", e);
		}

		// 啟用MainControlActivity的按鈕
		MainControlActivity.setAllEnabled((ViewGroup) getTabHost()
				.getChildAt(0), true);

		if (reconnectDialog != null)
			reconnectDialog.dismiss();

		if (connectDialog != null)
			connectDialog.dismiss();
	}

	private void doStateChange(int state) {
		switch (state) {
		case BluetoothManager.STATE_CONNECTED:
			title_right.setText(R.string.title_connected_to);
			title_right.append(mConnectedDeviceName);
			break;
		case BluetoothManager.STATE_CONNECTING:
			title_right.setText(R.string.title_connecting);
			break;
//		case BluetoothManager.STATE_LISTEN:
//			title_right.setText(R.string.title_not_connected);
//			break;
		case BluetoothManager.STATE_NONE:
		case BluetoothManager.STATE_CONNECTION_LOST:
			title_right.setText(R.string.title_not_connected);
			break;
		}
	}

	private void doConnectLost() {
		// 停用MainControlActivity的按鈕
		MainControlActivity.setAllEnabled((ViewGroup) getTabHost()
				.getChildAt(0), false);

		// Cancel Dialog
		if (reconnectDialog != null)
			reconnectDialog.dismiss();

		if (connectDialog != null)
			connectDialog.dismiss();
	}

	private void doConnected() {
		try {
			mountControl.Connect_Bluetooth(mManager.getSocket());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Bluetooth Connect Failed", e);
		}

		// 啟動腳架連線
		try {
			mountControl.MCOpenTelescopeConnection();
		} catch (MountControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Open Telescope Connect Failed", e);
		}

		// Cancel Dialog
		if (reconnectDialog != null)
			reconnectDialog.dismiss();

		if (connectDialog != null)
			connectDialog.dismiss();

		// 啟用MainControlActivity的按鈕
		MainControlActivity.setAllEnabled((ViewGroup) getTabHost()
				.getChildAt(0), true);

	}

	// Popup a reconnection dialog
	public static void PopupReconnectDialog(final Context context) {
		if (reconnectDialog != null)
			reconnectDialog.dismiss();

		reconnectDialog = new AlertDialog.Builder(context).setCancelable(true)
				.setTitle("Reconnect to Device").setNegativeButton("Cancel",
						null).setPositiveButton(R.string.dialog_title,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								connectDialog = ProgressDialog.show(context,
										"", "Connecting. Please Wait", true,
										true,
										new DialogInterface.OnCancelListener() {
											@Override
											public void onCancel(
													DialogInterface dialog) {
												// TODO Auto-generated method
												// stub
												// Stop Bluetooth connection
												mManager.stop();
											}
										});
								mManager.reconnect();
							}
						}).create();
		reconnectDialog.show();
	}

	// Popup a connection dialog
	public static void PopupConnectDialog(Context context, String Address) {
		if (connectDialog != null)
			connectDialog.dismiss();

		connectDialog = ProgressDialog.show(context, "",
				"Connecting. Please Wait", true, false);
		// new DialogInterface.OnCancelListener() {
		// @Override
		// public void onCancel(DialogInterface dialog) {
		// // TODO Auto-generated method stub
		// // Stop Bluetooth connection
		// mManager.stop();
		// }
		// });
		mManager.connect(Address);
	}

	 // Intent request codes
//    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
//    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
//    private static final int REQUEST_ENABLE_BT = 3;
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(D) Log.d(TAG, "onActivityResult " + resultCode);
//        switch (requestCode) {
////        case REQUEST_CONNECT_DEVICE_SECURE:
////            // When DeviceListActivity returns with a device to connect
////            if (resultCode == Activity.RESULT_OK) {
////                connectDevice(data, true);
////            }
////            break;
////        case REQUEST_CONNECT_DEVICE_INSECURE:
////            // When DeviceListActivity returns with a device to connect
////            if (resultCode == Activity.RESULT_OK) {
////                connectDevice(data, false);
////            }
////            break;
////        case REQUEST_ENABLE_BT:
////            // When the request to enable Bluetooth returns
////            if (resultCode == Activity.RESULT_OK) {
////                // Bluetooth is now enabled, so set up a chat session
////                setupChat();
////            } else {
////                // User did not enable Bluetooth or an error occurred
////                Log.d(TAG, "BT not enabled");
////                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
////                finish();
////            }
//        }
//    }
}