package cn.gov.jyq.imageloader;

import android.graphics.Bitmap;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class ImageRenderTask implements Runnable {
	private static final int mMILLS = 2000;
	
	private final Bitmap mBitmap;
	private final ImageView mView;
	private final String mMemCacheKey;
	private final ImageLoadListener mListener;
	private final ImageLoader mImageLoader;
	private final boolean withFadein;
	private final boolean onlyDownload;
	
	public ImageRenderTask(Bitmap bitmap, LoadTaskInfo info, ImageLoader loader) {
		this.mBitmap = bitmap;
		this.mImageLoader = loader;
		mView = info.mView;
		mMemCacheKey = info.mUri.toString();
		mListener = info.mListener;
		withFadein = info.withFadein;
		onlyDownload = info.onlyDownload;
	}
	
	@Override
	public void run() {
		if(onlyDownload) {
			if(null != mListener){
				mListener.onLoadSuccess(mMemCacheKey, null);
			}
		} else {
			if(mMemCacheKey.equals(mImageLoader.getUriForView(mView))) {
				renderImage();
				if(null != mListener) {
					mListener.onLoadSuccess(mMemCacheKey, mBitmap);
				}
				mImageLoader.cancelUriForView(mView);
			}
		}
	}
	
	private void renderImage() {
		mImageLoader.putMemCache(mMemCacheKey, mBitmap);
		mView.setImageBitmap(mBitmap);
		
		if(withFadein) {
			AlphaAnimation anim = new AlphaAnimation(0f, 1f);
			anim.setDuration(mMILLS);
			anim.setInterpolator(new DecelerateInterpolator());
			mView.startAnimation(anim);
		}
	}
}
