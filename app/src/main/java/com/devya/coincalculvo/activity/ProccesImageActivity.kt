package com.devya.coincalculvo.activity

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.devya.coincalculvo.R
import com.devya.coincalculvo.util.calculSum
import com.devya.coincalculvo.util.getCoinTypeListLength
import com.devya.coincalculvo.util.getIntTypefromstring
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type


class ProccesImageActivity : AppCompatActivity() {

    private val CAMERA_PHOTO = 111

    private var currontBitmapImage: Bitmap? = null

    //-------Views declarationv ----------//
    private lateinit var coin1: TextView
    private lateinit var coin2: TextView
    private lateinit var coin3: TextView
    private lateinit var coin4: TextView
    private lateinit var coin5: TextView
    private lateinit var result: TextView
    private lateinit var coinTypeText: TextView
    private lateinit var cameraBtn: Button
    private lateinit var readimage: Button
    private lateinit var processbtn: Button
    private lateinit var undistortbtn: Button
    private lateinit var myImage: ImageView

    //------- Calibration declation ------*/
    /** Camera intrinsic matrix. */
    private var mCameraMatrix : Mat? = null
    /** Camera distortion matrix. */
    private var mDistortionCoefficients : Mat? = null

    private var testt:String? = null

    //---------Utils values-------------//
    var coinType: String? = null
    private var takenPhoto: Mat? = null

    var images: Mat? = null
    //ddd
    private var mCameraMatri: Mat? = null
    private var mDistortionCoefficient: Mat? = null


    private val baseLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    run {
                        Log.d(TAG, "onManagerConnected: ")
                        //  cameraBridgeViewBase!!.enableView()
                    }
                    run { super.onManagerConnected(status) }
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_procces_image)

        supportActionBar?.title = "Coins Calcul"

        coinType =  intent.getStringExtra("coinType")

        coin1 = findViewById(R.id.coin1)
        coin2 = findViewById(R.id.coin2)
        coin3 = findViewById(R.id.coin3)
        coin4 = findViewById(R.id.coin4)
        coin5 = findViewById(R.id.coin5)
        result = findViewById(R.id.result)
        coinTypeText = findViewById(R.id.cointype)


        cameraBtn = findViewById(R.id.cameraBtn)
        readimage = findViewById(R.id.readimage)
        processbtn = findViewById(R.id.procesbtn)
        undistortbtn = findViewById(R.id.undistortbtn)
        myImage = findViewById(R.id.myImage)
        /**get Permission*/

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(
            this@ProccesImageActivity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )

        loadData()

        /**set camera Open*/
        cameraBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {

                val dest = File(getImageUri()!!.path)
                try {
                    val out = FileOutputStream(dest)
                    currontBitmapImage!!.compress(Bitmap.CompressFormat.PNG, 90, out)
                    out.flush()
                    out.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

        }

        readimage.setOnClickListener {
            pickImageFromGallery()
        }

        undistortbtn.setOnClickListener {
            if (currontBitmapImage != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    var map : Bitmap = currontBitmapImage !!
                    Utils.bitmapToMat(map, images)
                   // var matDistor = removeDistorsionFromImage(images!!)
                    var matix = mCameraMatrix
                    var mdist = mDistortionCoefficients
                   Log.d("mylog", "Matrix is " + matix!!.dump())
                    Log.d("mylog", "Matrix distor is " + mdist!!.dump())
                    Log.d("mylog", "Matrix test  is " + testt)
                   // var matDistor = testCalibration(images!!)
                    var matDistor = removeDistorsionFromImage(images!!)
                    Utils.matToBitmap(matDistor,map)
                    runOnUiThread {
                        myImage.setImageBitmap(currontBitmapImage)
                    }
                }

            }
        }

        processbtn.setOnClickListener {
            if (currontBitmapImage != null) {
                CoroutineScope(Dispatchers.IO).launch {
                /*        var matix = mCameraMatrix
                    var mdist = mDistortionCoefficients
                    Log.d("mylog", "Matrix is " + matix!!.dump())
                    Log.d("mylog", "Matrix distor is " + mdist!!.dump())
                    Log.d("mylog", "Matrix test  is " + testt)*/
                    Log.d("mylog", "currandtbitmap is not null from proceess button ")
                    Utils.bitmapToMat(currontBitmapImage, images)
                    
                    var matDistor = Mat(processMat(images!!.nativeObjAddr,getCoinTypeListLength(coinType),getIntTypefromstring(coinType)))
                    Utils.matToBitmap(matDistor, currontBitmapImage)
                    runOnUiThread {
                        myImage.setImageBitmap(currontBitmapImage)
                    }


                }

            } else {
                Log.d("mylog", "currant button is null from process btn")
            }

        }


    }



    private fun loadData() {
        try {
            val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
            val gson = Gson()
            val mCameraMatrixString= sharedPreferences.getString("matrix", null)
            val test= sharedPreferences.getString("test", null)
            val mDistortionCoefficientsString : String? = sharedPreferences.getString("distor", null)
            val type: Type? = object : TypeToken<Mat?>() {}.type
            mCameraMatrix = gson.fromJson<Any>(mCameraMatrixString, type) as Mat?
            mDistortionCoefficients = gson.fromJson<Any>(mDistortionCoefficientsString, type) as Mat?
            testt = test
        }catch (ex:Exception){
            Log.e("error", ex.message.toString())
        }
        
    }

    /**
     * Call of opencv function that remove distorsion from given Mat image
     * @param Mat image
     * @return Mat image without distortion
     */
   private fun removeDistorsionFromImage(inputFrame: Mat): Mat? {
        val renderedFrame = Mat(inputFrame.size(), inputFrame.type())
       if(mCameraMatrix != null && mDistortionCoefficients !=null){
           Calib3d.undistort(
               inputFrame, renderedFrame,
               mCameraMatrix, mDistortionCoefficients
           )
       }

        return renderedFrame
    }



    private fun testCalibration(mat: Mat):Mat{
        mCameraMatri = Mat()
        mDistortionCoefficient = Mat()
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatri)
        mCameraMatri!!.put(0, 0, 1.0)
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficient)

        val row = 0
        val col = 0
        val mCamData = floatArrayOf(
            1996.807433241645F, 0F, 479.5F,
            0F, 1996.807433241645F, 359.5F,
            0F, 0F, 1F
        )

        val mDisData = floatArrayOf(
                 -400.01682438.toFloat(), 41.368842493074.toFloat(), 0.0.toFloat(), 0.0.toFloat(), 10.096412142704.toFloat()
        )

        var mCamMat = Mat(3, 3, CvType.CV_32FC1)
        val mDisMat= Mat(5, 1, CvType.CV_32FC1)

        mCamMat.put(row, col, mCamData)
        mDisMat.put(row, col, mDisData)

        if ( mCameraMatrix!=null && mDistortionCoefficients!=null) {
            // Calibration data is available.
            return Mat(
                findArUCo(
                    mat.nativeObjAddr,
                    mCameraMatrix!!.nativeObjAddr, mDistortionCoefficients!!.nativeObjAddr, true
                )
            )
        }
        else{
          /*  Log.d("mylog", "Matrix is " + mCamMat!!.dump())
            Log.d("mylog", "Matrix distor is " + mDisMat!!.dump())
            Log.d("mylog", "Matrix test  is " + testt)*/
            return Mat(
                findArUCo(
                    mat.nativeObjAddr,
                    mCameraMatri!!.nativeObjAddr,  mDistortionCoefficient!!.nativeObjAddr, true
                )
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data
            useImage(uri)
        }
        if (requestCode == CAMERA_PHOTO && data != null) {
            Log.d("mylog", "data is not null")

        } else {
            Log.d("mylog", "datais nuull")
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  //  mOpenCvCameraView!!.setCameraPermissionGranted()
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

    //---------------- Take image from galery ----------------//
    fun useImage(uri: Uri?) {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        //use the bitmap as you like
        myImage.setImageBitmap(bitmap)
        currontBitmapImage = bitmap
    }


    /**
     * Createne a file where storing an image in Galery of device
     * @return Uri of created file
     */
    fun getImageUri(): Uri? {
        val filename = "${System.currentTimeMillis()}.jpg"
        Log.d("mylog", "imagename : $filename")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                return imageUri
            }
        } else {
            val imagesDir =
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            return  image.toUri()
        }
        return null
    }
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }


    /**
     * Set the result of c++ function in JNI in UI
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun getCalculResulFromJNI(array:IntArray){

       CoroutineScope(Dispatchers.IO).launch {
           val sum: Float = calculSum(coinType!!, array)
            Log.e(TAG,"result is " + result )
           runOnUiThread {
               result.text = sum.toString()
               coinTypeText.text = coinType
               setCalculatedValueToTextView(array)
                }
        }
    }

    fun setCalculatedValueToTextView(array:IntArray){
        when(coinType){
            "dirham" -> {

               coin1.text = "${array[0]} (0.5 dh)"
                coin2.text = "${array[1]} (1 dh)"
                coin3.text = "${array[2]} (5 dh)"
                coin4.text = "${array[3]} (10 dh)"
            }
            "dollar" -> {
                coin1.text = "${array[0]} dollar"
                coin2.text = "${array[1]} dollar"
                coin3.text = "${array[2]} dollar"
                coin4.text = "${array[3]} dollar"

            }
            "euro" -> {
                coin1.text = "${array[0]} 0.05euro"
                coin2.text = "${array[1]} 0.1euro"
                coin3.text = "${array[2]} 0.2euro"
                coin4.text = "${array[3]} 0.5euro"

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "opencv initiaalistation is done")
            Toast.makeText(this, "open cv is loaded", Toast.LENGTH_SHORT).show()
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        } else {
            Log.d(TAG, "opencv is not loaded")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback)
        }
        images = Mat()


    }


    /**
     * reference for c++ JNI function tha process image.
     * @return a Long value of image that must be converted to Mat or Bitmap
     */
    private external fun processMat(matAddr: Long, length: Int, coinType: Int): Long
    private external fun findArUCo(matAddr: Long, cameraMatrix: Long, distortionCoefficients: Long, isCalibrationAvailable: Boolean): Long

    companion object {

        private const val IMAGE_REQUEST_CODE = 1_000
        private  val TAG = ProccesImageActivity::class.java.simpleName
        private const val CAMERA_PERMISSION_REQUEST = 1

        init {
            System.loadLibrary("coincalculvo")
        }
    }
}