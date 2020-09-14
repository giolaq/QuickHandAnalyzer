package com.huawei.quickhandanalyzer


import com.huawei.quickhandanalyzer.utils.OnSwipeTouchListener
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzerSetting
import com.huawei.quickhandanalyzer.databinding.ActivityMainBinding
import com.huawei.quickhandanalyzer.processor.hand.HandDetectorProcessor
import com.huawei.quickhandanalyzer.processor.VisionImageProcessor
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var previewUseCase: Preview
    private lateinit var analysisUseCase: ImageAnalysis
    private var imageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraSelector: CameraSelector

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTouchListener()

        if (savedInstanceState != null) {
            lensFacing =
                savedInstanceState.getInt(
                    STATE_LENS_FACING,
                    CameraSelector.LENS_FACING_BACK
                )
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )
            .get(CameraXViewModel::class.java)
            .processCameraProvider
            .observe(
                this,
                { provider: ProcessCameraProvider ->
                    cameraProvider = provider
                    if (allPermissionsGranted()) {
                        bindAllCameraUseCases()
                    }
                }
            )

        if (!allPermissionsGranted()) {
            runtimePermissions
        }
    }

    private fun setTouchListener() {
        preview_view.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {
            override fun onSwipeDown() {
                super.onSwipeDown()

                if (cameraProvider == null) {
                    return
                }

                val newLensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                val newCameraSelector =
                    CameraSelector.Builder().requireLensFacing(newLensFacing).build()
                try {
                    if (cameraProvider!!.hasCamera(newCameraSelector)) {
                        lensFacing = newLensFacing
                        cameraSelector = newCameraSelector
                        bindAllCameraUseCases()
                        return
                    }
                } catch (e: CameraInfoUnavailableException) {
                    // Falls through
                }

                Toast.makeText(
                    applicationContext,
                    "This device does not have lens with facing: $newLensFacing",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt(STATE_LENS_FACING, lensFacing)
    }

    public override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()
        imageProcessor?.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.stop()
    }

    private fun bindAllCameraUseCases() {
        cameraProvider?.unbindAll()
        bindPreviewUseCase()
        bindAnalysisUseCase()
    }

    private fun bindPreviewUseCase() {
        previewUseCase = Preview.Builder().build()
        previewUseCase.setSurfaceProvider(binding.previewView.createSurfaceProvider())
        cameraProvider?.bindToLifecycle(this, cameraSelector, previewUseCase)
    }

    @SuppressLint("NewApi")
    private fun bindAnalysisUseCase() {
        imageProcessor?.stop()

        imageProcessor = try {

            val handDetectorOptions: MLHandKeypointAnalyzerSetting =
                MLHandKeypointAnalyzerSetting.Factory() // MLHandKeypointAnalyzerSetting.TYPE_ALL indicates that all results are returned.
                    // MLHandKeypointAnalyzerSetting.TYPE_KEYPOINT_ONLY indicates that only hand keypoint information is returned.
                    // MLHandKeypointAnalyzerSetting.TYPE_RECT_ONLY indicates that only palm information is returned.
                    .setSceneType(MLHandKeypointAnalyzerSetting.TYPE_ALL) // Set the maximum number of hand regions that can be detected in an image. By default, a maximum of 10 hand regions can be detected.
                    .setMaxHandResults(1)
                    .create()
            HandDetectorProcessor(this, handDetectorOptions)

        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: ${e.message}", e)
            Toast.makeText(
                applicationContext, "Can not create image processor: " + e.localizedMessage,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        analysisUseCase = ImageAnalysis.Builder().build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        binding.graphicOverlay.setImageSourceInfo(
                            imageProxy.width, imageProxy.height, isImageFlipped
                        )
                    } else {
                        binding.graphicOverlay.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor?.processImageProxy(imageProxy, binding.graphicOverlay)
                } catch (e: MLException) {
                    Log.e(
                        TAG,
                        "Failed to process image. Error: " + e.localizedMessage
                    )
                    Toast.makeText(
                        applicationContext,
                        e.localizedMessage,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        )
        cameraProvider?.bindToLifecycle(this, cameraSelector, analysisUseCase)
    }

    private val requiredPermissions: Array<String?>
        get() = try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }

    private fun allPermissionsGranted(): Boolean {
        requiredPermissions.forEach { permission ->
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    allNeededPermissions.toTypedArray(),
                    PERMISSION_REQUESTS
                )
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!allPermissionsGranted()) {
            return
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUESTS = 1
        private const val STATE_LENS_FACING = "lens_facing"

        private fun isPermissionGranted(
            context: Context,
            permission: String?
        ): Boolean {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission!!
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}
