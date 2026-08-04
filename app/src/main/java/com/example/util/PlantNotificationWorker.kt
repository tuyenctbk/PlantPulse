package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.R

class PlantNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val plants = database.plantDao().getAllPlantsSync()

            val duePlants = plants.filter { it.isAnyCareDue() }
            if (duePlants.isNotEmpty()) {
                val title = "🌱 Plant Care & Fertilization Reminder"
                val message = if (duePlants.size == 1) {
                    val p = duePlants[0]
                    val tasks = mutableListOf<String>()
                    if (p.isWaterDue()) tasks.add("watering")
                    if (p.isFertilizeDue()) tasks.add("fertilizing / feeding")
                    "${p.name} needs ${tasks.joinToString(" and ")} today based on its seasonal schedule!"
                } else {
                    val waterDueCount = duePlants.count { p -> p.isWaterDue() }
                    val fertilizeDueCount = duePlants.count { p -> p.isFertilizeDue() }
                    "Plant attention due: $waterDueCount watering, $fertilizeDueCount feeding tasks pending."
                }

                showNotification(applicationContext, title, message)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "plant_care_channel_id"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Plant Care Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for watering and plant care based on season and schedule"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(1001, notification)
            }
        } catch (e: Exception) {
            // Ignored if permission missing
        }
    }
}
