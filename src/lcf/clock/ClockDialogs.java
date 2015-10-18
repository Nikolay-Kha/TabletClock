package lcf.clock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.widget.TextView;

abstract public class ClockDialogs {

	static void rateUs(Context context) {
		final Intent browserIntent = new Intent("android.intent.action.VIEW",
				Uri.parse("market://details?id=" + context.getPackageName()));
		browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(browserIntent);
	}

	static void share(Context context) {
		final Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_SUBJECT,
				context.getText(R.string.app_name));
		sendIntent.putExtra(
				Intent.EXTRA_TEXT,
				context.getText(R.string.share_text)
						+ " https://market.android.com/details?id="
						+ context.getPackageName());
		context.startActivity(Intent.createChooser(sendIntent,
				context.getText(R.string.share)));
	}

	private static final LinkMovementMethod mMovementCheck = new LinkMovementMethod() {
		@Override
		public boolean onTouchEvent(TextView widget, Spannable buffer,
				MotionEvent event) {
			try {
				return super.onTouchEvent(widget, buffer, event);
			} catch (Exception ex) {
				return true;
			}
		}
	};

	static void about(Context context) {
		final SpannableString s = new SpannableString(
				context.getText(R.string.aboutText));
		Linkify.addLinks(s, Linkify.ALL);
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(context.getText(R.string.aboutTitle));
		alertDialog.setMessage(s);
		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
				context.getText(R.string.aboutCloseButton),
				new Dialog.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						try {
							finalize();
						} catch (final Throwable e) {
							e.printStackTrace();
						}
					}
				});
		alertDialog.show();
		((TextView) alertDialog.findViewById(android.R.id.message))
				.setMovementMethod(mMovementCheck);
	}

}
