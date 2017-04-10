package lcf.weather;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;

abstract class OWMWeather {
	private static final String TAG = "OWMWeather";
	private final static int CACHE_OUTDATE_TIMEOUT = 25 * 60 * 1000; // 25 min

	/* deprecated due to the bad icons quality */
	static public Drawable getIcon(String weather) { // do not call in UI Thread
		Drawable d = null;
		InputStream iconstream = null;
		try {
			iconstream = OWMUrl.getIconUrl(weather).download();
		} catch (IOException e1) {
			return null;
		}
		if (iconstream != null) {
			d = Drawable.createFromStream(iconstream, "");
			try {
				iconstream.close();
			} catch (IOException e) {
			}
		}
		return d;
	}

	static public List<Weather> get(OWMUrl url, File cacheDirForRead,
			boolean forceCache, File cacheDirForStore) {
		InputStream stream = null;
		try {
			boolean useCache = cacheDirForRead != null && url.isCachable();
			File cacheFile = useCache ? new File(cacheDirForRead,
					url.cacheName()) : null;
			boolean indated = false;
			if (forceCache
					|| (useCache && (indated = (Math.abs(cacheFile
							.lastModified() - new Date().getTime()) < CACHE_OUTDATE_TIMEOUT)))) {
				if (!useCache) {
					return null;
				}
				stream = new FileInputStream(cacheFile);
				//Log.i("tag", "from cache - " + url.cacheName());
				List<Weather> res = OWMWeatherTagParser.parse(stream);
				if (res != null && res.size() > 0) {
					return res;
				} else if (!indated) {
					return null;
				}
			}
			stream = url.download();
			//Log.i("tag", "network - " + url.cacheName());
			InputStream streamForXml = stream;
			ByteArrayOutputStream baos = null;

			if (useCache) {
				baos = new ByteArrayOutputStream();
				int chunk = 0;
				byte[] data = new byte[256];
				while (-1 != (chunk = stream.read(data))) {
					baos.write(data, 0, chunk);
				}
				streamForXml = new ByteArrayInputStream(baos.toByteArray());
			}

			List<Weather> result = OWMWeatherTagParser.parse(streamForXml);

			if (result != null && result.size() > 0 && cacheDirForStore != null
					&& url.isCachable() && baos != null) {
				FileOutputStream fo = null;
				try {
					fo = new FileOutputStream(new File(cacheDirForStore,
							url.cacheName()));
					fo.write(baos.toByteArray());
					fo.flush();
				} catch (Exception e) {
					Log.e(TAG, "Error", e);
				} finally {
					if (fo != null) {
						fo.close();
					}
				}
			}
			return result;
		} catch (Exception e) {
			Log.i(TAG, "OWMWeather get Exception", e);
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static PointF getCoordsByCurrentIp(String apiKey) {
		InputStream stream = null;
		try {
			stream = OWMUrl.getFindCityUrlByIpJson(apiKey).download();
			BufferedReader reader = new BufferedReader(new InputStreamReader( // debug
					stream));
			String line = null;
			PointF r = new PointF();
			String fre = "([-+]?[0-9]+\\.?[0-9]*)";
			Pattern p1 = Pattern.compile("\"lat\":" + fre);
			Pattern p2 = Pattern.compile("\"lng\":" + fre);
			boolean latf = false, lonf = false;
			while ((line = reader.readLine()) != null) {
				if (!latf) {
					Matcher m1 = p1.matcher(line);
					if (m1.find()) {
						r.x = Float.parseFloat(m1.group(1));
						latf = true;
					}
				}
				if (!lonf) {
					Matcher m2 = p2.matcher(line);
					if (m2.find()) {
						r.y = Float.parseFloat(m2.group(1));
						lonf = true;
					}
				}
				if (latf && lonf) {
					break;
				}
			}
			if (latf && lonf) {
				return r;
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
