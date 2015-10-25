package lcf.clock.prefs;

import lcf.clock.R;
import lcf.clock.Style;
import lcf.weather.City;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CityListAdapter extends ArrayAdapter<City> {

	public CityListAdapter(Context context) {
		super(context, R.layout.cityrow, R.id.label);
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		Context context = getContext();
		final LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.cityrow, null, true);

		final TextView textView = (TextView) rowView.findViewById(R.id.label);

		textView.setText(getCityReadable(getItem(position), context));

		return rowView;
	}

	static String getCityReadable(City city, Context context) {
		float lat = city.getLatitude();
		int lac = (int) Math.abs(lat);
		int lam = Math.round((Math.abs(lat) - lac) * 60.0f);
		String laz = lat >= 0.0f ? context.getString(R.string.N) : context
				.getString(R.string.S);
		String latitude = lac + Style.CHAR_CODE_DEGREE + lam
				+ Style.CHAR_CODE_MINUTE + laz;

		float lon = city.getLongtitude();
		int loc = (int) Math.abs(lon);
		int lom = Math.round((Math.abs(lon) - loc) * 60.0f);
		String loz = lon >= 0.0f ? context.getString(R.string.E) : context
				.getString(R.string.W);
		String longtitude = loc + Style.CHAR_CODE_DEGREE + lom
				+ Style.CHAR_CODE_MINUTE + loz;

		return city.getName() + ", " + city.getCountry() + " (" + latitude
				+ " " + longtitude + ")";
	}

}
