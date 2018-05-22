package blog.cmcmcmcm.webvideoarchiving.view

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View

class WebFloatingBehavior(context: Context, attributeSet: AttributeSet) : FloatingActionButton.Behavior() {


    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
                                target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {

       if (child.visibility == View.VISIBLE && dyConsumed > 0) {
           child.hide()
       } else if ( child.visibility == View.GONE && dyConsumed < 0) {
           child.show()
       }
    }




}