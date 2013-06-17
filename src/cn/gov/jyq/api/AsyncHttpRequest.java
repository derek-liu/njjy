package cn.gov.jyq.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import cn.gov.jyq.api.AsyncHttpClient.CacheControl;
import cn.gov.jyq.utils.AppUtils;
import cn.gov.jyq.utils.IOHelper;

import android.text.TextUtils;

public class AsyncHttpRequest implements Runnable {
	private static final int TIMEOUT = 10 * 1000;
	private static final int DEFAULT_MAX_RETRIES = 3;
	
	private final ResponseHandler mHandler;
	private final RequestParams mParams;
	private final CacheControl mCacheControl;
	
	private String mUrl;
	private RetryHandler mRetryHandler;
	
	public AsyncHttpRequest(RequestParams params, CacheControl control, ResponseHandler handler) {
		mParams = params;
		mCacheControl = control;
		mHandler = handler;
		
		mRetryHandler = new RetryHandler(DEFAULT_MAX_RETRIES);
		mUrl = ApiConfig.HOST + mParams.getParamsString();
	}
	
	@Override
	public void run() {
		if(mCacheControl == CacheControl.Normal) {
			String content = HttpCache.getCache(mUrl);
			if(!TextUtils.isEmpty(content)) {
				mHandler.sendResponseMessage(ResponseHandler.STATUS_OK, content);
				return;
			}
		}
		
		if(AppUtils.isOnline()) {
			boolean retry = true;
			int count = 0;
			while(retry) {
				try {
					if(!Thread.currentThread().isInterrupted()) {
						get();
					}
					return;
				} catch(IOException e) {
					retry = mRetryHandler.retryRequest(e, ++count);
				} 
			}
		}
		
		mHandler.sendResponseMessage(ResponseHandler.STATUS_NETWORK_ERROR);
	}
	
	private void get() throws IOException {
		HttpURLConnection conn = null;
		InputStream in = null;
		
		try {
			URL mURL = new URL(mUrl);
			conn = (HttpURLConnection)mURL.openConnection();
			configConn(conn);
			conn.setRequestMethod("GET");
			conn.connect();
			
			int statusCode = conn.getResponseCode();
			if(statusCode <= ResponseHandler.STATUS_NO_ACCESS) {
				in = new BufferedInputStream(conn.getInputStream());
				if(!TextUtils.isEmpty(conn.getContentEncoding()) &&
						conn.getContentEncoding().equalsIgnoreCase("GZIP")) {
					in = new GZIPInputStream(in);
				}
				String content = IOHelper.toString(in);
				int size = content.length();
				content = content.substring(1, size - 1);
				
				if(statusCode == ResponseHandler.STATUS_OK &&
						mCacheControl != CacheControl.NoCache) {
					HttpCache.putCache(mUrl, content);
				}
				
				mHandler.sendResponseMessage(statusCode, content);
			} else {
				mHandler.sendResponseMessage(statusCode);
			}
			
		} finally {
			IOHelper.closeQuietly(in);
			if(conn != null) {
				conn.disconnect();
			}
		}
	}
	
	private void configConn(HttpURLConnection conn) {
		conn.setReadTimeout(TIMEOUT);
		conn.setConnectTimeout(TIMEOUT);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		conn.setRequestProperty("Accept-Encoding", "gzip");
		conn.setRequestProperty("Accept", "application/json");
//		conn.setRequestProperty("Authorization", AccountUtils.getBasicAuth());
	}
}
