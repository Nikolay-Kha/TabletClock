package lcf.weather;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;
import android.os.Handler;

class CityWorker extends Thread {
	private static List<CityWorker> workingThreads = new ArrayList<CityWorker>(); // to keep from GC
	private final String mPattern;
	CitiesCallback mCitiesCallback;
	Handler mHandler;

	CityWorker(String pat, CitiesCallback cb) { // if mPattern == null - looking by current ip
		workingThreads.add(this);
		mPattern = pat;
		mCitiesCallback = cb;
		mCitiesCallback.setPattern(pat);
		mHandler = new Handler();
		start();
	}

	@Override
	public void run() {
		List<City> list = null;
		List<Weather> wlist = null;
		if (mPattern == null) {
			PointF p = OWMWeather.getCoordsByCurrentIp();
			if (p != null) {
				wlist = OWMWeather.get(OWMUrl.getFindCityUrlByCoords(p.x, p.y),
						null, false, null);
			}
		} else {
			wlist = OWMWeather.get(OWMUrl.getFindCityUrlByName(mPattern), null,
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
