#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <android/log.h>
#include <opencv2/aruco.hpp>
#include "CoinCalcul.cpp"

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT jlong JNICALL
Java_com_devya_coincalculvo_activity_ProccesImageActivity_processMat(JNIEnv *env, jobject thiz,
                                                                     jlong mat_addr,jint length,jint coin_type) {
    // Get Mat data for image input and camera calibration matrices.
    Mat &input_mat = *(Mat *) mat_addr;


    // ArUco library requires CV_8UC3 (without alpha channel) input.
    cv::Size input_size = input_mat.size();
    cv::Mat *mat_dst = new cv::Mat(input_size.height, input_size.width, CV_8UC3);
    cv::cvtColor(input_mat, *mat_dst, cv::COLOR_RGBA2RGB);

    // initialise array of coins calculated
    jclass thisClass = env->GetObjectClass(thiz);
    jint *intCArray =  new jint[length];
    for(int i=0; i<length ; i++){
        intCArray[i]=0;
    }

    // Initialization for ArUco library functions.
    std::vector<int> ids;
    std::vector<std::vector<cv::Point2f>> corners, rejectedCandidates;
    cv::Ptr<cv::aruco::DetectorParameters> parameters = cv::aruco::DetectorParameters::create();
    cv::Ptr<cv::aruco::Dictionary> dictionary = aruco::Dictionary::get(aruco::DICT_5X5_50);
    cv::aruco::detectMarkers(*mat_dst, dictionary, corners, ids);

    ///logiing
    std::string ss = "size od id detected  " +  std::to_string(ids.size());
    char* char_arrayy = stringToCharArray(ss);
    __android_log_write(ANDROID_LOG_ERROR, "arucolog", char_arrayy);
    ///logiing

    // If at least one marker detected.
    if (ids.size() > 0) {
        // Draw the indicators around the detected markers.
       //cv::aruco::drawDetectedMarkers(*mat_dst, corners, ids);

        // loop over the detected ArUCo corners
       for (int i = 0; i < ids.size(); i++) {
            int markerID = ids[i];
            vector<Point2f> markerCorner = corners[i];

            // convert each of the (x, y)-coordinate pairs to integers
            cv::Point2f topRight = cv::Point2i(static_cast<int>(markerCorner[1].x), static_cast<int>(markerCorner[1].y));
            cv::Point2f bottomRight = cv::Point2i(static_cast<int>(markerCorner[2].x), static_cast<int>(markerCorner[2].y));
            cv::Point2f bottomLeft = cv::Point2i(static_cast<int>(markerCorner[3].x), static_cast<int>(markerCorner[3].y));
            cv::Point2f topLeft = cv::Point2i(static_cast<int>(markerCorner[0].x), static_cast<int>(markerCorner[0].y));

            // draw the bounding box of the ArUCo detection
            cv::line(*mat_dst, topLeft, topRight, cv::Scalar(0, 0, 255), 2);
            cv::line(*mat_dst, topRight, bottomRight, cv::Scalar(0, 0, 255), 2);
            cv::line(*mat_dst, bottomRight, bottomLeft, cv::Scalar(0, 0, 255), 2);
            cv::line(*mat_dst, bottomLeft, topLeft, cv::Scalar(0, 0, 255), 2);
            // compute and draw the center (x, y)-coordinates of the ArUco
            // marker
            int cX = (topLeft.x + bottomRight.x) / 2.0;
            int cY = (topLeft.y + bottomRight.y) / 2.0;
            cv::circle(*mat_dst, cv::Point(cX, cY), 4, cv::Scalar(0, 0, 255), -1);

            // Aruco Perimeter
           //double aruco_perimeter = cv::arcLength(cv::InputArray(markerCorner[0]), true);


          double aruco_perimeter = cv::arcLength(cv::InputArray(corners[0]), true);

            // Pixel to cm ratio
            double pixel_cm_ratio = aruco_perimeter / 20;
            std::vector<std::vector<cv::Point>> contours = detect_objects(*mat_dst);

           ///logiing
           std::string s = "radiopxcm is " +  std::to_string(pixel_cm_ratio);
           char* char_array = stringToCharArray(s);
           __android_log_write(ANDROID_LOG_ERROR, "arucolog", char_array);
           ///logiing

            double object_radius;
            for (auto cnt : contours) {
                cv::Point2f center;
                float radius;
                cv::minEnclosingCircle(cnt, center, radius);
                cv::Point center_int = cv::Point(static_cast<int>(center.x),
                                                 static_cast<int>(center.y));
                int radius_int = static_cast<int>(radius);
                 object_radius = radius / pixel_cm_ratio;

                ///logiing
                std::string s = "radios is " +  std::to_string(object_radius);
                char* char_array = stringToCharArray(s);
                __android_log_write(ANDROID_LOG_ERROR, "arucolog", char_array);
                ///logiing

               if (object_radius < 1.6 && object_radius > 0.9 ) {
                    cv::circle(*mat_dst, center_int, radius_int, cv::Scalar(0, 255, 0), 4);
                }
               // get index of array of detected coin and increment his number
                int res = assignvaluetocoin(object_radius,coin_type);
               if(res != -1)
                intCArray[res] += 1;

            }
        }
    }

    //
    // Step 3: Convert the C's Native jdouble[] to JNI jdoublearray,  and call java function to update result
    jintArray outJNIArray = (*env).NewIntArray(length);  // allocate
  //  if (NULL == outJNIArray) return;
    (*env).SetIntArrayRegion(outJNIArray, 0 , length,intCArray );  //

    jmethodID printInt = env->GetMethodID(thisClass, "getCalculResulFromJNI", "([I)V");
   /* if (NULL == printInt)
        return null;*/
    env->CallVoidMethod(thiz, printInt, outJNIArray);



    return (jlong)mat_dst;

}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_devya_coincalculvo_activity_ProccesImageActivity_findArUCo(JNIEnv *env, jobject thiz,
                                                                    jlong mat_addr,
                                                                    jlong camera_matrix,
                                                                    jlong distortion_coefficients,
                                                                    jboolean is_calibration_available) {
    // Get Mat data for image input and camera calibration matrices.
    Mat &input_mat = *(Mat *) mat_addr;
    Mat &cameraMatrix = *(Mat *) camera_matrix;
    Mat &distCoeffs = *(Mat *) distortion_coefficients;

    // ArUco library requires CV_8UC3 (without alpha channel) input.
    cv::Size input_size = input_mat.size();
    cv::Mat *mat_dst = new cv::Mat(input_size.height, input_size.width, CV_8UC3);
    cv::cvtColor(input_mat, *mat_dst, cv::COLOR_RGBA2GRAY);

    cv::undistort(input_mat,*mat_dst,cameraMatrix,distCoeffs);

    // Initialization for ArUco library functions.
    std::vector<int> ids;
    std::vector<std::vector<cv::Point2f>> corners;
    cv::Ptr<cv::aruco::Dictionary> dictionary = aruco::Dictionary::get(aruco::DICT_5X5_50);
    cv::aruco::detectMarkers(*mat_dst, dictionary, corners, ids);

    // If at least one marker detected.
    if (ids.size() > 0) {
        // Draw the indicators around the detected markers.
        cv::aruco::drawDetectedMarkers(*mat_dst, corners, ids);

        // Pose estimation can be performed only when the calibration is available.
        if(is_calibration_available) {
            // Initialize the pose estimation vectors.
            std::vector<cv::Vec3d> rvecs, tvecs;
            // Estimate the pose.
            cv::aruco::estimatePoseSingleMarkers(corners, 0.05, cameraMatrix, distCoeffs, rvecs,
                                                 tvecs);
            // Draw axis for each marker.
            for (int i = 0; i < ids.size(); i++)
                cv::drawFrameAxes(*mat_dst, cameraMatrix, distCoeffs, rvecs[i], tvecs[i], 0.1);
        }
    }

    return (jlong)mat_dst;
}