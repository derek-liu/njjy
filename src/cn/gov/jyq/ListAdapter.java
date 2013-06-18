package cn.gov.jyq;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.gov.jyq.imageloader.ImageLoader;
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
			holder.mBigImage = (ImageView)view.findViewById(R.id.item_big_image);
			holder.mImageView = (ImageView)view.findViewById(R.id.item_pic);
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
		holder.mDate.setText(Html.fromHtml(news.mDate));
		
		if(TextUtils.isEmpty(news.mPicUrl)) {
			holder.mBigImage.setVisibility(View.GONE);
			holder.mImageView.setVisibility(View.GONE);
		} else {
			if(pos == 0) {
				holder.mBigImage.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().bind(news.mPicUrl, holder.mBigImage);
			} else {
				holder.mImageView.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().bind(news.mPicUrl, holder.mImageView);
			}
		}
	}
	
	private static class ViewHolder {
		TextView mTitle, mContent, mDate;
		ImageView mBigImage, mImageView;
	}
}
