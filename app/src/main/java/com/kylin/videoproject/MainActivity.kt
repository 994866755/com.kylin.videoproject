package com.kylin.videoproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.launcher.ARouter
import com.kylin.libkcommons.widget.BottomMenuBar

class MainActivity : AppCompatActivity() {

    var bv : BottomMenuBar?= null
    var vp : ViewPager2?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bv = findViewById(R.id.bv_content)
        vp = findViewById(R.id.vp_content)
    }

    override fun onResume() {
        super.onResume()
        bv?.post {
            Log.v("mmp", "测试宽高  "+bv?.width + "    "+bv?.height)
        }

        bv?.onChildClickListener = object : BottomMenuBar.Companion.OnChildClickListener{
            override fun onClick(index: Int) {
                if (index == 1){
                    ARouter.getInstance().build("/video/activity/recording").navigation()
                }
            }
        }

        val data: MutableList<Fragment> = mutableListOf()
        data.add(FragmentA.newInstance())
        data.add(FragmentB.newInstance())
        val adapter = TestAdapter(supportFragmentManager, lifecycle, data)
        adapter?.let {
            vp?.adapter = it
        }
    }

}