package cn.gov.jyq.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public class BitmapUtils {
	public static void saveBitmapToFile(File file, Bitmap bitmap) {
		if(bitmap == null) return;
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
			fos.flush();
		} catch(IOException e) {
			
		} finally {
			IOHelper.closeQuietly(fos);
		}
	}
	
	public static Bitmap readFromFileWithScale(String path, int destWidth, int destHeight, int orientation) {
		return readFromFileWithScale(new File(path), destWidth, destHeight);
	}
	
	public static Bitmap readFromFileWithScale(File file, int destWidth, int destHeight) {
		return readFromFileWithScale(file, destWidth, destHeight, 0);
	}
	
 	public static Bitmap readFromFileWithScale(File file, int destWidth, int destHeight, int orientation) {
		Bitmap result = null;
		FileInputStream fis = null;
		
		Options option = new Options();
		option.inDither = true;
		option.inTempStorage = new byte[32 * 1024];
		option.inPreferredConfig = Bitmap.Config.RGB_565;
		
		try {
			fis = new FileInputStream(file);
			FileDescriptor fd = fis.getFD();
			option.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, option);
			
			option.inSampleSize = computeSampleSize(option, destWidth, destHeight);
			option.inJustDecodeBounds = false;
			Bitmap src = BitmapFactory.decodeFileDescriptor(fd, null, option);
			
			result = scaleBitmap(src, destWidth, destHeight, orientation);
		} catch(IOException e) {
			
		} finally {
			IOHelper.closeQuietly(fis);
		}
		
		return result;
	}
	
	public static Bitmap scaleBitmap(Bitmap src, int destWidth, int destHeight, int orientation) {
		float srcWidth = src.getWidth();
		float srcHeight = src.getHeight();
		float scaleWidth = srcWidth / destWidth;
		float scaleHeight = srcHeight / destHeight;
		if(scaleWidth < 1f || scaleHeight < 1f)	 {
			return src;
		}
		
		int w, h;
		if(scaleWidth >= scaleHeight) {
			w = destWidth;
			h = (int)(srcHeight / scaleWidth);
		} else {
			w = (int)(srcWidth / scaleHeight);
			h = destHeight;
		}
		
		Bitmap result = Bitmap.createScaledBitmap(src, w, h, true);
		if(result != src) {
			src.recycle();
		}
		
		return result;
	}
	
	private static int computeSampleSize(Options options, int width, int height) {
		int scale = 1;
		
		int srcWidth = options.outWidth;
		int srcHeight = options.outHeight;
		int scaleWidth = srcWidth / width;
		int scaleHeight = srcHeight / height;
		scale = Math.min(scaleWidth, scaleHeight);
		
		if(scale < 1) {
			scale = 1;
		}
		return scale;
	}
}

