package de.andre.sleeptimer

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.app.ActivityManager
import android.util.Log

class ServiceExecutor(private val context: Context) {

    companion object {
        private const val TAG = "ServiceExecutor"
    }

    fun executeAll(settings: ServiceSettings) {
        Log.d(TAG, "Executing all enabled services: $settings")
        if (settings.muteSound) muteSound()
        if (settings.closeRunningApps) closeRunningApps()
        if (settings.turnOffWifi) turnOffWifi()
        if (settings.turnOffMobileData) turnOffMobileData()
        if (settings.enablePowerSaving) enablePowerSaving()
        if (settings.enableAirplaneMode) enableAirplaneMode()
    }

    private fun muteSound() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
            Log.d(TAG, "Sound muted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Could not mute sound", e)
        }
    }

    @Suppress("DEPRECATION")
    private fun turnOffWifi() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Öffne WiFi-Einstellungen-Panel
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
                panelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(panelIntent)
            } else {
                val wifiManager = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiManager.isWifiEnabled = false
            }
            Log.d(TAG, "WiFi toggle triggered")
        } catch (e: Exception) {
            Log.e(TAG, "Could not turn off WiFi", e)
        }
    }

    private fun turnOffMobileData() {
        try {
            // Benötigt MODIFY_PHONE_STATE - per ADB zu gewähren
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val method = telephonyManager.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.java)
            method.isAccessible = true
            method.invoke(telephonyManager, false)
            Log.d(TAG, "Mobile data disabled via reflection")
        } catch (e: Exception) {
            Log.w(TAG, "Reflection method failed, trying Settings.Global", e)
            try {
                Settings.Global.putInt(context.contentResolver, "mobile_data", 0)
            } catch (e2: Exception) {
                Log.e(TAG, "Could not disable mobile data", e2)
            }
        }
    }

    private fun enableAirplaneMode() {
        try {
            // Benötigt WRITE_SECURE_SETTINGS - per ADB zu gewähren
            Settings.Global.putInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 1)
            val broadcastIntent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            broadcastIntent.putExtra("state", true)
            context.sendBroadcast(broadcastIntent)
            Log.d(TAG, "Airplane mode enabled")
        } catch (e: SecurityException) {
            Log.e(TAG, "WRITE_SECURE_SETTINGS not granted. Run: adb shell pm grant de.andre.sleeptimer android.permission.WRITE_SECURE_SETTINGS", e)
        } catch (e: Exception) {
            Log.e(TAG, "Could not enable airplane mode", e)
        }
    }

    private fun enablePowerSaving() {
        try {
            // Benötigt WRITE_SECURE_SETTINGS - per ADB zu gewähren
            Settings.Global.putInt(context.contentResolver, Settings.Global.LOW_POWER_MODE, 1)
            Log.d(TAG, "Power saving mode enabled")
        } catch (e: SecurityException) {
            Log.e(TAG, "WRITE_SECURE_SETTINGS not granted for power saving", e)
        } catch (e: Exception) {
            Log.e(TAG, "Could not enable power saving", e)
        }
    }

    private fun closeRunningApps() {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            val runningProcesses = activityManager.runningAppProcesses
            runningProcesses?.forEach { processInfo ->
                if (processInfo.processName != context.packageName &&
                    processInfo.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
                ) {
                    activityManager.killBackgroundProcesses(processInfo.processName)
                }
            }
            Log.d(TAG, "Background apps closed")
        } catch (e: Exception) {
            Log.e(TAG, "Could not close running apps", e)
        }
    }
}
