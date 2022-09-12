package com.kylin.libkcommons

import android.content.Context

class DimensionUtils {

    companion object{

        fun dp2px(context: Context?, value : Double) : Double{
            val fontScale: Float? = context?.resources?.displayMetrics?.density
            fontScale?.let {
                return (value * fontScale + 0.5f)
            }
            return 0.0
        }

    }

}