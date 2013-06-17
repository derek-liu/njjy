package cn.gov.jyq.model;

import org.json.JSONObject;

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
		mPicUrl = JsonHandler.parseString(src, "remote");
		mDate = JsonHandler.parseString(src, "dateline");
	}
}
