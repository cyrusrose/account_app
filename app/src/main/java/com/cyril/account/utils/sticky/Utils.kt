package com.cyril.account.utils.sticky

import android.content.res.Resources

fun dpToPx(dp: Float): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}