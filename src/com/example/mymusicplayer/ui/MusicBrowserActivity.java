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
		
		int activeTab=MusicUtils.getIntPref(this,"activeTab",R.id.artisttab);
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
