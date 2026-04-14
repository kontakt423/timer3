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
        } catch (e: Exception) {
            Log.e(TAG, "muteSound failed", e)
        }
    }

    @Suppress("DEPRECATION")
    private fun turnOffWifi() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
                panelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(panelIntent)
            } else {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wm.isWifiEnabled = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "turnOffWifi failed", e)
        }
    }

    private fun turnOffMobileData() {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val method = tm.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.java)
            method.isAccessible = true
            method.invoke(tm, false)
        } catch (e: Exception) {
            try {
                Settings.Global.putInt(context.contentResolver, "mobile_data", 0)
            } catch (e2: Exception) {
                Log.e(TAG, "turnOffMobileData failed", e2)
            }
        }
    }

    private fun enableAirplaneMode() {
        try {
            Settings.Global.putInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 1)
            val broadcastIntent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", true)
            }
            context.sendBroadcast(broadcastIntent)
        } catch (e: Exception) {
            Log.e(TAG, "enableAirplaneMode failed – WRITE_SECURE_SETTINGS required", e)
        }
    }

    private fun enablePowerSaving() {
        try {
            // "low_power" is the Settings.Global.LOW_POWER_MODE constant value
            Settings.Global.putInt(context.contentResolver, "low_power", 1)
        } catch (e: Exception) {
            Log.e(TAG, "enablePowerSaving failed – WRITE_SECURE_SETTINGS required", e)
        }
    }

    private fun closeRunningApps() {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            am.runningAppProcesses?.forEach { proc ->
                if (proc.processName != context.packageName &&
                    proc.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                    am.killBackgroundProcesses(proc.processName)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "closeRunningApps failed", e)
        }
    }
}
