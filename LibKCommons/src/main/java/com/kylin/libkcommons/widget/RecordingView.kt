package com.kylin.libkcommons.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.kylin.libkcommons.R
import java.lang.Exception

class RecordingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paint : Paint?= null

    private var paint2 : Paint?= null
    private var paint3 : Paint?= null

    private var baseDim = 0f

    private var mValueAnimator1 : ValueAnimator?= null
    private var mValueAnimator2 : ValueAnimator?= null
    private var path1 :Path ?= null
    private var path2 :Path ?= null
    var isRecordingStart = false

    init {
        paint = Paint()
        paint2 = Paint()
        paint3 = Paint()

        paint?.color = context.resources.getColor(R.color.kylin_main_color)
        paint?.isAntiAlias = true
        paint2?.color = context.resources.getColor(R.color.kylin_white)
        paint2?.style = Paint.Style.FILL
        paint3?.color = context.resources.getColor(R.color.kylin_white)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (baseDim == 0f){
            baseDim = (0.25 * width).toFloat()
            Log.v("mmp", "lll "+width+"  "+baseDim)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint?.let {
            canvas?.drawCircle((width/2).toFloat(), (height/2).toFloat(), (width/2).toFloat(),
                it
            )
        }

        if (path1 == null || path2 == null){
            path1 = Path()
            path2 = Path()
            // 设置初始状态
            startToStopAnim(0f)
        }
        paint2?.let { canvas?.drawPath(path1!!, it) }
        paint3?.let { canvas?.drawPath(path2!!, it) }
    }

    fun startToStopChange(){
        isRecordingStart = true
        if (mValueAnimator1 == null) {
            mValueAnimator1 = ValueAnimator.ofFloat(0f, 1f)
            mValueAnimator1?.addUpdateListener {
                val currentValue: Float = it.animatedValue as Float
                startToStopAnim(currentValue)
                postInvalidate()
            }
            mValueAnimator1?.interpolator = AccelerateInterpolator()
        }
        mValueAnimator1?.setDuration(500)?.start();
    }

    fun stopToStartChange(){
        isRecordingStart = false
        if (mValueAnimator2 == null) {
            mValueAnimator2 = ValueAnimator.ofFloat(1f, 0f)
            mValueAnimator2?.addUpdateListener {
                val currentValue: Float = it.animatedValue as Float
                startToStopAnim(currentValue)
                postInvalidate()
            }
            mValueAnimator2?.interpolator = AccelerateInterpolator()
        }
        mValueAnimator2?.setDuration(500)?.start();
    }

    // 做动画，那目标状态的x,y和原状态的x,y的差值做百分比的计算
    // 如果你觉得直接从三角形变成2个矩形不好看，可以先变成正方形再变成2个矩形
    private fun startToStopAnim(currentValue : Float){
        val offset : Int = (baseDim * 0.25 * (1-currentValue)).toInt()

        path1?.reset()
        path1?.fillType = Path.FillType.WINDING
        path1?.moveTo(baseDim + offset, baseDim) // 点1不变
        path1?.lineTo(2 * baseDim+ offset - baseDim/3*currentValue,
            baseDim + (0.5 * baseDim).toInt() * (1-currentValue))
        path1?.lineTo(2 * baseDim+ offset - baseDim/3*currentValue,
            2 * baseDim +(0.5 * baseDim).toInt() + (0.5 * baseDim).toInt() * currentValue)
        path1?.lineTo(baseDim+ offset, 3 * baseDim)  // 点4不变
        path1?.close()


        path2?.reset()
        path2?.fillType = Path.FillType.WINDING
        if (currentValue <= 0f) {
            path2?.moveTo(2 * baseDim + offset, baseDim + (0.5 * baseDim).toInt())
            path2?.lineTo(3 * baseDim + offset, 2 * baseDim)
            path2?.lineTo(2 * baseDim + offset, 2 * baseDim + (0.5 * baseDim).toInt())
        }else {
            path2?.moveTo(2 * baseDim+ offset + baseDim/3*currentValue,
                baseDim + (0.5 * baseDim).toInt() * (1-currentValue))
            path2?.lineTo(3 * baseDim + offset, baseDim + baseDim * (1-currentValue))
            path2?.lineTo(3 * baseDim + offset, 2 * baseDim + baseDim * currentValue)
            path2?.lineTo(2 * baseDim+ offset + baseDim/3*currentValue,
            2 * baseDim +(0.5 * baseDim).toInt() + (0.5 * baseDim).toInt() * currentValue)
        }
        path2?.close()
    }

    fun close(){
        try {
            if (mValueAnimator1?.isStarted == true){
                mValueAnimator1?.cancel()
            }
            if (mValueAnimator2?.isStarted == true){
                mValueAnimator2?.cancel()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }finally {
            mValueAnimator1 = null
            mValueAnimator2 = null
        }
    }

}