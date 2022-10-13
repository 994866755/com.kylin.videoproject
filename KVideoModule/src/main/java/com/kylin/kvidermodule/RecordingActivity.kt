package com.kylin.kvidermodule

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.ImageFormat
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.ImageView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.common.util.concurrent.ListenableFuture
import com.kylin.kvidermodule.viewmodel.RecordingActivityViewModel
import com.kylin.libkbase.base.KBaseViewModel
import com.kylin.libkbase.base.KVmActivity
import com.kylin.libkcommons.widget.RecordingView
import java.nio.ByteBuffer
import java.util.concurrent.locks.ReentrantLock


@Route(path = "/video/activity/recording")
class RecordingActivity : KVmActivity<RecordingActivityViewModel>() {

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    private var previewView : PreviewView?= null
    private var camera : Camera?= null
    private var suview : Preview.SurfaceProvider ?= null
    private var cameraSelector : CameraSelector ?= null
    private var cameraProvider : ProcessCameraProvider ?= null
    private var preview : Preview ?= null
    private var videoCapture : VideoCapture<Recorder>?= null
    private var cameraOrientation = CameraSelector.LENS_FACING_BACK

    // todo 图像分析，会一直回调，可以在用它来配合编码器
    private var imageAnalysis : ImageAnalysis ?= null

    private var recording : Recording ?= null
    private val lock = ReentrantLock()

    override fun initView() {
        previewView = findViewById(R.id.pv_content)
        val rv : RecordingView = findViewById(R.id.rv_content)
        val ivRotate : ImageView = findViewById(R.id.iv_rotate)
        rv.setOnClickListener {
            if (!rv.isRecordingStart){
                rv.startToStopChange()
                startRecording()
            }else {
                rv.stopToStartChange()
                recording?.stop()
            }
        }
        ivRotate.setOnClickListener {
            selectCameraOrientation()
        }

        initCamera()
    }

    private fun initCamera(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            bindPreview()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun initRecording(){
        val recorder = Recorder.Builder()
//            .setQualitySelector(qualitySelector)
//            .setQualitySelector(Recorder.DEFAULT_QUALITY_SELECTOR)  // 默认就是这个吧
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
    }

    private fun initImageAnalysis(){
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis?.setAnalyzer(ContextCompat.getMainExecutor(this), ImageAnalysis.Analyzer { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            // insert your code here.
            // 查看日志打印，发现一秒回调30次，那就是30帧
            val format = imageProxy.format
            ImageFormat.YUV_420_888
            Log.v("mmp" , "图像分析格式  "+format)
            Log.v("mmp" , "线程ID  "+Thread.currentThread().getId())
            // after done, release the ImageProxy object
            // todo 是同个线程，但好像ByteBuffer是NIO，导致单个线程也会出现多线程混乱的情况，
            // todo 会重复打印“图像分析格式”和"${row.toString()}"
            lock.lock() // 这里如果用不可重入锁是不是屌炸了
                val planes: Array<ImageProxy.PlaneProxy> = imageProxy.planes
                val pixelStride = planes[0].pixelStride
                val pixelStride2 = planes[1].pixelStride
                Log.v("mmp", "采样模式 $pixelStride  $pixelStride2")

                val y : ByteBuffer = planes[0].buffer
                val row = ByteArray(1280)
                for (i in 0 .. 720) {
                    y.get(row)
                    Log.v("mmp", "${row.toString()}")
                }
                Log.v("mmp","================")
                var u : ByteBuffer? = null
                if (pixelStride2 == 1) {
                    u = planes[1].buffer // U数据
                } else if (pixelStride2 == 2) {
                    val uBuffer = planes[1].buffer
                }
            lock.unlock()
            imageProxy.close()
        })
    }

    private fun bindPreview() {
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraOrientation)
            .build()
        startCamera()
    }

    private fun selectCameraOrientation(){
        if (cameraOrientation == CameraSelector.LENS_FACING_BACK){
            cameraOrientation = CameraSelector.LENS_FACING_FRONT
        }else {
            cameraOrientation = CameraSelector.LENS_FACING_BACK
        }
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraOrientation)
            .build()

        startCamera()
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera(){
        try {
            initRecording()
            initImageAnalysis()

            cameraProvider?.unbindAll()
            suview = previewView?.surfaceProvider
            preview = Preview.Builder().build()
//            camera = cameraProvider?.bindToLifecycle(this as LifecycleOwner, cameraSelector!!,
//                videoCapture, imageAnalysis, preview)
            camera = cameraProvider?.bindToLifecycle(this as LifecycleOwner, cameraSelector!!,
                imageAnalysis, preview)
//            camera = cameraProvider?.bindToLifecycle(this as LifecycleOwner, cameraSelector!!,
//                preview)
            preview!!.setSurfaceProvider(suview)
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    private fun startRecording() {
        val name = "yeshuai.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(this.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture?.output
            ?.prepareRecording(this, mediaStoreOutput)
            ?.start(ContextCompat.getMainExecutor(this)) {

            }
    }

    override fun getLayoutId(): Int {
        return R.layout.k_video_activity_recording
    }

    override fun getViewModel(): Class<out KBaseViewModel> {
        return RecordingActivityViewModel::class.java
    }

    override fun initViewModel() {

    }

}