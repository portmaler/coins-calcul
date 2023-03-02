package com.devya.coincalculvo.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.devya.coincalculvo.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
           if(onBoardingFinished()){
               startActivity(Intent(this, WelcomActivity::class.java))
               finish()
               }else{
               startActivity(Intent(this, OnboardinActivity::class.java))
               finish()
               }
        },3000)
}

    private fun onBoardingFinished(): Boolean{
        val sharedPreference = getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val result = sharedPreference.getBoolean("Finished", false)
        var editor = sharedPreference.edit()
        editor.putBoolean("Finished",true)
        editor.putLong("l",100L)
        editor.commit()

        return result

    }
}