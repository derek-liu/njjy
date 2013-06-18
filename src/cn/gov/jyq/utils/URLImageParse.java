package cn.gov.jyq.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.widget.TextView;

public class URLImageParse implements ImageGetter {
	private int mMaxSize;
	private Context mContext;
	private TextView mContainer;

	public URLImageParse(TextView t, Context context) {
		mContainer = t;
		mContext = context;
		mMaxSize = AppUtils.dp2px(100);
	}
	
	@Override
	public Drawable getDrawable(String source) {
		URLDrawable urlDrawable = new URLDrawable();
		urlDrawable.setBounds(0, 0, mMaxSize, mMaxSize);
		
		ImageGetterAsyncTask task = new ImageGetterAsyncTask(urlDrawable);
		task.execute(source);
		return urlDrawable;
	}

	public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
        	int iw = result.getIntrinsicWidth();
        	int ih = result.getIntrinsicHeight();
        	
        	if(iw > ih) {
        		float ratio = mMaxSize * 1f / iw;
        		urlDrawable.setBounds(0, 0, iw, (int)(ratio * ih));
        	} else {
        		float ratio = mMaxSize * 1f / ih;
        		urlDrawable.setBounds(0, 0, (int)(ratio * iw), ih);
        	}
        	
            urlDrawable.drawable = result;
            URLImageParse.this.mContainer.invalidate();
        }

        public Drawable fetchDrawable(String urlString) {
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");
                int w = (int)drawable.getIntrinsicWidth() * 5;
                int h = (int)drawable.getIntrinsicHeight() * 5;
                drawable.setBounds(0, 0, w, h); 
                return drawable;
            } catch (Exception e) {
                return null;
            } 
        }

        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }
}
