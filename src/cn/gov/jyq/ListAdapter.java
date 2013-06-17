package cn.gov.jyq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.gov.jyq.model.News;

public class ListAdapter extends AbsAdapter<News>{

	public ListAdapter(Context context) {
		super(context);
	}
	
	@Override
	public View getView(int pos, View convertView, ViewGroup container) {
		View view = convertView;
		if(view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
			ViewHolder holder = new ViewHolder();
			holder.mTitle = (TextView)view.findViewById(R.id.item_title);
			holder.mContent = (TextView)view.findViewById(R.id.item_text);
			holder.mImage = (ImageView)view.findViewById(R.id.item_pic);
			holder.mDate = (TextView)view.findViewById(R.id.item_date);
			view.setTag(holder);
		}
		
		bindView(view, pos);
		return view;
	}
	
	private void bindView(View view, int pos) {
		ViewHolder holder = (ViewHolder)view.getTag();
		News news = mList.get(pos);
		
		holder.mTitle.setText(news.mTitle);
		holder.mContent.setText(news.mContent);
		holder.mDate.setText(news.mDate);
	}
	
	private static class ViewHolder {
		TextView mTitle, mContent, mDate;
		ImageView mImage;
	}
}
