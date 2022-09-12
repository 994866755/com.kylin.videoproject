package com.kylin.videoproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class FragmentA : Fragment() {

    companion object{

        fun newInstance(): FragmentA{
            val args = Bundle()
            val fragment = FragmentA()
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
        tv.text = "F111111111111111111111111111111"
        return tv
    }


}