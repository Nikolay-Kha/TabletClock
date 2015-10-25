package lcf.clock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lcf.weather.Moon;
import lcf.weather.Weather;
import lcf.weather.WeatherUnits;
import lcf.weather.WeatherUnits.PressureUnits;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherView extends LinearLayout {
	private final View mIconBrightnessView;
	private final TextView mWhenView;
	private final ImageView mMoonView;
	private final ImageView mIconView;
	private final TextView mDescriptionView;
	private final static int ICON_PADDING = 5;
	private int mSize = 120;
	private Typeface mTypeface = null;
	private float mLineSpaccingMulti = 1.0f;
	private static float ICON_ASPECT_RATIO = 0.0f;
	private int mIconViewWidth;
	private int mIconViewHeight;
	private float mFontSize;

	public WeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeatherUnits.setResourceContext(context.getApplicationContext());
		if (ICON_ASPECT_RATIO == 0.0f) {
			Drawable r = getResources().getDrawable(R.drawable.p01d);
			ICON_ASPECT_RATIO = (float) r.getIntrinsicHeight()
					/ (float) r.getIntrinsicWidth();
			mIconViewWidth = r.getIntrinsicWidth();
			mIconViewHeight = r.getIntrinsicHeight();
		}
		setOrientation(HORIZONTAL);
		LinearLayout layoutV1 = new LinearLayout(getContext());
		FrameLayout fLayout = new FrameLayout(getContext());
		mIconBrightnessView = new View(getContext());
		mWhenView = new TextView(getContext());
		mMoonView = new ImageView(getContext());
		mIconView = new ImageView(getContext());
		mDescriptionView = new TextView(getContext());

		layoutV1.setOrientation(VERTICAL);
		mWhenView.setGravity(Gravity.CENTER_HORIZONTAL);
		mMoonView.setScaleType(ScaleType.MATRIX);
		mIconView.setScaleType(ScaleType.MATRIX);
		setPadding(0, 0, 0, 0);
		layoutV1.setPadding(0, 0, 0, 0);
		mMoonView.setPadding(0, ICON_PADDING, ICON_PADDING, 0);
		mIconView.setPadding(0, ICON_PADDING, ICON_PADDING, 0);
		mIconBrightnessView.setPadding(0, ICON_PADDING, ICON_PADDING, 0);

		fLayout.addView(mMoonView);
		fLayout.addView(mIconView);
		fLayout.addView(mIconBrightnessView);

		layoutV1.addView(mWhenView);
		layoutV1.addView(fLayout);

		addView(layoutV1);
		addView(mDescriptionView);

		mFontSize = mWhenView.getTextSize();
	}

	public void setWeather(Weather weather) {
		String when = "";
		if (weather.getDatePeriodHours() == 0) {
			when = getContext().getText(R.string.now).toString();
		} else if (weather.getDatePeriodHours() >= 24) {
			if (DateUtils.isToday(weather.getDate().getTime())) {
				when = getResources().getString(R.string.today);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("d, EEEE",
						Locale.getDefault());
				when = sdf.format(weather.getDate());
			}
		} else if (weather.getDatePeriodHours() < 12) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(weather.getDate());
			int h = cal.get(Calendar.HOUR_OF_DAY);
			when = cal.get(Calendar.DAY_OF_MONTH) + ", ";
			if (h < 6) {
				when += getContext().getText(R.string.night);
			} else if (h >= 6 && h < 12) {
				when += getContext().getText(R.string.morning);
			} else if (h >= 12 && h < 18) {
				when += getContext().getText(R.string.day);
			}
			if (h >= 18) {
				when += getContext().getText(R.string.evening);
			}
		}
		when += ":";
		mWhenView.setTextSize(mFontSize);
		Style.adjustFontSizeForWidth(mWhenView, when, mIconViewWidth
				+ ICON_PADDING * 2);
		mWhenView.setText(when);

		setIcon(weather);

		mDescriptionView.setText(getDesribeString(weather));
	}

	private int getWeatherIconResId(Weather w) {
		// codes http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
		String weather = w.getWeatherIcon();
		if (weather.equals("01d")) {
			return R.drawable.p01d;
		} else if (weather.equals("01n")) {
			return R.drawable.p01n;
		} else if (weather.equals("02d")) {
			return R.drawable.p02d;
		} else if (weather.equals("02n")) {
			return R.drawable.p02n;
		} else if (weather.equals("03d") || weather.equals("03n")) {
			return R.drawable.p03;
		} else if (weather.equals("04d") || weather.equals("04n")) {
			return R.drawable.p04;
		} else if (weather.equals("09d") || weather.equals("09n")) {
			return R.drawable.p09;
		} else if (weather.equals("10d") || weather.equals("10n")) {
			if (w.getWeatherId() == 500) {
				return R.drawable.p08;
			}
			return R.drawable.p10;
		} else if (weather.equals("11d") || weather.equals("11n")) {
			return R.drawable.p11;
		} else if (weather.equals("13d") || weather.equals("13n")) {
			if (w.getWeatherId() == 602 || w.getWeatherId() == 621
					|| w.getWeatherId() == 622) {
				return R.drawable.p14;
			} else if (w.getWeatherId() == 600 || w.getWeatherId() == 620) {
				return R.drawable.p12;
			} else {
				return R.drawable.p13;
			}
		} else if (weather.equals("50d") || weather.equals("50n")) {
			return R.drawable.p50;
		}
		return 0;
	}

	private int getMoonIconResId(Moon.MoonPhases phase,
			boolean northernHemisphere) {
		switch (phase) {
		case MOON_NEW:
			return R.drawable.m1;
		case MOON_EVENING_CRESCENT:
			if (northernHemisphere) {
				return R.drawable.m2;
			} else {
				return R.drawable.m8;
			}
		case MOON_FIRST_QUARTER:
			if (northernHemisphere) {
				return R.drawable.m3;
			} else {
				return R.drawable.m7;
			}
		case MOON_WAXING_GIBBOUS:
			if (northernHemisphere) {
				return R.drawable.m4;
			} else {
				return R.drawable.m6;
			}
		case MOON_FULL:
			return R.drawable.m5;
		case MOON_WANING_GIBBOUS:
			if (northernHemisphere) {
				return R.drawable.m6;
			} else {
				return R.drawable.m4;
			}
		case MOON_LAST_QUARTER:
			if (northernHemisphere) {
				return R.drawable.m7;
			} else {
				return R.drawable.m3;
			}
		case MOON_MORNING_CRESCENT:
			if (northernHemisphere) {
				return R.drawable.m8;
			} else {
				return R.drawable.m2;
			}
		default:
			return 0;
		}
	}

	private Drawable prepareIcon(int resId) {
		Drawable dr = getResources().getDrawable(resId);
		Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
		return new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
				bitmap, mIconViewWidth, mIconViewHeight, true));
	}

	private void setIcon(Weather weather) {
		int iconResId = (weather == null) ? 0 : getWeatherIconResId(weather);
		if (iconResId != 0) {
			if (iconResId == R.drawable.p01n || iconResId == R.drawable.p02n) {
				mMoonView.setVisibility(View.VISIBLE);
				boolean isNorthernHemisphere = weather.getCity().getLatitude() >= 0.0f;
				int moonIconResId = getMoonIconResId(
						Moon.get(weather.getDate()), isNorthernHemisphere);
				if (moonIconResId != 0) {
					mMoonView.setImageDrawable(prepareIcon(moonIconResId));
				} else {
					mMoonView.setVisibility(View.GONE);
				}
			} else {
				mMoonView.setVisibility(View.GONE);
			}
			mIconView.setVisibility(View.VISIBLE);
			mIconView.setImageDrawable(prepareIcon(iconResId));
		} else {
			mIconView.setVisibility(View.GONE);
			mMoonView.setVisibility(View.GONE);
		}
	}

	private String getDesribeString(Weather weather) {
		String desribe = "";

		if (weather.getDatePeriodHours() > 0) {
			desribe += Style.CHAR_CODE_TEMPERATURE + " ";
			if (Math.round(weather.getTemperatureMin()) == Math.round(weather
					.getTemperatureMax())
					|| (weather.getDatePeriodHours() != 0 && weather
							.getDatePeriodHours() < 12)) {
				desribe += weather.getTemperatureString()
						+ Style.CHAR_CODE_DEGREE
						+ WeatherUnits.getTemperatureUnitsString();
			} else {
				desribe += weather.getTemperatureMinString() + ".."
						+ weather.getTemperatureMaxString()
						+ Style.CHAR_CODE_DEGREE
						+ WeatherUnits.getTemperatureUnitsString();
			}
		} else {
			if (weather.getWeatherDescription() == null) {
				desribe += Style.LINE_SEPARATOR;
			} else {
				String res = Style.splitTextForWidth(
						mDescriptionView.getPaint(),
						weather.getWeatherDescription(), mIconViewWidth, 2);
				desribe += res;
				if (res.indexOf(Style.LINE_SEPARATOR) < 0) {
					desribe += Style.LINE_SEPARATOR;
				}
			}
		}
		desribe += Style.LINE_SEPARATOR;

		desribe += Style.CHAR_CODE_WIND + " "
				+ Math.round(weather.getWindSpeed()) + " "
				+ WeatherUnits.getSpeedUnitsString() + " "
				+ describeWindDirection(weather.getWindDirection());
		desribe += Style.LINE_SEPARATOR;

		String tmp;
		if (WeatherUnits.getPressureUnits() == PressureUnits.inHg) {
			tmp = FloatToString(weather.getPressure());
		} else {
			tmp = String.valueOf(Math.round(weather.getPressure()));
		}
		desribe += Style.CHAR_CODE_BAROMETER + " " + tmp + " "
				+ WeatherUnits.getPressureUnitsString();
		desribe += Style.LINE_SEPARATOR;

		desribe += Style.CHAR_CODE_CLOUD + +Math.round(weather.getCloudValue())
				+ " " + WeatherUnits.getCloudValueUnitsString();
		desribe += "  " + Style.CHAR_CODE_HUMIDITY
				+ Math.round(weather.getHumidity()) + " "
				+ WeatherUnits.getHumidityUnitsString();

		if (weather.getDatePeriodHours() > 0) {

			desribe += Style.LINE_SEPARATOR;

			if (weather.getPrecipitation() > 0.0f) {
				desribe += Style.CHAR_CODE_PRECIPTATION
						+ FloatToString((weather.getPrecipitation()))
						+ " "
						+ WeatherUnits.getPrecipitationUnitsString(weather
								.getDatePeriodHours());
			} else {
				desribe += Style.CHAR_CODE_PRECIPTATION
						+ getContext().getString(R.string.no);
			}
		}
		return desribe;
	}

	private String describeWindDirection(float direction) {
		if (direction < 22.5f || direction >= 337.5f) {
			return getResources().getString(R.string.N);
		} else if (direction < 67.5f) {
			return getResources().getString(R.string.N)
					+ getResources().getString(R.string.E);
		} else if (direction < 112.5f) {
			return getResources().getString(R.string.E);
		} else if (direction < 157.5f) {
			return getResources().getString(R.string.S)
					+ getResources().getString(R.string.E);
		} else if (direction < 202.5f) {
			return getResources().getString(R.string.S);
		} else if (direction < 247.5f) {
			return getResources().getString(R.string.S)
					+ getResources().getString(R.string.W);
		} else if (direction < 292.5f) {
			return getResources().getString(R.string.W);
		} else if (direction < 337.5f) {
			return getResources().getString(R.string.N)
					+ getResources().getString(R.string.W);
		}
		return getResources().getString(R.string.Unknown);
	}

	private String FloatToString(float value) {
		float f = Math.round(value * 100.f) / 100.0f;
		String res;
		if (f - ((int) f) > 0.0f) {
			res = String.valueOf(f);
		} else {
			res = String.valueOf((int) f);
		}
		return res;
	}

	private int getNumberOfLines() {
		String yourInput = getDesribeString(new Weather());
		Matcher m = Pattern.compile("(" + Style.LINE_SEPARATOR + ")").matcher(
				yourInput);
		int lines = 1;
		while (m.find()) {
			lines++;
		}
		return lines;
	}

	public void setTextColor(int color) {
		mWhenView.setTextColor(color);
		mDescriptionView.setTextColor(color);
		int c = color & 0xFFFFFF;
		int r = c / 0x10000;
		int g = (c & 0xFF00) / 0x100;
		int b = c & 0xFF;
		int y = Math.max(Math.max(r, g), b);
		c = (255 - y) * 0x1000000;
		mIconBrightnessView.setBackgroundColor(c);
	}

	public void setTypeface(Typeface typeface, float lineSpaccingMulti) {
		mTypeface = typeface;
		mLineSpaccingMulti = lineSpaccingMulti;
		reacalSize();
	}

	public void setSize(int size) {
		mSize = size;
		reacalSize();
	}

	private void reacalSize() {
		if (mTypeface != null) {
			mWhenView.setTypeface(mTypeface);
			mDescriptionView.setTypeface(mTypeface);
		}
		mDescriptionView.setLineSpacing(0, mLineSpaccingMulti);

		int sz5 = (int) (mSize / (getNumberOfLines() * mLineSpaccingMulti));
		mFontSize = Style.PxToSp(sz5);
		mWhenView.setTextSize(mFontSize);
		mDescriptionView.setTextSize(mFontSize);
		float szIC = (mSize - sz5 - ICON_PADDING * 2);
		mIconViewWidth = (int) (szIC / ICON_ASPECT_RATIO);
		mIconViewHeight = (int) szIC;
		mIconView.setMinimumHeight(mIconViewHeight);
		mIconView.setMinimumWidth(mIconViewWidth);
		mIconView.setMaxHeight(mIconViewHeight);
		mIconView.setMaxWidth(mIconViewWidth);

		mMoonView.setMinimumHeight(mIconViewHeight);
		mMoonView.setMinimumWidth(mIconViewWidth);
		mMoonView.setMaxHeight(mIconViewHeight);
		mMoonView.setMaxWidth(mIconViewWidth);

		mIconBrightnessView.setMinimumHeight(mIconViewHeight);
		mIconBrightnessView.setMinimumWidth(mIconViewWidth);
		ViewGroup.LayoutParams p = mIconBrightnessView.getLayoutParams();
		p.width = mIconViewWidth;
		p.height = mIconViewHeight;
		mIconBrightnessView.setLayoutParams(p);
	}

	public int getMaximumWidth() {
		return (mIconViewWidth + ICON_PADDING) * 2;
	}

	public void clear() {
		mWhenView.setText("");
		setIcon(null);
		mDescriptionView.setText("");
	}

	public void setExpired() {
		String res = Style.splitTextForWidth(mWhenView.getPaint(),
				getResources().getText(R.string.outdated).toString(),
				mIconViewWidth * 2 - ICON_PADDING * 2, 5);
		mWhenView.setText(res);
		setIcon(null);
		mDescriptionView.setText("");
	}
}
