package com.devya.coincalculvo.activity

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.devya.coincalculvo.R
import com.devya.coincalculvo.calibration.CameraCalibrator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.*
import org.opencv.core.Mat


class CalibrationActivity : Activity(),CameraBridgeViewBase.CvCameraViewListener2 {


    /** OpenCV camera view. JavaCamera2View uses android.hardware.camera2 */
    private var mOpenCvCameraView: JavaCamera2View? = null

    /** Camera intrinsic parameter calibration. This is required to estimate the pose for ArUco detection. */
    var mCalibrator: CameraCalibrator? = null

    /** Camera intrinsic matrix. */
    private var mCameraMatrix : Mat? = null

    /** Camera distortion matrix. */
    private var mDistortionCoefficients : Mat? = null

    /** Flag to determine whether to do camera calibration. Set by the capture button. */
    private var doCalibration: Boolean = false


    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("coincalculvo")

                    mOpenCvCameraView!!.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(
            this@CalibrationActivity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_calibration)

        mOpenCvCameraView = findViewById<JavaCamera2View>(R.id.camera_view)
        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView!!.setCvCameraViewListener(this)

        // Initialize the camera calibration button.
        val calibrationButton: FloatingActionButton =findViewById<FloatingActionButton>(R.id.calibration_button)
        calibrationButton.setOnClickListener{
            doCalibration = true
        }

        val backButton: FloatingActionButton =findViewById<FloatingActionButton>(R.id.back_button)
        backButton.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch{
                saveData()
            }


          finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mOpenCvCameraView!!.setCameraPermissionGranted()
                } else {
                    val message = "Camera permission was not granted"
                    Log.e(TAG, message)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                Log.e(TAG, "Unexpected permission request")
            }
        }
    }


    override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

        mCalibrator = CameraCalibrator(width, height)
        
    }

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(frame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        // Get the current camera frame as OpenCV Mat object
        val mat = frame.rgba()


        if(doCalibration){
            // Perform the calibration.
            var patternFound : Boolean? = mCalibrator?.processFrame(frame.gray(), frame.rgba() )

            if (patternFound!!) {
                mCalibrator?.addCorners()
                mCalibrator?.calibrate()

                mCameraMatrix = mCalibrator?.cameraMatrix
                mDistortionCoefficients = mCalibrator?.distortionCoefficients

                Log.d("calibrelog", "cameramatrex  : ${mCameraMatrix}" + " distorsion :  ${mDistortionCoefficients}" )

                // Save the calibration data using SharedPreferences.
                //CalibrationResult.save(this,  mCalibrator!!.getCameraMatrix(), mCalibrator!!.getDistortionCoefficients())
            }else{
                Log.d("calibrelog", "cameramatrex  : ${mCameraMatrix}" + " distorsion :  ${mDistortionCoefficients}" )
                Toast.makeText(applicationContext,"The calibration pattern was not found.", Toast.LENGTH_SHORT).show()
            }
            // Do calibration only for the current frame.
            doCalibration = false

        }
        return mat
    }


    private fun saveData() {
        if(mCameraMatrix != null && mDistortionCoefficients != null){
            val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val jsonMatrix:String = gson.toJson(mCameraMatrix!!)
            val jsonDistors:String = gson.toJson(mDistortionCoefficients!!)
            editor.putString("matrix", jsonMatrix)
            editor.putString("distor", jsonDistors)
            editor.putString("test", "this is test value hh")
            editor.apply()
        }

    }



    companion object {
        private const val TAG = "CalibrationActivity"
        private const val CAMERA_PERMISSION_REQUEST = 1
    }
}