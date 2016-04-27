package com.example.mymusicplayer.utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.example.mymusicplayer.IMediaPlaybackService;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.service.MediaPlaybackService;
import com.example.mymusicplayer.ui.MediaPlaybackActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class MusicUtils {

	private static final String TAG = "MusicUtils";

	public interface Defs {
		public final static int OPEN_URL = 0;
		public final static int ADD_TO_PLAYLIST = 1;
		public final static int USE_AS_RINGTONE = 2;
		public final static int PLAYLIST_SELECTED = 3;
		public final static int NEW_PLAYLIST = 4;
		public final static int PLAY_SELECTION = 5;
		public final static int GOTO_START = 6;
		public final static int GOTO_PLAYBACK = 7;
		public final static int PARTY_SHUFFLE = 8;
		public final static int SHUFFLE_ALL = 9;
		public final static int DELETE_ITEM = 10;
		public final static int SCAN_DONE = 11;
		public final static int QUEUE = 12;
		public final static int EFFECTS_PANEL = 13;
		public final static int CHILD_MENU_BASE = 14; // this should be the last
														// item
	}

	/**
	 * 获取int类型的SharePreferences的值
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
			// 失败并返回
		default:
			break;
		}
		intent.putExtra("withtabs", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		/**
		 * Intent.FLAG_ACTIVITY_CLEAR_TOP If set, and the activity being
		 * launched is already running in the current task, then instead of
		 * launching a new instance of that activity, all of the other
		 * activities on top of it will be closed and this Intent will be
		 * delivered to the (now on top) old activity as a new Intent.
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

	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
	private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
	private static Bitmap mCachedBit = null;

	private static final HashMap<Long, Drawable> sArtCache = new HashMap<Long, Drawable>();
	private static int sArtCacheId = -1;
	static {
		// for the cache,
		// 565 is faster to decode and display
		// and we don't want to dither here because the image will be scaled
		// down later
		sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
		sBitmapOptionsCache.inDither = false;

		sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		sBitmapOptions.inDither = false;

	}

	public static void initAlbumArtCache() {
		try {
			int id = sService.getMediaMountedCount();
			if (id != sArtCacheId) {
				clearAlbumArtCache();
				sArtCacheId = id;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void clearAlbumArtCache() {
		synchronized (sArtCache) {
			sArtCache.clear();
		}
	}

	/**
	 * 获得指定专辑的封面,-1代表未知专辑
	 */
	public static Bitmap getArtWork(Context context, long song_id, long album_id) {

		return getArtWork(context, song_id, album_id, true);

	}

	public static Bitmap getArtWork(Context context, long song_id, long album_id, boolean allowdefalut) {

		if (album_id < 0) {
			// 如果专辑没有在数据库中，那么就从直接从文件获得
			if (song_id >= 0) {
				Bitmap bm = getArtWork(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefalut) {
				return getDefaultArtwork(context);
			}
			return null;
		}

		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, sBitmapOptions);
			} catch (FileNotFoundException e) {
				// 专辑图片的缩略图不存的，可能被删除或压根没有
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefalut) {
							return getDefaultArtwork(context);
						}
					}
				} else if (allowdefalut) {
					bm = getDefaultArtwork(context);
				}
				return bm;

			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
				}
			}
		}
		return null;

	}

	// 从指定文件获取专辑封面
	private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException("必须指定一个专辑或歌曲id");
		}

		try {
			Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
			ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
			if (pfd != null) {
				FileDescriptor fd = pfd.getFileDescriptor();
				bm = BitmapFactory.decodeFileDescriptor(fd);
			}
		} catch (IllegalStateException ex) {
		} catch (FileNotFoundException ex) {
		}
		if (bm != null) {
			mCachedBit = bm;
		}
		return bm;
	}

	private static Bitmap getDefaultArtwork(Context context) {
		return null;
	}

	public static CharSequence makeTimeString(Context context, long secs) {

		return null;
	}

	public static class ServiceToken {
		ContextWrapper mWrappedContext;

		public ServiceToken(ContextWrapper context) {
			mWrappedContext = context;
		}

	}

	public static IMediaPlaybackService sService = null;
	private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

	public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
		Activity realActivity = context.getParent();
		if (realActivity == null) {
			realActivity = context;
		}
		ContextWrapper cw = new ContextWrapper(realActivity);
		cw.startService(new Intent(cw, MediaPlaybackService.class));
		ServiceBinder sb = new ServiceBinder(callback);
		if (cw.bindService((new Intent()).setClass(cw, MediaPlaybackService.class), sb, 0)) {
			sConnectionMap.put(cw, sb);
			return new ServiceToken(cw);
		}
		Log.e("Music", "Failed to bind to service");
		return null;
	}

	public static void unbindFromService(ServiceToken token) {
		if (token == null) {
			Log.e("MusicUtils", "Trying to unbind with null token");
			return;
		}
		ContextWrapper cw = token.mWrappedContext;
		ServiceBinder sb = sConnectionMap.remove(cw);
		if (sb == null) {
			Log.e("MusicUtils", "Trying to unbind with null token");
			return;
		}
		cw.unbindService(sb);
		if (sConnectionMap.isEmpty()) {
			sService = null;
		}
	}

	private static class ServiceBinder implements ServiceConnection {
		ServiceConnection mCallback;

		ServiceBinder(ServiceConnection callback) {
			mCallback = callback;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			sService = IMediaPlaybackService.Stub.asInterface(service);
			initAlbumArtCache();
			if (mCallback != null) {
				mCallback.onServiceConnected(name, service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (mCallback != null) {
				mCallback.onServiceDisconnected(name);
			}
			sService = null;
		}

	}

	public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, int limit) {
		try {
			ContentResolver resolver = context.getContentResolver();
			if (resolver == null) {
				return null;
			}
			if (limit > 0) {
				uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
			}
			return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (UnsupportedOperationException ex) {
			return null;
		}

	}

	public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
	}

	public static long getCurrentAudioId() {
		if (MusicUtils.sService != null) {
			try {
				return sService.getAudioId();
			} catch (RemoteException ex) {
			}
		}
		return -1;
	}

	private static ContentValues[] sContentValuesCache = null;

	/**
	 * @param ids
	 *            The source array containing all the ids to be added to the
	 *            playlist
	 * @param offset
	 *            Where in the 'ids' array we start reading
	 * @param len
	 *            How many items to copy during this pass
	 * @param base
	 *            The play order offset to use for this pass
	 */
	private static void makeInsertItems(long[] ids, int offset, int len, int base) {
		// adjust 'len' if would extend beyond the end of the source array
		if (offset + len > ids.length) {
			len = ids.length - offset;
		}
		// allocate the ContentValues array, or reallocate if it is the wrong
		// size
		if (sContentValuesCache == null || sContentValuesCache.length != len) {
			sContentValuesCache = new ContentValues[len];
		}
		// fill in the ContentValues array with the right values for this pass
		for (int i = 0; i < len; i++) {
			if (sContentValuesCache[i] == null) {
				sContentValuesCache[i] = new ContentValues();
			}

			sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
			sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
		}
	}

	public static void addToPlayList(Context context, long[] ids, int playlistid) {
		if (ids == null) {
			// this shouldn't happen (the menuitems shouldn't be visible
			// unless the selected item represents something playable
			Log.e("MusicBase", "ListSelection null");
		} else {
			int size = ids.length;
			ContentResolver resolver = context.getContentResolver();
			// need to determine the number of items currently in the playlist,
			// so the play_order field can be maintained.
			String[] cols = new String[] { "count(*)" };
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
			Cursor cur = resolver.query(uri, cols, null, null, null);
			cur.moveToFirst();
			int base = cur.getInt(0);
			cur.close();
			int numinserted = 0;
			for (int i = 0; i < size; i += 1000) {
				makeInsertItems(ids, i, 1000, base);
				numinserted += resolver.bulkInsert(uri, sContentValuesCache);
			}
			String message = context.getResources().getQuantityString(R.plurals.NNNtrackstoplaylist, numinserted,
					numinserted);
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			// mLastPlaylistSelected = playlistid;
		}
	}

}
