package cn.gov.jyq.imageloader;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.os.Environment;

public class UnLimitedDiscCache {
	private static final String HASH_ALGORITHM = "MD5";
	private static final String INDIVIDUAL_DIR_NAME = "images";
	private static final int RADIX = 10 + 26;
	
	private File mCacheDir;
	
	public UnLimitedDiscCache(Context context) {
		File appCacheDir = null;
		if(Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			appCacheDir = context.getExternalCacheDir();
		}
		if(null == appCacheDir) {
			appCacheDir = context.getCacheDir();
		}
		
		mCacheDir = new File(appCacheDir, INDIVIDUAL_DIR_NAME);
		if(!mCacheDir.exists()) {
			if(!mCacheDir.mkdir()) {
				mCacheDir = appCacheDir;
			}
		}
	}
	
	public File get(String key) {
		String name = generate(key);
		return new File(mCacheDir, name);
	}
	
	public void clear() {
		File[] files = mCacheDir.listFiles();
		if(null != files) {
			for(File f : files) {
				f.delete();
			}
		}
	}
	
	public static String generate(String uri) {
		byte[] md5 = getMD5(uri.getBytes());
		BigInteger bi = new BigInteger(md5).abs();
		return bi.toString(RADIX);
	}

	private static byte[] getMD5(byte[] data) {
		byte[] hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			digest.update(data);
			hash = digest.digest();
		} catch (NoSuchAlgorithmException e) {
			
		}
		return hash;
	}
}
