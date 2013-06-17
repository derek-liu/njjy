package cn.gov.jyq.api;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;

public class AsyncHttpClient {
    private ThreadPoolExecutor mThreadPool;
    private final Map<Context, List<WeakReference<Future<?>>>> mRequestMap;
    
    public static AsyncHttpClient mInstance;
    public static AsyncHttpClient getInstance() {
    	if(mInstance == null) {
    		mInstance = new AsyncHttpClient();
    	}
    	return mInstance;
    }

	private AsyncHttpClient() {
        mThreadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        mRequestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
	}
	
	public void passdue(String url) {
		HttpCache.deleteCache(url);
	}
	
	public void request(RequestParams params, ResponseHandler handler, CacheControl control, Context context) {
		Future<?> request = mThreadPool.submit(
				new AsyncHttpRequest(params, control, handler));
		if(context != null) {
			List<WeakReference<Future<?>>> list = mRequestMap.get(context);
			if(list == null) {
				list = new LinkedList<WeakReference<Future<?>>>();
				mRequestMap.put(context, list);
			}
			list.add(new WeakReference<Future<?>>(request));
		}
	}
	
	public void cancelRequest(Context context) {
		cancelRequest(context, true);
	}
	
	public void cancelRequest(Context context, boolean mayInterrupt) {
		List<WeakReference<Future<?>>> list = mRequestMap.get(context);
		if(list != null) {
			for(WeakReference<Future<?>> ref : list) {
				Future<?> request = ref.get();
				if(request != null) {
					request.cancel(mayInterrupt);
				}
			}
		}
		mRequestMap.remove(context);
	}
	
	public enum CacheControl {
		NoCache, ForceRefresh, Normal
	}
} 
