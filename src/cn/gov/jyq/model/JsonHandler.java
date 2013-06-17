package cn.gov.jyq.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class JsonHandler{
	
	public static JSONObject parse(String response) {
		if(!TextUtils.isEmpty(response)) {
			try {
				return new JSONObject(response);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static JSONArray	parseArray(String response) {
		if(!TextUtils.isEmpty(response)) {
			try {
				return new JSONArray(response);
			} catch(JSONException e) {
				
			}
		}
		return null;
	}
	
	public static JSONObject parseObj(JSONArray array, int pos) {
		if(array != null) {
			if(pos >= 0 && pos < array.length()) {
				return array.optJSONObject(pos);
			}
		}
		return null;
	}
	
	public static int parseInt(JSONObject response, String key) {
		if(null != response) {
			return response.optInt(key);
		}
		return 0;
	}
	
	public static String parseString(JSONObject response, String key) {
		if(null != response) {
			Object r = response.opt(key);
			return r == null || r == JSONObject.NULL ?
					"" : (String)r;
		}
		return null;
	}
	
	public static boolean parseBoolean(JSONObject response, String key) {
		if(null != response) {
			return response.optBoolean(key);
		}
		return false;
	}
	
	public static float parseDouble(JSONObject response, String key) {
		if(null != response) {
			return (float)response.optDouble(key);
		}
		return Float.NaN;
	}
	
	public static long parseLong(JSONObject response, String key) {
		if(null != response) {
			return response.optLong(key);
		}
		return 0L;
	}
	
	public static List<JSONObject> parseList(JSONObject response, String key) {
		List<JSONObject> list = new ArrayList<JSONObject>();
		
		if(null != response) {
			JSONArray array = response.optJSONArray(key);
			if(null != array) {
				for(int i = 0; i < array.length(); i++) {
					list.add(array.optJSONObject(i));
				}
			}
		}
		
		return list;
	}
	
	public static JSONObject parseJson(JSONObject response, String key) {
		if(null != response) {
			return response.optJSONObject(key);
		}
		return null;
	}
	
	public static Date parseDate(JSONObject response, String key) {
		if(null != response) {
			String date = parseString(response, key);
			if(!TextUtils.isEmpty(date)) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
						Locale.getDefault());
				try {
					return format.parse(date);
				} catch (ParseException e) {
					
				}
			}
		}
		return null;
	}
}

