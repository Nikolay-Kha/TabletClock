package lcf.weather;

import java.util.Date;

import lcf.weather.WeatherUnits.TemperatureUnits;
import android.util.Log;

public class Weather { // all data should be setted in metric system, returned values is seted by global class WeatherUnits 
	private float temperature;
	private float temperatureMin;
	private float temperatureMax;
	private float humidity;
	private float pressure;
	private float windSpeed;
	private float windDirection;
	private float cloudValue;
	private float precipitation;
	private String precipitationType;
	private String weatherDescription;
	private String weatherIcon;
	private int weatherId;
	private Date date = null;;
	private int datePeriodHours;
	private City city = null;

	@Override
	public String toString() {
		return "Weather in "
				+ getCity().toString()
				+ " temperature: "
				+ getTemperature()
				+ " ("
				+ getTemperatureMin()
				+ ".."
				+ getTemperatureMax()
				+ ") "
				+ WeatherUnits.getTemperatureUnitsString()
				+ ", "
				+ getWeatherDescription()
				+ ", "
				+ "Clouds: "
				+ getCloudValue()
				+ WeatherUnits.getCloudValueUnitsString()
				+ ", precipitation: "
				+ getPrecipitation()
				+ WeatherUnits
						.getPrecipitationUnitsString(getDatePeriodHours())
				+ " " + getPrecipitationType() + ", humidity: " + getHumidity()
				+ WeatherUnits.getHumidityUnitsString() + ", pressure: "
				+ getPressure() + WeatherUnits.getPressureUnitsString()
				+ ", Wind: " + getWindSpeed()
				+ WeatherUnits.getSpeedUnitsString() + " " + getWindDirection()
				+ ", icon: " + getWeatherIcon() + ", id: " + getWeatherId()
				+ " " + getDate().toString() + " sunrise: "
				+ getCity().getSunRise().toString() + " sunset: "
				+ getCity().getSunSet() + " for " + getDatePeriodHours()
				+ " hours";
	}

	private String temperatureToString(float temperature) {
		int t = Math.round(temperature);
		if (t > 0
				&& WeatherUnits.getTemperatureUnits() == TemperatureUnits.Celsius) {
			return "+" + t;
		}
		return String.valueOf(t);
	}

	public Weather add(Weather weather) {
		if (getCity().getId() != weather.getCity().getId()) {
			Log.w("Weather", "Incorect weather summation - cities is different");
			return null;
		}
		Weather result = new Weather();
		float s = getDatePeriodHours() + weather.getDatePeriodHours();
		float wt = (getDatePeriodHours()) / s;
		float ww = ((weather.getDatePeriodHours())) / s;
		result.setTemperature(wt * getTemperatureWoConvertion() + ww
				* weather.getTemperatureWoConvertion());
		result.setTemperatureMax(Math.max(getTemperatureMaxWoConvertion(),
				weather.getTemperatureMaxWoConvertion()));
		result.setTemperatureMin(Math.min(getTemperatureMinWoConvertion(),
				weather.getTemperatureMinWoConvertion()));
		result.setHumidity(wt * getHumidityWoConvertion() + ww
				* weather.getHumidityWoConvertion());
		result.setPressure(wt * getPressureWoConvertion() + ww
				* weather.getPressureWoConvertion());

		// http://www.dpva.info/Guide/GuideMathematics/linearAlgebra/vectorsaddition/ in js source - calculateVector();
		float f1 = getWindSpeedWoConvertion() * wt;
		float f2 = weather.getWindSpeedWoConvertion() * ww;
		float angle = getWindDirection() - weather.getWindDirection();
		double angle1 = angle * Math.PI / 180.0f;
		double fr = Math.sqrt(f1 * f1 + f2 * f2 - 2 * f1 * f2
				* Math.cos(Math.PI - angle1));
		double ar = Math.asin(f2 * Math.sin(Math.PI - angle1) / fr);
		result.setWindSpeed(f1 + f2/*(float) fr*/); // we don't use vector summ, because we interested in actual wind speed
		result.setWindDirection(getWindDirection()
				- (float) (180.0f * ar / Math.PI));

		result.setCloudValue(wt * getCloudValue() + ww
				* weather.getCloudValue());
		result.setPrecipitation(getPrecipitationWoConvertion()
				+ weather.getPrecipitationWoConvertion());
		if (getPrecipitationWoConvertion() >= weather
				.getPrecipitationWoConvertion()) {
			result.setPrecipitationType(getPrecipitationType());
		} else {
			result.setPrecipitationType(weather.getPrecipitationType());
		}

		//choose worse weather icon and compleate other fields
		int it = Integer.parseInt(getWeatherIcon().substring(0, 2));
		int iw = Integer.parseInt(weather.getWeatherIcon().substring(0, 2));
		if (it >= iw) {
			result.setWeatherDescription(getWeatherDescription());
			result.setWeatherId(getWeatherId());
			result.setWeatherIcon(getWeatherIcon());
		} else {
			result.setWeatherDescription(weather.getWeatherDescription());
			result.setWeatherId(weather.getWeatherId());
			result.setWeatherIcon(weather.getWeatherIcon().substring(0, 2) // save day or nighnt icon
					+ getWeatherIcon().charAt(2));
		}

		result.setDate(getDate());
		result.setDatePeriodHours(getDatePeriodHours()
				+ weather.getDatePeriodHours());
		result.setCity(getCity());
		return result;
	}

	public String getTemperatureString() {
		return temperatureToString(getTemperature());
	}

	public String getTemperatureMinString() {
		return temperatureToString(getTemperatureMin());
	}

	public String getTemperatureMaxString() {
		return temperatureToString(getTemperatureMax());
	}

	public float getTemperature() {
		return WeatherUnits.getTemperatureInUnits(temperature);
	}

	float getTemperatureWoConvertion() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getTemperatureMin() {
		return WeatherUnits.getTemperatureInUnits(temperatureMin);
	}

	float getTemperatureMinWoConvertion() {
		return temperatureMin;
	}

	public void setTemperatureMin(float temperatureMin) {
		this.temperatureMin = temperatureMin;
	}

	public float getTemperatureMax() {
		return WeatherUnits.getTemperatureInUnits(temperatureMax);
	}

	float getTemperatureMaxWoConvertion() {
		return temperatureMax;
	}

	public void setTemperatureMax(float temperatureMax) {
		this.temperatureMax = temperatureMax;
	}

	public float getHumidity() {
		return WeatherUnits.getHumidityInUnits(humidity);
	}

	float getHumidityWoConvertion() {
		return humidity;
	}

	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}

	public float getPressure() {
		return WeatherUnits.getPressureInUnits(pressure);
	}

	float getPressureWoConvertion() {
		return pressure;
	}

	public void setPressure(float pressure) {
		this.pressure = pressure;
	}

	public float getWindSpeed() {
		return WeatherUnits.getSpeedInUnits(windSpeed);
	}

	float getWindSpeedWoConvertion() {
		return windSpeed;
	}

	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}

	public float getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(float windDirection) {
		this.windDirection = windDirection;
	}

	public String getWeatherIcon() {
		return weatherIcon;
	}

	public void setWeatherIcon(String weatherIcon) {
		this.weatherIcon = weatherIcon;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public float getCloudValue() {
		return cloudValue;
	}

	public void setCloudValue(float cloudValue) {
		this.cloudValue = cloudValue;
	}

	public float getPrecipitation() {
		return WeatherUnits.getPrecipitationInUnits(precipitation);
	}

	float getPrecipitationWoConvertion() {
		return precipitation;
	}

	public void setPrecipitation(float precipitation) {
		this.precipitation = precipitation;
	}

	public String getPrecipitationType() {
		return precipitationType;
	}

	public void setPrecipitationType(String precipitationType) {
		this.precipitationType = precipitationType;
	}

	public String getWeatherDescription() {
		return weatherDescription;
	}

	public void setWeatherDescription(String weatherDescription) {
		this.weatherDescription = weatherDescription;
	}

	public int getDatePeriodHours() {
		return datePeriodHours;
	}

	public void setDatePeriodHours(int datePeriodHours) {
		this.datePeriodHours = datePeriodHours;
	}

	public int getWeatherId() {
		return weatherId;
	}

	public void setWeatherId(int weatherId) {
		this.weatherId = weatherId;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

}
