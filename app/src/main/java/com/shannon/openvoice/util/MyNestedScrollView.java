package com.shannon.openvoice.util;

/**
 * Date:2022/4/26
 * Time:16:48
 * author:dimple
 * 自定义NestedScrollView  实现滑动状态
 */

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class MyNestedScrollView extends NestedScrollView implements NestedScrollView.OnScrollChangeListener {

    private AddScrollChangeListener addScrollChangeListener;

    /**
     * 滚动状态
     */
    public enum ScrollState {
        DRAG,      // 拖拽中
        SCROLLING, // 正在滚动
        IDLE       // 已停止
    }

    /**
     * 记录上一次滑动
     */
    private int lastScrollY;
    /**
     *
     */
    private boolean isStart = false;
    /**
     * 上一次记录的时间
     */
    private long lastTime;


    private Handler handler;
    /**
     * 整個滾動内容高度
     */
    public int totalHeight = 0;

    /**
     * 当前view的高度
     */
    public int viewHeight = 0;

    /**
     * 是否滚动到底了
     */
    private boolean bottom = false;

    /**
     * 是否滚动在顶部
     *
     * @param context
     */
    private boolean top = false;


    public MyNestedScrollView(@NonNull Context context) {
        this(context, null);
    }

    public MyNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnScrollChangeListener(this);
        handler = new Handler(context.getMainLooper());
    }


    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        /*实时滚动回调*/
        if (addScrollChangeListener != null) {
            addScrollChangeListener.onScrollChange(scrollX, scrollY, oldScrollX, oldScrollY);
        }

        if (totalHeight > viewHeight && (totalHeight - viewHeight) == scrollY) {
            bottom = true;
        } else {
            bottom = false;
        }

        if (getScrollY() <= 0) {
            top = true;
        } else {
            top = false;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        totalHeight = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            totalHeight += view.getMeasuredHeight();
        }
        viewHeight = getHeight();
    }


    /**
     * 是否动到底
     *
     * @return
     */
    public boolean isBottom() {
        return bottom;
    }


    /**
     * 是否滚动到了 顶部
     *
     * @return
     */
    public boolean isTop() {
        return top;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isStart = false;
                if (addScrollChangeListener != null) {
                    addScrollChangeListener.onScrollState(ScrollState.DRAG);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_UP:
                isStart = true;
                start();
                break;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 开始计算是否停止还是正在滚性滑动
     */
    private void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 表示已停止
                 */
                while (isStart) {
                    if ((System.currentTimeMillis() - lastTime) > 50) {
                        int newScrollY = getScrollY();
                        lastTime = System.currentTimeMillis();
                        if (newScrollY - lastScrollY == 0) {
                            isStart = false;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (addScrollChangeListener != null) {
                                        addScrollChangeListener.onScrollState(ScrollState.IDLE);
                                    }
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (isStart && addScrollChangeListener != null) {
                                        addScrollChangeListener.onScrollState(ScrollState.SCROLLING);
                                    }
                                }
                            });
                        }
                        lastScrollY = newScrollY;
                    }
                }
            }
        }).start();
    }


    /**
     * 设置监听
     *
     * @param addScrollChangeListener
     * @return
     */
    public MyNestedScrollView addScrollChangeListener(AddScrollChangeListener addScrollChangeListener) {
        this.addScrollChangeListener = addScrollChangeListener;
        return this;
    }


    public interface AddScrollChangeListener {
        /**
         * 滚动监听
         *
         * @param scrollX
         * @param scrollY
         * @param oldScrollX
         * @param oldScrollY
         */
        void onScrollChange(int scrollX, int scrollY, int oldScrollX, int oldScrollY);


        /**
         * 滚动状态
         *
         * @param state
         */
        void onScrollState(ScrollState state);
    }
}


