package com.example.mymusicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MediaPlaybackService extends Service {
	/**
	 * used to specify whether enqueue() should start playing the new list of
	 * files right away, next or once all the currently queued files have been
	 * played
	 */
	/**
	 * 用于指定enqueue()是否立即播放新文件列表，下一个或所有当前文件已播放
	 */
	public static final int NOW = 1;
	public static final int NEXT = 2;
	public static final int LAST = 3;
	public static final int PLAYBACKSERVICE_STATUS = 1;

	public static final int SHUFFLE_NONE = 0;
	public static final int SHUFFLE_NORMAL = 1;
	public static final int SHUFFLE_AUTO = 2;

	public static final int REPEAT_NONE = 0;
	public static final int REPEAT_CURRENT = 1;
	public static final int REPEAT_ALL = 2;

	public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
	public static final String META_CHANGED = "com.android.music.metachanged";
	public static final String QUEUE_CHANGED = "com.android.music.queuechanged";

	public static final String SERVICECMD = "com.android.music.musicservicecommand";
	public static final String CMDNAME = "command";
	public static final String CMDTOGGLEPAUSE = "togglepause";
	public static final String CMDSTOP = "stop";
	public static final String CMDPAUSE = "pause";
	public static final String CMDPLAY = "play";
	public static final String CMDPREVIOUS = "previous";
	public static final String CMDNEXT = "next";

	public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	public static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
	public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
	public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";

	private static final int TRACK_ENDED = 1;
	private static final int RELEASE_WAKELOCK = 2;
	private static final int SERVER_DIED = 3;
	private static final int FOCUSCHANGE = 4;
	private static final int FADEDOWN = 5;
	private static final int FADEUP = 6;
	private static final int TRACK_WENT_TO_NEXT = 7;
	private static final int MAX_HISTORY_SIZE = 100;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
