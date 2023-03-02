package com.devya.coincalculvo.util

val TAG = "Extension"


val dhArray:FloatArray = floatArrayOf(0.5f,1.0f,2.0f,5.0f, 10.0f)
val euroArray:FloatArray = floatArrayOf(0.5f,1.0f,2.0f,5.0f, 10.0f)
val dollarArray:FloatArray = floatArrayOf(0.5f,1.0f,2.0f,5.0f, 10.0f)

fun calculSum(coinName:String, array:IntArray):Float{
    var sum:Float = 0.0F
    when(coinName){
        "dirham" -> {
         for(i in array.indices){
             sum += (array[i]* dhArray[i])
         }
        }
        "dollar" -> {
            for(i in array.indices){
                sum += array[i]* dollarArray[i]
            }
        }
        "euro" -> {
            for(i in array.indices){
                sum += array[i]* euroArray[i]
            }
        }
    }
    return sum
}
