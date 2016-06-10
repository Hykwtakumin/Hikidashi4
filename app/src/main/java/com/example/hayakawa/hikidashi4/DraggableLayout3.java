package com.example.hayakawa.hikidashi4;

/**
 * Created by hayakawa on 2016/06/03.
 */
import android.annotation.TargetApi;
import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class DraggableLayout3 extends ViewGroup {

    private static final float SENSITIVITY = 1.0f;

    private ViewDragHelper viewDragHelper;

    private View handle;
    private View hikidashi;

    private float initialMotionY;
    private float initialMotionX;

    private int top;
    private int left;

    private int dragRange;
    private float dragOffset;


    public DraggableLayout3(Context context) {
        super(context);
    }

    public DraggableLayout3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableLayout3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public DraggableLayout3(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void setup() {
        viewDragHelper = ViewDragHelper.create(this, SENSITIVITY, new ViewDragHelper.Callback() {

            DraggableLayout3 self = DraggableLayout3.this;

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return this.self.handle == child;
            }

//            @Override
//            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//                this.self.top = top;
//                this.self.dragOffset = (float) top / this.self.dragRange;
//                this.self.view.setAlpha(1 - this.self.dragOffset);
//                requestLayout();
//            }
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                this.self.left = left;
                this.self.dragOffset = (float) left / this.self.dragRange;
                this.self.hikidashi.setAlpha(1 - this.self.dragOffset);
                requestLayout();
            }

//            @Override
//            public void onViewReleased(View releasedChild, float xvel, float yvel) {
//                int top = getPaddingTop();
//                if (yvel > 0 || (yvel == 0 && this.self.dragOffset > 0.5f)) {
//                    top += this.self.dragRange;
//                }
//                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
//            }
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                int left = getPaddingLeft();
                if (xvel > 0 || (xvel == 0 && this.self.dragOffset > 0.5f)) {
                    left += this.self.dragRange;
                }
                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getWidth(), left);
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return this.self.dragRange;
            }

//            @Override
//            public int clampViewPositionVertical(View child, int top, int dy) {
//                final int topBound = getPaddingTop();
//                final int bottomBound = getHeight() - this.self.headerView.getHeight() - this.self.headerView.getPaddingBottom();
//                return Math.min(Math.max(top, topBound), bottomBound);
//            }
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = getPaddingLeft();
                final int RightBound = getWidth() - this.self.handle.getWidth() - this.self.handle.getPaddingRight();
                return Math.min(Math.max(left, leftBound), RightBound);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.handle = findViewById(R.id.handle);
        this.hikidashi = findViewById(R.id.hikidashi);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setup();
    }

    @Override
    public void computeScroll() {
        if (this.viewDragHelper.continueSettling(true)) {
            postInvalidateOnAnimation();
        }
    }

//    public void smoothSlideTo(float offset) {
//        final int topBound = getPaddingTop();
//        float y = topBound + offset * this.dragRange;
//        if (this.viewDragHelper.smoothSlideViewTo(this.headerView, this.headerView.getLeft(), (int) y)) {
//            postInvalidateOnAnimation();
//        }
//    }
    public void smoothSlideTo(float offset) {
        final int leftBound = getPaddingLeft();
        float x = leftBound + offset * this.dragRange;
        if (this.viewDragHelper.smoothSlideViewTo(this.handle, this.handle.getLeft(), (int) x)) {
            postInvalidateOnAnimation();
        }
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();

        if (action != MotionEvent.ACTION_DOWN) {
            this.viewDragHelper.cancel();
            return super.onInterceptTouchEvent(event);
        }

        final float x = event.getX();
        final float y = event.getY();
        boolean isHandleUnder = false;

//        switch (action) {
//            case MotionEvent.ACTION_DOWN: {
//                this.initialMotionY = y;
//                isHeaderViewUnder = this.viewDragHelper.isViewUnder(this.headerView, (int) x, (int) y);
//                break;
//            }
//        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                this.initialMotionX = x;
                isHandleUnder = this.viewDragHelper.isViewUnder(this.handle, (int) y, (int) x);
                break;
            }
        }

        return this.viewDragHelper.shouldInterceptTouchEvent(event) || isHandleUnder;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.viewDragHelper.processTouchEvent(event);

        final int action = event.getActionMasked();
        final float x = event.getX();
        final float y = event.getY();

//        boolean isHeaderViewUnder = this.viewDragHelper.isViewUnder(this.headerView, (int) x, (int) y);
        boolean isHandleUnder = this.viewDragHelper.isViewUnder(this.handle, (int) y, (int) x);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                //this.initialMotionY = y;
                this.initialMotionX = x;
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (isHandleUnder) {
                    final float dx = x - this.initialMotionX;
                    final int slop = this.viewDragHelper.getTouchSlop();
                    if (Math.abs(dx) < Math.abs(slop)) {
                        if (this.dragOffset == 0) {
                            smoothSlideTo(1f);
                        } else {
                            smoothSlideTo(0f);
                        }
                    } else {
                        float handleCenterX = this.handle.getX() + this.handle.getWidth() / 2;
                        ;
                        if (handleCenterX >= getWidth() / 2) { //スライド判定
                            smoothSlideTo(1f);
                        } else {
                            smoothSlideTo(0f);
                        }
                    }
                }
                break;
            }
        }

//        return isHeaderViewUnder && isViewHit(this.headerView, (int) y) || isViewHit(this.view, (int) y);
        return isHandleUnder && isViewHit(this.handle, (int) x) || isViewHit(this.hikidashi, (int) x);
    }

    private boolean isViewHit(View view, int x) {
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int screenX = parentLocation[1] + x;
        return screenX >= viewLocation[1] && screenX < viewLocation[1] + view.getWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        this.dragRange = getHeight() - this.headerView.getHeight();
//        this.headerView.layout(0, this.top, r, this.top + this.headerView.getMeasuredHeight());
//        this.view.layout(0, this.top + this.headerView.getMeasuredHeight(), r, this.top + b);
//    }
    //left,top,right,bottom
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.dragRange = this.hikidashi.getWidth();
        this.handle.layout(r - (this.hikidashi.getMeasuredWidth())-this.handle.getMeasuredWidth(), (getHeight() - this.hikidashi.getMeasuredHeight()), r-this.handle.getMeasuredWidth(), b);
        this.hikidashi.layout(r-this.handle.getMeasuredWidth(), (getHeight() - this.hikidashi.getMeasuredHeight()), r, b);
    }
}