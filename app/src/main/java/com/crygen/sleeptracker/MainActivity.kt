package com.crygen.sleeptracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import com.crygen.sleeptracker.SleepTrackerService.Companion.getDateString
import com.crygen.sleeptracker.SleepTrackerService.Companion.getMySharedPref
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
 * Sleep tracker for Sunday
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        val calendar = Calendar.getInstance()
        todayTotalSleepTv.text = getTimeSlept("Today you slept: ", calendar)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        yesterdayTotalSleepTv.text = getTimeSlept("Yesterday you slept:", calendar)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        dayBeforeTotalSleepTv.text = getTimeSlept("Day before yesterday you slept: ", calendar)

    }

    private fun getTimeSlept(prefix: String, calendar: Calendar): String {
        val mins = getMySharedPref(this).getLong(getDateString(calendar), -1)
        if (mins == -1L) return ""
        return "$prefix  ${mins / 60} hours and ${mins % 60} minutes"
    }

    fun startService(v: View) {
        val serviceIntent = Intent(this, SleepTrackerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun stopService(v: View) {
        val serviceIntent = Intent(this, SleepTrackerService::class.java)
        stopService(serviceIntent)
    }

    override fun onResume() {
        super.onResume()
        initView()
    }
}
