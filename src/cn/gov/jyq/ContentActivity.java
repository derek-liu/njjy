package cn.gov.jyq;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.gov.jyq.api.AsyncHttpClient;
import cn.gov.jyq.api.AsyncHttpClient.CacheControl;
import cn.gov.jyq.api.RequestParams;
import cn.gov.jyq.api.ResponseHandler;
import cn.gov.jyq.model.JsonHandler;

public class ContentActivity extends Activity {
	private ImageView mNaviLeft, mLoadingView;
	private View mScrollView;
	private TextView mTitleView, mNameView;
	private TextView mContentView;
	private String mId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		mScrollView = findViewById(R.id.content_view);
		mTitleView = (TextView)findViewById(R.id.content_title);
		mNameView = (TextView)findViewById(R.id.content_name);
		mContentView = (TextView)findViewById(R.id.content_text);
		mLoadingView = (ImageView)findViewById(R.id.content_loading);
		mId = getIntent().getStringExtra("aid");
		
		mNaviLeft = (ImageView)findViewById(R.id.content_navi_left);
		mNaviLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		final AnimationDrawable anim = (AnimationDrawable)mLoadingView.getDrawable();
		anim.start();
		request();
	}
	
	private void request() {
		RequestParams params = new RequestParams();
		params.put("mod", "view");
		params.put("aid", mId);
		AsyncHttpClient.getInstance().request(params, mHandler, CacheControl.NoCache, this);
	}
	
	private ResponseHandler mHandler = new ResponseHandler() {
		@Override
		public void onSuccess(int status, String response) {
			JSONObject data = JsonHandler.parse(response);
			String title = JsonHandler.parseString(data, "title");
			String name = JsonHandler.parseString(data, "username") + "发表于 2天前";
			String text = JsonHandler.parseString(data, "content");
			
			mTitleView.setText(title);
			mNameView.setText(name);
			mContentView.setText(text);
		}
		
		@Override
		public void onFinished() {
			mLoadingView.setVisibility(View.GONE);
			mScrollView.setVisibility(View.VISIBLE);
		}
	};
}
