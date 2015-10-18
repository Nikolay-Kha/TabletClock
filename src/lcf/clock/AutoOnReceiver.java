package lcf.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class AutoOnReceiver extends BroadcastReceiver {
	static final String EXTRA_AUTO_ON = "lcf.clock.AutoOn";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (isAutorun(context)) {
			final Intent i = new Intent(context, ClockActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra(EXTRA_AUTO_ON, true);
			context.startActivity(i);
		}
	}

	private static boolean isAutorun(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(context.getString(R.string.key_autorun), false);
	}

	static void prepareFlagsIfNeed(Window window, Context context) {
		if (isAutorun(context)) {
			window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}
	}

	private static void clearFlags(Window window) {
		window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	}

	static void clearFlagsIfNeed(Window window, Intent intent) {
		if (!intent.getBooleanExtra(AutoOnReceiver.EXTRA_AUTO_ON, false)) {
			clearFlags(window);
		}

	}
}
