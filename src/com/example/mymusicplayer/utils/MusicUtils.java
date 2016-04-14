package com.example.mymusicplayer.utils;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.ui.MediaPlaybackActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;

public class MusicUtils {

	private static final String TAG = "MusicUtils";

	/**
	 * ��ȡint���͵�SharePreferences��ֵ
	 * 
	 * @param context
	 * @param name
	 * @param def
	 * @return
	 */
	public static int getIntPref(Context context, String name, int def) {
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

	public static void activateTab(Activity activity, int id) {
		/**
		 * Activity Action: Pick an item from the data, returning what was
		 * selected.
		 * 
		 * Input: getData is URI containing a directory of data
		 * (vnd.android.cursor.dir/*) from which to pick an item.
		 * 
		 * Output: The URI of the item that was picked.
		 * 
		 */
		Intent intent = new Intent(Intent.ACTION_PICK);
		switch (id) {
		case R.id.artisttab:
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/artistalbum");
			break;
		case R.id.albumtab:
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/album");
			break;
		case R.id.songtab:
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
			break;
		case R.id.playlisttab:
			intent.setDataAndType(Uri.EMPTY, MediaStore.Audio.Playlists.CONTENT_TYPE);
			break;
		case R.id.nowplayingtab:
			intent = new Intent(activity, MediaPlaybackActivity.class);
			activity.startActivity(intent);
		default:
			break;
		}
		intent.putExtra("withtabs", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		/**
		 * Intent.FLAG_ACTIVITY_CLEAR_TOP
		 * If set, and the activity being launched is already running in the
		 * current task, then instead of launching a new instance of that
		 * activity, all of the other activities on top of it will be closed and
		 * this Intent will be delivered to the (now on top) old activity as a
		 * new Intent.
		 */
		activity.startActivity(intent);
		activity.finish();
		activity.overridePendingTransition(0, 0);
		/**
		 * Call immediately after one of the flavors of startActivity(Intent) or
		 * finish to specify an explicit transition animation to perform next.
		 * 
		 * As of android.os.Build.VERSION_CODES.JELLY_BEAN an alternative to
		 * using this with starting activities is to supply the desired
		 * animation information through a ActivityOptions bundle to or a
		 * related function. This allows you to specify a custom animation even
		 * when starting an activity from outside the context of the current top
		 * activity.
		 * 
		 * Parameters: enterAnim A resource ID of the animation resource to use
		 * for the incoming activity. Use 0 for no animation. exitAnim A
		 * resource ID of the animation resource to use for the outgoing
		 * activity. Use 0 for no animation.
		 */
	}

}
