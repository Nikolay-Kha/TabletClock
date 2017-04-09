package lcf.clock.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import lcf.clock.R;

/**
 * Facade for SharedPreferences to ease access to preferences.
 *
 * @author jacek
 * @since 09.04.17
 */
public class AppPreferences {
	static final String CITY_NOT_SET = " ";

	private final Context context;
	private final SharedPreferences sharedPreferences;

	public AppPreferences(Context context, SharedPreferences sharedPreferences) {
		this.context = context;
		this.sharedPreferences = sharedPreferences;
	}

	public String getApiKey() {
		return getStringPreference(R.string.key_api_key);
	}

	public int getCityId() {
		return sharedPreferences.getInt(context.getString(R.string.key_city_id), 0);
	}

	public String getCityName() {
		return getStringPreference(R.string.key_city);
	}

	private String getStringPreference(int key) {
		return sharedPreferences.getString(context.getString(key), "");
	}

	public boolean checkFirstTime() {
		if (getCityName().length() == 0) {
			sharedPreferences.edit()
					.putString(context.getString(R.string.key_city), CITY_NOT_SET)
					.commit();
			return true;
		}
		return false;
	}
}
