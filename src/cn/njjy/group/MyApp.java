package cn.njjy.group;

import cn.njjy.group.utils.AppUtils;
import android.app.Application;
import android.os.Environment;

public class MyApp extends Application {
	private static boolean mExternalStorageAvailable = false;
	private static boolean mExternalStorageWriteable = false;
	
	private static Application mInstance;
	
	public static Application getInstance() {
		return mInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		AppUtils.initialize(this);
	}
	
	public static boolean getExternalAvailable() {
		updateExternalStorageState();
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}
	
	private static void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}
}
