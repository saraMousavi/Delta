package com.example.delta.permission

import android.Manifest
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat

class Notification(
    caller: ActivityResultCaller,
    private val context: Context,
    private val onGranted: () -> Unit = {},
    private val onDenied: () -> Unit = {}
) {
    private val launcher = caller.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onGranted() else onDenied()
    }

    fun ensurePermission() {
        if (Build.VERSION.SDK_INT < 33) {
            // No runtime permission below 33; just check global app-notification toggle
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                onGranted()
            } else {
                showEnableInSettingsDialog()
            }
            return
        }
        // Android 13+
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.areNotificationsEnabled()) {
            onGranted()
        } else {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showEnableInSettingsDialog() {
        AlertDialog.Builder(context)
            .setTitle("Enable notifications")
            .setMessage("Notifications are disabled for this app. Open Settings to enable?")
            .setPositiveButton("Open Settings") { _, _ -> openNotificationSettings() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openNotificationSettings() {
        try {
            val intent = Intent().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:${context.packageName}")
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // Fallback
            val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
