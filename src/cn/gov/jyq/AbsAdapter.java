package cn.gov.jyq;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class AbsAdapter<T> extends BaseAdapter {
	protected List<T> mList;
	protected Context mContext;
	
	public AbsAdapter(Context context) {
		mContext = context;
		mList = new ArrayList<T>();
	}
	
	public void add(T t) {
		mList.add(t);
	}
	
	public void clear() {
		mList.clear();
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		return null;
	}
}
