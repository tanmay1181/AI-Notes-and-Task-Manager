package com.example.ainotesandtaskmanager.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ainotesandtaskmanager.R
import androidx.core.net.toUri
import com.example.ainotesandtaskmanager.screens.formatDate

class NotificationReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != "com.example.ainotesandtaskmanager.TASK_REMINDER") {
            return
        }

        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val taskDueTime = intent.getLongExtra("TASK_DUE", 0)
        val taskId = intent.getIntExtra("TASK_ID", 0)

        // Intent to open MainActivity when tapping notification
        val openIntent = Intent(
            Intent.ACTION_VIEW,
            "myapp://tasks".toUri()
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create NotificationChannel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Build notification
        val builder = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)
            .setContentTitle(taskTitle)
            .setContentText("Due At: ${formatDate(taskDueTime)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // 👈 Important

        val manager = NotificationManagerCompat.from(context)
        manager.notify(taskId, builder.build())
    }
}

