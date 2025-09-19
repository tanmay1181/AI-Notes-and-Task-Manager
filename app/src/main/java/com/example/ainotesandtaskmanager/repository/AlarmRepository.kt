package com.example.ainotesandtaskmanager.repository

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import com.example.ainotesandtaskmanager.model.Task
import com.example.ainotesandtaskmanager.notification.NotificationReceiver

class AlarmRepository(val context: Context){
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleExactTaskAlarm(task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val taskId = task.id
        val triggerAtMillis = task.dueDate

        // Check if exact alarms are allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            if (!alarmManager.canScheduleExactAlarms()) {
                // Ask user to allow exact alarms
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return // Stop here until user grants permission
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.ainotesandtaskmanager.TASK_REMINDER"
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_DUE", task.dueDate)
        }

        // Schedule the alarm
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis!!,
            pendingIntent
        )
    }

    fun scheduleTaskReminder(task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires this permission
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleExactTaskAlarm(task)
            } else {
                // Redirect user to settings to grant permission
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } else {
            // Pre-Android 12 → schedule directly
            scheduleExactTaskAlarm(task)
        }
    }

    fun cancelTaskReminder(taskId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.ainotesandtaskmanager.TASK_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if(pendingIntent != null){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.w("AlarmRepository", "No PendingIntent found for taskId=$taskId")
        }
        else{
            Log.w("AlarmRepository", "No PendingIntent found for taskId=$taskId")
        }
    }
}