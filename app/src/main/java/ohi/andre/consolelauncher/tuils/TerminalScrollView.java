package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class TerminalScrollView extends ScrollView {
    private int lineHeight = 1;

    private boolean isSnapping = false;
    private int lastRem = 0;

    public TerminalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TerminalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    @Override
    public void scrollTo(int x, int y) {
        isSnapping = true;
        super.scrollTo(x, y);
        isSnapping = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int paddingTop = h % this.lineHeight;
        this.getChildAt(0).setPadding(0, paddingTop, 0, 0);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     *
     * @param l Current horizontal scroll origin.
     * @param t Current vertical scroll origin.
     * @param oldl Previous horizontal scroll origin.
     * @param oldt Previous vertical scroll origin.
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (isSnapping)
            return;

        // Round to a multiple of lineHeight
        // TODO: should this be a floor? A ceil? A round?
        int newHeight = (t / this.lineHeight) * this.lineHeight;

        // The user tried to scroll further in one direction -- probably. How much did we cut them off?
        // Have to remember this so that if the scrolling is slow -- think 1px per call of this function,
        // we still get a snap to the next line eventually...
        int rem = t - newHeight;
        int cumulativeRem = rem + lastRem;

        newHeight += (cumulativeRem / this.lineHeight) * this.lineHeight;
        this.lastRem = cumulativeRem % this.lineHeight;

        this.scrollTo(l, newHeight);
    }
}
