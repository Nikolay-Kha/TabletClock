package lcf.clock.prefs;

import lcf.clock.R;
import lcf.weather.WeatherUnits;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preference extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceChangeListener,
		OnPreferenceClickListener {
	private SharedPreferences mSharedPreferences;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WeatherUnits.setResourceContext(getApplicationContext());
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		addPreferencesFromResource(R.xml.preference);

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		ListPreference updateList = (ListPreference) findPreference(getString(R.string.key_update));
		// warning!!! hardcode
		CharSequence updatePeriods[] = new CharSequence[5];
		CharSequence updatePeriodsValues[] = new CharSequence[5];
		updatePeriods[0] = "30 " + getString(R.string.mins);
		updatePeriodsValues[0] = "30";
		updatePeriods[1] = "1 " + getString(R.string.hour);
		updatePeriodsValues[1] = "60";
		updatePeriods[2] = "2 " + getString(R.string.hours);
		updatePeriodsValues[2] = "120";
		updatePeriods[3] = "4 " + getString(R.string.hours);
		updatePeriodsValues[3] = "240";
		updatePeriods[4] = "6 " + getString(R.string.hours6);
		updatePeriodsValues[4] = "360";
		updateList.setEntries(updatePeriods);
		updateList.setEntryValues(updatePeriodsValues);
		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_update));

		// fill another list preferences
		ListPreference temperatureUnitsList = (ListPreference) findPreference(getString(R.string.key_tunit));
		int count = WeatherUnits.TemperatureUnits.values().length;
		CharSequence keys1[] = new CharSequence[count];
		CharSequence values1[] = new CharSequence[count];
		for (int i = 0; i < count; i++) {
			keys1[i] = WeatherUnits
					.getTemperatureUnitsString(WeatherUnits.TemperatureUnits
							.values()[i]);
			values1[i] = String.valueOf(i);
		}
		temperatureUnitsList.setEntries(keys1);
		temperatureUnitsList.setEntryValues(values1);
		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_tunit));

		ListPreference pressureUnitsList = (ListPreference) findPreference(getString(R.string.key_punit));
		int count2 = WeatherUnits.PressureUnits.values().length;
		CharSequence keys2[] = new CharSequence[count2];
		CharSequence values2[] = new CharSequence[count2];
		for (int i = 0; i < count2; i++) {
			keys2[i] = WeatherUnits
					.getPressureUnitsString(WeatherUnits.PressureUnits.values()[i]);
			values2[i] = String.valueOf(i);
		}
		pressureUnitsList.setEntries(keys2);
		pressureUnitsList.setEntryValues(values2);
		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_punit));

		ListPreference speedUnitsList = (ListPreference) findPreference(getString(R.string.key_sunit));
		int count3 = WeatherUnits.SpeedUnits.values().length;
		CharSequence keys3[] = new CharSequence[count3];
		CharSequence values3[] = new CharSequence[count3];
		for (int i = 0; i < count3; i++) {
			keys3[i] = WeatherUnits.getSpeedUnitsString(WeatherUnits.SpeedUnits
					.values()[i]);
			values3[i] = String.valueOf(i);
		}
		speedUnitsList.setEntries(keys3);
		speedUnitsList.setEntryValues(values3);
		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_sunit));

		// warning!!! hardcode
		ListPreference blevelsList = (ListPreference) findPreference(getString(R.string.key_blevel));
		CharSequence keys4[] = new CharSequence[9];
		CharSequence values4[] = new CharSequence[9];
		keys4[0] = getString(R.string.no_keep);
		values4[0] = "-1";
		keys4[1] = getString(R.string.keep_plugged);
		values4[1] = "101";
		int counter = 2;
		for (int i = 90; i >= 30; i -= 10) {
			keys4[counter] = getString(R.string.battery_till) + " " + i + " "
					+ getString(R.string.percents);
			values4[counter++] = String.valueOf(i);
		}
		blevelsList.setEntries(keys4);
		blevelsList.setEntryValues(values4);
		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_blevel));

		// warning!!! hardcode
		ListPreference brightnessList = (ListPreference) findPreference(getString(R.string.key_brightness));
		boolean camera = false;
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			if (Camera.getNumberOfCameras() > 0) {
				camera = true;
			}
		} else {
			camera = true;
		}
		CharSequence keys5[] = new CharSequence[camera ? 3 : 2];
		CharSequence values5[] = new CharSequence[camera ? 3 : 2];
		keys5[0] = getString(R.string.cat2_key_system);
		values5[0] = getString(R.string.value_brightness_system);
		keys5[1] = getString(R.string.cat2_brightness_manual);
		values5[1] = getString(R.string.value_brightness_manual);
		if (camera) {
			keys5[2] = getString(R.string.cat2_brightness_camera);
			values5[2] = getString(R.string.value_brightness_camera);
		}
		brightnessList.setEntries(keys5);
		brightnessList.setEntryValues(values5);
		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_brightness));
		brightnessList.setOnPreferenceChangeListener(this);

		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_color));
		findPreference(getString(R.string.key_color))
				.setOnPreferenceClickListener(this);

		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_background_color));
		findPreference(getString(R.string.key_background_color))
				.setOnPreferenceClickListener(this);

		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_city));
		findPreference(getString(R.string.key_city))
				.setOnPreferenceClickListener(this);

		findPreference(getString(R.string.key_show_system))
				.setOnPreferenceClickListener(this);

		// warning!!! hardcode
		ListPreference dotPreference = (ListPreference) findPreference(getString(R.string.key_dot));
		CharSequence keys6[] = new CharSequence[3];
		CharSequence values6[] = new CharSequence[3];
		keys6[0] = getString(R.string.cat3_dot_no);
		values6[0] = getString(R.string.value_dot_no);
		keys6[1] = getString(R.string.cat3_dot_flash);
		values6[1] = getString(R.string.value_dot_flash);
		keys6[2] = getString(R.string.cat3_dot_perm);
		values6[2] = getString(R.string.value_dot_perm);
		dotPreference.setEntries(keys6);
		dotPreference.setEntryValues(values6);
		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_dot));

		// warning!!! hardcode
		ListPreference orientationPreference = (ListPreference) findPreference(getString(R.string.key_orientation));
		CharSequence keys7[] = new CharSequence[6];
		CharSequence values7[] = new CharSequence[6];
		keys7[0] = getString(R.string.cat2_key_system);
		values7[0] = String
				.valueOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		keys7[1] = getString(R.string.cat2_orientation_p);
		values7[1] = String.valueOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		keys7[2] = getString(R.string.cat2_orientation_l);
		values7[2] = String.valueOf(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		keys7[3] = getString(R.string.cat2_orientation_rp);
		values7[3] = String
				.valueOf(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
		keys7[4] = getString(R.string.cat2_orientation_rl);
		values7[4] = String
				.valueOf(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		keys7[5] = getString(R.string.cat2_orientation_sensor);
		values7[5] = String.valueOf(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		orientationPreference.setEntries(keys7);
		orientationPreference.setEntryValues(values7);
		onSharedPreferenceChanged(mSharedPreferences,
				getString(R.string.key_orientation));

	}

	@Override
	protected void onDestroy() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		android.preference.Preference pref = findPreference(key);

		if (key.equals(getString(R.string.key_brightness))
				|| key.equals(getString(R.string.key_brightness_manual_value))) {
			ListPreference listPref = (ListPreference) findPreference(getString(R.string.key_brightness));
			if (listPref.getValue().equals(
					getString(R.string.value_brightness_manual))) {
				listPref.setSummary(listPref.getEntry()
						+ " "
						+ (int) (100.0f * BrightnessDialog.getBrighnessLevel(
								sharedPreferences, this)) + " "
						+ getString(R.string.percents));
			} else {
				listPref.setSummary(listPref.getEntry());
			}
		} else if (pref != null) {
			if (pref instanceof ListPreference) {
				ListPreference listPref = (ListPreference) pref;
				listPref.setSummary(listPref.getEntry());
			} else if (key.equals(getString(R.string.key_color))) {
				pref.setSummary(ColorDialog.getTextColorString(
						sharedPreferences, this));
			} else if (key.equals(getString(R.string.key_background_color))) {
				pref.setSummary(ColorDialog.getBackgroundColorString(
						sharedPreferences, this));
			} else if (key.equals(getString(R.string.key_city))) {
				pref.setSummary(CityDialog.getCityName(sharedPreferences, this));
			}
		}
	}

	@Override
	public boolean onPreferenceChange(android.preference.Preference preference,
			Object newValue) {
		if (preference instanceof ListPreference) {
			if (getString(R.string.value_brightness_manual).equals(newValue)) {
				final Intent intent = new Intent(this, BrightnessDialog.class);
				startActivity(intent);
			}
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(android.preference.Preference preference) {
		if (preference.getKey().equals(getString(R.string.key_color))) {
			final Intent intent = new Intent(this, ColorDialog.class);
			intent.putExtra(ColorDialog.EXTRA_BACKGROUND_BOOLEAN, false);
			startActivity(intent);
		} else if (preference.getKey().equals(
				getString(R.string.key_background_color))) {
			final Intent intent = new Intent(this, ColorDialog.class);
			intent.putExtra(ColorDialog.EXTRA_BACKGROUND_BOOLEAN, true);
			startActivity(intent);
		} else if (preference.getKey().equals(getString(R.string.key_city))) {
			final Intent intent = new Intent(this, CityDialog.class);
			startActivity(intent);
		} else if (preference.getKey().equals(
				getString(R.string.key_show_system))) {
			final Intent intent = new Intent(
					android.provider.Settings.ACTION_DATE_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		return true;
	}
}
