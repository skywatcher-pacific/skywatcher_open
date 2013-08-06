package com.skywatcher.PanoramaApp;

import com.skywatcher.PanoramaApp.ShootScenario.ShootScenarioException;
import com.skywatcher.PanoramaApp.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ScenarioSettingActivity extends Activity {
	public static final String PREVIEW_SCENARIO = "PREVIEW_SCENARIO";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;

	// Views
	private Button button_back, button_save, button_preview;
	private EditText edittext_name, edittext_min_az, edittext_max_az,
			edittext_min_alt, edittext_max_alt;

	private ScenariosDBAdapter dbAdapter; // Database操作用

	private int requestCode; // 判斷為是 ADD or EDIT 劇本

	private ShootScenario scenario; // 目前在編輯的劇本

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.scenario_setting);

		findViews();
		setListeners();
		setDBAdapter();
		getDataFromPreviousActivity();
	}

	// 連結畫面元件與程式內的控制
	private void findViews() {
		edittext_name = (EditText) findViewById(R.id.edittext_name);
		edittext_min_az = (EditText) findViewById(R.id.edittext_min_az);
		edittext_max_az = (EditText) findViewById(R.id.edittext_max_az);
		edittext_min_alt = (EditText) findViewById(R.id.edittext_min_alt);
		edittext_max_alt = (EditText) findViewById(R.id.edittext_max_alt);

		button_back = (Button) findViewById(R.id.button_back);
		button_save = (Button) findViewById(R.id.button_save);
		button_preview = (Button) findViewById(R.id.button_preview);
	}

	// 設定 Button 的 Action
	private void setListeners() {
		button_back.setOnClickListener(backListener);
		button_save.setOnClickListener(saveListener);
		button_preview.setOnClickListener(previewListener);
	}

	private OnClickListener backListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			finish();
		}
	};

	private OnClickListener saveListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 設定劇本的數值。若超出規定的範圍時，則丟出 Exception並通知使用者，結束儲存的動作。
			try {
				setScenarioFromViews();
			} catch (Exception e) {
				Toast.makeText(ScenarioSettingActivity.this,
						"Save failed: " + e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return; // 數值設定有問題，所以不執行資料庫的操作
			}

			// 根據編輯的模式來決定操作資料庫的動作
			switch (requestCode) {
			case SettingGuideActivity.SCENARIO_NEW:
			case QuickSelectActivity.SCENARIO_ADD:
				if (dbAdapter.insert(scenario) == -1) // insert 失敗
					Toast.makeText(ScenarioSettingActivity.this,
							"Add scenario failed.", Toast.LENGTH_SHORT).show();
				else
					// insert 成功
					Toast.makeText(ScenarioSettingActivity.this,
							"Add scenario succeeded.", Toast.LENGTH_SHORT)
							.show();
				break;
			case QuickSelectActivity.SCENARIO_EDIT:
				if (dbAdapter.update(scenario) <= 0) // update 失敗
					Toast.makeText(ScenarioSettingActivity.this,
							"Update scenario failed.", Toast.LENGTH_SHORT)
							.show();
				else
					// update 成功
					Toast.makeText(ScenarioSettingActivity.this,
							"Update scenario succeeded.", Toast.LENGTH_SHORT)
							.show();
				break;
			}
		}
	};

	private OnClickListener previewListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// 設定劇本的數值。若超出規定的範圍時，則丟出 Exception並通知使用者，不接下去拍攝。
			try {
				setScenarioFromViews();
			} catch (Exception e) {
				Toast
						.makeText(ScenarioSettingActivity.this,
								"Preview stoped: " + e.getMessage(),
								Toast.LENGTH_SHORT).show();
				return; // 數值設定有問題，所以不接下去拍攝。
			}

			// 測試有無連線，若無則請user重新連線
			int state = PanoramaAppActivity.getBluetoothManager().getState();
			switch (state) {
			case BluetoothManager.STATE_NONE:
			case BluetoothManager.STATE_CONNECTION_LOST:
				// Launch the DeviceListActivity to see devices and do scan
				Intent serverIntent = new Intent(ScenarioSettingActivity.this,
						DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				return;				
//			case BluetoothManager.STATE_LISTEN:
			case BluetoothManager.STATE_CONNECTING:
				return;

			case BluetoothManager.STATE_CONNECTED:
				break;
			}

			// 開啟拍攝介面
			Intent intent = new Intent(ScenarioSettingActivity.this,
					ShootActivity.class);
			intent.putExtra(PREVIEW_SCENARIO, scenario);
			startActivity(intent);
		}
	};

	private void setDBAdapter() {
		dbAdapter = new ScenariosDBAdapter(this);
		dbAdapter.open();
	}

	// 從之前的 Activity 取得編輯模式及劇本資料
	private void getDataFromPreviousActivity() {
		Bundle extras = getIntent().getExtras();

		if (extras == null) { // Add new Scenario
			requestCode = QuickSelectActivity.SCENARIO_ADD;
			scenario = new ShootScenario();
			return;
		}

		// Edit old Scenario
		if ((scenario = extras.getParcelable(QuickSelectActivity.SCENARIO)) != null) {
			requestCode = QuickSelectActivity.SCENARIO_EDIT;
		} else { // Get new scenario from step by step
			scenario = extras
					.getParcelable(SettingGuideActivity.GUIDE_SCENARIO);
			requestCode = SettingGuideActivity.SCENARIO_NEW;
		}

		edittext_name.setText(scenario.getName());
		edittext_min_az.setText(String.valueOf(scenario.getMinAz()));
		edittext_max_az.setText(String.valueOf(scenario.getMaxAz()));
		edittext_min_alt.setText(String.valueOf(scenario.getMinAlt()));
		edittext_max_alt.setText(String.valueOf(scenario.getMaxAlt()));
	}

	// 從介面中讀入數值來設定劇本物件
	private void setScenarioFromViews() throws NumberFormatException,
			ShootScenarioException {
		scenario.setName(edittext_name.getText().toString());
		scenario.setMinAz(Integer
				.parseInt(edittext_min_az.getText().toString()));
		scenario.setMaxAz(Integer
				.parseInt(edittext_max_az.getText().toString()));
		scenario.setMinAlt(Integer.parseInt(edittext_min_alt.getText()
				.toString()));
		scenario.setMaxAlt(Integer.parseInt(edittext_max_alt.getText()
				.toString()));
		scenario.setInterlace(false);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				PanoramaAppActivity.PopupConnectDialog(
						ScenarioSettingActivity.this, address);
				// PanoramaAppActivity.getBluetoothManager().requestConnection(address);
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}
}
