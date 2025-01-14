package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class TerminalScrollView extends ScrollView {
    private int lineHeight = 1;

    private boolean isSnapping = false;

    public TerminalScrollView(Context context) {
        super(context);
    }

    public TerminalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TerminalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (isSnapping)
            return;

        this.scrollTo(l,(t / this.lineHeight) * this.lineHeight);
    }
}
