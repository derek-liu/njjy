package cn.gov.jyq.imageloader;

import java.util.concurrent.locks.ReentrantLock;

import android.net.Uri;
import android.widget.ImageView;

public class LoadTaskInfo {
	final int mMediaId;
	final Uri mUri;
	final boolean withFadein;
	final boolean onlyDownload;
	final ImageView mView;
	final ImageSize mSize;
	final ImageLoadListener mListener;
	final ReentrantLock mLock;
	
	public static LoadTaskInfo createRenderTask(int id, String uri, ImageView view, ImageSize size, 
			ImageLoadListener listener, ReentrantLock lock, boolean fadein) {
		return new LoadTaskInfo(id, uri, view, size, listener, lock, false, false);
	}
	
	public static LoadTaskInfo createLoadTask(String uri, ImageLoadListener listener, ReentrantLock lock) {
		return new LoadTaskInfo(0, uri, null, null, listener, lock, false, true);
	}
	
	public LoadTaskInfo(int id, String uri, ImageView view, ImageSize size, ImageLoadListener listener,
			ReentrantLock lock, boolean fadein, boolean onlyLoad) {
		this.mMediaId = id;
		this.mUri = Uri.parse(uri);
		this.mView = view;
		this.mSize = size;
		this.mListener = listener;
		this.mLock = lock;
		this.withFadein = fadein;
		this.onlyDownload = onlyLoad;
	}
}
