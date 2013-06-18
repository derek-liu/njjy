package cn.gov.jyq.imageloader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImageLoader {
	private static final int DEFAULT_SIZE = 640;

	private Context mContext;
	private ImageLoaderEngine mEngine;
	private Map<Integer, String> mBindingViews;
	private Map<String, ReentrantLock> mUriLocks;
	private UnLimitedDiscCache mDiscCache;
	private Map<String, WeakReference<Bitmap>> mMemCache;
	
	private final AtomicBoolean isPaused = new AtomicBoolean(false);
	private final AtomicBoolean isNetworkSlow = new AtomicBoolean(false);
	private final boolean isNetworkDenied = false;
	
	private volatile static ImageLoader mInstance;
	public static ImageLoader getInstance() {
		if(null == mInstance) {
			synchronized (ImageLoader.class) {
				if(null == mInstance) {
					mInstance = new ImageLoader();
				}
			}
		}
		return mInstance;
	}
	
	public void init(Context context) {
		mContext = context;
		mEngine = new ImageLoaderEngine();
		mBindingViews = new ConcurrentHashMap<Integer, String>();
		mMemCache = new ConcurrentHashMap<String, WeakReference<Bitmap>>();
		mUriLocks = new WeakHashMap<String, ReentrantLock>();
		mDiscCache = new UnLimitedDiscCache(mContext);
	}
	
	public void bind(String uri, ImageView view) {
		bind(uri, view, false);
	}
	
	public void bind(String uri, ImageView view, boolean fadein) {
		bind(uri, view, null, false);
	}
	
	public void bind(String uri, ImageView view, ImageLoadListener listener, boolean fadein) {
		bind(0, uri, view, listener, fadein);
	}
	
	public void bind(int id, String uri, ImageView view, ImageLoadListener listener, boolean fadein) {
		if(null == view || TextUtils.isEmpty(uri)) {
			return;
		}
		
		updateUriForView(view, uri);
		
		Bitmap bitmap = null;
		if(null != mMemCache.get(uri)) {
			bitmap = mMemCache.get(uri).get();
		}
	
		if(null != bitmap && !bitmap.isRecycled()) {
			view.setImageBitmap(bitmap);
			
			if(null != listener) {
				listener.onLoadSuccess(uri, bitmap);
			}
		} else {
			view.setImageBitmap(null);
			if(null != listener) {
				listener.onLoadStart();
			}
			
			LoadTaskInfo info = LoadTaskInfo.createRenderTask(id, uri, view, 
					getImageSize(view), listener, getLockForUri(uri), fadein);
			ImageLoadTask task = new ImageLoadTask(this, info, new Handler());
			mEngine.submit(task);
		}
	}
	
	public void load(String uri) {
		load(uri, null);
	}
	
	public void load(String uri, ImageLoadListener listener) {
		if(TextUtils.isEmpty(uri)) {
			return;
		}
		
		if(null != listener) {
			listener.onLoadStart();
		}
		LoadTaskInfo info = LoadTaskInfo.createLoadTask(uri, listener, getLockForUri(uri));
		ImageLoadTask task = new ImageLoadTask(this, info, new Handler());
		mEngine.submit(task);
	}

	public void clearMemCache() {
		mMemCache.clear();
	}
	
	public void clearDiscCache() {
		mDiscCache.clear();
	}
	
	public void pause() {
		isPaused.set(true);
	}
	
	public void resume() {
		synchronized (isPaused) {
			isPaused.set(false);
			isPaused.notifyAll();
		}
	}
	
	public void stop() {
		mEngine.shutdown();
	}
	
	boolean isNetworkDenied() {
		return isNetworkDenied;
	}
	
	boolean isNetworkSlow() {
		return isNetworkSlow.get();
	}
	
	AtomicBoolean isPaused() {
		return isPaused;
	}
	
	void putMemCache(String key, Bitmap value) {
		mMemCache.put(key, new WeakReference<Bitmap>(value));
	}
	
	Bitmap getMemCache(String key) {
		if(null != mMemCache.get(key)) {
			return mMemCache.get(key).get();
		}
		return null;
	}
	
	public boolean isCachedOnDisc(String uri) {
		return mDiscCache.get(uri).exists();
	}
	
	public File getDiscCache(String uri) {
		return mDiscCache.get(uri);
	}
	
	String getUriForView(ImageView view) {
		return mBindingViews.get(view.hashCode());
	}

	ReentrantLock getLockForUri(String uri) {
		ReentrantLock lock = mUriLocks.get(uri);
		if (null == lock) {
			lock = new ReentrantLock();
			mUriLocks.put(uri, lock);
		}
		return lock;
	}
	
	void cancelUriForView(ImageView view) {
		mBindingViews.remove(view.hashCode());
	}
	
	private void updateUriForView(ImageView view, String uri) {
		mBindingViews.put(view.hashCode(), uri);
	}
	
	private ImageSize getImageSize(ImageView imageView) {
		final LayoutParams params = imageView.getLayoutParams();
		int width = 0, height = 0;
		
		if(params != null) {
			width = params.width == LayoutParams.WRAP_CONTENT ? 0 : imageView.getWidth(); 
			if (width <= 0) width = params.width;
			if (width <= 0) width = getFieldValue(imageView, "mMaxWidth"); 
			if (width <= 0) width = DEFAULT_SIZE;

			height = params.height == LayoutParams.WRAP_CONTENT ? 0 : imageView.getHeight(); 
			if (height <= 0) height = params.height; 
			if (height <= 0) height = getFieldValue(imageView, "mMaxHeight"); 
			if (height <= 0) height = DEFAULT_SIZE;
		}

		return new ImageSize(width, height);
	}

	private int getFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = (Integer) field.get(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;
			}
		} catch (Exception e) {
			
		}
		return value;
	}
}
