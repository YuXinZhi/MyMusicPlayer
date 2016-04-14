package com.example.mymusicplayer.ui;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.utils.MusicUtils;

import android.app.Activity;
import android.os.Bundle;

public class MusicBrowserActivity extends Activity {

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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
