package lcf.weather;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;
import android.os.Handler;

class CityWorker extends Thread {
	private static List<CityWorker> workingThreads = new ArrayList<CityWorker>(); // to keep from GC
	private final String mPattern;
	private final String mApiKey;
	private final CitiesCallback mCitiesCallback;
	private final Handler mHandler;

	CityWorker(String pat, CitiesCallback cb, String apiKey) { // if mPattern == null - looking by current ip
		workingThreads.add(this);
		mPattern = pat;
		mCitiesCallback = cb;
		mCitiesCallback.setPattern(pat);
		mHandler = new Handler();
		mApiKey = apiKey;
		start();
	}

	@Override
	public void run() {
		List<City> list = null;
		List<Weather> wlist = null;
		if (mPattern == null) {
			PointF p = OWMWeather.getCoordsByCurrentIp(mApiKey);
			if (p != null) {
				wlist = OWMWeather.get(OWMUrl.getFindCityUrlByCoords(p.x, p.y, mApiKey),
						null, false, null);
			}
		} else {
			wlist = OWMWeather.get(OWMUrl.getFindCityUrlByName(mPattern, mApiKey), null,
					false, null);
		}
		if (wlist != null) {
			list = new ArrayList<City>();
			for (Weather w : wlist) {
				list.add(w.getCity());
			}
		}

		mCitiesCallback.setList(list);
		mHandler.post(mCitiesCallback);
		workingThreads.remove(this);
	}
}
