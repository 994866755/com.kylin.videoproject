package com.kylin.libkbase.base

import androidx.lifecycle.ViewModelProvider


abstract class KVmActivity< VM : KBaseViewModel?> : KBaseActivity() {

    protected var mViewModel : VM ?= null

    override fun initView() {
         mViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(getViewModel()) as VM
        initViewModel()
    }

    abstract fun initViewModel()

    abstract fun getViewModel() : Class<out KBaseViewModel>

}