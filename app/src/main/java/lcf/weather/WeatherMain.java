package lcf.weather;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.util.Log;

public class WeatherMain {
	private static final String TAG = "WeatherMain";

	private WeatherWorker mWeatherWorker = null;
	private final Runnable mCallbackRunnable;
	private Timer mTimer = null;
	private final Handler mHandler = new Handler();
	private int mInterval = 30;
	private int mCityId;
	private String mApiKey;
	private final int mDaysForForecast;
	private final File mCacheDir;

	public WeatherMain(File cacheDir, Runnable callback, int daysForForecast) {
		mCallbackRunnable = callback;
		mDaysForForecast = daysForForecast;
		mCacheDir = cacheDir;
	}

	public void start(int cityId, int updateIntervalMIN, String apiKey) {
		Log.d(TAG, "start: " + cityId + ", " + updateIntervalMIN + ", " + apiKey);
		mCityId = cityId;
		mApiKey = apiKey;
		stop();
		if (cityId == 0) {
			return;
		}
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				startWorker(false);
			}
		}, 10, updateIntervalMIN * 1000 * 60);
		mInterval = updateIntervalMIN;
	}

	public void stop() {
		if (mTimer == null) {
			return;
		}
		mTimer.cancel();
		mTimer = null;
	}

	public void update(boolean imideatlyAnswer) { // force update data
		if (isWorking()) {
			if (imideatlyAnswer && mWeatherWorker != null
					&& mWeatherWorker.isWorking()) {
				mWeatherWorker.askForImideatlyAnswer();
			} else {
				new Thread() {
					@Override
					public void run() {
						startWorker(true);
					};
				}.start();
			}
		}

	}

	public void softUpdate() { // just asking about call callback, data will be updated only if it needed
		if (isWorking()) {
			start(mCityId, mInterval, mApiKey);
		}
	}

	private void startWorker(boolean forceNetwork) { // do not call in UI thread, call it using timer
		if (mWeatherWorker == null || mWeatherWorker.getCityId() != mCityId || !mWeatherWorker.getApiKey().equals(mApiKey)) {
			mWeatherWorker = new WeatherWorker(mCacheDir, mCityId,
					mCallbackRunnable, mHandler, mDaysForForecast, mApiKey);
		}
		if (mWeatherWorker.isWorking()) {
			return;
		}
		mWeatherWorker.run(forceNetwork);
	}

	public List<Weather> today() { // first in list - weather right now, after forecast for short period
		if (mWeatherWorker == null) {
			return null;
		}
		return mWeatherWorker.today();
	}

	public List<Weather> forecast() { // daily forecast
		if (mWeatherWorker == null) {
			return null;
		}
		return mWeatherWorker.forecast();
	}

	public boolean isErrorWhileLastUpdate() {
		if (mWeatherWorker == null) {
			return true;
		}
		return mWeatherWorker.isErrorWhileLastUpdate();
	}

	public static void findCities(String pat, CitiesCallback cb, String apiKey) { //result will come in the same thread that call this method
		new CityWorker(pat, cb, apiKey); // null for pat to find nearest cities by current IP

	}

	public static void findNearestCitiesByCurrentIP(CitiesCallback cb, String apiKey) { //result will come in the same thread that call this method
		new CityWorker(null, cb, apiKey);

	}

	public boolean isWorking() {
		return mTimer != null;
	}
}
