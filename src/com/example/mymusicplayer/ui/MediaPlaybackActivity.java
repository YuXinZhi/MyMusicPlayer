package com.example.mymusicplayer.ui;

import com.example.mymusicplayer.utils.MusicUtils;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MediaPlaybackActivity extends Activity
		implements MusicUtils.Defs, View.OnTouchListener, View.OnLongClickListener {

	private Worker mAlbumArtWorker;

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

	private ImageView mAlbum;

	private static final int REFRESH = 1;
	private static final int QUIT = 2;
	private static final int GET_ALBUM_ART = 3;
	private static final int ALBUM_ART_DECODED = 4;

	private void queueNextFresh(long next) {

	}

	private long refreshNow() {
		return 0;
	}

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ALBUM_ART_DECODED:
				mAlbum.setImageBitmap((Bitmap) msg.obj);
				// Dither（图像的抖动处理，当每个颜色值以低于8位表示时，
				// 对应图像做抖动处理可以实现在可显示颜色总数比较低（比如256色）
				// 时还保持较好的显示效果：
				mAlbum.getDrawable().setDither(true);
				break;

			case REFRESH:
				long next = refreshNow();
				queueNextFresh(next);
				break;

			case QUIT:
				/**
				 * <string name="service_start_error_title" >"播放问题"
				 * name="service_start_error_msg" "无法播放此歌曲。"
				 * name="service_start_error_button" "确定"
				 */
				new AlertDialog.Builder(MediaPlaybackActivity.this).setTitle("播放问题").setMessage("无法播放此歌曲")
						.setPositiveButton("确定", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						}).setCancelable(false).show();
				break;
			default:
				break;
			}
		}

	};

	public class AlbumArtHandler extends Handler {
		private long mAlbumId = -1;

		public AlbumArtHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			long albumid = ((AlbumSongIdWrapper) msg.obj).albumid;
			long songid = ((AlbumSongIdWrapper) msg.obj).songid;
			if (msg.what == GET_ALBUM_ART && (mAlbumId != albumid || albumid < 0)) {\
				// while decoding the new image, show the default album art
				Message numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, null);
				mHandler.removeMessages(ALBUM_ART_DECODED);
				mHandler.sendMessageDelayed(numsg, 300);
				// Don't allow default artwork here, because we want to fall
				// back to song-specific
				// album art if we can't find anything for the album.
				Bitmap bm=MusicUtils.getArtWork(MediaPlaybackActivity.this,songid,albumid,false);
			}

		}

	}

	private static class AlbumSongIdWrapper {
		public long albumid;
		public long songid;

		AlbumSongIdWrapper(long aid, long sid) {
			albumid = aid;
			songid = sid;
		}
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
