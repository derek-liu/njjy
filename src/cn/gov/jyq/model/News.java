package cn.gov.jyq.model;

import org.json.JSONObject;

import android.text.TextUtils;

import cn.gov.jyq.api.ApiConfig;

public class News {
	public String mId;
	public String mRemote;
	public String mTitle;
	public String mContent;
	public String mPicUrl;
	public String mDate;
	
	public News(JSONObject src) {
		mId = JsonHandler.parseString(src, "aid");
		mRemote = JsonHandler.parseString(src, "remote");
		mContent = JsonHandler.parseString(src, "content");
		mTitle = JsonHandler.parseString(src, "title");
		String url = JsonHandler.parseString(src, "pic");
		if(!TextUtils.isEmpty(url) && !url.startsWith("http")) {
			url = ApiConfig.PREFIX + url;
		}
		mPicUrl = url;
		mDate = JsonHandler.parseString(src, "dateline");
	}
}
