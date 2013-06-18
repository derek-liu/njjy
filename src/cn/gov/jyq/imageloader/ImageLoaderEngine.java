package cn.gov.jyq.imageloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoaderEngine {
	public static final int DEFAULT_THREAD_POOL_SIZE = 3;
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
	
	private ExecutorService mLoadExecutor;
	private ExecutorService mRenderExecutor;
	private ExecutorService mDistributor;
	
	void submit(final ImageLoadTask task) {
		initExecutorsIfNeed();
		mDistributor.submit(new Runnable() {
			@Override
			public void run() {
				if(task.isCachedOnDisc()) {
					mRenderExecutor.submit(task);
				} else {
					mLoadExecutor.submit(task);
				}
			}
		});
	}
	
	void shutdown() {
		if(null != mLoadExecutor) {
			mLoadExecutor.shutdown();
		}
		if(null != mRenderExecutor) {
			mRenderExecutor.shutdown();
		}
		if(null != mDistributor) {
			mDistributor.shutdown();
		}
	}
	
	private void initExecutorsIfNeed() {
		if(null == mLoadExecutor || mLoadExecutor.isShutdown()) {
			mLoadExecutor = createExecutor();
		}
		if(null == mRenderExecutor || mRenderExecutor.isShutdown()) {
			mRenderExecutor = createExecutor();
		}
		if(null == mDistributor || mDistributor.isShutdown()) {
			mDistributor = Executors.newCachedThreadPool();
		}
	}
	
	private ExecutorService createExecutor() {
		LIFOBlockingDeque<Runnable> taskQueue = new LIFOBlockingDeque<Runnable>();
		return new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE,
				DEFAULT_THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, taskQueue,
				new DefaultThreadFactory(DEFAULT_THREAD_PRIORITY));
	}	
	
	private static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		DefaultThreadFactory(int threadPriority) {
			this.threadPriority = threadPriority;
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) t.setDaemon(false);
			t.setPriority(threadPriority);
			return t;
		}
	}
}
