package cn.gov.jyq;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import cn.gov.jyq.api.AsyncHttpClient;
import cn.gov.jyq.api.AsyncHttpClient.CacheControl;
import cn.gov.jyq.api.RequestParams;
import cn.gov.jyq.api.ResponseHandler;
import cn.gov.jyq.model.JsonHandler;

public class ContentActivity extends Activity {
	private ImageView mLoadingView;
	private TextView mNaviLeft;
	private TextView mTitleView, mNameView;
	private WebView mWebView;
	private String mId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		mTitleView = (TextView)findViewById(R.id.content_title);
		mNameView = (TextView)findViewById(R.id.content_name);
		mLoadingView = (ImageView)findViewById(R.id.content_loading);
		mWebView = (WebView)findViewById(R.id.content_webview);
		
		mNaviLeft = (TextView)findViewById(R.id.content_navi_left);
		mNaviLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
		mWebView.setBackgroundColor(0);
		mId = getIntent().getStringExtra("aid");
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
			mWebView.loadDataWithBaseURL("", text, "text/html", "UTF-8", null);
		}
		
		@Override
		public void onFinished() {
			mLoadingView.setVisibility(View.GONE);
			mWebView.setVisibility(View.VISIBLE);
		}
	};
}
