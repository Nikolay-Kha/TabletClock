package lcf.weather;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

abstract class OWMWeatherTagParser {
	private static final String TAG = "OWMWeatherTagParser";

	static Date readDate(String strDate) {
		Date res = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
				Locale.getDefault());
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			res = sdf.parse(strDate);
		} catch (ParseException e) {
			sdf.applyPattern("yyyy-MM-dd");
			try {
				res = sdf.parse(strDate);
			} catch (ParseException e1) {
				Log.e(TAG, "unable to parse date: '" + strDate + "'", e1);
			}
		}
		return res;
	}

	static public List<Weather> parse(InputStream stream)
			throws XmlPullParserException, IOException {
		Weather weather = null;
		ArrayList<Weather> resultList = null;
		City city = null;

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(stream, null);
		boolean nameTag = false;
		boolean countryTag = false;

		while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
			switch (xpp.getEventType()) {
			// document start
			case XmlPullParser.START_DOCUMENT:
				resultList = new ArrayList<Weather>();
				break;
			// tag start
			case XmlPullParser.START_TAG:
				if (xpp.getName().equals("time")) {
					weather = new Weather();
				}

				if (xpp.getName().equals("location")
						&& xpp.getAttributeCount() == 0) {
					city = new City();
				} else if (xpp.getName().equals("country")) {
					countryTag = true;
				} else if (xpp.getName().equals("name")) {
					nameTag = true;
				} else if (xpp.getName().equals("current")
						|| xpp.getName().equals("item")) {
					weather = new Weather();
					city = new City();
				} else {
					Date from = null;
					Date to = null;
					if (xpp.getAttributeCount() == 0
							&& xpp.getName().equals("precipitation")) { // if no precipitation
						weather.setPrecipitationType("");
						weather.setPrecipitation(0.0f);
					}

					for (int i = 0; i < xpp.getAttributeCount(); i++) {
						if (xpp.getName().equals("city")) {
							if (xpp.getAttributeName(i).equals("id")) {
								city.setId(Integer.parseInt(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals("name")) {
								city.setName(xpp.getAttributeValue(i));
							}
						} else if (xpp.getName().equals("coord")) {
							if (xpp.getAttributeName(i).equals("lon")) {
								city.setLongtitude(Float.parseFloat(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals("lat")) {
								city.setLatitude(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("sun")) {
							if (xpp.getAttributeName(i).equals("rise")) {
								city.setSunRise(readDate(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals("set")) {
								city.setSunSet(readDate(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("location")) {
							if (xpp.getAttributeName(i).equals("longitude")) {
								city.setLongtitude(Float.parseFloat(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals(
									"latitude")) {
								city.setLatitude(Float.parseFloat(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals(
									"geobaseid")) {
								city.setId(Integer.parseInt(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("temperature")) {
							if (xpp.getAttributeName(i).equals("value")) {
								weather.setTemperature(Float.parseFloat(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals("day")) {
								weather.setTemperature(Float.parseFloat(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals("min")) {
								weather.setTemperatureMin(Float.parseFloat(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals("max")) {
								weather.setTemperatureMax(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("humidity")) {
							if (xpp.getAttributeName(i).equals("value")) {
								weather.setHumidity(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("pressure")) {
							if (xpp.getAttributeName(i).equals("value")) {
								weather.setPressure(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("speed")) {
							if (xpp.getAttributeName(i).equals("value")) {
								weather.setWindSpeed(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("windSpeed")) {
							if (xpp.getAttributeName(i).equals("mps")) {
								weather.setWindSpeed(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("direction")) {
							if (xpp.getAttributeName(i).equals("value")) {
								weather.setWindDirection(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}

						} else if (xpp.getName().equals("windDirection")) {
							if (xpp.getAttributeName(i).equals("deg")) {
								weather.setWindDirection(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("clouds")
								&& xpp.getAttributeCount() == 2) {
							if (xpp.getAttributeName(i).equals("value")) {
								weather.setCloudValue(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("clouds")
								&& xpp.getAttributeCount() == 3) {
							if (xpp.getAttributeName(i).equals("all")) {
								weather.setCloudValue(Float.parseFloat(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("precipitation")) {
							if (xpp.getAttributeName(i).equals("mode")) {
								weather.setPrecipitationType(xpp
										.getAttributeValue(i));
								weather.setPrecipitation(0.0f); // hack, no info in xml
							} else if (xpp.getAttributeName(i).equals("value")) {
								weather.setPrecipitation(Float.parseFloat(xpp
										.getAttributeValue(i)));
							} else if (xpp.getAttributeName(i).equals("type")) {
								weather.setPrecipitationType(xpp
										.getAttributeValue(i));
							}

						} else if (xpp.getName().equals("weather")) {
							if (xpp.getAttributeName(i).equals("icon")) {
								weather.setWeatherIcon(xpp.getAttributeValue(i));
							} else if (xpp.getAttributeName(i).equals("value")) {
								weather.setWeatherDescription(xpp
										.getAttributeValue(i));
							} else if (xpp.getAttributeName(i).equals("number")) {
								weather.setWeatherId(Integer.parseInt(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("symbol")) {
							if (xpp.getAttributeName(i).equals("var")) {
								weather.setWeatherIcon(xpp.getAttributeValue(i));
							} else if (xpp.getAttributeName(i).equals("name")) {
								weather.setWeatherDescription(xpp
										.getAttributeValue(i));
							} else if (xpp.getAttributeName(i).equals("number")) {
								weather.setWeatherId(Integer.parseInt(xpp
										.getAttributeValue(i)));
							}
						} else if (xpp.getName().equals("lastupdate")
								&& xpp.getAttributeName(i).equals("value")) {
							weather.setDate(readDate(xpp.getAttributeValue(i)));
							weather.setDatePeriodHours(0); // notice
						} else if (xpp.getName().equals("time")) {
							if (xpp.getAttributeName(i).equals("day")) {
								weather.setDate(readDate(xpp
										.getAttributeValue(i)));
								weather.setDatePeriodHours(24); // notice
							} else if (xpp.getAttributeName(i).equals("from")) {
								from = readDate(xpp.getAttributeValue(i));
								weather.setDate(from);
							} else if (xpp.getAttributeName(i).equals("to")) {
								to = readDate(xpp.getAttributeValue(i));
							}
						}

					}
					if (from != null && to != null) {
						float t = (to.getTime() - from.getTime()) / 3600000; // to hours
						weather.setDatePeriodHours(Math.round(t));
					}
				}
				break;
			// ����� ����
			case XmlPullParser.END_TAG:
				if (xpp.getName().equals("name")) {
					nameTag = false;
				} else if (xpp.getName().equals("country")) {
					countryTag = false;
				} else if (weather != null
						&& (xpp.getName().equals("current")
								|| xpp.getName().equals("item") || xpp
								.getName().equals("time"))) {
					weather.setCity(city);
					resultList.add(weather);
					weather = null;
				}
				break;
			// ���������� ����
			case XmlPullParser.TEXT:
				if (countryTag && city != null) {
					city.setCountry(xpp.getText());
				} else if (nameTag && city != null) {
					city.setName(xpp.getText());
				}
				break;

			default:
				break;
			}
			xpp.next();
		}
		//Log.i("tag", weather.toString());
		return resultList;
	}
}
