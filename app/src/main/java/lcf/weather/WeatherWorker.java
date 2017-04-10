package lcf.weather;

import java.io.File;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

class WeatherWorker { // for openweathermap, shouldn't call run() in UI thread
	private static final String TAG = "WeatherWorker";

	private List<Weather> mToday = null;
	private List<Weather> mForecast = null;
	private final Runnable mCallbackRunnable;
	private final Handler mHandler;
	private boolean mWorking = false;
	private boolean mErrorWhileLastUpdate = false;
	private final int mCityId;
	private final int mDaysForForecast;
	private final File mCacheDir;
	private final String mApiKey;
	private boolean imideatlyRequest = false;
	private final static int REPEAT_COUNT = 10;

	/** Forecast is returned in 3 hours intervals, so 24/3 = 8.
	 *  Plus 1 for current weather. */
	private static final int NUMBER_OF_READINGS_FOR_24H_FORECAST = 9;

	public WeatherWorker(File cacheDir, int cityId, Runnable callbackRunnable,
						 Handler handler, int daysForForecast, String apiKey) {
		Log.d(TAG, "new");
		mCallbackRunnable = callbackRunnable;
		mHandler = handler;
		mCityId = cityId;
		mDaysForForecast = daysForForecast;
		mCacheDir = cacheDir;
		mApiKey = apiKey;
	}

	public void run(boolean forceNetwork) { // do not call in UI thread
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			Log.i("tag", "NetworkOnMainThreadException");
			throw new RuntimeException();
		}
		boolean markNowAsOld = false;
		mWorking = true;
		boolean repeat = true;
		int repeatCount = 0;
		List<Weather> today;
		List<Weather> forecast;
		OWMUrl nowUrl = OWMUrl.getNowWeatherUrl(mCityId, mApiKey);
		OWMUrl forecastUrl = OWMUrl.getForecastDaylyWeatherUrl(mCityId, mDaysForForecast, mApiKey);
		OWMUrl detailUrl = OWMUrl.getForecastWeatherUrl(mCityId, NUMBER_OF_READINGS_FOR_24H_FORECAST, mApiKey);
		boolean initRequere = false;

		if (!forceNetwork) {
			if (mToday == null) {
				mToday = OWMWeather.get(detailUrl, mCacheDir, true, null);
				if (mToday != null) {
					mToday.add(0, new Weather()); // instead of now
					initRequere = true;
					markNowAsOld = true;
				}
			}

			if (mForecast == null) {
				mForecast = OWMWeather.get(forecastUrl, mCacheDir, true, null);
				initRequere = true;
			}
			if (initRequere && mToday != null && mForecast != null) {
				mErrorWhileLastUpdate = false;
				mHandler.post(mCallbackRunnable);
			}
		}

		File cacheDir = forceNetwork ? null : mCacheDir;
		if (mForecast != null && mForecast.size() > 0
				&& mForecast.get(0).getCity() != null
				&& mForecast.get(0).getCity().getId() != mCityId) {
			cacheDir = null;
		}

		if (mToday != null && mToday.size() > 1
				&& mToday.get(1).getCity() != null
				&& mToday.get(1).getCity().getId() != mCityId) {
			cacheDir = null;
		}

		do {
			today = OWMWeather.get(nowUrl, cacheDir, false, mCacheDir);
			forecast = OWMWeather.get(forecastUrl, cacheDir, false, mCacheDir);
			List<Weather> detailDay = OWMWeather.get(detailUrl, cacheDir,
					false, mCacheDir);
			repeat = today == null || forecast == null || detailDay == null;

			if (repeat) {
				if (imideatlyRequest) {
					imideatlyRequest = false;
					mErrorWhileLastUpdate = true;
					mHandler.post(mCallbackRunnable);
				}
				if (markNowAsOld && mToday != null && mToday.size() > 0) {
					today = OWMWeather.get(nowUrl, mCacheDir, true, null);
					if (today != null && today.size() > 0) {
						mToday.set(0, today.get(0));
						mErrorWhileLastUpdate = true;
						mHandler.post(mCallbackRunnable);
					}
					markNowAsOld = false;
				}

				try {
					Thread.sleep(10 * 1000);
				} catch (Exception e) {
				}
				repeatCount++;
				if (repeatCount > REPEAT_COUNT) {
					mErrorWhileLastUpdate = true;
					mHandler.post(mCallbackRunnable);
					mWorking = false;
					return;
				}
			} else {
				if (today != null && detailDay != null) {
					today.addAll(detailDay);
				} else if (detailDay != null) {
					detailDay.add(0, new Weather());
					today = detailDay;
				}
			}
		} while (repeat);

		if (today != null) {
			mToday = today;
		}
		if (forecast != null) {
			mForecast = forecast;
		}
		mErrorWhileLastUpdate = false;
		mHandler.post(mCallbackRunnable);
		mWorking = false;
	}

	public List<Weather> today() { // first in list - weather right now, after forecast for short period
		return mToday;
	}

	public List<Weather> forecast() { // daily forecast
		return mForecast;
	}

	public boolean isWorking() {
		return mWorking;
	}

	public int getCityId() {
		return mCityId;
	}

	public boolean isErrorWhileLastUpdate() {
		return mErrorWhileLastUpdate;
	}

	public void askForImideatlyAnswer() {
		imideatlyRequest = true;
	}

	public String getApiKey() {
		return mApiKey;
	}
}
