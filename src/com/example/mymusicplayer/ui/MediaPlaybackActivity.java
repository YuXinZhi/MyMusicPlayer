package com.example.mymusicplayer.ui;

import com.example.mymusicplayer.utils.MusicUtils;
import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

public class MediaPlaybackActivity extends Activity
		implements MusicUtils.Defs, View.OnTouchListener, View.OnLongClickListener {

	public MediaPlaybackActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * 设置音量键控制的音量（音乐）
		 */
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
	}

	private static class Worker implements Runnable {

		private final Object mLock = new Object();
		private Looper mLooper;

		/**
		 * 创建一个name线程
		 * 
		 * @param name
		 */
		public Worker(String name) {
			Thread t = new Thread(null, this, name);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
			synchronized (mLock) {
				while (mLooper == null) {
					try {
						mLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		public Looper getLooper() {
			return mLooper;
		}

		@Override
		public void run() {
			synchronized (mLock) {
				Looper.prepare();
				mLooper = Looper.myLooper();
				mLock.notifyAll();
			}
			Looper.loop();
		}

		public void quit() {
			mLooper.quit();
		}

	}

	/**
	 * 轮询器 android.os.Looper
	 * 
	 * Class used to run a message loop for a thread. Threads by default do not
	 * have a message loop associated with them; to create one, call prepare in
	 * the thread that is to run the loop, and then loop to have it process
	 * messages until the loop is stopped.
	 * 
	 * Most interaction with a message loop is through the Handler class.
	 * 
	 * This is a typical example of the implementation of a Looper thread, using
	 * the separation of prepare and loop to create an initial Handler to
	 * communicate with the Looper.
	 * 
	 * class LooperThread extends Thread { public Handler mHandler;
	 * 
	 * public void run() { Looper.prepare();
	 * 
	 * mHandler = new Handler() { public void handleMessage(Message msg) { //
	 * process incoming messages here } };
	 * 
	 * Looper.loop(); } }
	 * 
	 */

	@Override
	public boolean onLongClick(View v) {
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
}
