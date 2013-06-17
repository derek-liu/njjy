package cn.gov.jyq;

import cn.gov.jyq.model.Category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CateAdapter extends AbsAdapter<Category> {
	
	public CateAdapter(Context context) {
		super(context);
	}
	
	@Override
	public View getView(int pos, View convertView, ViewGroup container) {
		View view = convertView;
		if(view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.cate_item, null);
			ViewHolder holder = new ViewHolder();
			holder.mTitle = (TextView)view.findViewById(R.id.cate_title);
			view.setTag(holder);
		}
		
		ViewHolder h = (ViewHolder)view.getTag();
		h.mTitle.setText(mList.get(pos).mTitle);
		
		return view;
	}
	
	private static class ViewHolder {
		TextView mTitle;
	}
}
