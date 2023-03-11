package com.devya.coincalculvo.activity

import android.content.Intent
import android.os.Bundle
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devya.coincalculvo.R
import com.devya.coincalculvo.adapter.CustomAdapter
import com.devya.coincalculvo.databinding.ActivityWelcomBinding
import com.devya.coincalculvo.model.CoinType


class WelcomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomBinding
    private lateinit var coinTypeArray : ArrayList<CoinType>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Coins Calcul"

        val imageIds: IntArray = intArrayOf(
            R.drawable.dh,
            R.drawable.euro,
            R.drawable.other
        )

        val names = arrayOf(
            "dirham",
            "euro",
            "dollar"
        )
        coinTypeArray = ArrayList()

        for (i in names.indices) {
            val cointype = CoinType(names[i], imageIds[i])
            coinTypeArray.add(cointype)
        }


        val gridView = findViewById<GridView>(R.id.welcomegridview) as GridView


        val customAdapter = CustomAdapter(this@WelcomActivity, imageIds, names)

        gridView.adapter = customAdapter

        gridView.setOnItemClickListener { adapterView, parent, position, l ->
            val name = names[position]
            val image = imageIds[position]

            val i = Intent(this, ActionChoice::class.java)
            i.putExtra("coinType", name)
            startActivity(i)
            Toast.makeText(this@WelcomActivity, "Click on : " + name, Toast.LENGTH_LONG)
                .show()
        }
    }
}