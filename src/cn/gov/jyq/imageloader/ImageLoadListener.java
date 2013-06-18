package cn.gov.jyq.imageloader;

import android.graphics.Bitmap;

public interface ImageLoadListener {
	void onLoadStart();
	void onLoadProgress(int progress);
	void onLoadSuccess(String url, Bitmap bitmap);
	void onLoadFailed();
}
