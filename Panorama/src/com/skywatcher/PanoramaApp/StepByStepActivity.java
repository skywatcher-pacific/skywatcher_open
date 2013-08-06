package com.skywatcher.PanoramaApp;

import com.skywatcher.PanoramaApp.R;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StepByStepActivity extends ListActivity {
	public final static String GUIDE_TYPE = "GUIDE_TYPE";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] step_by_step = getResources().getStringArray(
				R.array.step_by_step_list);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, step_by_step));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		// 測試有無連線，若無則請user重新連線
		int state = PanoramaAppActivity.getBluetoothManager().getState();
		switch (state) {
		case BluetoothManager.STATE_NONE:
		case BluetoothManager.STATE_CONNECTION_LOST:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(StepByStepActivity.this,
					DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return;
//		case BluetoothManager.STATE_LISTEN:
		case BluetoothManager.STATE_CONNECTING:
			return;

		case BluetoothManager.STATE_CONNECTED:
			break;
		}

		Intent intent = new Intent(this, SettingGuideActivity.class);
		intent.putExtra(GUIDE_TYPE, position);
		startActivity(intent);

		super.onListItemClick(l, v, position, id);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
 if (resultCode == Activity.RESULT_OK) {
			// // Get the device MAC address
			String address = data.getExtras().getString(
					DeviceListActivity.EXTRA_DEVICE_ADDRESS);
			// PanoramaAppActivity.getBluetoothManager().requestConnection(address);
			PanoramaAppActivity.PopupConnectDialog(StepByStepActivity.this,
					address);
 }
			 break;
		}
	}
}
