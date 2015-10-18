package lcf.clock.prefs;

import lcf.clock.R;
import lcf.clock.Style;
import lcf.clock.TimeViewUpdater;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

abstract class TimePrefsDialogs extends PrefsDialog implements OnClickListener,
		OnSeekBarChangeListener {
	private TimeViewUpdater mUpdater = null;
	protected TextView mTimeView;
	protected SharedPreferences mSharedPreferences;
	protected int mTextColor;
	protected int mBackgroundColor;

	protected void reservePlaceForSeekBarsLevels(TextView textView) {
		Rect b = new Rect();
		textView.getPaint().getTextBounds("888", 0, 3, b);
		textView.setMinimumWidth(b.width());
	}

	@Override
	public void onClick(View v) {
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mUpdater != null) {
			mUpdater.start();
		}
		mTextColor = getTextColorInt(mSharedPreferences, this);
		mBackgroundColor = getBackgroundColorInt(mSharedPreferences, this);
		mTimeView.setTextColor(mTextColor);
		mRootView.setBackgroundColor(mBackgroundColor);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mUpdater != null) {
			mUpdater.stop();
		}
		commitData();
	}

	abstract protected void commitData();

	protected void initTimeViewAndButtonsPreview(int resId, int buttonsResId) {
		mTimeView = (TextView) findViewById(resId);
		mUpdater = new TimeViewUpdater(mTimeView, null);
		mUpdater.start();
		Style.applyTimeViewForPrefs(mTimeView, getWidth() - DIALOG_PADDING * 6);
		if (isSmallScreen()) {
			findViewById(buttonsResId).setVisibility(View.GONE);
			mTimeView.setOnClickListener(this);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}

	public static String getBackgroundColorString(
			SharedPreferences sharedPreferences, Context context) {
		int color = getBackgroundColorInt(sharedPreferences, context);
		return colorToText(color);
	}

	public static int getBackgroundColorInt(
			SharedPreferences sharedPreferences, Context context) {
		return sharedPreferences.getInt(
				context.getString(R.string.key_background_color), 0xFF000000);
	}

	public static int getTextColorInt(SharedPreferences sharedPreferences,
			Context context) {
		return sharedPreferences.getInt(context.getString(R.string.key_color),
				0xFF00FF00);
	}

	private static String colorToText(int c) {
		int color = c & 0xFFFFFF;
		int r = color / 0x10000;
		int g = (color & 0xFF00) / 0x100;
		int b = color & 0xFF;
		return String.format("#%02X%02X%02X", r, g, b);
	}

	public static String getTextColorString(
			SharedPreferences sharedPreferences, Context context) {
		int color = getTextColorInt(sharedPreferences, context);
		return colorToText(color);
	}

}
