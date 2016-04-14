package com.example.mymusicplayer.ui;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.utils.MusicUtils;

import android.app.Activity;
import android.os.Bundle;

public class MusicBrowserActivity extends Activity {

	public MusicBrowserActivity() {
	}

	/**
	 * activity��һ�δ���ʱ����
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * ��ý���ҳ��ʱ���ҳ���ǩ
		 */
		int activeTab = MusicUtils.getIntPref(this, "activeTab", R.id.artisttab);

		if (activeTab != R.id.artisttab && activeTab != R.id.albumtab && activeTab != R.id.songtab
				&& activeTab != R.id.playlisttab) {
			// Ĭ��artisttab�
			activeTab = R.id.artisttab;
		}
		MusicUtils.activateTab(this, activeTab);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
