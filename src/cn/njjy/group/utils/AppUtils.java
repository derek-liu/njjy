package cn.njjy.group.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import cn.njjy.group.MyApp;

public class AppUtils {
	public static int displayWidth, displayHeight;
	public static float density;
	public static final float LENGTH_FACTOR = 160 * 160 / 100f;
	
	public static void initialize(Context context) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager)
				context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		density = metrics.density;
		displayWidth = metrics.widthPixels;
		displayHeight = metrics.heightPixels;
	}
	
	public static int dp2px(int dp) {
		return (int)((float)dp * density + 0.5f);
	}
	
	public static boolean isOnline() {
		ConnectivityManager conn = (ConnectivityManager) MyApp.getInstance()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = conn.getActiveNetworkInfo();
		return net != null && net.isConnected();
	}
	
	public static String md5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);
			while(md5.length() < 32) {
				md5 = "0" + md5;
			}
			return md5;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}
