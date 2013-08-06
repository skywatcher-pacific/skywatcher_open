package com.skywatcher.PanoramaApp;

import com.skywatcher.api.AXISID;
import com.skywatcher.api.AstroMisc;
import com.skywatcher.api.Mount;
import com.skywatcher.api.MountControlException;
import com.skywatcher.PanoramaApp.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity {
	// Default value
	private static final String DEFAULT_FOV_X = "30";
	private static final String DEFAULT_FOV_Y = "20";
	private static final String DEFAULT_OVERLAP = "20";
	private static final String DEFAULT_STEP_DELAY = "1";
	private static final String DEFAULT_TRIGGER_DELAY = "0";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;

	public void showPreference() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
		SharedPreferences.Editor editor = settings.edit();

		// FovX
		EditTextPreference etp = (EditTextPreference) findPreference(getText(R.string.key_fov_x));
		String fovX;
		if ((fovX = settings.getString(getString(R.string.key_fov_x), null)) == null) {
			editor.putString(getString(R.string.key_fov_x), DEFAULT_FOV_X);
			fovX = DEFAULT_FOV_X;
		}
		etp.setSummary(fovX);

		// FovY
		etp = (EditTextPreference) findPreference(getText(R.string.key_fov_y));
		String fovY;
		if ((fovY = settings.getString(getString(R.string.key_fov_y), null)) == null) {
			editor.putString(getString(R.string.key_fov_y), DEFAULT_FOV_Y);
			fovY = DEFAULT_FOV_Y;
		}
		etp.setSummary(fovY);

		// Overlap
		etp = (EditTextPreference) findPreference(getText(R.string.key_overlap));
		String overlap;
		if ((overlap = settings.getString(getString(R.string.key_overlap), null)) == null) {
			editor.putString(getString(R.string.key_overlap), DEFAULT_OVERLAP);
			overlap = DEFAULT_OVERLAP;
		}
		etp.setSummary(overlap);

		// Step Delay
		etp = (EditTextPreference) findPreference(getText(R.string.key_step_delay));
		String stepDealy;
		if ((stepDealy = settings.getString(getString(R.string.key_step_delay), null)) == null) {
			editor.putString(getString(R.string.key_step_delay), DEFAULT_STEP_DELAY);
			stepDealy = DEFAULT_STEP_DELAY;
		}
		etp.setSummary(stepDealy);

		// Trigger Delay
		etp = (EditTextPreference) findPreference(getText(R.string.key_trigger_delay));
		String triggerDealy;
		if ((triggerDealy = settings.getString(getString(R.string.key_trigger_delay), null)) == null) {
			editor.putString(getString(R.string.key_trigger_delay), DEFAULT_TRIGGER_DELAY);
			triggerDealy = DEFAULT_TRIGGER_DELAY;
		}
		etp.setSummary(triggerDealy);

		// Write default value to preference
		editor.commit();

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.setting);

		showPreference();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
		SharedPreferences.Editor editor = settings.edit();

		// FovX
		EditTextPreference etp = (EditTextPreference) findPreference(getText(R.string.key_fov_x));
		etp.setOnPreferenceChangeListener(fov_OnPreferenceChangeListener);

		// FovY
		etp = (EditTextPreference) findPreference(getText(R.string.key_fov_y));
		etp.setOnPreferenceChangeListener(fov_OnPreferenceChangeListener);

		// Overlap
		etp = (EditTextPreference) findPreference(getText(R.string.key_overlap));
		etp.setOnPreferenceChangeListener(overlap_OnPreferenceChangeListener);

		// Step Delay
		etp = (EditTextPreference) findPreference(getText(R.string.key_step_delay));
		etp.setOnPreferenceChangeListener(delay_OnPreferenceChangeListener);

		// Trigger Delay
		etp = (EditTextPreference) findPreference(getText(R.string.key_trigger_delay));
		etp.setOnPreferenceChangeListener(delay_OnPreferenceChangeListener);

		// Connect
		Preference connectPref = (Preference) findPreference(getText(R.string.connectPref));
		connectPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				// Launch the DeviceListActivity to see devices and do
				// scan
				Intent serverIntent = new Intent(SettingActivity.this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

				return true;
			}
		});

		Preference setAzPref = (Preference) findPreference(getText(R.string.setting_az));
		setAzPref.setOnPreferenceChangeListener(CurrentPosition_OnPreferenceChangeListener);

		Preference setAltPref = (Preference) findPreference(getText(R.string.setting_alt));
		setAltPref.setOnPreferenceChangeListener(CurrentPosition_OnPreferenceChangeListener);
	}

	@Override
	public void onResume() {
		super.onResume();

		showPreference();
	}

	private OnPreferenceChangeListener CurrentPosition_OnPreferenceChangeListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String input = newValue.toString();
			if (input != null && !input.equals("") && Integer.parseInt(input) >= -180 && Integer.parseInt(input) <= 180) {
				// preference.setSummary(input);
				try {
					BluetoothManager manager = PanoramaAppActivity.getBluetoothManager();
					if (manager.getState() == BluetoothManager.STATE_CONNECTED) {
						Mount mountControl = PanoramaAppActivity.getMountControl();
						if (preference.getKey() == getResources().getText(R.string.setting_az)) {

							mountControl.MCSetAxisPosition(AXISID.AXIS1, AstroMisc.DegToRad(Integer.parseInt(input)));
						} else if (preference.getKey() == getResources().getText(R.string.setting_alt)) {
							mountControl.MCSetAxisPosition(AXISID.AXIS2, AstroMisc.DegToRad(Integer.parseInt(input)));
						}
						return true;
					}
					Toast.makeText(SettingActivity.this, R.string.error_not_connected, Toast.LENGTH_SHORT).show();
					return false;
					
				} catch (MountControlException e) {
					Toast.makeText(SettingActivity.this, R.string.error_not_connected, Toast.LENGTH_SHORT).show();
					return false;
				}
			} else {
				Toast.makeText(SettingActivity.this, R.string.error_set_position, Toast.LENGTH_SHORT).show();
				return false;
			}
		}
	};

	private OnPreferenceChangeListener fov_OnPreferenceChangeListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String input = newValue.toString();
			if (input != null && !input.equals("") && Integer.parseInt(input) >= 10 && Integer.parseInt(input) <= 100) {
				preference.setSummary(input);
				return true;
			} else {
				Toast.makeText(SettingActivity.this, R.string.error_fov, Toast.LENGTH_SHORT).show();
				return false;
			}
		}
	};

	private OnPreferenceChangeListener overlap_OnPreferenceChangeListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String input = newValue.toString();
			if (input != null && !input.equals("") && Integer.parseInt(input) >= 20 && Integer.parseInt(input) <= 50) {
				preference.setSummary(input);
				return true;
			} else {
				Toast.makeText(SettingActivity.this, R.string.error_overlap, Toast.LENGTH_SHORT).show();
				return false;
			}
		}
	};

	private OnPreferenceChangeListener delay_OnPreferenceChangeListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String input = newValue.toString();
			if (input != null && !input.equals("") && Integer.parseInt(input) >= 1 && Integer.parseInt(input) <= 10) {
				preference.setSummary(input);
				return true;
			} else {
				Toast.makeText(SettingActivity.this, R.string.error_delay, Toast.LENGTH_SHORT).show();
				return false;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				PanoramaAppActivity.PopupConnectDialog(SettingActivity.this, address);
//				PanoramaAppActivity.getBluetoothManager().requestConnection(address);
				

				
				// Generate ProgressDialog
				
			}
			break;
		}
	}
}
