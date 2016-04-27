package com.example.mymusicplayer.ui;

import com.example.mymusicplayer.IMediaPlaybackService;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.service.MediaPlaybackService;
import com.example.mymusicplayer.utils.MusicUtils;
import com.example.mymusicplayer.utils.MusicUtils.ServiceToken;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

//MusicUtils.Defs是内部接口
public class MusicBrowserActivity extends Activity implements MusicUtils.Defs {
	ServiceToken mToken;

	public MusicBrowserActivity() {
	}

	/**
	 * activity第一次创建时调用
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * 获得进入页面时活动的页面标签
		 */
		int activeTab = MusicUtils.getIntPref(this, "activeTab", R.id.artisttab);

		if (activeTab != R.id.artisttab && activeTab != R.id.albumtab && activeTab != R.id.songtab
				&& activeTab != R.id.playlisttab) {
			// 默认artisttab活动
			activeTab = R.id.artisttab;
		}
		MusicUtils.activateTab(this, activeTab);

		String shuf = getIntent().getStringExtra("sutoshuffle");
		if ("true".equals(shuf)) {
			mToken = MusicUtils.bindToService(this, autoshuffle);
		}
	}

	@Override
	protected void onDestroy() {
		if (mToken!=null) {
			MusicUtils.unbindFromService(mToken);
		}
		super.onDestroy();
	}

	private ServiceConnection autoshuffle = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// we need to be able to bind again, so unbind
			// 需要能够再次绑定，所以线解绑
			try {
				unbindService(this);
			} catch (IllegalArgumentException e) {
			}

			IMediaPlaybackService serv = IMediaPlaybackService.Stub.asInterface(service);
			if (serv != null) {
				try {
					serv.setShuffleMode(MediaPlaybackService.SHUFFLE_AUTO);
				} catch (RemoteException ex) {
				}
			}

		}
	};

}
