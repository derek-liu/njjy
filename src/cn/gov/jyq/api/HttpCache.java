package cn.gov.jyq.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cn.gov.jyq.MyApp;
import cn.gov.jyq.utils.AppUtils;
import cn.gov.jyq.utils.IOHelper;

import android.text.TextUtils;

public class HttpCache {
	private static final String HTTP_CACHE = "api_cache";
	
	public static String getCache(String url) {
		if(!TextUtils.isEmpty(url)) {
			CacheData cache = read(url);
			if(cache != null) {
				return cache.getContent();
			}
		}
		return null;
	}
	
	public static void putCache(String url, String content) {
		CacheData cache = new CacheData(content);
		if(!TextUtils.isEmpty(url)) {
			write(url, cache);
		}
	}
	
	public static void deleteCache(String url) {
		File file = getFile(url);
		file.delete();
	}
	
	private static CacheData read(String id) {
		FileInputStream fis = null;

		try {
			fis = IOHelper.openInputStream(getFile(id));
			ObjectInputStream in = new ObjectInputStream(fis);
			return (CacheData) in.readObject();
		} catch (IOException e) {

		} catch (ClassNotFoundException e) {

		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {

				}
			}
		}
		return null;
	}

	private static void write(String id, CacheData cache) {
		FileOutputStream fos = null;
		File file = getFile(id);
		file.delete();

		try {
			fos = IOHelper.openOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(cache);
		} catch (IOException e) {
			file.delete();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {

				}
			}
		}
	}
	
	private static File getFile(String url) {
		String file = AppUtils.md5(url);
		File dir = MyApp.getExternalAvailable() ?
				new File(MyApp.getInstance().getExternalCacheDir(), HTTP_CACHE) :
				new File(MyApp.getInstance().getCacheDir(), HTTP_CACHE);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return new File(dir, file);
	}
	
	private static class CacheData implements Serializable {
		private static final long serialVersionUID = -2439343216261638026L;
		private String content;
		
		public CacheData(String content) {
			this.content = content;
		}
		
		public String getContent() {
			return content;
		}
	}
}
