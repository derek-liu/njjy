package cn.gov.jyq;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.gov.jyq.api.AsyncHttpClient;
import cn.gov.jyq.api.AsyncHttpClient.CacheControl;
import cn.gov.jyq.api.RequestParams;
import cn.gov.jyq.api.ResponseHandler;
import cn.gov.jyq.model.Category;
import cn.gov.jyq.model.JsonHandler;
import cn.gov.jyq.model.News;
import cn.gov.jyq.view.PTRListView;
import cn.gov.jyq.view.PTRListView.OnRefreshListener;

public class MainActivity extends Activity implements OnItemClickListener {
	private PTRListView mListView;
	private TextView mTitleView;
	private ImageView mRightBtn;
	private ListView mCateList;
	
	private ListAdapter mAdapter;
	private CateAdapter mCateAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (PTRListView)findViewById(R.id.main_list);
		mCateList = (ListView)findViewById(R.id.main_cate);
		mTitleView = (TextView)findViewById(R.id.main_sub_title);
		mRightBtn = (ImageView)findViewById(R.id.main_navi_right);
		mRightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( mCateList.getVisibility() == View.GONE) {
					mCateList.setVisibility(View.VISIBLE);
				} else {
					mCateList.setVisibility(View.GONE);
				}
			}
		});
		
		mAdapter = new ListAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.getListView().setOnItemClickListener(this);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				update(CacheControl.ForceRefresh);
			}
		});
		
		mCateAdapter = new CateAdapter(this);
		mCateList.setAdapter(mCateAdapter);
		mCateList.setOnItemClickListener(mListener);
		
		mListView.setRefreshing();
		update(CacheControl.Normal);
	}
	
	private void update(CacheControl cache) {
		RequestParams params = new RequestParams();
		params.put("mod", "ajax");
		params.put("act", "newarticle");
		AsyncHttpClient.getInstance().request(params, mHandler, cache, this);
		
		RequestParams p = new RequestParams();
		params.put("mod", "index");
		AsyncHttpClient.getInstance().request(p, cateHandler, CacheControl.ForceRefresh, this);
	}
	
	private void updateCate(String id) {
		RequestParams params = new RequestParams();
		params.put("mod", "list");
		params.put("catid", id);
		mAdapter.clear();
		AsyncHttpClient.getInstance().request(params, mListHandler, CacheControl.ForceRefresh, this);
	}
	
	private OnItemClickListener mListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			Category c = (Category)mCateAdapter.getItem(pos);
			updateCate(c.mId);
			mCateList.setVisibility(View.GONE);
			mTitleView.setText(c.mTitle);
		}
	};
	
	private ResponseHandler cateHandler = new ResponseHandler() {
		@Override
		public void onSuccess(int status, String response) {
			JSONArray array = JsonHandler.parseArray(response);
			mCateAdapter.clear();
			for(int i = 0; i < array.length(); i++) {
				mCateAdapter.add(new Category(JsonHandler.parseObj(array, i)));
			}
			mCateAdapter.notifyDataSetChanged();
		}
	};
	
	private ResponseHandler mHandler = new ResponseHandler() {
		@Override
		public void onSuccess(int status, String response) {
			JSONArray array = JsonHandler.parseArray(response);
			mAdapter.clear();
			if(array != null) {
				for(int i = 0; i < array.length(); i++) {
					mAdapter.add(new News(JsonHandler.parseObj(array, i)));
				}
			}
			mAdapter.notifyDataSetChanged();
		}
		
		@Override
		public void onFinished() {
			mListView.setRefreshComplete();
		}
	};
	
	private ResponseHandler mListHandler = new ResponseHandler() {
		@Override
		public void onSuccess(int status, String response) {
			JSONObject data = JsonHandler.parse(response);
			for(JSONObject obj : JsonHandler.parseList(data, "0")) {
				mAdapter.add(new News(obj));
			}
			mAdapter.notifyDataSetChanged();
		}
	};

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		News n = (News)mAdapter.getItem(pos);
		Intent i = new Intent(this, ContentActivity.class);
		i.putExtra("aid", n.mId);
		startActivity(i);
	}
}
