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
                //"handle"と"hikidashi"がViewGroupのChildであった場合にtrue
                return this.self.handle == child || self.hikidashi == child;
            }

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
//                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getBottom(), right);
                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), releasedChild.getTop());
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return this.self.dragRange;
            }

            //縦方向へのドラッグを制限
            @Override
            public int getViewVerticalDragRange(View child) {
                return 0;
            }


            @Override
            public int clampViewPositionVertical(View handle, int top, int dy) {
                //return child.getY();これが(0,0)になっていると左上にぶっ飛ぶ
                return handle.getTop();
            }

            //引き出した後の座標
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
//                  return (this.self.hikidashi.getMeasuredWidth());
                return getLeft()+handle.getMeasuredWidth();
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
        //smoothslidetoはchild,childfinalLeft,childfinalTopのパラメーター
        //ここのyの値を0にすると上にぶっ飛ぶ(一定の高さを指定する必要がある)
        if (this.viewDragHelper.smoothSlideViewTo(this.handle, (int) x, this.handle.getTop())){
            postInvalidateOnAnimation();
        }
        if (this.viewDragHelper.smoothSlideViewTo(this.hikidashi, (int) x, this.hikidashi.getTop())){
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
        boolean isHikidashiUnder = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                this.initialMotionX = x;
                isHandleUnder = this.viewDragHelper.isViewUnder(this.handle, (int) x, handle.getTop());
                isHikidashiUnder = this.viewDragHelper.isViewUnder(this.hikidashi,(int) x, hikidashi.getTop());
                break;
            }
        }

        return this.viewDragHelper.shouldInterceptTouchEvent(event) || isHandleUnder || isHikidashiUnder;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.viewDragHelper.processTouchEvent(event);

        final int action = event.getActionMasked();
        final float x = event.getX();
        final float y = event.getY();

//        boolean isHeaderViewUnder = this.viewDragHelper.isViewUnder(this.headerView, (int) x, (int) y);
        boolean isHandleUnder = this.viewDragHelper.isViewUnder(this.handle, (int) x, handle.getTop());
        boolean isHikidashiUnder = this.viewDragHelper.isViewUnder(this.hikidashi,(int) x,hikidashi.getTop());
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
                if (isHikidashiUnder) {
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

        return isHandleUnder && isHikidashiUnder && isViewHit(this.handle, (int) x) || isViewHit(this.hikidashi, (int) x);
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

        this.dragRange = getWidth();
        
        int handleTop = Math.round(myHeight*0.5f - handle.getMeasuredHeight()*0.5f);
        int handleBottom = handleTop + handle.getMeasuredHeight();

        
        int hikidashiTop = Math.round(myHeight*0.5f - hikidashi.getMeasuredHeight()*0.5f);
        int hikidashiBottom = hikidashiTop + hikidashi.getMeasuredHeight();
//        int hikidashiLeft = Math.round(myWidth*0.5f - hikidashi.getMeasuredWidth()*0.5f);
//        int hikidashiRight = hikidashiLeft + hikidashi.getMeasuredWidth();
        //飛び出過ぎているのはdragRangeとかonViewなんたらのせい

//        this.handle.layout(this.handle.getLeft(), handleTop, this.handle.getLeft() +this.handle.getMeasuredWidth(), handleBottom);
//        this.hikidashi.layout(this.hikidashi.getLeft(),hikidashiTop,this.hikidashi.getLeft()+r,hikidashiBottom);
        this.handle.layout(0, handleTop, this.left +this.handle.getMeasuredWidth(), handleBottom);
        this.hikidashi.layout(handle.getMeasuredWidth() + this.handle.getMeasuredWidth(),hikidashiTop,this.left+r,hikidashiBottom);

    }
}