package lcf.clock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class BigTextView extends TextView {
	private Bitmap mBitmap = null;
	private Canvas mCanvas = null;
	private Rect mRect;
	private boolean mUpdateNeed = true;

	public BigTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mUpdateNeed && mCanvas != null) {
			mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			super.onDraw(mCanvas);
			mUpdateNeed = false;
		}
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, mRect, mRect, null);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if ((w != oldw || h != oldh || mBitmap == null) && w > 0 && h > 0) {
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mRect = new Rect(0, 0, w, h);
			mCanvas = new Canvas(mBitmap);
		}
	}

	@Override
	public void invalidate() {
		mUpdateNeed = true;
		super.invalidate();
	}

	@Override
	public void invalidate(int l, int t, int r, int b) {
		mUpdateNeed = true;
		super.invalidate(l, t, r, b);
	}

	@Override
	public void invalidate(Rect dirty) {
		mUpdateNeed = true;
		super.invalidate(dirty);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		mUpdateNeed = true;
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}
}
