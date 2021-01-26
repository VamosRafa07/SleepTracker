package com.crygen.sleeptracker

import android.app.Service
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.content.*
import com.crygen.sleeptracker.App.Companion.CHANNEL_ID
import java.util.*


/**
 * Using foreground service because after API 26 or Android Oreo the background service will go into doze mode
 * or get killed off
 */

class SleepTrackerService : Service() {
    val SCREEN_OFF = "screen_off"
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private lateinit var mLockScreenStateReceiver: LockScreenStateReceiver

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sunday Sleep Tracking")
            .setContentText("tracking sleep")
            .setSmallIcon(R.drawable.sleepy_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)


        mLockScreenStateReceiver = LockScreenStateReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(mLockScreenStateReceiver, filter)

        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(mLockScreenStateReceiver)
        super.onDestroy()
    }

    inner class LockScreenStateReceiver : BroadcastReceiver() {
        fun findDiffInMins(t1: Long, t2: Long): Long {
            var diffMillSec = t2 - t1
            return (diffMillSec / 1000) / 60
        }

        override fun onReceive(context: Context, intent: Intent) {
            val pref = getMySharedPref(context)
            val editor = pref.edit()
            val currentTime = Calendar.getInstance()
            val date = getDateString(currentTime)

            /**
             * We don't need to invalidate @param SCREEN_OFF sharedPref because screen_off will be overridden when the
             * screen turns off after the service is online
             */

            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                editor.putLong(SCREEN_OFF, currentTime.time.time)
                editor.apply()

            } else {
                val screenOffTime = pref.getLong(SCREEN_OFF, -1)
                val screenOnTime = currentTime.time.time
                var timeSlept = findDiffInMins(screenOffTime, screenOnTime)
                if (timeSlept > pref.getLong(date, -1) && timeSlept > 3 * 60 &&
                    didContainHour(
                        3,
                        screenOffTime,
                        screenOnTime
                    )
                ) {
                    editor.putLong(date, timeSlept)
                    editor.apply()
                }

            }
        }
    }


    companion object {
        fun getDateString(calendar: Calendar): String {
            return "${calendar.get(Calendar.DAY_OF_MONTH)} / ${calendar[Calendar.MONTH]} / ${calendar[Calendar.YEAR]}"
        }

        fun getMySharedPref(context: Context): SharedPreferences {
            return context.getSharedPreferences("sunday_sleep_tracker", MODE_PRIVATE)
        }


        //to check whether the person  sleep time contains specific hour (e.g: 3 AM)
        fun didContainHour(hour: Int, startTime: Long, endTime: Long): Boolean {
            val calendarStart = Calendar.getInstance()
            val calendarEnd = Calendar.getInstance()
            calendarStart.timeInMillis = startTime
            calendarEnd.timeInMillis = endTime
            val calendarTime = Calendar.getInstance()
            calendarTime.set(Calendar.HOUR_OF_DAY, hour)
            return (calendarTime.after(calendarStart)).and(calendarTime.before(calendarEnd))
        }
    }
}
