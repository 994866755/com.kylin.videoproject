package com.kylin.videoproject

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter

class KylinApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ARouter.openLog()
        ARouter.openDebug()

        ARouter.init(this@KylinApplication)

    }

}