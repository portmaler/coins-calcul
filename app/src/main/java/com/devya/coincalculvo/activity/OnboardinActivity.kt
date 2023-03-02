package com.devya.coincalculvo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.viewpager2.widget.ViewPager2
import com.devya.coincalculvo.R
import com.devya.coincalculvo.adapter.OnboardingViewPagerAdapter
import com.devya.coincalculvo.databinding.ActivityOnboardinBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnboardinActivity : AppCompatActivity() {

    private lateinit var mViewPager: ViewPager2
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button

    private lateinit var binding: ActivityOnboardinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardinBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mViewPager = binding.viewPager
        mViewPager.adapter = OnboardingViewPagerAdapter(this, this)
        mViewPager.offscreenPageLimit = 1
        btnBack = binding.btnPreviousStep
        btnNext = binding.btnNextStep
        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 2) {
                    btnNext.text = getText(R.string.finish)
                } else {
                    btnNext.text = getText(R.string.next)
                }
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })
        TabLayoutMediator(binding.pageIndicator, mViewPager) { _, _ -> }.attach()

        btnNext.setOnClickListener {
            if (getItem() > mViewPager.childCount) {
                startActivity(Intent(this, WelcomActivity::class.java))
                finish()
            } else {
                mViewPager.setCurrentItem(getItem() + 1, true)
            }
        }

        btnBack.setOnClickListener {
            if (getItem() == 0) {
                finish()
            } else {
                mViewPager.setCurrentItem(getItem() - 1, true)
            }
        }
    }

    private fun getItem(): Int {
        return mViewPager.currentItem
    }
}