package blog.cmcmcmcm.webvideoarchiving.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun hideSoftKeyboard(context: Context?, v: View?) {
    (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(v?.windowToken, 0)
}

