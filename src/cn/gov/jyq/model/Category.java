package cn.gov.jyq.model;

import org.json.JSONObject;

public class Category {
	public String mId;
	public String mTitle;
	public String mCount;
	
	public Category(JSONObject src) {
		mId = JsonHandler.parseString(src, "catid");
		mTitle = JsonHandler.parseString(src, "catname");
		mCount = JsonHandler.parseString(src, "articles");
	}
}
