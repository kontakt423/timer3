package de.andre.sleeptimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class TimerService : Service() {

    companion object {
        const val ACTION_START = "de.andre.sleeptimer.ACTION_START"
        const val ACTION_STOP = "de.andre.sleeptimer.ACTION_STOP"

        const val EXTRA_DURATION_SECONDS = "EXTRA_DURATION_SECONDS"
        const val EXTRA_WIFI_OFF = "EXTRA_WIFI_OFF"
        const val EXTRA_MOBILE_OFF = "EXTRA_MOBILE_OFF"
        const val EXTRA_MUTE = "EXTRA_MUTE"
        const val EXTRA_AIRPLANE = "EXTRA_AIRPLANE"
        const val EXTRA_POWER_SAVE = "EXTRA_POWER_SAVE"
        const val EXTRA_CLOSE_APPS = "EXTRA_CLOSE_APPS"

        const val ACTION_TICK = "de.andre.sleeptimer.ACTION_TICK"
        const val ACTION_FINISHED = "de.andre.sleeptimer.ACTION_FINISHED"
        const val EXTRA_REMAINING = "EXTRA_REMAINING"

        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "sleep_timer_channel"
    }

    private var countDownTimer: CountDownTimer? = null
    private lateinit var notificationManager: NotificationManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationSeconds = intent.getLongExtra(EXTRA_DURATION_SECONDS, 0L)
                if (durationSeconds <= 0L) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                val settings = ServiceSettings(
                    turnOffWifi = intent.getBooleanExtra(EXTRA_WIFI_OFF, false),
                    turnOffMobileData = intent.getBooleanExtra(EXTRA_MOBILE_OFF, false),
                    muteSound = intent.getBooleanExtra(EXTRA_MUTE, false),
                    enableAirplaneMode = intent.getBooleanExtra(EXTRA_AIRPLANE, false),
                    enablePowerSaving = intent.getBooleanExtra(EXTRA_POWER_SAVE, false),
                    closeRunningApps = intent.getBooleanExtra(EXTRA_CLOSE_APPS, false)
                )
                startTimer(durationSeconds, settings)
            }
            ACTION_STOP -> {
                stopTimer()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationSeconds: Long, settings: ServiceSettings) {
        countDownTimer?.cancel()
        startForeground(NOTIFICATION_ID, buildNotification(durationSeconds))

        countDownTimer = object : CountDownTimer(durationSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = millisUntilFinished / 1000L
                updateNotification(remaining)
                sendTickBroadcast(remaining)
            }

            override fun onFinish() {
                sendFinishedBroadcast()
                ServiceExecutor(this@TimerService).executeAll(settings)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun sendTickBroadcast(remaining: Long) {
        val intent = Intent(ACTION_TICK).apply {
            putExtra(EXTRA_REMAINING, remaining)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun sendFinishedBroadcast() {
        val intent = Intent(ACTION_FINISHED).apply {
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun buildNotification(remainingSeconds: Long): Notification {
        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 1, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🌙 Sleep Timer läuft")
            .setContentText("Verbleibend: ${formatTime(remainingSeconds)}")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Stoppen", stopPendingIntent)
            .build()
    }

    private fun updateNotification(remainingSeconds: Long) {
        val notification = buildNotification(remainingSeconds)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sleep Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Zeigt den laufenden Sleep-Timer an"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SleepTimer::TimerWakeLock"
        ).also { it.acquire(8 * 60 * 60 * 1000L) } // Max 8 Stunden
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        wakeLock?.let { if (it.isHeld) it.release() }
    }
}
