package lcf.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

abstract class OWMUrl {
	private static final String OWM_APIKEY = "1bb5226ac7cc4c180f487affc89eb7a7";
	private static final String WEATHER_ICON_URL = "http://openweathermap.org/img/w/%s.png";
	private static final String WEATHER_NOW_URL = "http://api.openweathermap.org/data/2.5/weather?mode=xml&units=metric&lang=%s&id=%d&APPID=" + OWM_APIKEY;
	private static final String WEATHER_DAYLY_FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=xml&cnt=%d&units=metric&lang=%s&id=%d&APPID=" + OWM_APIKEY;
	private static final String WEATHER_FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast?mode=xml&cnt=%d&units=metric&lang=%s&id=%d&APPID=" + OWM_APIKEY;
	private static final String CITY_SEARCH_URL_BY_NAME = "http://api.openweathermap.org/data/2.5/find?&q=%s&type=like&sort=population&units=metric&cnt=30&mode=xml&APPID=" + OWM_APIKEY;
	private static final String CITY_SEARCH_URL_BY_COORDS = "http://api.openweathermap.org/data/2.5/find?&lat=%f&lon=%f&sort=population&units=metric&cnt=30&mode=xml&APPID=" + OWM_APIKEY;
	private static final String CITY_BY_CURRENT_IP_JSON = "http://openweathermap.org/data/weather&APPID=" + OWM_APIKEY; // no xml version, old version
	// http://openweathermap.org/data/weather/?type=json - upper url is stable
	private static final String CACHE_NONE = "";
	private static final String CACHE_NOW = "now.xml";
	private static final String CACHE_FORECAST = "forecast.xml";
	private static final String CACHE_DAYLY = "dayly.xml";

	abstract String url();

	abstract String cacheName();

	boolean isCachable() {
		return !CACHE_NONE.equals(cacheName());
	}

	public InputStream download() // !!! do not call in UI Thread !!!
			throws IOException {
		URL url = new URL(url());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* ms */);
		conn.setConnectTimeout(15000 /* ms */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect();
		return conn.getInputStream();
	}

	static private class OWMUrlImplementation extends OWMUrl {
		String mUrl;
		String mCacheName;

		OWMUrlImplementation(String url, String cacheName) {
			mUrl = url;
			mCacheName = cacheName;
		}

		@Override
		String url() {
			return mUrl;
		}

		@Override
		String cacheName() {
			return mCacheName;
		}

	}

	static protected OWMUrl getFindCityUrlByCoords(float latitude,
			float longitude) {
		return new OWMUrlImplementation(String.format(Locale.ENGLISH,
				CITY_SEARCH_URL_BY_COORDS, latitude, longitude), CACHE_NONE);
	}

	static protected OWMUrl getFindCityUrlByName(String pat) {
		return new OWMUrlImplementation(String.format(CITY_SEARCH_URL_BY_NAME,
				pat), CACHE_NONE);
	}

	static protected OWMUrl getForecastDaylyWeatherUrl(int cityId,
			int forecastDays) {
		Locale loc = Locale.getDefault();
		return new OWMUrlImplementation(String.format(loc,
				WEATHER_DAYLY_FORECAST_URL, forecastDays, Locale.getDefault()
						.getLanguage(), cityId), CACHE_DAYLY);
	}

	static protected OWMUrl getForecastWeatherUrl(int cityId, int forecastDays) {
		Locale loc = Locale.getDefault();
		return new OWMUrlImplementation(String.format(loc,
				WEATHER_FORECAST_URL, forecastDays, Locale.getDefault()
						.getLanguage(), cityId), CACHE_FORECAST);
	}

	static protected OWMUrl getNowWeatherUrl(int cityId) {
		Locale loc = Locale.getDefault();
		return new OWMUrlImplementation(String.format(loc, WEATHER_NOW_URL,
				Locale.getDefault().getLanguage(), cityId), CACHE_NOW);
	}

	static protected OWMUrl getFindCityUrlByIpJson() {
		return new OWMUrlImplementation(CITY_BY_CURRENT_IP_JSON, CACHE_NONE);
	}

	static protected OWMUrl getIconUrl(String weather) {
		return new OWMUrlImplementation(
				String.format(WEATHER_ICON_URL, weather), CACHE_NONE);
	}
}
