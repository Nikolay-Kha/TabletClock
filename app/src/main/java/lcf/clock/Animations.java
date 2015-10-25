package lcf.clock;

import java.util.ArrayList;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

abstract class Animations {
	private static Animation mHideAnimation = null;
	private static Animation mShowAnimation = null;
	private static Animation mClickInAnimation = null;
	private static Animation mClickOutAnimation = null;
	private static final int ANIMATION_DURATION = 100;
	private static ArrayList<View> mWorkingAnimation = new ArrayList<View>();

	private static class animationListener implements AnimationListener {

		private final View mViewToHide;
		private final View mViewToShow;
		private final OnClickListener mOnClickListener;

		public animationListener(View viewToHide, View viewToShow,
				OnClickListener onClickListener) {
			mViewToHide = viewToHide;
			mViewToShow = viewToShow;
			mOnClickListener = onClickListener;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (animation == mShowAnimation || animation == mClickInAnimation) {
				mViewToShow.clearAnimation();
				if (mViewToHide != null) {
					mWorkingAnimation.remove(mViewToHide);
				}
				mWorkingAnimation.remove(mViewToShow);
				if (mOnClickListener != null) {
					mOnClickListener.onClick(null);
				}
			} else if (animation == mClickOutAnimation) {
				if (mClickInAnimation == null) {
					mClickInAnimation = createClickInAnimation();
				}
				mClickInAnimation.setAnimationListener(new animationListener(
						null, mViewToShow, mOnClickListener));
				mViewToShow.startAnimation(mClickInAnimation);

			} else if (animation == mHideAnimation) {
				mViewToHide.setVisibility(View.GONE);
				mViewToHide.clearAnimation();

				if (mShowAnimation == null) {
					mShowAnimation = createShowAnimation();
				}
				mShowAnimation.setAnimationListener(new animationListener(
						mViewToHide, mViewToShow, null));
				mViewToShow.setVisibility(View.VISIBLE);
				mViewToShow.startAnimation(mShowAnimation);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

	}

	public static boolean click(View view, OnClickListener onClickListener) {
		if (mWorkingAnimation.indexOf(view) != -1) {
			return false;
		}
		mWorkingAnimation.add(view);
		if (mClickOutAnimation == null) {
			mClickOutAnimation = createClickOutAnimation();
		}
		mClickOutAnimation.setAnimationListener(new animationListener(null,
				view, onClickListener));
		view.startAnimation(mClickOutAnimation);
		return true;
	}

	public static boolean flip(View viewToShow, View viewToHide) {
		if (mWorkingAnimation.indexOf(viewToShow) != -1
				|| mWorkingAnimation.indexOf(viewToHide) != -1) {
			return false;
		}
		mWorkingAnimation.add(viewToShow);
		mWorkingAnimation.add(viewToHide);
		if (mHideAnimation == null) {
			mHideAnimation = createHideAnimation();
		}
		mHideAnimation.setAnimationListener(new animationListener(viewToHide,
				viewToShow, null));
		viewToHide.startAnimation(mHideAnimation);
		return true;
	}

	private static Animation createShowAnimation() {
		ScaleAnimation anim = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setDuration(ANIMATION_DURATION);
		return anim;
	}

	private static Animation createHideAnimation() {
		ScaleAnimation anim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setDuration(ANIMATION_DURATION);
		return anim;
	}

	private static Animation createClickInAnimation() {
		ScaleAnimation anim = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setDuration(ANIMATION_DURATION / 2);
		return anim;
	}

	private static Animation createClickOutAnimation() {
		ScaleAnimation anim = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.6f);
		anim.setDuration(ANIMATION_DURATION / 2);
		return anim;
	}

}
