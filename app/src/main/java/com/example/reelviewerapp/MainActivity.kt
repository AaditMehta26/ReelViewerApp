package com.example.reelviewerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val videoResIds = listOf(R.raw.video1, R.raw.video2, R.raw.video3,R.raw.video4,R.raw.video5,R.raw.video6,R.raw.video7)
        val adapter = ReelPagerAdapter(this, videoResIds)
        viewPager.adapter = adapter
    }
}
