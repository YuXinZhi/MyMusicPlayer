package com.example.mymusicplayer.ui;

import com.example.mymusicplayer.IMediaPlaybackService;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.utils.MusicUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MediaPlaybackActivity extends Activity
		implements MusicUtils.Defs, View.OnTouchListener, View.OnLongClickListener {
	private boolean mSeeking = false;
	private long mStartSeekPos = 0;
	private long mLastSeekEventTime;
	private boolean mDeviceHasDpad;
	private IMediaPlaybackService mService = null;
	private boolean mFromTouch = false;
	private long mDuration;
	private Worker mAlbumArtWorker;
	private AlbumArtHandler mAlbumArtHandler;

	private RepeatingImageButton mPrevButton;
	private ImageButton mPauseButton;
	private RepeatingImageButton mNextButton;

	public MediaPlaybackActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * 设置音量键控制的音量（音乐）
		 */
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mAlbumArtWorker = new Worker("album art work");
		mAlbumArtHandler = new AlbumArtHandler(mAlbumArtWorker.getLooper());
		// Enable extended window features. This is a convenience for calling
		// getWindow().requestFeature().
		// 设置MediaPlaybackActivity布局
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.audio_player);

		mCurrentTime = (TextView) findViewById(R.id.currenttime);
		mTotalTime = (TextView) findViewById(R.id.totaltime);
		mProgress = (ProgressBar) findViewById(android.R.id.progress);
		mAlbum = (ImageView) findViewById(R.id.album);
		mArtistName = (TextView) findViewById(R.id.artistname);
		mAlbumName = (TextView) findViewById(R.id.albumname);
		mTrackName = (TextView) findViewById(R.id.trackname);

		// 设置三个图标（歌手，专辑，歌名）的监听
		View v = (View) mArtistName.getParent();
		v.setOnTouchListener(this);
		v.setOnLongClickListener(this);

		v = (View) mAlbumName.getParent();
		v.setOnTouchListener(this);
		v.setOnLongClickListener(this);

		v = (View) mTrackName.getParent();
		v.setOnTouchListener(this);
		v.setOnLongClickListener(this);

		// 设置播放按钮的监听
		mPrevButton = (RepeatingImageButton) findViewById(R.id.prev);
		mPrevButton.setOnClickListener(mPrevListener);
		mPrevButton.setRepeatListener(mRewListener, 260);
		mPauseButton = (ImageButton) findViewById(R.id.pause);
		mPauseButton.requestFocus();
		mPauseButton.setOnClickListener(mPauseListener);
		mNextButton = (RepeatingImageButton) findViewById(R.id.next);
		mNextButton.setOnClickListener(mNextListener);
		mNextButton.setRepeatListener(mFfwdListener, 260);

		// 判断是否有方向键盘
		mDeviceHasDpad = (getResources().getConfiguration().navigation == Configuration.NAVIGATION_DPAD);

		if (mProgress instanceof SeekBar) {
			SeekBar seeker = (SeekBar) mProgress;
			// 设置进度条监听器
			seeker.setOnSeekBarChangeListener(mSeekListener);
		}

	}

	private RepeatingImageButton.RepeatListener mRewListener = new RepeatingImageButton.RepeatListener() {

		@Override
		public void onRepeat(View view, long duration, int repeatcount) {
			scanBackward(repeatcount, duration);
		}

	};

	private RepeatingImageButton.RepeatListener mFfwdListener = new RepeatingImageButton.RepeatListener() {
		/**
		 * view 作为按钮的view duration 按钮按下的时间 repeatcount 连续按下的次数
		 */
		@Override
		public void onRepeat(View view, long duration, int repeatcount) {
			scanForward(repeatcount, duration);
		}
	};

	private void scanBackward(int repeatcount, long duration) {
		if (mService == null) {
			return;
		}
		try {
			if (repeatcount == 0) {
				mStartSeekPos = mService.position();
				mLastSeekEventTime = 0;
				mSeeking = false;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void scanForward(int repeatcount, long duration) {

	}

	private View.OnClickListener mPauseListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			doPauseResume();
		}

	};

	private void doPauseResume() {
		try {
			if (mService != null) {
				if (mService.isPlaying()) {
					mService.pause();
				} else {
					mService.play();
				}
				refreshNow();
				setPauseButtonImage();

			}
		} catch (RemoteException ex) {

		}
	}

	// zhu暂停/播放按鈕
	private void setPauseButtonImage() {
		try {
			if (mService != null && mService.isPlaying()) {
				mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
			} else {
				mPauseButton.setImageResource(android.R.drawable.ic_media_play);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private View.OnClickListener mPrevListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mService == null) {
				return;
			}
			try {
				if (mService.position() < 2000) {
					mService.prev();
				} else {
					mService.seek(0);
					mService.play();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	private View.OnClickListener mNextListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mService == null) {
				return;
			}
			try {
				mService.next();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	// 歌曲进度条的监听
	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mLastSeekEventTime = 0;
			mFromTouch = true;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (!fromUser || (mService == null)) {
				return;
			}
			long now = SystemClock.elapsedRealtime();
			// 两次时间差
			if ((now - mLastSeekEventTime) > 250) {
				mLastSeekEventTime = now;
				// 当前歌曲播放的时间
				mPosOverride = mDuration * progress / 1000;
				try {
					mService.seek(mPosOverride);
				} catch (RemoteException e) {
					e.printStackTrace();
				}

				// trackball(轨迹球；追踪球) event, allow progress updates
				if (!mFromTouch) {
					refreshNow();
					mPosOverride = -1;
				}
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mPosOverride = -1;
			mFromTouch = false;
		}
	};

	private ImageView mAlbum;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private TextView mArtistName;
	private TextView mAlbumName;
	private TextView mTrackName;
	private ProgressBar mProgress;
	private long mPosOverride = -1;
	private static final int REFRESH = 1;
	private static final int QUIT = 2;
	private static final int GET_ALBUM_ART = 3;
	private static final int ALBUM_ART_DECODED = 4;

	private void queueNextFresh(long next) {

	}

	private long refreshNow() {
		if (mService == null) {
			return 500;
		}

		try {
			long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
			if ((pos >= 0) && (mDuration > 0)) {
				mCurrentTime.setText(MusicUtils.makeTimeString(this, pos / 1000));
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

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
			if (msg.what == GET_ALBUM_ART && (mAlbumId != albumid || albumid < 0)) {
				// 当解码一张新图片时，显示默认专辑图片
				Message numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, null);
				mHandler.removeMessages(ALBUM_ART_DECODED);
				mHandler.sendMessageDelayed(numsg, 300);
				// 这里不允许默认专辑封面，因为我们需要特定的专辑封面，如果我们找不到专辑的任何信息
				Bitmap bm = MusicUtils.getArtWork(MediaPlaybackActivity.this, songid, albumid, false);
				if (bm == null) {
					bm = MusicUtils.getArtWork(MediaPlaybackActivity.this, songid, -1);
					albumid = -1;
				}
				if (bm != null) {
					numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, bm);
					mHandler.removeMessages(ALBUM_ART_DECODED);
					mHandler.sendMessage(numsg);
				}
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
