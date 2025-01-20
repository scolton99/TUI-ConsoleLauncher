package ohi.andre.consolelauncher.tuils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class TerminalScrollView extends ScrollView {
    private int lineHeight = 1;

    private boolean isSnapping = false;
    private int lastRem = 0;
    private boolean isBeingTouched = false;

    public TerminalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int paddingTop = h % this.lineHeight;
        this.getChildAt(0).setPadding(0, paddingTop, 0, 0);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void scrollTo(int x, int y) {
        this.isSnapping = true;
        super.scrollTo(x, y);
        this.isSnapping = false;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (this.isSnapping)
            return;

        this.scrollTo(l, getNewScrollY(t));

        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                this.isBeingTouched = true;
                break;
            }
            case MotionEvent.ACTION_UP: {
                this.isBeingTouched = false;
                break;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                this.isBeingTouched = true;
                break;
            }
            case MotionEvent.ACTION_UP: {
                this.isBeingTouched = false;
                break;
            }
        }

        return super.onTouchEvent(ev);
    }

    private int prevFixedScrollY = 0;
    private int getNewScrollY(int scrollY) {
        // Round to a multiple of lineHeight
        int newHeight = Math.round((float) scrollY / this.lineHeight) * this.lineHeight;

        if (newHeight == this.prevFixedScrollY && scrollY != newHeight && isBeingTouched) {
            // We're moving within one line...keep track of movement without actually scrolling.
            int rem = scrollY - newHeight;
            this.lastRem += rem;

            int incLines = this.lastRem / this.lineHeight;

            // Add a line (or more) to the new height based on the rem calculation
            // scrollY < prevY ? We're scrolling UP -- only DECREASE lines. don't snap the other way
            // vice versa for scrollY > prevY
            int add = incLines * this.lineHeight;
            newHeight += add;
        }

        if (newHeight != this.prevFixedScrollY) {
            // We've switched lines either by scrollY itself or with rem -- time to reset rem
            this.lastRem = 0;
        }

        this.prevFixedScrollY = newHeight;
        return newHeight;
    }


}
