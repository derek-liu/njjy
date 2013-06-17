package cn.njjy.group.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.njjy.group.R;
import cn.njjy.group.utils.AppUtils;

public class PTRListView extends LinearLayout implements
		OnTouchListener {

	static final int START_ANIM = 0;
	static final int STOP_ANIM = 1;
	static final int PULL_TO_REFRESH = 0;
	static final int RELEASE_TO_REFRESH = PULL_TO_REFRESH + 1;
	static final int REFRESHING = RELEASE_TO_REFRESH + 1;
	static final int EVENT_COUNT = 3;

	private int mState = PULL_TO_REFRESH;
	private ListView mListView;
	private AnimationDrawable mAnim;
	private ImageView mHeaderProgress;
	private TextView mHeaderText;
	private ImageView mHeaderImage;
	private Animation mFlipAnimation, mReverseAnimation;
	private Handler mHandler = new Handler();
	private OnTouchListener onTouchListener;
	private OnRefreshListener onRefreshListener;
	private SmoothScrollRunnable mSmoothScrollRunnable;

	private int mHeaderHeight = AppUtils.dp2px(50);
	private boolean isRefreshable = true;
	private float mStartY = -1;
	private final float[] mLastYs = new float[EVENT_COUNT];

	public void setOnRefreshListener(OnRefreshListener listener) {
		onRefreshListener = listener;
	}

	public void setOnTouchListener(OnTouchListener listener) {
		onTouchListener = listener;
	}

	public void setAdapter(ListAdapter adapter) {
		mListView.setAdapter(adapter);
	}
	
	public void addHeaderView(View view) {
		mListView.addHeaderView(view);
	}
	
	public void addFooterView(View view) {
		mListView.addFooterView(view);
	}
	
	public ListView getListView() {
		return mListView;
	}
	
	public void setRefreshable(boolean able) {
		isRefreshable = able;
	}
	
	public void setRefreshing() {
		mState = REFRESHING;
		mHeaderText.setText(R.string.pull_to_refresh_refreshing);
		mHeaderImage.clearAnimation();
		mHeaderImage.setVisibility(View.INVISIBLE);
		mHeaderProgress.setVisibility(View.VISIBLE);
		smoothScrollTo(mHeaderHeight);
		startAnim();
	}
	
	public void setRefreshComplete() {
		mState = PULL_TO_REFRESH;
		initializeYsHistory();
		mStartY = -1;
		mHeaderImage.setVisibility(View.VISIBLE);
		mHeaderProgress.setVisibility(View.GONE);
		smoothScrollTo(0);
		stopAnim();
	}
	
	public PTRListView(Context context) {
		this(context, null);
	}

	public PTRListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PTRListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.PTRListView, defStyle, 0);
			int dp = a.getInt(R.styleable.PTRListView_header_padding, 50);
			mHeaderHeight = AppUtils.dp2px(dp);
			a.recycle();
		}

		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		setOrientation(LinearLayout.VERTICAL);

		ViewGroup header = (ViewGroup)LayoutInflater.from(context).inflate(
				R.layout.refresh_header, this, false);
		mHeaderText = (TextView)header.findViewById(R.id.pull_to_refresh_text);
		mHeaderImage = (ImageView)header.findViewById(R.id.pull_to_refresh_image);
		mHeaderProgress = (ImageView)header.findViewById(R.id.pull_to_refresh_progress);
		mAnim = (AnimationDrawable)mHeaderProgress.getDrawable();
		addView(header, ViewGroup.LayoutParams.MATCH_PARENT, mHeaderHeight);

		mListView = new ListView(context);
		mListView.setOnTouchListener(this);
		addView(mListView, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		mListView.setDividerHeight(0);
		mListView.setCacheColorHint(0);
		mListView.setDivider(null);
		mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));

		mFlipAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mFlipAnimation.setInterpolator(new LinearInterpolator());
		mFlipAnimation.setDuration(250);
		mFlipAnimation.setFillAfter(true);

		mReverseAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mReverseAnimation.setInterpolator(new LinearInterpolator());
		mReverseAnimation.setDuration(250);
		mReverseAnimation.setFillAfter(true);

		setPadding(getPaddingLeft(), -mHeaderHeight, getPaddingRight(),
				getPaddingBottom());
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (mState == REFRESHING) {
			return true;
		} else {
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				updateEventStates(event);
				if (isPullingDownToRefresh() && mStartY == -1) {
					if (mStartY == -1) {
						mStartY = event.getY();
					}
					return false;
				}

				if (mStartY != -1 && !mListView.isPressed()) {
					pullDown(event, mStartY);
					if(mStartY >= event.getY()){
						return false;
					}
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				initializeYsHistory();
				mStartY = -1;
				if (mState == RELEASE_TO_REFRESH) {
					setRefreshing();
					if (onRefreshListener != null) {
						onRefreshListener.onRefresh();
					}
				} else {
					smoothScrollTo(0);
				}
				break;
			}
			
			if (null != onTouchListener) {
				return onTouchListener.onTouch(view, event);
			}
			return false;
		}
	}
	
	private void startAnim() {
		if (mAnim != null) {
			mAnim.start();
		}
	}

	private void stopAnim() {
		if (mAnim != null) {
			mAnim.stop();
		}
	}

	private void pullDown(MotionEvent event, float firstY) {
		float averageY = average(mLastYs);
		int height = (int) (Math.max(averageY - firstY, 0));
		setHeaderScroll(height);

		if (mState == PULL_TO_REFRESH && mHeaderHeight < height) {
			mState = RELEASE_TO_REFRESH;
			mHeaderText.setText(R.string.pull_to_refresh_release);
			mHeaderImage.clearAnimation();
			mHeaderImage.startAnimation(mFlipAnimation);
		}
		if (mState == RELEASE_TO_REFRESH && mHeaderHeight >= height) {
			mState = PULL_TO_REFRESH;
			mHeaderText.setText(R.string.pull_to_refresh_normal);
			mHeaderImage.clearAnimation();
			mHeaderImage.startAnimation(mReverseAnimation);
		}
	}

	private void setHeaderScroll(int y) {
		scrollTo(0, -y);
	}

	private int getHeaderScroll() {
		return -getScrollY();
	}

	private float average(float[] ysArray) {
		float avg = 0;
		for (int i = 0; i < EVENT_COUNT; i++) {
			avg += ysArray[i];
		}
		return avg / EVENT_COUNT;
	}

	private void initializeYsHistory() {
		for (int i = 0; i < EVENT_COUNT; i++) {
			mLastYs[i] = 0;
		}
	}

	private void updateEventStates(MotionEvent event) {
		for (int i = 0; i < EVENT_COUNT - 1; i++) {
			mLastYs[i] = mLastYs[i + 1];
		}
		float y = event.getY();
		int top = mListView.getTop();
		mLastYs[EVENT_COUNT - 1] = y + top;
	}

	private boolean isPullingDownToRefresh() {
		return mState != REFRESHING && isUserDraggingDownwards()
				&& isFirstVisible();
	}

	private boolean isFirstVisible() {
		if (mListView.getCount() == 0) {
			return true;
		} else if (mListView.getFirstVisiblePosition() == 0) {
			return mListView.getChildAt(0).getTop() >= mListView.getTop();
		} else {
			return false;
		}
	}

	private boolean isUserDraggingDownwards() {
		return this.isUserDraggingDownwards(0, EVENT_COUNT - 1);
	}

	private boolean isUserDraggingDownwards(int from, int to) {
		return isRefreshable && mLastYs[from] != 0 && mLastYs[to] != 0
				&& Math.abs(mLastYs[from] - mLastYs[to]) > 5
				&& mLastYs[from] < mLastYs[to];
	}

	private void smoothScrollTo(int y) {
		if (null != mSmoothScrollRunnable) {
			mSmoothScrollRunnable.stop();
			mHandler.removeCallbacks(mSmoothScrollRunnable);
		}

		mSmoothScrollRunnable = new SmoothScrollRunnable(mHandler,
				getHeaderScroll(), y);
		mHandler.post(mSmoothScrollRunnable);
	}

	public static interface OnRefreshListener {
		public void onRefresh();
	}

	private final class SmoothScrollRunnable implements Runnable {

		static final int ANIMATION_DURATION_MS = 190;
		static final int ANIMATION_FPS = 1000 / 60;

		private final Interpolator interpolator;
		private final int scrollToY;
		private final int scrollFromY;
		private final Handler handler;

		private boolean continueRunning = true;
		private long startTime = -1;
		private int currentY = -1;

		public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
			this.handler = handler;
			this.scrollFromY = fromY;
			this.scrollToY = toY;
			this.interpolator = new AccelerateDecelerateInterpolator();
		}

		@Override
		public void run() {
			if (startTime == -1) {
				startTime = System.currentTimeMillis();
			} else {
				long normalizedTime = (1000 * (System.currentTimeMillis() - startTime))
						/ ANIMATION_DURATION_MS;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

				final int deltaY = Math
						.round((scrollFromY - scrollToY)
								* interpolator
										.getInterpolation(normalizedTime / 1000f));
				this.currentY = scrollFromY - deltaY;
				setHeaderScroll(currentY);
			}

			if (continueRunning && scrollToY != currentY) {
				handler.postDelayed(this, ANIMATION_FPS);
			}
		}

		public void stop() {
			this.continueRunning = false;
			this.handler.removeCallbacks(this);
		}
	};
}


