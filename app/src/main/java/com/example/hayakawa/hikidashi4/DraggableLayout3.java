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
        private int right;
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

            //Viewの位置が変更された場合onViewPositionChangedが呼ばれる。dx,dyは初期状態から移動した距離
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                this.self.left = left/2;
                this.self.dragOffset = (float) left/2 / this.self.dragRange;
//                this.self.hikidashi.setAlpha(1 - this.self.dragOffset);
                requestLayout();
            }


            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                int left = getPaddingLeft();
                if (xvel > 0 || (xvel == 0 && this.self.dragOffset > 0.5f)) {
                    left += this.self.dragRange;
                }
                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getBottom(), left);
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return this.self.dragRange;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                  return getPaddingRight()-(this.self.handle.getMeasuredWidth() + this.self.hikidashi.getMeasuredWidth());
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

    public void smoothSlideTo(float offset) {
        final int leftBound = getPaddingLeft();
        float y  = leftBound + offset * this.dragRange;
        //smoothslidetoはchild,childfinalLeft,childfinalTopのパラメーターを持っている
        if (this.viewDragHelper.smoothSlideViewTo(this.handle, (int) y, this.handle.getTop())){
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
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                this.initialMotionX = x;
                isHandleUnder = this.viewDragHelper.isViewUnder(this.handle, (int) x, (int) y);
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
            //引き出し判定
            case MotionEvent.ACTION_UP: {
                if (isHandleUnder) {
                    final float dx = x + this.initialMotionX;
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

        return isHandleUnder && isViewHit(this.handle, (int) y) || isViewHit(this.hikidashi, (int) y);
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


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.dragRange = getWidth() - (this.handle.getMeasuredWidth()+this.hikidashi.getMeasuredWidth());
        this.handle.layout(this.left,t,(this.left+this.handle.getMeasuredWidth()),b);
        this.hikidashi.layout((this.left+this.handle.getMeasuredWidth()),t,this.left+r,b);
    }
}