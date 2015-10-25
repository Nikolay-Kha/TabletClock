package lcf.clock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lcf.clock.TimeViewUpdater.DOT_MODE;
import lcf.clock.prefs.BrightnessDialog;
import lcf.clock.prefs.CityDialog;
import lcf.clock.prefs.ColorDialog;
import lcf.clock.prefs.Preference;
import lcf.weather.Weather;
import lcf.weather.WeatherMain;
import lcf.weather.WeatherUnits;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ClockActivity extends Activity {
    private BatteryScreenReciever mBatteryScreenChecker = null;
    private CameraAsLightSensor mLightSensor;
    private NavBarHider mNavBarHider;
    private TimeViewUpdater mTimeViewUpdater;
    private WeatherMain mWeatherReciever;
    private static final int WEATHER_FORECAST_SHOW_INTERVAL_HOURS = 6;
    private static final long FORECAST_OUTDATED = 7 * 3600 * 1000;
    private WeatherView mNowWeather;
    private TextView mTimeView;
    private TextView mTemperatureView;
    private TextView mDate1View;
    private TextView mDate2View;
    private TextView mAlarm1View;
    private TextView mAlarm2View;
    private TextView mExtraData;
    private final ArrayList<WeatherView> mForecast = new ArrayList<WeatherView>();
    private final ArrayList<WeatherView> mWeekForecast = new ArrayList<WeatherView>();
    private LinearLayout mLayoutWeekForecast;
    private LinearLayout mLayoutToday;
    private Date mSunRise = null;
    private Date mSunSet = null;
    private boolean mUpdateWeatherCalled = false;
    private boolean enoughSpaceForWeekForecast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoOnReceiver.prepareFlagsIfNeed(getWindow(), this);
        String lan = Locale.getDefault().getLanguage();
        if (!lan.equalsIgnoreCase("ru") && !lan.equalsIgnoreCase("en")) {
            Locale.setDefault(Locale.ENGLISH);
            Configuration config = getResources().getConfiguration();
            config.locale = Locale.getDefault();
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        WeatherUnits.setResourceContext(getApplicationContext());
        Style.init(this);

        setContentView(R.layout.activity_clock);

        mTimeView = (TextView) findViewById(R.id.timeView);
        mTemperatureView = (TextView) findViewById(R.id.temperature);
        mDate1View = (TextView) findViewById(R.id.date1);
        mDate2View = (TextView) findViewById(R.id.date2);
        mAlarm1View = (TextView) findViewById(R.id.alarm1);
        mAlarm2View = (TextView) findViewById(R.id.alarm2);
        mNowWeather = (WeatherView) findViewById(R.id.weatherNow);
        mForecast.add((WeatherView) findViewById(R.id.weatherSlot1));
        mForecast.add((WeatherView) findViewById(R.id.weatherSlot2));
        mForecast.add((WeatherView) findViewById(R.id.weatherSlot3));
        mForecast.add((WeatherView) findViewById(R.id.weatherSlot4));
        mWeekForecast.add((WeatherView) findViewById(R.id.weatherWeekSlot1));
        mWeekForecast.add((WeatherView) findViewById(R.id.weatherWeekSlot2));
        mWeekForecast.add((WeatherView) findViewById(R.id.weatherWeekSlot3));
        mWeekForecast.add((WeatherView) findViewById(R.id.weatherWeekSlot4));
        mWeekForecast.add((WeatherView) findViewById(R.id.weatherWeekSlot5));
        mWeekForecast.add((WeatherView) findViewById(R.id.weatherWeekSlot6));
        mWeekForecast.add((WeatherView) findViewById(R.id.weatherWeekSlot7));
        mWeekForecast.add((WeatherView) findViewById(R.id.weatherWeekSlot8));
        mLayoutWeekForecast = (LinearLayout) findViewById(R.id.layoutWeekForecast);
        mLayoutToday = (LinearLayout) findViewById(R.id.layoutToday);
        mExtraData = (TextView) findViewById(R.id.extraData);

        calcSizes();

        mWeatherReciever = new WeatherMain(getFilesDir(), new Runnable() {
            @Override
            public void run() {
                updateWeatherData();
            }
        }, mWeekForecast.size() + 6); // +6 to receive for 14 days - to store it

        mTimeViewUpdater = new TimeViewUpdater(mTimeView, new Runnable() {
            @Override
            public void run() {
                updateData();
                mWeatherReciever.softUpdate();
            }
        });

        mNavBarHider = new NavBarHider(this, findViewById(R.id.rootView));

        mTimeView.setOnClickListener(mShowMenu);
        mAlarm1View.setOnClickListener(mShowAlarmSet);
        mAlarm2View.setOnClickListener(mShowAlarmSet);
        mLayoutWeekForecast.setOnClickListener(mWheatherFlipListener);
        mLayoutToday.setOnClickListener(mWheatherFlipListener);
        findViewById(R.id.dummyView2).setOnClickListener(mWheatherFlipListener);

        registerForContextMenu(mTimeView);
    }

    private void calcSizes() {
        final int reserved = 10; // padding, should be exclude from calculation same time as used
        float spaceAfterTime = Style.applyTimeView(mTimeView) - reserved;
        float heightLeft = spaceAfterTime
                / Style.getScreenAspectRatioViewsCoeficient();
        mTimeView.setPadding(0, reserved, 0, 0);
        float secondLineHeight = heightLeft * 0.6f; //should be more that 0.55, beacuse of calculation base on this size
        mTemperatureView.setPadding(0, reserved, 0, reserved);
        float twHeight = secondLineHeight - reserved * 2;
        Style.applyTextsView(mTemperatureView, twHeight);
        findViewById(R.id.layout1).setMinimumHeight((int) (twHeight / 2));
        Rect tmp = new Rect();
        mTemperatureView.getPaint().getTextBounds("-88", 0, 3, tmp);
        mTemperatureView.setMinWidth(tmp.width());
        Style.applyTextsView(mDate1View, secondLineHeight * 0.40f - reserved); // sum should be 1.0 or less with next
        mDate1View.setPadding(0, reserved, 0, reserved);
        Style.applyTextsView(mDate2View, secondLineHeight * 0.24f - reserved);// sum should be 1.0 or less with prev
        mDate2View.setPadding(0, 0, 0, reserved);
        float alarmSize = secondLineHeight * 0.24f - reserved;
        Style.applyTextsView(mAlarm1View, alarmSize);
        mNowWeather.setPadding(0, reserved, 0, reserved);
        float thirdLineHeight = heightLeft - secondLineHeight;
        float extraSize = (heightLeft - 2 * thirdLineHeight) / 3.0f;
        mExtraData.setPadding(0, (int) extraSize, 0, (int) extraSize);
        Style.applyTextsView(mExtraData, extraSize);
        Style.applyTextsView(mAlarm2View, alarmSize);
        findViewById(R.id.layouExtraHs).setMinimumHeight((int) (extraSize * 3));
        findViewById(R.id.layoutWeekForecastHs).setMinimumHeight(
                (int) (twHeight / 2 - extraSize * 3));
        Style.applyWeatherView(mNowWeather, secondLineHeight - reserved * 2);
        float wH = thirdLineHeight - reserved / 2;
        for (WeatherView wv : mForecast) {
            Style.applyWeatherView(wv, wH);
        }
        for (WeatherView wv : mWeekForecast) {
            Style.applyWeatherView(wv, wH);
        }
        findViewById(R.id.dummyView3).setMinimumHeight(
                (int) (twHeight + wH / 2));

        int leftForDate = Style.getDisplayMetrics().widthPixels - tmp.width()
                - mNowWeather.getMaximumWidth();
        String maxDate = "88/88/8888";
        Style.adjustFontSizeForWidth(mDate1View, maxDate, leftForDate);

        enoughSpaceForWeekForecast = (spaceAfterTime - secondLineHeight
                - thirdLineHeight > wH * 2.05f); // 0.05f - reserved for padding

    }

    private void updateWeatherData() {
        List<Weather> today = mWeatherReciever.today();
        List<Weather> forecast = mWeatherReciever.forecast();
        //Log.i("tag",today.get(0).toString());
        //Log.i("tag",today.get(1).toString());
        //Log.i("tag", forecast.get(1).toString());

        int jCounter = 0;
        Date now = new Date();
        if (today != null && today.size() > 1) {
            if (today.get(0) == null || today.get(0).getDate() == null) {
                mTemperatureView.setText("");
                mNowWeather.clear();
            } else {
                if (Math.abs(now.getTime() - today.get(0).getDate().getTime()) > FORECAST_OUTDATED) { // more than 6 hour
                    mTemperatureView.setText("");
                    mNowWeather.setExpired();
                } else {
                    String twt = today.get(0).getTemperatureString();
                    if (twt.length() == 4 && twt.charAt(1) == '1') { // for short 1 in farenheit temperature in the begin
                        twt = twt.charAt(0) + Style.CHAR_CODE_SHORT_ONE
                                + twt.substring(2);
                    }
                    mTemperatureView.setText(twt);
                    mNowWeather.setWeather(today.get(0));
                }
            }

            // fill mForecast every WEATHER_FORECAST_SHOW_INTERVAL_HOURS from mWeatherReciever.today()
            Weather calcedWeather = null;

            for (int i = 1; i < today.size(); i++) {
                Weather forecastWeather = today.get(i);
                if (forecastWeather.getDate().after(now)) {
                    if (calcedWeather == null) {
                        calcedWeather = forecastWeather;
                    } else {
                        calcedWeather = calcedWeather.add(forecastWeather);
                        if (calcedWeather.getDatePeriodHours() >= WEATHER_FORECAST_SHOW_INTERVAL_HOURS) {
                            if (mForecast.size() > jCounter) {
                                mForecast.get(jCounter).setWeather(
                                        calcedWeather);
                                calcedWeather = null;
                                jCounter++;
                                if (mForecast.size() <= jCounter) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int i = jCounter; i < mForecast.size(); i++) {
            mForecast.get(i).clear();
        }

        boolean todayFound = false;
        int jWeekCounter = 0;
        if (forecast != null) {
            for (int i = 0; i < forecast.size(); i++) {
                Weather forecastWeather = forecast.get(i);
                boolean isToday = DateUtils.isToday(forecastWeather.getDate()
                        .getTime());
                if (isToday && forecastWeather.getCity() != null) {
                    Date d;
                    if ((d = forecastWeather.getCity().getSunSet()) != null) {
                        mSunSet = d;
                    }
                    if ((d = forecastWeather.getCity().getSunRise()) != null) {
                        mSunRise = d;
                    }
                }
                if (todayFound || isToday
                        || forecastWeather.getDate().after(now)) {
                    todayFound = true;
                    if (mWeekForecast.size() > jWeekCounter) {
                        mWeekForecast.get(jWeekCounter).setWeather(
                                forecastWeather);
                        jWeekCounter++;
                        if (mWeekForecast.size() <= jWeekCounter) {
                            break;
                        }
                    }
                }
            }
        }
        for (int i = jWeekCounter; i < mWeekForecast.size(); i++) {
            mWeekForecast.get(i).clear();
        }
        updateData();
        if (mUpdateWeatherCalled) {
            boolean isError = mWeatherReciever.isErrorWhileLastUpdate();
            Toast.makeText(
                    this,
                    getText(isError ? R.string.weather_updated_error
                            : R.string.weather_updated), Toast.LENGTH_SHORT)
                    .show();
            mUpdateWeatherCalled = false;
        }
    }

    private void updateData() {
        Date now = new Date();
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(getApplicationContext());
        String d = dateFormat.format(now);
        mDate1View.setText(d);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        String dow = sdf.format(now);
        mDate2View.setText(dow);

        DateFormat timeFormat = android.text.format.DateFormat
                .getTimeFormat(getApplicationContext());
        String sunrise = mSunRise == null ? getResources().getString(
                R.string.Unknown) : timeFormat.format(mSunRise);
        String sunset = mSunSet == null ? getResources().getString(
                R.string.Unknown) : timeFormat.format(mSunSet);

        String nextAlarm = Settings.System.getString(getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED);
        if (nextAlarm != null) {
            if (nextAlarm.length() > 0) {
                nextAlarm = Style.CHAR_CODE_ALARM + " " + nextAlarm;
            } else {
                nextAlarm = Style.CHAR_CODE_ALARM + " "
                        + getResources().getString(R.string.off);
            }
        } else {
            nextAlarm = "";
        }
        mAlarm1View.setText(nextAlarm);
        mAlarm2View.setText(nextAlarm);

        String res;
        if (enoughSpaceForWeekForecast) {
            res = String.format(getResources().getString(R.string.extra2),
                    sunrise, sunset);
        } else {
            res = String.format(getResources().getString(R.string.extra1), dow
                    + " " + d, sunrise, sunset);
        }
        mExtraData.setText(res);

        //mDate2View // fonts check
        //		.setText("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzАБВГДЕЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдезийклмнопрстуфхцчшщъыьэюя!\"\\%'()*/-+,.[]");
		/*mDate2View.setText(Style.CHAR_CODE_ALARM + "   " + Style.CHAR_CODE_AM
				+ "   " + Style.CHAR_CODE_CLOUD + "   "
				+ Style.CHAR_CODE_DEGREE + "   " + Style.CHAR_CODE_HUMIDITY
				+ "   " + Style.CHAR_CODE_PM + "   "
				+ Style.CHAR_CODE_PRECIPTATION);*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mNavBarHider.hide();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        mTimeViewUpdater.start();
		/*if (*/mTimeViewUpdater.retrive24Format();/*) { // due to the font pm and am don't take place on screen
													Style.applyTimeView(mTimeView);
													}*/
        updateData();
        mNavBarHider.start();
        AutoOnReceiver.clearFlagsIfNeed(getWindow(), getIntent());
    }

    private int prefsVisibility(SharedPreferences prefs, int keyResId) {
        // doesn't care about defaults value - always true, becuae values should be set before call
        if (prefs.getBoolean(getText(keyResId).toString(), true)) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    private void loadSettings() { // default setting doesn't make any sense
        PreferenceManager.setDefaultValues(this, R.xml.preference, false); // because of this call
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        int orientation = Integer.parseInt(prefs.getString(
                getString(R.string.key_orientation),
                String.valueOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)));
        setRequestedOrientation(orientation);

        int tUnit = Integer.parseInt(prefs.getString(
                getString(R.string.key_tunit), "0"));
        WeatherUnits
                .setTemperatureUnits(WeatherUnits.TemperatureUnits.values()[tUnit]);
        int pUnit = Integer.parseInt(prefs.getString(
                getString(R.string.key_punit), "0"));
        WeatherUnits
                .setPressureUnits(WeatherUnits.PressureUnits.values()[pUnit]);
        int sUnit = Integer.parseInt(prefs.getString(
                getString(R.string.key_sunit), "0"));
        WeatherUnits.setSpeedUnits(WeatherUnits.SpeedUnits.values()[sUnit]);

        int blevel = Integer.parseInt(prefs.getString(
                getString(R.string.key_blevel), "80"));
        boolean autoclose = prefs.getBoolean(getText(R.string.key_autoclose)
                .toString(), false);
        if (blevel >= 0 || autoclose) {
            mBatteryScreenChecker = new BatteryScreenReciever(this, blevel,
                    autoclose);
        }

        String val = prefs.getString(getText(R.string.key_dot).toString(),
                getText(R.string.value_dot_flash).toString());
        if (val.equals(getText(R.string.value_dot_flash).toString())) {
            TimeViewUpdater.setDotMode(DOT_MODE.DOT_FLASH);
        } else if (val.equals(getText(R.string.value_dot_perm).toString())) {
            TimeViewUpdater.setDotMode(DOT_MODE.DOT_PERMANENT);
        } else if (val.equals(getText(R.string.value_dot_no).toString())) {
            TimeViewUpdater.setDotMode(DOT_MODE.DOT_NO);
        }

        mTimeViewUpdater.setSecondsVisible(prefs.getBoolean(
                getText(R.string.key_seconds).toString(), false));

        int now = prefsVisibility(prefs, R.string.key_now);
        mTemperatureView.setVisibility(now);
        mNowWeather.setVisibility(now);

        int today = prefsVisibility(prefs, R.string.key_today);
        mDate1View.setVisibility(today);
        mDate2View.setVisibility(today);
        int sun = prefsVisibility(prefs, R.string.key_sun);
        mExtraData.setVisibility(sun);

        int alarm = prefsVisibility(prefs, R.string.key_alarm);
        mAlarm1View.setVisibility(alarm);
        mAlarm2View.setVisibility(alarm);

        int weather24 = prefsVisibility(prefs, R.string.key_weather_24h);
        for (WeatherView w : mForecast) {
            w.setVisibility(weather24);
        }

        if (weather24 == View.GONE && now == View.VISIBLE) { // margin from time if no weather displaying
            findViewById(R.id.dummyView3).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.dummyView3).setVisibility(View.GONE);
        }

        int weatherWeek = prefsVisibility(prefs, R.string.key_weather_week);
        for (WeatherView w : mWeekForecast) {
            w.setVisibility(weatherWeek);
        }

        if (prefs.getString(getString(R.string.key_brightness), "").equals(
                getString(R.string.value_brightness_camera))) {
            mLightSensor = new CameraAsLightSensor(this);
        } else if (prefs.getString(getString(R.string.key_brightness), "")
                .equals(getString(R.string.value_brightness_manual))) {
            CameraAsLightSensor.applyManualBrightness(getWindow(),
                    BrightnessDialog.getBrighnessLevel(prefs, this));
        } else {
            CameraAsLightSensor.applySystemBrighness(getWindow());
        }

        setColor(ColorDialog.getTextColorInt(prefs, this));
        findViewById(R.id.rootView).setBackgroundColor(
                ColorDialog.getBackgroundColorInt(prefs, this));

        int weatherUpdateIntervalMin = Integer.parseInt(prefs.getString(
                getString(R.string.key_update), "0"));
        int cityId = CityDialog.getCityId(prefs, this);
        if (weather24 == View.VISIBLE || now == View.VISIBLE
                || weatherWeek == View.VISIBLE || sun == View.VISIBLE) {
            mWeatherReciever.start(cityId, weatherUpdateIntervalMin);
        }

        if (CityDialog.checkFirstTime(prefs, this)) {
            final Intent intent = new Intent(this, CityDialog.class);
            startActivity(intent);
            Toast.makeText(this, getString(R.string.firstTime),
                    Toast.LENGTH_LONG).show();
        }

        if (enoughSpaceForWeekForecast) {
            mLayoutWeekForecast.setVisibility(View.VISIBLE);
            mLayoutToday.setVisibility(View.VISIBLE);
            mAlarm1View.setVisibility(View.GONE);
        } else {
            if (prefs.getBoolean(getText(R.string.key_flip).toString(), false)) {
                mLayoutWeekForecast.setVisibility(View.VISIBLE);
                mLayoutToday.setVisibility(View.GONE);
            } else {
                mLayoutWeekForecast.setVisibility(View.GONE);
                mLayoutToday.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimeViewUpdater.stop();
        if (mBatteryScreenChecker != null) {
            mBatteryScreenChecker.free();
            mBatteryScreenChecker = null;
        }
        if (mLightSensor != null) {
            mLightSensor.free();
            mLightSensor = null;
        }
        mNavBarHider.stop();
        mWeatherReciever.stop();

        if (!enoughSpaceForWeekForecast) {
            PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(getString(R.string.key_flip),
                            mLayoutToday.getVisibility() != View.VISIBLE)
                    .commit();
        }
        AutoOnReceiver.prepareFlagsIfNeed(getWindow(), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        createMenu(menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View v,
                                    android.view.ContextMenu.ContextMenuInfo menuInfo) {
        createMenu(menu);
    }

    private void createMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.clock, menu);
        menu.findItem(R.id.m_update_weather).setVisible(
                mWeatherReciever.isWorking());
        if (android.os.Build.VERSION.SDK_INT < 11) {
            menu.findItem(R.id.m_setup_alarm).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onContextItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_update_weather:
                if (mWeatherReciever.isWorking()) {
                    mUpdateWeatherCalled = true;
                    mWeatherReciever.update(true);
                }
                return true;
            case R.id.m_setup_alarm:
                mShowAlarmSet.onClick(null);
                return true;
            case R.id.m_settings:
                startActivity(new Intent(this, Preference.class));
                return true;
            case R.id.m_share:
                ClockDialogs.share(this);
                return true;
            case R.id.m_rateus:
                ClockDialogs.rateUs(this);
                return true;
            case R.id.m_about:
                ClockDialogs.about(this);
                return true;
            case R.id.m_exit:
                finish();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private final OnClickListener mShowAlarmSet = new OnClickListener() {
        @SuppressLint("InlinedApi")
        @Override
        public void onClick(View v) {
            if (android.os.Build.VERSION.SDK_INT >= 9) {
                if (v == mAlarm1View || v == mAlarm2View) {
                    Animations.click(v, this);
                } else {
                    try {
                        Intent i = new Intent(
                                android.os.Build.VERSION.SDK_INT >= 19 ? AlarmClock.ACTION_SHOW_ALARMS
                                        : AlarmClock.ACTION_SET_ALARM);
                        startActivity(i);
                    } catch (Exception e) {
                        Toast.makeText(ClockActivity.this, R.string.alerror,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                mWheatherFlipListener.onClick(v);
            }
        }
    };

    private final OnClickListener mShowMenu = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mTimeView) {
                Animations.click(v, this);
            } else {
                openContextMenu(mTimeView);
            }
        }
    };

    private final OnClickListener mWheatherFlipListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mLayoutToday.getVisibility() == View.VISIBLE) {
                showWeekForecast();
            } else {
                showToday();
            }
        }
    };

    public void showWeekForecast() {
        if (enoughSpaceForWeekForecast) {
            return;
        }
        Animations.flip(mLayoutWeekForecast, mLayoutToday);
        mNavBarHider.hideDelayed();
    }

    public void showToday() {
        if (enoughSpaceForWeekForecast) {
            return;
        }
        Animations.flip(mLayoutToday, mLayoutWeekForecast);
        mNavBarHider.hideDelayed();
    }

    private void setColor(int color) {
        mTimeView.setTextColor(color);
        mTemperatureView.setTextColor(color);
        mDate1View.setTextColor(color);
        mDate2View.setTextColor(color);
        mAlarm1View.setTextColor(color);
        mAlarm2View.setTextColor(color);
        mExtraData.setTextColor(color);
        mNowWeather.setTextColor(color);
        for (WeatherView wv : mForecast) {
            wv.setTextColor(color);
        }
        for (WeatherView wv : mWeekForecast) {
            wv.setTextColor(color);
        }
    }
}