package lcf.clock.prefs;

import lcf.clock.CameraAsLightSensor;
import lcf.clock.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class BrightnessDialog extends TimePrefsDialogs {
	private TextView mLevel;
	private float mBrightness = 0.5f;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.brightness_dialog);
		setTitle(R.string.cat2_brightness);
		applySize(R.id.dbroot);
		initTimeViewAndButtonsPreview(R.id.dtimeView1, R.id.button1Cl);

		findViewById(R.id.button1Cl).setOnClickListener(this);
		SeekBar sb = (SeekBar) findViewById(R.id.brightnessSeek);
		sb.setMax(100);
		sb.setOnSeekBarChangeListener(this);
		mLevel = (TextView) findViewById(R.id.dbrightnesslevel);
		reservePlaceForSeekBarsLevels(mLevel);

		mBrightness = getBrighnessLevel(mSharedPreferences, this);
		sb.setProgress((int) (mBrightness * 100.0f));

	}

	public static float getBrighnessLevel(SharedPreferences sharedPreferences,
			Context context) {
		return sharedPreferences.getFloat(
				context.getString(R.string.key_brightness_manual_value), 0.5f);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		mBrightness = progress / 100.0f;
		CameraAsLightSensor.applyManualBrightness(getWindow(), mBrightness);
		mLevel.setText(String.valueOf(progress));
	}

	@Override
	protected void commitData() {
		mSharedPreferences
				.edit()
				.putFloat(getString(R.string.key_brightness_manual_value),
						mBrightness).commit();
	}
}
