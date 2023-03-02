//
// Created by hp on 25/02/2023.
//

#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <android/log.h>
#include <opencv2/aruco.hpp>

using namespace std;
using namespace cv;

std::vector<std::vector<cv::Point>> detect_objects( cv::Mat& frame) {
    // Convert Image to grayscale
    cv::Mat gray;
    cv::cvtColor(frame, gray, cv::COLOR_BGR2GRAY);

    // Create a Mask with adaptive threshold
    cv::Mat mask;
    cv::adaptiveThreshold(gray, mask, 255, cv::ADAPTIVE_THRESH_MEAN_C, cv::THRESH_BINARY_INV, 19, 5);

    // Find contours
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(mask, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

    //cv::imshow("mask", mask);
    std::vector<std::vector<cv::Point>> objects_contours;

    for (auto cnt : contours) {
        double area = cv::contourArea(cnt);
        if (area > 2000) {
            //cnt = cv::approxPolyDP(cnt, 0.03*cv::arcLength(cnt, true), true);
            objects_contours.push_back(cnt);
        }
    }

    return objects_contours;
}

double getPxToCmRatioFromAruco(cv::Mat &mat_dst){

    double pixel_cm_ratio;



    return pixel_cm_ratio;

}

char* stringToCharArray(string s){
    const int length = s.length();

    // declaring character array (+1 for null terminator)
    char* char_array = new char[length + 1];

    // copying the contents of the
    // string to char array
    strcpy(char_array, s.c_str());

    for (int i = 0; i < length; i++)
    {
        std::cout << char_array[i];
    }
return char_array;
}


int assignvaluetocoin(double lemin) {

     if (lemin <= 1.147 && lemin > 0.9) {
        return 0;
    }
    else if (lemin <= 1.137 && lemin > 0.9) {
        return 1;
    }
    else if (lemin <= 1.310 && lemin > 0.9) {
        return 2;
    }
     else if (lemin <= 1.330 && lemin > 0.9) {
         return 3;
     }
    else if (lemin <= 1.415 && lemin > 0.9) {
        return 4;
    }
    else {
        return -1;
    }
}

