package com.andef.facecontourdetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class MainActivity : AppCompatActivity() {
    private var isFront = false
    private var contourColor = Color.GREEN

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView

    private lateinit var floatingActionButtonUpheaval: FloatingActionButton
    private lateinit var imageButtonContourColor: ImageButton

    private lateinit var cameraController: LifecycleCameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        val permissionStatus = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            startAndInitCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSION_CAMERA
            )
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)

        floatingActionButtonUpheaval = findViewById(R.id.floatingActionButtonUpheaval)
        imageButtonContourColor = findViewById(R.id.imageButtonContourColor)

        floatingActionButtonUpheaval.setOnClickListener {
            isFront = !isFront
            startAndInitCamera()
        }
        imageButtonContourColor.setOnClickListener {
            it.background = getDrawable(getDrawableId())
            overlayView.contourColor = contourColor
        }
    }

    private fun getDrawableId(): Int {
        val drawableId: Int
        when (contourColor) {
            Color.GREEN -> {
                contourColor = Color.YELLOW
                drawableId = R.drawable.circle_orange
            }

            Color.YELLOW -> {
                contourColor = Color.RED
                drawableId = R.drawable.circle_red
            }

            else -> {
                contourColor = Color.GREEN
                drawableId = R.drawable.circle_green
            }
        }
        return drawableId
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAndInitCamera()
                } else {
                    showSettingsDialog()
                }
            }
        }
    }

    private fun startAndInitCamera() {
        ProcessCameraProvider.getInstance(this).get().unbindAll()
        cameraController = LifecycleCameraController(this)
        cameraController.cameraSelector = if (isFront) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController

        val options = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        val faceDetector = FaceDetection.getClient(options)

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(faceDetector),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result ->
                val faces = result?.getValue(faceDetector)
                if (faces == null || faces.size == 0 || faces.first() == null) {
                    overlayView.faces = listOf()
                    return@MlKitAnalyzer
                }
                if (faces.isNotEmpty()) {
                    overlayView.faces = faces
                }
            }
        )
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_is_required)
            .setMessage(R.string.you_can_grant_permissionuse_camera_app_settings)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancellation) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .create()
            .show()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION_CAMERA = 1
    }
}