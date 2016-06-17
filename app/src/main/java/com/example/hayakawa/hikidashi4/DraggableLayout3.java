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
            //ここの値が引っ込む/出っ張る長さに関わっている
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                this.self.left = left;
                this.self.dragOffset = (float) left / this.self.dragRange;
//                this.self.hikidashi.setAlpha(1 - this.self.dragOffset);
                requestLayout();
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
//                int left = getPaddingLeft();
                int right = getPaddingRight();
                if (xvel > 0 || (xvel == 0 && this.self.dragOffset > 0.5f)) {
//                    left += this.self.dragRange;
                    right -= this.self.dragRange;
                }
                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getBottom(), right);
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return this.self.dragRange;
            }

            //引き出した後の座標
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {

                  return (this.self.hikidashi.getMeasuredWidth());
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
        float x  = leftBound + offset * this.dragRange;
        //smoothslidetoはchild,childfinalLeft,childfinalTopのパラメーターを持っている
        if (this.viewDragHelper.smoothSlideViewTo(this.handle, (int) x, this.handle.getLeft())){
            postInvalidateOnAnimation();
        }
    }



        //タッチ追跡イベント
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();

        //ACTION_DOWN : ボタン押下時イベント
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
                    final float dx = x - this.initialMotionX; //指の座標が画面中どこまで来ているのか
                    final int slop = this.viewDragHelper.getTouchSlop(); //判定閾値
                    if (Math.abs(dx) < Math.abs(slop)) {
                        if (this.dragOffset == 0) {
                            smoothSlideTo(1f);
                        } else {
                            smoothSlideTo(0f);
                        }
                    } else {
//                        float handleCenterX = (this.handle.getX() + this.handle.getWidth())*0.5f;
//                        if (handleCenterX >= getWidth() *0.5f) { //スライド判定
//                            smoothSlideTo(1f);
//                        } else {
//                            smoothSlideTo(0f);
//                        }
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
//        measureChildren(widthMeasureSpec, heightMeasureSpec);
//        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
//        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
//        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
//                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
        measureChildren(widthMeasureSpec,heightMeasureSpec);
            int reqWidth = handle.getMeasuredWidth() + hikidashi.getMeasuredWidth();
            int reqHeight = Math.max(handle.getMeasuredHeight(), hikidashi.getMeasuredHeight());

        setMeasuredDimension(resolveSizeAndState(reqWidth, widthMeasureSpec, 0),
                resolveSizeAndState(reqHeight, heightMeasureSpec, 0));
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        this.dragRange = getWidth() - (this.handle.getMeasuredWidth()+this.hikidashi.getMeasuredWidth());
//        this.handle.layout(this.left,t,(this.left+this.handle.getMeasuredWidth()),t+this.getMeasuredHeight());
//        this.hikidashi.layout((this.left+this.handle.getMeasuredWidth()),t,this.left+r,b);
        int myWidth  = l - r;
        int myHeight = b - t;

        this.dragRange = r - hikidashi.getMeasuredWidth();
        
        int handleTop = Math.round(myHeight*0.5f - handle.getMeasuredHeight()*0.5f);
        int handleBottom = handleTop + handle.getMeasuredHeight();

        
        int hikidashiTop = Math.round(myHeight*0.5f - hikidashi.getMeasuredHeight()*0.5f);
        int hikidashiBottom = hikidashiTop + hikidashi.getMeasuredHeight();
//        int hikidashiLeft = Math.round(myWidth*0.5f - hikidashi.getMeasuredWidth()*0.5f);
//        int hikidashiRight = hikidashiLeft + hikidashi.getMeasuredWidth();
        //飛び出過ぎているのはdragRangeとかonViewなんたらのせい

        this.handle.layout(this.left, handleTop, this.left +this.handle.getMeasuredWidth(), handleBottom);
        this.hikidashi.layout(this.left + this.handle.getMeasuredWidth(),hikidashiTop,this.left + r,hikidashiBottom);

    }
}






//package com.goka.drager;
//
//        import android.annotation.TargetApi;
//        import android.content.Context;
//        import android.support.v4.widget.ViewDragHelper;
//        import android.util.AttributeSet;
//        import android.view.MotionEvent;
//        import android.view.View;
//        import android.view.ViewGroup;
//
//public class DraggableLayout3 extends ViewGroup {
//
//    private static final float SENSITIVITY = 1.0f;
//
//    private ViewDragHelper viewDragHelper;
//
//    private View headerView;
//    private View view;
//
//    private float initialMotionY;
//
//    private int top;
//    private int dragRange;
//    private float dragOffset;
//
//
//    public DraggableLayout3(Context context) {
//        super(context);
//    }
//
//    public DraggableLayout3(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public DraggableLayout3(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    @TargetApi(21)
//    public DraggableLayout3(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }
//
//    private void setup() {
//        viewDragHelper = ViewDragHelper.create(this, SENSITIVITY, new ViewDragHelper.Callback() {
//
//            DraggableLayout3 self = DraggableLayout3.this;
//
//            @Override
//            public boolean tryCaptureView(View child, int pointerId) {
//                return this.self.headerView == child;
//            }
//
//            @Override
//            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//                this.self.top = top;
//                this.self.dragOffset = (float) top / this.self.dragRange;
////                this.self.view.setAlpha(1 - this.self.dragOffset);
//                requestLayout();
//            }
//
//            @Override
//            public void onViewReleased(View releasedChild, float xvel, float yvel) {
//                int top = getPaddingTop();
//                if (yvel > 0 || (yvel == 0 && this.self.dragOffset > 0.5f)) {
//                    top += this.self.dragRange;
//                }
//                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
//            }
//
//            @Override
//            public int getViewVerticalDragRange(View child) {
//                return this.self.dragRange;
//            }
//
//            @Override
//            public int clampViewPositionVertical(View child, int top, int dy) {
//                final int topBound =getPaddingTop();
//                final int bottomBound = getHeight() - (this.self.headerView.getHeight() + this.self.headerView.getPaddingBottom());
//                return Math.min(Math.max(top, topBound), bottomBound);
//            }
//        });
//    }
//
//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        this.headerView = findViewById(R.id.header3);
//        this.view = findViewById(R.id.view3);
//    }
//
//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        setup();
//    }
//
//    @Override
//    public void computeScroll() {
//        if (this.viewDragHelper.continueSettling(true)) {
//            postInvalidateOnAnimation();
//        }
//    }
//
//    public void smoothSlideTo(float offset) {
//        final int topBound = getPaddingTop();
//        float y = topBound + offset * this.dragRange;
//        if (this.viewDragHelper.smoothSlideViewTo(this.headerView, this.headerView.getLeft(), (int) y)) {
//            postInvalidateOnAnimation();
//        }
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        final int action = event.getActionMasked();
//
//        if (action != MotionEvent.ACTION_DOWN) {
//            this.viewDragHelper.cancel();
//            return super.onInterceptTouchEvent(event);
//        }
//
//        final float x = event.getX();
//        final float y = event.getY();
//        boolean isHeaderViewUnder = false;
//
//        switch (action) {
//            case MotionEvent.ACTION_DOWN: {
//                this.initialMotionY = y;
//                isHeaderViewUnder = this.viewDragHelper.isViewUnder(this.headerView, (int) x, (int) y);
//                break;
//            }
//        }
//
//        return this.viewDragHelper.shouldInterceptTouchEvent(event) || isHeaderViewUnder;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        this.viewDragHelper.processTouchEvent(event);
//
//        final int action = event.getActionMasked();
//        final float x = event.getX();
//        final float y = event.getY();
//
//        boolean isHeaderViewUnder = this.viewDragHelper.isViewUnder(this.headerView, (int) x, (int) y);
//        switch (action) {
//            case MotionEvent.ACTION_DOWN: {
//                this.initialMotionY = y;
//                break;
//            }
//
//            case MotionEvent.ACTION_UP: {
//                if (isHeaderViewUnder) {
//                    final float dy = y - this.initialMotionY;
//                    final int slop = this.viewDragHelper.getTouchSlop();
//                    if (Math.abs(dy) < Math.abs(slop)) {
//                        if (this.dragOffset == 0) {
//                            smoothSlideTo(1f);
//                        } else {
//                            smoothSlideTo(0f);
//                        }
//                    } else {
//                        float headerViewCenterY = this.headerView.getY() + this.headerView.getHeight()/2;
//                        ;
//                        if (headerViewCenterY >= getHeight() / 2) {
//                            smoothSlideTo(1f);
//                        } else {
//                            smoothSlideTo(0f);
//                        }
//                    }
//                }
//                break;
//            }
//        }
//
//        return isHeaderViewUnder && isViewHit(this.headerView, (int) y) || isViewHit(this.view, (int) y);
//    }
//
//    private boolean isViewHit(View view, int y) {
//        int[] parentLocation = new int[2];
//        this.getLocationOnScreen(parentLocation);
//        int[] viewLocation = new int[2];
//        view.getLocationOnScreen(viewLocation);
//        int screenY = parentLocation[1] + y;
//        return screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        measureChildren(widthMeasureSpec, heightMeasureSpec);
//
//        int reqWidth = Math.max(headerView.getMeasuredWidth(), view.getMeasuredWidth());
//        int reqHeight = headerView.getMeasuredHeight() + view.getMeasuredHeight();
////
////        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
////        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
////        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
////                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
//
//        setMeasuredDimension(resolveSizeAndState(reqWidth, widthMeasureSpec, 0),
//                resolveSizeAndState(reqHeight, heightMeasureSpec, 0));
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        int myWidth = r - l;
//        int myHeight = b - t;
//
//        this.dragRange = myHeight - this.headerView.getHeight();
//
//        int headerLeft = Math.round(myWidth * 0.5f - headerView.getMeasuredWidth() * 0.5f);
//        int headerRight = headerLeft + headerView.getMeasuredWidth();
//
//        this.headerView.layout(headerLeft, 0, headerRight, this.headerView.getMeasuredHeight());
//
//        this.view.layout(0, this.headerView.getMeasuredHeight(), myWidth, myHeight);
//    }
//}


//        <?xml version="1.0" encoding="utf-8"?>
//<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
//        xmlns:tools="http://schemas.android.com/tools"
//        android:layout_width="match_parent"
//        android:layout_height="match_parent"
//        tools:context=".MainActivity3">
//
//<TextView
//android:layout_width="wrap_content"
//        android:layout_height="wrap_content"
//        android:text="Hello World!" />
//
//<com.goka.drager.DraggableLayout3
//        android:id="@+id/draggable_layout3"
//        android:layout_width="wrap_content"
//        android:layout_height="wrap_content"
//        android:layout_gravity="bottom|center_horizontal"
//        android:visibility="visible">
//
//<TextView
//android:id="@+id/header3"
//        android:layout_width="100dp"
//        android:layout_height="60dp"
//        android:background="@android:color/holo_blue_bright"
//        android:gravity="center"
//        android:text="header"
//        android:textColor="@android:color/white"
//        android:textSize="23sp" />
//
//<TextView
//android:id="@+id/view3"
//        android:layout_width="200dp"
//        android:layout_height="100dp"
//        android:background="@android:color/holo_blue_light"
//        android:gravity="center"
//        android:text="content"
//        android:textColor="@android:color/white"
//        android:textSize="32sp" />
//
//</com.goka.drager.DraggableLayout3>
//
//</FrameLayout>