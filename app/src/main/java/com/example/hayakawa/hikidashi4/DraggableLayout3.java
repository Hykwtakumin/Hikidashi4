package com.example.hayakawa.hikidashi4;

/**
 * Created by hayakawa on 2016/06/03.
 */
    import android.annotation.TargetApi;
    import android.content.Context;
    import android.support.v4.widget.ViewDragHelper;
    import android.util.AttributeSet;
    import android.util.Log;
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
        private int bottom;
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
                //"handle"と"hikidashi"がViewGroupのChildであった場合にtrueを返す
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
////                int left = getPaddingLeft();
//                int right = getPaddingRight();
//                if (xvel > 0 || (xvel == 0 && this.self.dragOffset > 0.5f)) {
////                    left += this.self.dragRange;
//                    right -= this.self.dragRange;
//                }
////                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getBottom(), right);
//                this.self.viewDragHelper.settleCapturedViewAt(0,Math.round(getHeight()* 0.5f - handle.getMeasuredHeight()*0.5f));
                //指が離れた(onReleasedな)Viewがhandleだった場合とhikidashiだった場合で条件分岐をつける
                if (releasedChild == this.self.handle){
                    this.self.viewDragHelper.settleCapturedViewAt(0,Math.round((this.self.getMeasuredHeight())* 0.5f - handle.getMeasuredHeight()*0.5f));//handleが上にぶっ飛ぶのを予防
                }else{
                    this.self.viewDragHelper.settleCapturedViewAt(handle.getMeasuredWidth(),this.self.getMeasuredHeight());
                }
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return this.self.dragRange;
            }

            //縦方向へのドラッグを制限
            @Override
            public int getViewVerticalDragRange(View child) {
                if (child ==this.self.handle){
                    return Math.round((this.self.getMeasuredHeight())* 0.5f - handle.getMeasuredHeight()*0.5f);
                }else{
                    return this.self.getMeasuredHeight();
                }
            }

            //dragした後に固定(clamp)しておく座標(垂直方向/y座標)
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                //return child.getY();これが(0,0)になっていると左上にぶっ飛ぶ
                if (child == this.self.handle){
                    return Math.round((this.self.getMeasuredHeight())* 0.5f - handle.getMeasuredHeight()*0.5f);
                }else{
                    return this.self.getMeasuredHeight();
                }
            }

            //dragした後に固定(clamp)しておく座標(水平方向/x座標)
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
//                return getLeft()+handle.getMeasuredWidth();
                if (child == this.self.handle){
                    return 0;
                }else{
                    return handle.getMeasuredWidth();
                }
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.handle = findViewById(R.id.handle);
        this.hikidashi = findViewById(R.id.hikidashi);
        //Log.d("Debug", ""+);
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
        //原則としてAndroidの座標は左上を原点(0,0)とし、右方向と下方向が正の向きとなる(OpenGL等は異なる場合がある)ß
        //Log.d("Debug", ""+);
        //この部分のleft,top,right,bottomはDraggableLayout3の座標
        int myWidth  = l - r;   //DraggableLayout3の"幅"(左端-右端)
        int myHeight = b - t;   //DraggableLayout3の"高さ"(下端-上端)

        this.dragRange = myWidth;



        int handleTop = Math.round(myHeight*0.5f - handle.getMeasuredHeight()*0.5f);    //handleの上端の座標 = (DraggableLayout3の全高の半分 - handleの全高の半分)
        int handleBottom = handleTop + handle.getMeasuredHeight(); //handleの下端の座標 = handleの上端の座標 + handleの全高

        //DraggableLayout3の高さ=hikidashiの高さなので恐らく以下は必要ない(0になる筈)
//        int hikidashiTop = Math.round(myHeight*0.5f - hikidashi.getMeasuredHeight()*0.5f);
//        int hikidashiBottom = hikidashiTop + hikidashi.getMeasuredHeight();

        //draggableLayout3内部の取ってと引き出しの座標を指定(onMeasure以外の数値は指定不可能)
        this.handle.layout(0, handleTop, handle.getMeasuredWidth(), handleBottom);    //0(左端),HandleTop(上端),0+handleの幅(右端),HandleBottom(下端)
        this.hikidashi.layout(handle.getMeasuredWidth(),0,handle.getMeasuredWidth()+hikidashi.getMeasuredWidth(),hikidashi.getMeasuredHeight());    //0+handleの幅(左端),DraggableLayout3の上端=0(上端),0+handleの幅+hikidashiの幅(右端),DraggableLayout3の上端=0+hikidashiの全高(下端)
    }
}