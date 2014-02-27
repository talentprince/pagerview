package com.tpr.pagerview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class PagerView extends ViewGroup {
	
	private VelocityTracker mVT;
	private static final int SNAP_VELOCITY = 600;
	private static final int SNAP_SPEED = 900;
	private Scroller mScroller;
	private int mCurScreen;
	private int mDefaultScreen = 0;
	private float mLastMotionX;
	private OnViewChangedListener listener;

	public PagerView(Context context) {
		super(context);
		init(context);
	}
	
	//不加这个构造函数，xml里面无法写属性
	public PagerView(Context context, AttributeSet attrs){
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		mCurScreen = mDefaultScreen;
		mScroller = new Scroller(context);
		mLastMotionX = 0;
	}
	/**
	 * 为每一个子View分派布局，这里每一个View都占一个屏幕
	 */
	@Override
	protected void onLayout(boolean change, int l, int t, int r, int b) {
		if(change){
			int childLeft = 0;
			int count = getChildCount();
			for(int i = 0; i < count; i++){
				View child = getChildAt(i);
				if(child.getVisibility() != View.GONE){
					int childWidth = child.getMeasuredWidth();
					child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
					childLeft += childWidth;
				}

			}
		}
	}
	
	/**
	 * 测量子View的大小
	 * 这里给每一个View都设置一屏的大小
	 * 如果需要自定大小，可以使用 MeasureSpec.makeMeasureSpec 进行分配
	 * 最后也可以通过setMeasureDemension设置整个ViewGroup的实际大小
	 * called before layout
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		for(int i = 0; i < count; i++){
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		
		//滑动到当前页
		int width = MeasureSpec.getSize(widthMeasureSpec);
		scrollTo(mCurScreen*width, 0);
	}
	
	//如果没有达到滑动下页要求，则正位
	public void snapToDestination() {
		final int screenWidth = getWidth();

		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}

	/**
	 * 滑动到某一屏
	 * 离得越远切换的越快
	 * @param whichScreen
	 */
	public void snapToScreen(int whichScreen) {
		if (getScrollX() != (whichScreen * getWidth())) {
			final int delta = whichScreen * getWidth() - getScrollX();
			mScroller.startScroll(getScrollX(), 0, delta, 0,
					Math.abs(delta / ((mCurScreen - whichScreen) == 0 ? 1 : (mCurScreen - whichScreen)))) ;
			mCurScreen = whichScreen;
			invalidate(); 

			if (listener != null) {
				listener.onPageChange(mCurScreen);
			}
		}
	}
	
	//刷新子View的滑动
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:

			if (mVT == null) {
				mVT = VelocityTracker.obtain();
				mVT.addMovement(event);
			}
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;

		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
			if (canMove(deltaX)) {
				if (mVT != null) {
					mVT.addMovement(event);
				}
				mLastMotionX = x;
				//正向或者负向移动，屏幕跟随手指移动
				scrollBy(deltaX, 0);
			}
			break;

		case MotionEvent.ACTION_UP:

			int velocityX = 0;
			if (mVT != null) {
				mVT.addMovement(event);
				mVT.computeCurrentVelocity(1000);
				//得到X轴方向手指移动速度
				velocityX = (int) mVT.getXVelocity();
			}
			//velocityX为正值说明手指向右滑动，为负值说明手指向左滑动
			if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
				// Fling enough to move left
				snapToScreen(mCurScreen - 1);
			} else if (velocityX < -SNAP_VELOCITY
					&& mCurScreen < getChildCount() - 1) {
				// Fling enough to move right
				snapToScreen(mCurScreen + 1);
			} else {
				//没划够 弹回来把
				snapToDestination();
			}

			if (mVT != null) {
				mVT.recycle();
				mVT = null;
			}

			break;
		}
		return true;
	}

	//无法托动超过边界的1/3
	private boolean canMove(int deltaX) {
		//deltaX<0说明手指向右划
		if (getScrollX() <= -getWidth()/3 && deltaX < 0) {
			return false;
		}
		//deltaX>0说明手指向左划
		if (getScrollX() >= (getChildCount() - 1)* getWidth() + getWidth()/3 && deltaX > 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 设置事件监听器
	 * @param listener
	 */
	public void setOnViewChangedListener(OnViewChangedListener listener){
		this.listener = listener;
	}
	
	interface OnViewChangedListener{
		void onPageChange(int current);
	}

}
