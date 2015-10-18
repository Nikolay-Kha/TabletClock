package lcf.clock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.text.format.DateFormat;
import android.widget.TextView;

public class TimeViewUpdater {
	public enum DOT_MODE {
		DOT_NO, DOT_FLASH, DOT_PERMANENT
	}

	private final TextView mTimeView;
	private Timer mTimer = null;
	private final Handler mHandler = new Handler();
	private static DOT_MODE mDotMode = DOT_MODE.DOT_FLASH;
	private boolean mSecondsVisible = false;
	private long lastDay = 0;
	private final Runnable mDateChangedRunnable;
	private static boolean is24hours = true;

	public TimeViewUpdater(TextView view, Runnable dateChangedCallback) {
		mDateChangedRunnable = dateChangedCallback;
		mTimeView = view;
		retrive24Format();
	}

	// make support for am/pm !!!
	// boolean b = DateFormat.is24HourFormat(context);

	static Date printCurrentTime(TextView timeView, boolean counter,
			boolean seconds) {
		Date date = new Date();
		SimpleDateFormat sdf;
		String dot = " ";
		switch (mDotMode) {
		case DOT_FLASH:
			dot = counter ? ":" : " ";
			break;
		case DOT_NO:
			dot = " ";
			break;
		case DOT_PERMANENT:
			dot = ":";
			break;
		}
		String result;
		String s = seconds ? (dot + "ss") : "";
		if (is24hours) {
			sdf = new SimpleDateFormat("HH" + dot + "mm" + s,
					Locale.getDefault());
			result = sdf.format(date);
		} else {
			sdf = new SimpleDateFormat("ahh" + dot + "mm" + s,
					Locale.getDefault());
			result = sdf.format(date);
			if (result.charAt(0) == 'A') { // optimization :)
				result = Style.CHAR_CODE_AM + result.substring(2);
			} else {
				result = Style.CHAR_CODE_PM + result.substring(2);
			}
			if (result.charAt(1) == '0') {
				result = result.charAt(0) + " " + result.substring(2);
			}
			if (result.charAt(1) == '1') {
				result = result.charAt(0) + Style.CHAR_CODE_SHORT_ONE
						+ result.substring(2);
			}
		}
		timeView.setText(result);
		return date;
	}

	public boolean retrive24Format() { // return true if changed
		boolean old = is24hours;
		is24hours = DateFormat.is24HourFormat(mTimeView.getContext());
		return old != is24hours;
	}

	public void start() {
		if (mTimer != null) {
			return;
		}
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			private boolean mCounter;

			@Override
			public void run() {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						Date date = printCurrentTime(mTimeView, mCounter,
								mSecondsVisible);

						mCounter = !mCounter;
						if (mCounter) {
							long day = date.getTime() / (24 * 3600 * 1000); // day since Jan. 1, 1970
							if (mDateChangedRunnable != null && lastDay != 0
									&& lastDay != day) {
								mHandler.post(mDateChangedRunnable);
							}
							lastDay = day;
						}
					}
				});
			}
		}, 10, 500);
	}

	public void stop() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	public static void setDotMode(DOT_MODE mode) {
		mDotMode = mode;
	}

	public void setSecondsVisible(boolean visible) {
		mSecondsVisible = visible;
		if (mSecondsVisible) {
			mTimeView.setTextSize(Style.getTimeFontSizeWSeconds());
		} else {
			mTimeView.setTextSize(Style.getTimeFontSizeWOSeconds());
		}
	}

}
