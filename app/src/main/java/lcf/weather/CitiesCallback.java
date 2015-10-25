package lcf.weather;

import java.util.List;

public abstract class CitiesCallback implements Runnable {
	private List<City> mList;
	private String mPattern = null;

	abstract public void ready(List<City> result);

	@Override
	public void run() {
		ready(mList);
	}

	void setList(List<City> list) {
		mList = list;
	}

	void setPattern(String pattern) {
		mPattern = pattern;
	}

	public String getPattern() {
		return mPattern;
	}

}
