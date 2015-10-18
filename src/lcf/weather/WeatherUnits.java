package lcf.weather;

import lcf.clock.R;
import android.content.Context;

abstract public class WeatherUnits {
	public enum TemperatureUnits {
		Celsius, Fahrenheit
	}

	public enum HumidityUnits {
		Percent
	}

	public enum PressureUnits {
		hPa, mmHg, inHg
	}

	public enum SpeedUnits {
		MetersPS, MilesPH
	}

	public enum PrecipitationUnits {
		MM
	}

	private static TemperatureUnits gTemperatureUnits = TemperatureUnits.Celsius;
	private static HumidityUnits gHumidityUnits = HumidityUnits.Percent;
	private static PressureUnits gPressureUnits = PressureUnits.hPa;
	private static SpeedUnits gSpeedUnits = SpeedUnits.MetersPS;
	private static PrecipitationUnits gPrecipitationUnits = PrecipitationUnits.MM;

	private static Context mContext = null;

	public static void setResourceContext(Context context) {
		mContext = context;
	}

	private static String getString(int resId) {
		if (mContext == null) {
			throw new RuntimeException(
					"set context by setResourceContext before using WeatherUnits");
		}
		return mContext.getResources().getString(resId);
	}

	static float getTemperatureInUnits(float temperatureCelsius) {
		switch (gTemperatureUnits) {
		case Celsius:
			return temperatureCelsius;
			/*case KelvinS:
				return temperatureCelsius + 273.15f;*/
		case Fahrenheit:
			return temperatureCelsius * 9.0f / 5.0f + 32;
		}
		return Float.NaN;
	}

	static float getHumidityInUnits(float humidityPercent) {
		switch (gHumidityUnits) {
		case Percent:
			return humidityPercent;
		}
		return Float.NaN;
	}

	static float getPressureInUnits(float pressureHPa) {
		switch (gPressureUnits) {
		case hPa:
			return pressureHPa;
		case inHg:
			return pressureHPa * 0.0295333727f;
		case mmHg:
			return pressureHPa * 0.750061683f;
		}
		return Float.NaN;
	}

	static float getSpeedInUnits(float speedMeterPS) {
		switch (gSpeedUnits) {
		case MetersPS:
			return speedMeterPS;
		case MilesPH:
			return speedMeterPS * 2.23693629f;
		}
		return Float.NaN;
	}

	public static String getTemperatureUnitsString(TemperatureUnits unit) {
		switch (unit) {
		case Celsius:
			return getString(R.string.Celsius);
			/*case KelvinS:
				return getString(R.string.KelvinS);*/
		case Fahrenheit:
			return getString(R.string.Fahrenheit);
		}
		return getString(R.string.Unknown);
	}

	public static String getTemperatureUnitsString() {
		return getTemperatureUnitsString(gTemperatureUnits);
	}

	public static float getPrecipitationInUnits(float precipitationMM) {
		switch (gPrecipitationUnits) {
		case MM:
			return precipitationMM;
		}
		return Float.NaN;
	}

	public static String getHumidityUnitsString(HumidityUnits unit) {
		switch (unit) {
		case Percent:
			return getString(R.string.percent);
		}
		return getString(R.string.Unknown);
	}

	public static String getHumidityUnitsString() {
		return getHumidityUnitsString(gHumidityUnits);
	}

	public static String getPressureUnitsString(PressureUnits unit) {
		switch (unit) {
		case hPa:
			return getString(R.string.hPa);
		case inHg:
			return getString(R.string.inHg);
		case mmHg:
			return getString(R.string.mmHg);
		}
		return getString(R.string.Unknown);
	}

	public static String getPressureUnitsString() {
		return getPressureUnitsString(gPressureUnits);
	}

	public static String getSpeedUnitsString() {
		return getSpeedUnitsString(gSpeedUnits);
	}

	public static String getSpeedUnitsString(SpeedUnits unit) {
		switch (unit) {
		case MetersPS:
			return getString(R.string.MetersPS);
		case MilesPH:
			return getString(R.string.MilesPH);
		}
		return getString(R.string.Unknown);
	}

	public static String getPrecipitationUnitsString(int periodHours) {
		return getPrecipitationUnitsString(gPrecipitationUnits, periodHours);
	}

	public static String getPrecipitationUnitsString(PrecipitationUnits unit,
			int periodHours) {
		switch (unit) {
		case MM:
			return getString(R.string.mm) + "/" + periodHours
					+ getString(R.string.h);
		}
		return getString(R.string.Unknown);
	}

	public static String getCloudValueUnitsString() {
		return getString(R.string.percent);
	}

	public static TemperatureUnits getTemperatureUnits() {
		return gTemperatureUnits;
	}

	public static void setTemperatureUnits(TemperatureUnits TemperatureUnits) {
		WeatherUnits.gTemperatureUnits = TemperatureUnits;
	}

	public static HumidityUnits getlHumidityUnits() {
		return gHumidityUnits;
	}

	public static void setHumidityUnits(HumidityUnits lHumidityUnits) {
		WeatherUnits.gHumidityUnits = lHumidityUnits;
	}

	public static PressureUnits getPressureUnits() {
		return gPressureUnits;
	}

	public static void setPressureUnits(PressureUnits lPressureUnits) {
		WeatherUnits.gPressureUnits = lPressureUnits;
	}

	public static SpeedUnits getSpeedUnits() {
		return gSpeedUnits;
	}

	public static void setSpeedUnits(SpeedUnits lSpeedUnits) {
		WeatherUnits.gSpeedUnits = lSpeedUnits;
	}

	public static PrecipitationUnits getPrecipitationUnits() {
		return gPrecipitationUnits;
	}

	public static void setPrecipitationUnits(
			PrecipitationUnits lPrecipitationUnits) {
		WeatherUnits.gPrecipitationUnits = lPrecipitationUnits;
	}

}
