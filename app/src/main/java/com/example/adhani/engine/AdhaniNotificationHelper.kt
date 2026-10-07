package com.example.adhani.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.adhani.model.PrayerTimeItem

object AdhaniNotificationHelper {

    const val CHANNEL_ID = "adhani_prayer_times_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Adhani Prayer Times"
            val descriptionText = "Live prayer times and upcoming prayer countdown alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPrayerNotification(
        context: Context,
        cityName: String,
        nextPrayer: PrayerTimeItem?,
        countdownText: String,
        allPrayers: List<PrayerTimeItem>
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val nextName = nextPrayer?.name?.englishName ?: "Fajr"
        val nextTime = nextPrayer?.formattedTime ?: "--:--"
        val nextArabic = nextPrayer?.name?.arabicName ?: "الفجر"

        val title = "Adhani • Next: $nextName ($nextArabic) at $nextTime"
        val content = "In $countdownText • $cityName"

        val timetableSummary = allPrayers.joinToString("  |  ") {
            "${it.name.englishName}: ${it.formattedTime}"
        }

        val bigText = StringBuilder()
            .append("📍 $cityName\n")
            .append("⏳ Next: $nextName in $countdownText\n\n")
            .append("Today's Timetable:\n")
            .apply {
                allPrayers.forEach {
                    val indicator = if (it.isNext) "👉 " else "• "
                    append("$indicator${it.name.englishName} (${it.name.arabicName}): ${it.formattedTime}\n")
                }
            }.toString()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Notification permission might not be granted yet
        }
    }

    fun cancelNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
