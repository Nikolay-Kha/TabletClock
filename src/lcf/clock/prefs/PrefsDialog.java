package lcf.clock.prefs;

import lcf.clock.Style;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

abstract public class PrefsDialog extends Activity {
	private final static float DIALOG_SIZE_WIDTH = 0.9f;
	private final static float DIALOG_SIZE_HEIGHT = 0.9f;
	protected final static int DIALOG_PADDING = 10;
	protected View mRootView;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (isSmallScreen()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
	}

	protected void applySize(int resId) {
		mRootView = findViewById(resId);
		sizeForDialogs();
		mRootView.setPadding(DIALOG_PADDING, DIALOG_PADDING, DIALOG_PADDING,
				DIALOG_PADDING);
	}

	protected int getHeight() {
		return (int) (Style.getDisplayMetrics().heightPixels * (isSmallScreen() ? 1.0f
				: DIALOG_SIZE_HEIGHT));
	}

	protected int getWidth() {
		return (int) (Style.getDisplayMetrics().widthPixels * DIALOG_SIZE_WIDTH);
	}

	protected boolean isSmallScreen() {
		return Style.getDisplayMetrics().heightPixels < 500;
	}

	protected void sizeForDialogs() {
		getWindow().setLayout(getWidth(), getHeight());
	}

}
