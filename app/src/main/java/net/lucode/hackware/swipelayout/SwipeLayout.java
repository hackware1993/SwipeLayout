package net.lucode.hackware.swipelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by hackware on 2016/8/25.
 */

public class SwipeLayout extends FrameLayout {
    private static final int DEFAULT_UNITS = 1000;
    private View mHeaderView;
    private View mContentView;

    private OnCheckHandler mOnCheckHandler;
    private OnSwipeListener mOnSwipeListener;
    private float mLastY;
    private float mDownY;
    private float mLastTranslationY;

    // 回弹处理
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    private int mVelocityUnits = DEFAULT_UNITS;
    private boolean mContentClickableWhenHeaderShow = true; // 当header显示时，content是否可点击
    private boolean mInterceptTouch;
    private float mTouchSlop;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public static boolean canChildScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = getChildAt(0);
        mHeaderView = getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeaderView.setTranslationY(-mHeaderView.getMeasuredHeight());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.abortAnimation();
                mVelocityTracker = VelocityTracker.obtain();
                mInterceptTouch = false;
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                }
                boolean isUp = y > mLastY;
                if (isUp) {
                    boolean canSwipe;
                    if (mOnCheckHandler != null) {
                        canSwipe = mOnCheckHandler.checkCanDoSwipe();
                    } else {
                        canSwipe = !canChildScrollUp(mContentView);
                    }
                    if (canSwipe) {
                        float translationY = mHeaderView.getTranslationY() + y - mLastY;
                        translationHeader(translationY);
                        mLastY = y;
                        if (!mInterceptTouch && Math.abs(y - mDownY) > mTouchSlop) {
                            mInterceptTouch = true;
                            dispatchCancelEventToContentView(ev);
                        }
                        return true;
                    }
                } else {
                    if (!isHeaderHidden()) {
                        float translationY = mHeaderView.getTranslationY() + y - mLastY;
                        translationHeader(translationY);
                        mLastY = y;
                        if (!mInterceptTouch && Math.abs(y - mDownY) > mTouchSlop) {
                            mInterceptTouch = true;
                            dispatchCancelEventToContentView(ev);
                        }
                        return true;
                    } else {

                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!isHeaderHidden()) {
                    float velocity = 0.0f;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.computeCurrentVelocity(mVelocityUnits);
                        velocity = mVelocityTracker.getYVelocity();
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    float visibleHeaderHeight = mHeaderView.getMeasuredHeight() + mHeaderView.getTranslationY();
                    boolean showHeader = (visibleHeaderHeight + velocity) > mHeaderView.getMeasuredHeight() / 2.0f;
                    float start = mHeaderView.getTranslationY();
                    float end;
                    if (showHeader) {
                        end = 0.0f;
                    } else {
                        end = -mHeaderView.getMeasuredHeight();
                    }
                    mScroller.startScroll(0, Math.round(start), 0, Math.round(end - start));
                    invalidate();
                }
                break;
            default:
                break;
        }
        mLastY = y;
        return super.dispatchTouchEvent(ev);
    }

    private void translationHeader(float translationY) {
        if (translationY > 0.0f) {
            translationY = 0.0f;
        } else if (translationY < -mHeaderView.getMeasuredHeight()) {
            translationY = -mHeaderView.getMeasuredHeight();
        }
        if (mLastTranslationY != translationY) {
            mHeaderView.setTranslationY(translationY);
            if (mOnSwipeListener != null) {
                mOnSwipeListener.onSwipe(1.0f + translationY / mHeaderView.getMeasuredHeight());
                if (translationY == 0.0f) {
                    mOnSwipeListener.onHeaderOpen();
                } else if (translationY == -mHeaderView.getMeasuredHeight()) {
                    mOnSwipeListener.onHeaderClose();
                }
            }
        }
        mLastTranslationY = translationY;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            translationHeader(y);
            invalidate();
        }
    }

    private void dispatchCancelEventToContentView(MotionEvent sourceEvent) {
        MotionEvent targetEvent = MotionEvent.obtain(sourceEvent.getDownTime(), sourceEvent.getEventTime(), MotionEvent.ACTION_CANCEL, sourceEvent.getX(), sourceEvent.getY(), sourceEvent.getMetaState());
        mContentView.dispatchTouchEvent(targetEvent);
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        mOnSwipeListener = onSwipeListener;
    }

    public void setOnCheckHandler(OnCheckHandler onCheckHandler) {
        mOnCheckHandler = onCheckHandler;
    }

    public void setVelocityUnits(int velocityUnits) {
        mVelocityUnits = velocityUnits;
    }

    public void closeHeader() {
        mScroller.startScroll(0, Math.round(mHeaderView.getTranslationY()), 0, Math.round(-mHeaderView.getMeasuredHeight() - mHeaderView.getTranslationY()));
        invalidate();
    }

    public void openHeader() {
        mScroller.startScroll(0, Math.round(mHeaderView.getTranslationY()), 0, Math.round(-mHeaderView.getTranslationY()));
        invalidate();
    }

    public boolean isHeaderHidden() {
        return mHeaderView.getTranslationY() == -mHeaderView.getMeasuredHeight();
    }

    public interface OnSwipeListener {
        void onSwipe(float percent);

        void onHeaderOpen();

        void onHeaderClose();
    }

    public interface OnCheckHandler {
        boolean checkCanDoSwipe();
    }
}
