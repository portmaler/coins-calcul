package com.devya.coincalculvo.util

import android.icu.text.DecimalFormat
import android.os.Build
import androidx.annotation.RequiresApi

val TAG = "Extension"


val dhArray:FloatArray = floatArrayOf(0.5f,1.0f,5.0f, 10.0f, 1.0f)
val euroArray:FloatArray = floatArrayOf(0.05f,0.1f,0.2f,0.5f, 1.0f)
val dollarArray:FloatArray = floatArrayOf(0.5f,1.0f,2.0f,5.0f, 10.0f)

@RequiresApi(Build.VERSION_CODES.N)
fun calculSum(coinName:String, array:IntArray):Float{
    var sum:Float = 0.0F
    when(coinName){
        "dirham" -> {
         for(i in array.indices){
             sum += (array[i]* dhArray[i])
         }
        }
        "dollar" -> {
            for(i in 0..3){
                sum += array[i]* dollarArray[i]
            }
        }
        "euro" -> {
            for(i in array.indices){
                sum += array[i]* euroArray[i]
            }
        }
    }
    val df = DecimalFormat("#.##")
    val roundsum = df.format(sum)
    return roundsum.toFloat()
}

fun getIntTypefromstring(coinType: String?): Int {
    when(coinType){
        "dirham" -> return 1
        "euro" -> return  2
        "dollar" -> return  3
    }
    return 0
}

fun getCoinTypeListLength(coinType: String?): Int {
    when(coinType){
        "dirham" -> return 4
        "euro" -> return  4
        "dollar" -> return  3
    }
    return 0
}
