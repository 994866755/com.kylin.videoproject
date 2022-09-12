package com.kylin.libkbase.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter

abstract class KBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        setContentView(getLayoutId())
        initView()
    }

    abstract fun initView()
    abstract fun getLayoutId() : Int

}