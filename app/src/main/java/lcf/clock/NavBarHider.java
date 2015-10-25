package lcf.clock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;

public class NavBarHider {
	private final Activity mActivity;
	private final Handler mHandler = new Handler();
	private static final int HIDE_TIMEOUT_MS = 1000;
	private static final int RESTORE_HIDE_TIMEOUT_MS = 3500;
	private static boolean hasNavigationBar = false;
	private boolean isRunning = true;
	private final Runnable mWorkRunnable = new Runnable() {

		@SuppressLint("NewApi")
		@Override
		public void run() {
			if (!isRunning) {
				return;
			}
			if (hasNavigationBar && android.os.Build.VERSION.SDK_INT >= 11) {
				mActivity
						.getWindow()
						.getDecorView()
						.setSystemUiVisibility(
								mActivity.getWindow().getDecorView()
										.getSystemUiVisibility()
										| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		}
	};

	public static int getNavigationBarHeight(Context context) {
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}

	@SuppressLint("NewApi")
	public NavBarHider(Activity activity, final View rootView) {
		mActivity = activity;

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			rootView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int visibility) {
					if (!isRunning
							|| (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
						return;
					}
					hide(RESTORE_HIDE_TIMEOUT_MS);
				}
			});
		}
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					private final View content = rootView;

					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						if (android.os.Build.VERSION.SDK_INT >= 16) {
							content.getViewTreeObserver()
									.removeOnGlobalLayoutListener(this);
						} else {
							content.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						}
						hasNavigationBar = content.getHeight() < Style
								.getDisplayMetrics().heightPixels;
						if (hasNavigationBar && isRunning) {
							hide();
						}
					}
				});
	}

	private void hide(int ms) {
		if (!isRunning) {
			return;
		}
		mHandler.removeCallbacks(mWorkRunnable);
		mHandler.postDelayed(mWorkRunnable, ms);
	}

	public void hide() {
		hide(HIDE_TIMEOUT_MS);
	}

	public void hideDelayed() {
		hide(RESTORE_HIDE_TIMEOUT_MS);
	}

	@SuppressLint("NewApi")
	public void show() {
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mActivity
					.getWindow()
					.getDecorView()
					.setSystemUiVisibility(
							mActivity.getWindow().getDecorView()
									.getSystemUiVisibility()
									& (~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION));
		}
	}

	public void stop() {
		isRunning = false;
		show();
	}

	public void start() {
		isRunning = true;
		hide();
	}

}
