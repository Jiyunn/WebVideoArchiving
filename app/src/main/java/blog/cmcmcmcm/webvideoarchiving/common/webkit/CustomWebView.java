package blog.cmcmcmcm.webvideoarchiving.common.webkit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class CustomWebView extends WebView{

    private WebViewScrollListener mOnScrollChangedCallback;

    public CustomWebView(final Context context)
    {
        super(context);
    }

    public CustomWebView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomWebView(final Context context, final AttributeSet attrs, final int defStyle)    {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt)    {
        super.onScrollChanged(l, t, oldl, oldt);
        if(mOnScrollChangedCallback != null) {
            mOnScrollChangedCallback.onScroll(l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangedCallback(final WebViewScrollListener onScrollChangedCallback)    {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

}
