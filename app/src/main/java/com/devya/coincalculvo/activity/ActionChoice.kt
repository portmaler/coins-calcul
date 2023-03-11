package com.devya.coincalculvo.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.devya.coincalculvo.R


class ActionChoice : AppCompatActivity() {

    var coinType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_choice)

        supportActionBar?.title = "Coins Calcul"

       coinType =  intent.getStringExtra("coinType")

        val btnsend = findViewById<Button>(R.id.choiceImage)

        btnsend.setOnClickListener {
            val intent = Intent(this, ProccesImageActivity::class.java)
            intent.putExtra("coinType" , coinType)
            startActivity(intent)
        }

        val btnsend3 = findViewById<Button>(R.id.calibrate)

        btnsend3.setOnClickListener {
            val intent = Intent(this, CalibrationActivity::class.java)
            startActivity(intent)
            // testIntarray(6)
        }
    }

}