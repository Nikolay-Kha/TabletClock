package lcf.clock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class CameraAsLightSensor implements Camera.PreviewCallback {
	private final Activity mActivity;
	private static final int MEASURE_INTERVAL_MS = 30000;
	private static final float MIN_BRIGHTNESS = 0.02f;
	private Camera mCamera = null;
	HandlerThread mHandlerThread;
	private final Handler mHandler;
	private final Handler mUiHandler;
	private int mPreviewWidth = 0;
	private int mPreviewHeight = 0;
	@SuppressWarnings("unused")
	private Object mPreview = null; // keep invisible preview
	private boolean mInitCamera = false;
	private float mLastMeasuredBrightness;
	private volatile boolean mFree = false;

	private final Runnable mMeasureRunnable = new Runnable() {
		@Override
		public void run() {
			measure();
		}
	};

	private final Runnable mSetBrighnessRunnable = new Runnable() {
		@Override
		public void run() {
			applyManualBrightness(mActivity.getWindow(),
					mLastMeasuredBrightness);
		}
	};

	public CameraAsLightSensor(Activity activity) {
		mHandlerThread = new HandlerThread("CameraAsLightSensorThread");
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
		mUiHandler = new Handler();
		mActivity = activity;

		mHandler.postDelayed(mMeasureRunnable, 1000);
	}

	public void free() {
		synchronized (this) {
			mFree = true;
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
			mHandler.removeCallbacks(mMeasureRunnable);
			mUiHandler.removeCallbacks(mSetBrighnessRunnable);
			mHandlerThread.quit();
			mInitCamera = false;
			mPreview = null;
		}
	}

	private void measure() {
		synchronized (this) {
			if (mFree) {
				return;
			}
			if (!mInitCamera) {
				initCamera();
				mInitCamera = true;
			}
			if (mCamera != null) {
				mCamera.startPreview();
				mCamera.setOneShotPreviewCallback(this);
			}
		}
	}

	@SuppressLint("NewApi")
	private void initCamera() {
		int cameraCount = 0;
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			try {
				cameraCount = Camera.getNumberOfCameras();
				for (int camIdx = cameraCount - 1; camIdx >= 0; camIdx--) {
					Camera.getCameraInfo(camIdx, cameraInfo);
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
						mCamera = Camera.open(camIdx);
						if (mCamera != null) {
							break;
						}
					}
				}
			} catch (final Exception e) {
				;
			}
		}
		if (mCamera == null) {
			mCamera = Camera.open();
		}
		if (mCamera != null) {
			final Camera.Parameters parametrs = mCamera.getParameters();
			Camera.Size min = null;
			int smin = -1;
			for (Camera.Size sz : parametrs.getSupportedPreviewSizes()) {
				if (smin == -1 || smin > sz.width * sz.height) {
					min = sz;
					smin = sz.width * sz.height;
				}
			}
			if (min != null) {
				parametrs.setPreviewSize(min.width, min.height);
			}
			mPreviewWidth = parametrs.getPreviewSize().width;
			mPreviewHeight = parametrs.getPreviewSize().height;

			if (parametrs.getSupportedPreviewFormats().contains(
					ImageFormat.NV21)) {
				parametrs.setPreviewFormat(ImageFormat.NV21);
			}

			if (android.os.Build.VERSION.SDK_INT >= 9) {
				smin = -1;
				int[] mmin = null;
				for (int[] f : parametrs.getSupportedPreviewFpsRange()) {
					if ((smin == -1 || f[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] < smin)
							&& f[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] > 0) {
						mmin = f;
						smin = f[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
					}
				}
				if (mmin != null) {
					parametrs.setPreviewFpsRange(
							mmin[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
							mmin[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
				}
			}

			try {
				if (android.os.Build.VERSION.SDK_INT >= 11) {
					SurfaceTexture surfaceTexture = new SurfaceTexture(
							R.string.app_name);
					mCamera.setPreviewTexture(surfaceTexture);
					mPreview = surfaceTexture;
				} else {
					SurfaceView mSurfaceView = new SurfaceView(mActivity);
					mCamera.setPreviewDisplay(mSurfaceView.getHolder());
					mPreview = mSurfaceView;

				}
			} catch (Exception e) {
			}

		}
	}

	@Override
	public void onPreviewFrame(final byte[] data, final Camera camera) {
		synchronized (this) {
			if (mCamera != null) {
				mCamera.stopPreview();
			}
		}
		try {
			int mid = -1;
			for (int j = 0, yp = 0; j < mPreviewHeight; j++) {
				for (int i = 0; i < mPreviewWidth; i++, yp++) {
					final int y = data[yp] & 0xff;
					if (mid == -1) {
						mid = y;
					} else {
						mid = (mid + y) / 2;
					}
				}
			}

			float a = (mid - 18) / 127.0f;
			if (a < 0.02f) {
				a = 0.02f;
			}
			if (a >= 1.00f) {
				a = 1.00f;
			}
			mLastMeasuredBrightness = a;
			//Log.i("tag", "CameraAsLightSensor result = "
			//		+ mLastMeasuredBrightness + " (" + mid + ")");
			mUiHandler.post(mSetBrighnessRunnable);
		} catch (final Exception e) {
			;
		}
		synchronized (this) {
			if (!mFree) {
				mHandler.postDelayed(mMeasureRunnable, MEASURE_INTERVAL_MS);
			}
		}
	}

	public static void applySystemBrighness(Window window) {
		applyManualBrightness(window, -1.0f); // less than 0 in WindowManager.LayoutParams.screenBrightness
	}

	public static void applyManualBrightness(Window window, float value) {
		if (value >= 0.0f && value < MIN_BRIGHTNESS) {
			value = MIN_BRIGHTNESS;
		}
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = value;
		window.setAttributes(lp);
	}
}
