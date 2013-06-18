package cn.gov.jyq.imageloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;
import cn.gov.jyq.utils.BitmapUtils;
import cn.gov.jyq.utils.IOHelper;

public class ImageLoadTask implements Runnable {
	public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000;
	public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000;
	private static final int BUFFER_SIZE = 8 * 1024;
	private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
	private static final String SCHEME_HTTP ="http";
	private static final String SCHEME_HTTPS = "https";
	private static final String SCHEME_FILE = "file";
	
	private final ImageLoader mImageLoader;
	private final Handler mHandler;
	private final LoadTaskInfo mLoadInfo;
	final Uri mUri;
	final String mEncodedUri;
	final ImageView mView;
	final ImageSize mSize;
	final ImageLoadListener mListener;
	
	public ImageLoadTask(ImageLoader loader, LoadTaskInfo info, Handler handler) {
		this.mImageLoader = loader;
		this.mLoadInfo = info;
		this.mHandler = handler;
		
		mUri = mLoadInfo.mUri;
		mEncodedUri = Uri.encode(mUri.toString(), ALLOWED_URI_CHARS);
		mView = mLoadInfo.mView;
		mSize = mLoadInfo.mSize;
		mListener = mLoadInfo.mListener;
	}
	
	@Override
	public void run() {
		AtomicBoolean pause = mImageLoader.isPaused();
		if(pause.get()) {
			synchronized(pause) {
				try {
					pause.wait();
				} catch(InterruptedException e) {
					return;
				}
			}
		}
		
		if(!mLoadInfo.onlyDownload && checkIsNotActual()) return;
		
		ReentrantLock lock = mLoadInfo.mLock;
		lock.lock();
		Bitmap bitmap = null;
		try {
			if(mLoadInfo.onlyDownload) {
				File file = mImageLoader.getDiscCache(mUri.toString());
				if(!file.exists()) {
					loadFromNetwork(file);
				}
			} else {
			
				if(checkIsNotActual()) return;
			 
				bitmap = mImageLoader.getMemCache(mUri.toString());
				if(null == bitmap) {
					String scheme = mUri.getScheme();
					if(scheme.equalsIgnoreCase(SCHEME_HTTP) || scheme.equalsIgnoreCase(SCHEME_HTTPS)) {
						bitmap = loadBitmap(mUri.toString());
					} else if(scheme.equalsIgnoreCase(SCHEME_FILE)) {
						bitmap = BitmapUtils.readFromFileWithScale(mUri.getPath(), mSize.getWidth(), mSize.getHeight(), 0);
					}
				
					if(null == bitmap) return;
					if(checkIsNotActual() || checkIsInterrupted()) return;
				}
			}
		} catch(IOException e) {
			
		} finally {
			lock.unlock();
		}
		
		ImageRenderTask renderTask = new ImageRenderTask(bitmap, mLoadInfo, mImageLoader);
		mHandler.post(renderTask);
	}
	
	boolean isCachedOnDisc() {
		if(mUri.getScheme().equalsIgnoreCase(SCHEME_HTTP) 
				|| mUri.getScheme().equalsIgnoreCase(SCHEME_HTTPS)) {
			return mImageLoader.isCachedOnDisc(mUri.toString());
		}
		return true;
	}
	
	private boolean checkIsNotActual() {
		String cacheKey = mImageLoader.getUriForView(mView);
		return !mUri.toString().equals(cacheKey);
	}
	
	private boolean checkIsInterrupted() {
		return Thread.interrupted();
	}
	
	private Bitmap loadBitmap(String url) {
		File file = mImageLoader.getDiscCache(url);
		
		Bitmap bitmap = null;
		try {
			if(file.exists()) {
				Bitmap b = BitmapUtils.readFromFileWithScale(file, mSize.getWidth(), mSize.getHeight());
				if(null != b) {
					return b;
				}
			}
			
			loadFromNetwork(file);
			bitmap = BitmapUtils.readFromFileWithScale(file, mSize.getWidth(), mSize.getHeight());
			
			if(null == bitmap) {
				fireLoadFailed();
			}
		} catch(Exception e) {
			fireLoadFailed();
		}
		
		return bitmap;
	}
	
	private void fireLoadFailed() {
		if(null != mListener ) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.onLoadFailed();
				}
			});
		}
	}
	
	private void loadFromNetwork(File file) throws IOException {
		HttpURLConnection conn = null;
		InputStream in = null;
		OutputStream os = null;
		
		try {
			conn = (HttpURLConnection)new URL(mEncodedUri).openConnection();
			conn.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
			conn.setReadTimeout(DEFAULT_HTTP_READ_TIMEOUT);
			conn.setRequestProperty("Accept-Encoding", "gzip");
			conn.connect();
			String encoding = conn.getContentEncoding();
		
			in = new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
			if(null != encoding && encoding.equalsIgnoreCase("GZIP")) {
				in = new GZIPInputStream(in);
			}
			os = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
			IOHelper.copy(in, os);
		} finally {
			IOHelper.closeQuietly(in);
			IOHelper.closeQuietly(os);
			if(null != conn) {
				conn.disconnect();
			}
		}
	}
}
