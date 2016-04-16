package com.example.mymusicplayer.ui;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

/**
 * A button that will repeatedly call a 'listener' method as long as the button
 * is pressed.
 */
public class RepeatingImageButton extends ImageButton {

	private long mStartTime;
	private int mRepeatCount;
	private RepeatListener mListener;
	private long mInterval = 500;

	public RepeatingImageButton(Context context) {
		super(context, null);
	}

	public RepeatingImageButton(Context context, AttributeSet attrs) {
		super(context, attrs, android.R.attr.imageButtonStyle);
	}

	public RepeatingImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// ���û�ý���͵��������
		setFocusable(true);
		setClickable(true);
	}

	/**
	 * 
	 * @param listener
	 *            ���ᱻ�ص���listener
	 * @param interval
	 *            �ص��ļ��
	 */
	public void setRepeatListener(RepeatListener listener, long interval) {
		mListener = listener;
		mInterval = interval;
	}

	@Override
	public boolean performLongClick() {
		// elapsed milliseconds since boot
		mStartTime = SystemClock.elapsedRealtime();
		mRepeatCount = 0;
		post(mRepeater);
		return true;
		// True if one of the above receivers consumed the event, false
		// otherwise.
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			// remove the repeater, but call the hook one more time
			removeCallbacks(mRepeater);
			if (mStartTime != 0) {
				doRepeat(true);
				mStartTime = 0;
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			// ��Ҫ���ø�����ɳ���������return true����application�õ�����ʱ��
			super.onKeyDown(keyCode, event);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:

			break;

		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private Runnable mRepeater = new Runnable() {

		@Override
		public void run() {
			doRepeat(false);
			if (isPressed()) {
				postDelayed(this, mInterval);
			}
		}

	};

	private void doRepeat(boolean last) {
		long now = SystemClock.elapsedRealtime();
		if (mListener != null) {
			mListener.onRepeat(this, now - mStartTime, last ? -1 : mRepeatCount++);
		}
	}

	public interface RepeatListener {
		/**
		 * 
		 * @param view
		 *            ��Ϊ��ť��view
		 * @param duration
		 *            ��ť���µ�ʱ��
		 * @param repeatcount
		 *            �������µĴ���
		 */
		void onRepeat(View view, long duration, int repeatcount);

	}

}
