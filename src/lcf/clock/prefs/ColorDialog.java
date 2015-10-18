package lcf.clock.prefs;

import lcf.clock.R;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;

public class ColorDialog extends TimePrefsDialogs {
	private SeekBar mRSeek;
	private SeekBar mGSeek;
	private SeekBar mBSeek;
	private TextView mLevelR;
	private TextView mLevelG;
	private TextView mLevelB;
	public static final String EXTRA_BACKGROUND_BOOLEAN = "lcf.clock.prefs.background";

	private final OnClickListener mColorListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Drawable d = v.getBackground();
			Bitmap bmp;
			Canvas c = new Canvas(bmp = Bitmap.createBitmap(1, 1,
					Bitmap.Config.ARGB_8888));
			Rect oldRect = d.copyBounds();
			d.setBounds(0, 0, 1, 1);
			d.draw(c);
			d.setBounds(oldRect);

			setSeekColor(bmp.getPixel(0, 0));
		}
	};

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.color_dialog);
		applySize(R.id.dcroot);
		initTimeViewAndButtonsPreview(R.id.dtimeView2, R.id.buttonLayout);

		findViewById(R.id.button2).setOnClickListener(this);
		mRSeek = (SeekBar) findViewById(R.id.rSeek);
		mGSeek = (SeekBar) findViewById(R.id.gSeek);
		mBSeek = (SeekBar) findViewById(R.id.bSeek);
		mRSeek.setMax(255);
		mGSeek.setMax(255);
		mBSeek.setMax(255);
		mRSeek.setOnSeekBarChangeListener(this);
		mGSeek.setOnSeekBarChangeListener(this);
		mBSeek.setOnSeekBarChangeListener(this);
		mLevelR = (TextView) findViewById(R.id.colorR);
		mLevelG = (TextView) findViewById(R.id.colorG);
		mLevelB = (TextView) findViewById(R.id.colorB);

		reservePlaceForSeekBarsLevels(mLevelR);
		reservePlaceForSeekBarsLevels(mLevelG);
		reservePlaceForSeekBarsLevels(mLevelB);

		findViewById(R.id.buttonR).setOnClickListener(mColorListener);
		findViewById(R.id.buttonG).setOnClickListener(mColorListener);
		findViewById(R.id.buttonB).setOnClickListener(mColorListener);
		findViewById(R.id.buttonO).setOnClickListener(mColorListener);
		findViewById(R.id.buttonY).setOnClickListener(mColorListener);
		findViewById(R.id.buttonV).setOnClickListener(mColorListener);
		findViewById(R.id.buttonC).setOnClickListener(mColorListener);
		findViewById(R.id.buttonP).setOnClickListener(mColorListener);
		findViewById(R.id.buttonW).setOnClickListener(mColorListener);
		findViewById(R.id.buttonBl).setOnClickListener(mColorListener);

	}

	@Override
	public void onResume() {
		super.onResume();
		int color;
		if (getIntent().getBooleanExtra(EXTRA_BACKGROUND_BOOLEAN, false)) {
			color = mBackgroundColor;
			setTitle(R.string.cat3_background_color);
		} else {
			color = mTextColor;
			setTitle(R.string.cat3_color);
		}
		setSeekColor(color);
		updateData(); // if color is same, but we need to update text
	}

	private void setSeekColor(int c) {
		int color = c & 0xFFFFFF;
		mRSeek.setProgress(color / 0x10000);
		mGSeek.setProgress((color & 0xFF00) / 0x100);
		mBSeek.setProgress(color & 0xFF);
	}

	private String componentToHex(int c) {
		return String.format("%02X", c);
	}

	private void updateData() {
		int r = mRSeek.getProgress();
		int g = mGSeek.getProgress();
		int b = mBSeek.getProgress();
		mLevelR.setText(componentToHex(r));
		mLevelG.setText(componentToHex(g));
		mLevelB.setText(componentToHex(b));

		int color = 0xFF000000 + r * 0x10000 + g * 0x100 + b;
		if (getIntent().getBooleanExtra(EXTRA_BACKGROUND_BOOLEAN, false)) {
			mRootView.setBackgroundColor(color);
			mBackgroundColor = color;
		} else {
			mTimeView.setTextColor(color);
			mTextColor = color;
		}
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean fromUser) {
		updateData();
	}

	@Override
	protected void commitData() {
		mSharedPreferences
				.edit()
				.putInt(getString(R.string.key_background_color),
						mBackgroundColor).commit();
		mSharedPreferences.edit()
				.putInt(getString(R.string.key_color), mTextColor).commit();

	}

}
