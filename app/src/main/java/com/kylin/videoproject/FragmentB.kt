package com.kylin.videoproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class FragmentB : Fragment() {

    companion object{

        fun newInstance(): FragmentB{
            val args = Bundle()
            val fragment = FragmentB()
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val tv = TextView(activity)
        tv.text = "F2222222222222222222222222"
        return tv
    }


}