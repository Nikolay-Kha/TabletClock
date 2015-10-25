package lcf.clock;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

public abstract class Style { // all data in pixels. not scaled
	private static Typeface mFont = null;
	private static DisplayMetrics mMetrics = null;
	private final static float FONT_LINE_SPACING = 1.5f;
	private final static String FONT_NAME = "S7.ttf";
	private static final float FONT_KOEF = 0.80f; // Coefficient to fit size in sp to px
	private static final float FONT_TARGET_ASPECT_RATIO = 0.5859375f;

	public static final String CHAR_CODE_DEGREE = "\u00b0";
	public static final String CHAR_CODE_MINUTE = "\u0027";
	public static final String CHAR_CODE_ALARM = "\u23f0";
	public static final String CHAR_CODE_AM = "\u23f2";
	public static final String CHAR_CODE_PM = "\u23f3";
	public static final String CHAR_CODE_SHORT_ONE = "\u23f4";
	public static final String CHAR_CODE_CLOUD = "\u2601";
	public static final String CHAR_CODE_HUMIDITY = "\u23f1";
	public static final String CHAR_CODE_PRECIPTATION = "\u26C6";
	public static final String CHAR_CODE_TEMPERATURE = "\u23f5";
	public static final String CHAR_CODE_BAROMETER = "\u23f6";
	public static final String CHAR_CODE_WIND = "\u23f7";
	public static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	private static int timeFontSizeWSeconds;
	private static int timeFontSizeWOSeconds;

	@SuppressLint("NewApi")
	protected static void init(Context context) {
		if (mFont == null) {
			mFont = Typeface.createFromAsset(context.getAssets(), FONT_NAME);
		}

		mMetrics = context.getResources().getDisplayMetrics();

		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Display dp = ((WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();

			if (android.os.Build.VERSION.SDK_INT <= 16) {

				try {
					Method mGetRawW = Display.class.getMethod("getRawWidth");
					Method mGetRawH = Display.class.getMethod("getRawHeight");
					mMetrics.widthPixels = (Integer) mGetRawW.invoke(dp);
					mMetrics.heightPixels = (Integer) mGetRawH.invoke(dp);
				} catch (Exception e) {
				}
			} else if (android.os.Build.VERSION.SDK_INT > 16) {
				Point out = new Point();
				dp.getRealSize(out);
				mMetrics.widthPixels = out.x;
				mMetrics.heightPixels = out.y;
			}
		}
	}

	public static DisplayMetrics getDisplayMetrics() {
		return mMetrics;
	}

	public static void applyWeatherView(WeatherView view, float height) {
		view.setTypeface(mFont, FONT_LINE_SPACING);
		view.setSize((int) height);
	}

	public static void applyTextsView(TextView textView, float height) {
		textView.setTypeface(mFont);
		textView.setTextSize(PxToSp(height));
		textView.setSingleLine(true);
		textView.setIncludeFontPadding(false);
	}

	public static void adjustFontSizeForWidth(TextView textView,
			String pattern, int maxWidth) {
		Rect r = new Rect();
		float size = textView.getTextSize();
		do {
			textView.setTextSize(size / mMetrics.scaledDensity);
			textView.getPaint().getTextBounds(pattern, 0, pattern.length(), r);
		} while (size-- > 0.0f && maxWidth <= r.width());

	}

	public static String splitTextForWidth(TextPaint paint, String text,
			int width, int maxLines) {
		String result = "";
		String[] descr = text.split(" ");
		int lines = 0;
		int used = 0;
		Rect tmp = new Rect();
		int i;
		for (i = 0; i < descr.length; i++) {
			paint.getTextBounds(descr[i], 0, descr[i].length(), tmp);
			if (used + tmp.width() < width) {
				result += (used == 0 ? "" : " ") + descr[i];
				used += tmp.width();
			} else {
				result += Style.LINE_SEPARATOR;
				lines++;
				used = 0;
				i--;
				if (lines >= maxLines) {
					break;
				}
			}
		}
		if (i < descr.length) {
			result += "..";
		}
		return result;
	}

	private static int getTimeViewSuitableFontSize(TextView timeView,
			int startsize, int width) {
		int size = startsize;
		int measuredResult;
		String measuredString = timeView.getText().toString();
		int measuredStringLength = measuredString.length();
		do {
			size++;
			timeView.setTextSize(size);
			measuredResult = timeView.getPaint().breakText(measuredString,
					true, width, null);
		} while (measuredStringLength <= measuredResult);
		size--;
		return size;
	}

	private static void applyTimeViewExcludeSize(TextView timeView) {
		timeView.setTypeface(mFont);
		timeView.setPadding(0, 0, 0, 0);
		timeView.setSingleLine(true);
		timeView.setIncludeFontPadding(false);
	}

	public static void applyTimeViewForPrefs(TextView timeView, int width) {
		applyTimeViewExcludeSize(timeView);

		TimeViewUpdater.printCurrentTime(timeView, true, false);

		timeView.setTextSize(getTimeViewSuitableFontSize(timeView, 1, width));
	}

	public static float applyTimeView(TextView timeView) { // return lefts pixels on screen height
		applyTimeViewExcludeSize(timeView);

		TimeViewUpdater.printCurrentTime(timeView, true, true);

		timeFontSizeWSeconds = getTimeViewSuitableFontSize(timeView, 1,
				mMetrics.widthPixels);

		TimeViewUpdater.printCurrentTime(timeView, true, false);
		timeFontSizeWOSeconds = getTimeViewSuitableFontSize(timeView,
				timeFontSizeWSeconds, mMetrics.widthPixels);

		timeView.setTextSize(timeFontSizeWOSeconds);
		/*Log.i("tag", "!!!!!!!!!! "
				+ (mMetrics.heightPixels - size * mMetrics.scaledDensity
						* mMetrics.density));*/

		Rect r = new Rect();
		timeView.getPaint().getTextBounds(timeView.getText().toString(), 0,
				timeView.getText().length(), r);
		return (mMetrics.heightPixels - r.height());

		//timeFontSizeWOSeconds* mMetrics.scaledDensity * mMetrics.density);
	}

	public static float PxToSp(float size) {
		return size / mMetrics.scaledDensity / FONT_KOEF;
	}

	public static float SpToPx(float size) {
		return size * mMetrics.scaledDensity * FONT_KOEF;
	}

	public static int getTimeFontSizeWSeconds() {
		return timeFontSizeWSeconds;
	}

	public static int getTimeFontSizeWOSeconds() {
		return timeFontSizeWOSeconds;
	}

	public static float getScreenAspectRatioViewsCoeficient() {
		float a = mMetrics.heightPixels;
		float b = mMetrics.widthPixels;
		float r = (float) Math.pow(a / b / FONT_TARGET_ASPECT_RATIO, 1.5f);
		if (r < 1.0f) {
			r = 1.0f;
		}
		return r;
	}
}
