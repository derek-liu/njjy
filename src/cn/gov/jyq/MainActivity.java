package cn.gov.jyq;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import cn.gov.jyq.api.AsyncHttpClient;
import cn.gov.jyq.api.AsyncHttpClient.CacheControl;
import cn.gov.jyq.api.RequestParams;
import cn.gov.jyq.api.ResponseHandler;
import cn.gov.jyq.model.JsonHandler;
import cn.gov.jyq.model.News;
import cn.gov.jyq.view.PTRListView;
import cn.gov.jyq.view.PTRListView.OnRefreshListener;

public class MainActivity extends Activity {
	private PTRListView mListView;
	private TextView mTitleView;
	
	private ListAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (PTRListView)findViewById(R.id.main_list);
		mTitleView = (TextView)findViewById(R.id.main_navi_title);
		
		mAdapter = new ListAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				update(CacheControl.ForceRefresh);
			}
		});
		
		mListView.setRefreshing();
		update(CacheControl.Normal);
	}
	
	private void update(CacheControl cache) {
		RequestParams params = new RequestParams();
		params.put("mod", "ajax");
		params.put("act", "newarticle");
		AsyncHttpClient.getInstance().request(params, mHandler, cache, this);
	}
	
	private ResponseHandler mHandler = new ResponseHandler() {
		@Override
		public void onSuccess(int status, String response) {
			JSONArray array = JsonHandler.parseArray(response);
			mAdapter.clear();
			for(int i = 0; i < array.length(); i++) {
				mAdapter.add(new News(JsonHandler.parseObj(array, i)));
			}
			mAdapter.notifyDataSetChanged();
		}
		
		@Override
		public void onFinished() {
			mListView.setRefreshComplete();
		}
	};

}
