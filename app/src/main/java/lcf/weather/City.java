package lcf.weather;

import java.util.Date;

public class City {
	private String name;
	private String country;
	private int id;
	private float longtitude;
	private float latitude;
	private Date sunRise = null;
	private Date sunSet = null;

	@Override
	public String toString() {
		return getName() + ", " + getCountry() + " (" + getLatitude() + " "
				+ getLongtitude() + ")";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(float longtitude) {
		this.longtitude = longtitude;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public Date getSunRise() {
		return sunRise;
	}

	public void setSunRise(Date sunRise) {
		this.sunRise = sunRise;
	}

	public Date getSunSet() {
		return sunSet;
	}

	public void setSunSet(Date sunSet) {
		this.sunSet = sunSet;
	}

}
