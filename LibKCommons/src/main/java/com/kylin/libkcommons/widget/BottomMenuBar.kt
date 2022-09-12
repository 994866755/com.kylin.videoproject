package com.kylin.libkcommons.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.kylin.libkcommons.DimensionUtils
import com.kylin.libkcommons.R
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *  todo
 *  1. 先绘制出效果，把点击事件处理好
 *  2. 点击的动画
 *  3. 绑定一个viewmodel来处理页面逻辑
 */
class BottomMenuBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    companion object{

        const val DIMENS_64 = 64.0
        const val DIMENS_96 = 96.0
        const val DIMENS_50 = 50.0
        const val DIMENS_48 = 48.0

        interface OnChildClickListener{
            fun onClick(index : Int)
        }

    }

    private var paint : Paint ?= null
    private var paint2 : Paint ?= null
    private var allHeight : Int = 0
    private var bgHeight : Int = 0
    private var mRadius : Int = 0
    private var mChildSize : Int = 0
    private var mChildCenterSize : Int = 0

    private var mWidthZone1 : Int = 0
    private var mWidthZone2 : Int = 0
    private var mChildCentre : Int = 0

    private var childViews : MutableList<View> = mutableListOf()
    private var objectAnimation : ObjectAnimator ?= null
    var onChildClickListener : OnChildClickListener ?= null

    init {
        initView()
    }

    private fun initView(){
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            DimensionUtils.dp2px(context, DIMENS_64).toInt())
        layoutParams = lp

        allHeight = DimensionUtils.dp2px(context, DIMENS_96).toInt()
        bgHeight = DimensionUtils.dp2px(context, DIMENS_64).toInt()
        mRadius = DimensionUtils.dp2px(context, DIMENS_50).toInt()
        mChildSize = DimensionUtils.dp2px(context, DIMENS_48).toInt()
        mChildCenterSize = DimensionUtils.dp2px(context, DIMENS_64).toInt()
        setWillNotDraw(false)

        initPaint()
    }

    private fun initPaint(){
        paint = Paint()
        paint?.isAntiAlias = true
        paint?.color = context.resources.getColor(R.color.kylin_main_color)

        paint2 = Paint()
        paint2?.isAntiAlias = true
        paint2?.color = context.resources.getColor(R.color.kylin_third_color)
    }

    private fun initChildView(cView : View?, index : Int) {
        cView?.setOnClickListener {
            if (index == childViews.size/2) {
                startAnim(cView)
            }else {
                onChildClickListener?.onClick(index)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)

        if (childViews.size <= 0) {
            for (i in 0 until childCount) {
                val cView = getChildAt(i)
                initChildView(cView, i)
                childViews.add(cView)
                if (i == childCount/2){
                    val ms: Int = MeasureSpec.makeMeasureSpec(mRadius, MeasureSpec.AT_MOST)
                    measureChild(cView, ms, ms)
                }else {
                    val ms: Int = MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.AT_MOST)
                    measureChild(cView, ms, ms)
                }
            }
        }

        setMeasuredDimension(wSize, allHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mChildCentre == 0){
            mChildCentre = width / 6
        }

        // 辅助事件分发区域
        if (mWidthZone1 == 0 || mWidthZone2 == 0) {
            mWidthZone1 = width / 2 - mRadius / 2
            mWidthZone2 = width / 2 + mRadius / 2
        }

        // 设置每个子view的显示区域
        for (i in 0 until childViews.size) {
            if (i == childCount/2){
                childViews[i].layout(mChildCentre*(2*i+1) - mChildCenterSize/2 ,
                    allHeight/2 - mChildCenterSize/2,
                    mChildCentre*(2*i+1) + mChildCenterSize/2 ,
                    allHeight/2 + mChildCenterSize/2)
            }else {
                childViews[i].layout(mChildCentre*(2*i+1) - mChildSize/2 ,
                    allHeight - bgHeight/2 - mChildSize/2,
                    mChildCentre*(2*i+1) + mChildSize/2 ,
                    allHeight - bgHeight/2 + mChildSize/2)
            }
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 绘制长方形区域
        canvas?.drawRect(left.toFloat(), ((allHeight - bgHeight).toFloat()),
            right.toFloat(), bottom.toFloat(), paint!!)

        // 绘制圆形区域
        paint?.let {
            canvas?.drawCircle(
                (width/2).toFloat(), mRadius.toFloat(),
                mRadius.toFloat(),
                it
            )
        }

        // 绘制内圆区域
        paint2?.let {
            canvas?.drawCircle(
                (width/2).toFloat(), mRadius.toFloat(),
                (mRadius - 28).toFloat(),
                it
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 点击区域进行拦截
        if (event?.action == MotionEvent.ACTION_DOWN && isShowZone(event.x, event.y)){
            return true
        }
        return super.onTouchEvent(event)
    }

    /**
     *  判断点击事件是否在点击区域中
     */
    private fun isShowZone(x : Float, y : Float) : Boolean{
        if (y >= allHeight - bgHeight){
            return true
        }
        if (x >= mWidthZone1 && x <= mWidthZone2){
            // 在圆内
            val relativeX = abs(x - width/2)
            val squareYZone = mRadius.toDouble().pow(2.0) - relativeX.toDouble().pow(2.0)
            return y >= mRadius - sqrt(squareYZone)
        }
        return false
    }

    /**
     *  中间按钮的动画
     */
    private fun startAnim(view : View){
        if (objectAnimation == null) {
            objectAnimation = ObjectAnimator.ofFloat(view,
                "rotation", 0f, -15f, 180f, 0f)
            objectAnimation?.addListener(object : Animator.AnimatorListener {

                override fun onAnimationStart(p0: Animator) {
                }

                override fun onAnimationEnd(p0: Animator) {
                    onChildClickListener?.onClick(childViews.size / 2)
                }

                override fun onAnimationCancel(p0: Animator) {
                    onChildClickListener?.onClick(childViews.size / 2)
                }

                override fun onAnimationRepeat(p0: Animator) {
                }

            })
            objectAnimation?.duration = 1000
            objectAnimation?.interpolator = AccelerateDecelerateInterpolator()
        }
        objectAnimation?.start()
    }

    fun onDestroy(){
        try {
            objectAnimation?.cancel()
            objectAnimation?.removeAllListeners()
        }catch (e : Exception){
            e.printStackTrace()
        }finally {
            objectAnimation = null
        }
    }

}