package blog.cmcmcmcm.webvideoarchiving.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class CustomWebView extends WebView{

    private OnScrollChangedCallback mOnScrollChangedCallback;

    public CustomWebView(final Context context)
    {
        super(context);
    }

    public CustomWebView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomWebView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt)
    {
        super.onScrollChanged(l, t, oldl, oldt);
        if(mOnScrollChangedCallback != null) {
            mOnScrollChangedCallback.onScroll(l, t, oldl, oldt);
        }
    }

    public OnScrollChangedCallback getOnScrollChangedCallback()
    {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback)
    {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }


    public static interface OnScrollChangedCallback
    {
        public void onScroll(int l, int t, int oldl, int oldt);
    }
}
