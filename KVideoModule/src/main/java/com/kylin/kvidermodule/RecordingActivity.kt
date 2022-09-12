package com.kylin.kvidermodule

import android.annotation.SuppressLint
import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.ImageView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.common.util.concurrent.ListenableFuture
import com.kylin.kvidermodule.viewmodel.RecordingActivityViewModel
import com.kylin.libkbase.base.KBaseViewModel
import com.kylin.libkbase.base.KVmActivity
import com.kylin.libkcommons.widget.RecordingView
import java.lang.Exception
import androidx.camera.core.*
import androidx.camera.video.*


@Route(path = "/video/activity/recording")
class RecordingActivity : KVmActivity<RecordingActivityViewModel>() {

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    private var previewView : PreviewView?= null
    private var camera : Camera?= null
    private var suview : Preview.SurfaceProvider ?= null
    private var cameraSelector : CameraSelector ?= null
    private var cameraProvider : ProcessCameraProvider ?= null
    private var preview : Preview ?= null
    private var videoCapture : androidx.camera.video.VideoCapture<Recorder> ?= null
    private var cameraOrientation = CameraSelector.LENS_FACING_BACK

    // todo 图像分析，会一直回调，可以在用它来配合编码器
    private var imageAnalysis : ImageAnalysis ?= null

    private var recording : Recording ?= null

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
//        val mList : MutableList<Quality> = mutableListOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
//        val qualitySelector = QualitySelector.fromOrderedList(
//            mList,
//            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))
        val recorder = Recorder.Builder()
//            .setQualitySelector(qualitySelector)
//            .setQualitySelector(Recorder.DEFAULT_QUALITY_SELECTOR)  // 默认就是这个吧
            .build()
        videoCapture =  androidx.camera.video.VideoCapture.withOutput(recorder)
    }

    private fun initImageAnalysis(){
        imageAnalysis = ImageAnalysis.Builder()
            // enable the following line if RGBA output is needed.
            // .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis?.setAnalyzer(ContextCompat.getMainExecutor(this), ImageAnalysis.Analyzer { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            // insert your code here.
            Log.v("mmp" , "图像分析回调")
            // after done, release the ImageProxy object
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
            camera = cameraProvider?.bindToLifecycle(this as LifecycleOwner, cameraSelector!!,
                videoCapture, imageAnalysis, preview)
            preview?.setSurfaceProvider(suview)
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