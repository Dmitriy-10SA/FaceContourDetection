package com.andef.facecontourdetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView

    private lateinit var floatingActionButtonUpheaval: FloatingActionButton

    private lateinit var cameraController: LifecycleCameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)

        floatingActionButtonUpheaval = findViewById(R.id.floatingActionButtonUpheaval)

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

        floatingActionButtonUpheaval.setOnClickListener {
            isFront = !isFront
            startAndInitCamera()
        }
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

        val options = FaceDetectorOptions.Builder().build()
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
                    return@MlKitAnalyzer
                }
                if (faces.isNotEmpty()) {
                    //
                }
            }
        )
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_is_required)
            .setMessage(R.string.you_can_grant_permissionuse_camera_app_settings)
            .setPositiveButton(R.string.open_settings) {_, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancellation) {dialog, _ ->
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