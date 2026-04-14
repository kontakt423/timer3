package de.andre.sleeptimer

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TimerUiState(
    val selectedHours: Int = 0,
    val selectedMinutes: Int = 30,
    val isRunning: Boolean = false,
    val remainingSeconds: Long = 0L,
    val settings: ServiceSettings = ServiceSettings()
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private val tickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                TimerService.ACTION_TICK -> {
                    val remaining = intent.getLongExtra(TimerService.EXTRA_REMAINING, 0L)
                    _uiState.update { it.copy(remainingSeconds = remaining, isRunning = true) }
                }
                TimerService.ACTION_FINISHED -> {
                    _uiState.update { it.copy(isRunning = false, remainingSeconds = 0L) }
                }
            }
        }
    }

    init {
        val app = getApplication<Application>()
        val filter = IntentFilter().apply {
            addAction(TimerService.ACTION_TICK)
            addAction(TimerService.ACTION_FINISHED)
        }
        ContextCompat.registerReceiver(app, tickReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun setHours(h: Int) = _uiState.update { it.copy(selectedHours = h) }
    fun setMinutes(m: Int) = _uiState.update { it.copy(selectedMinutes = m) }

    fun setPreset(totalMinutes: Int) {
        _uiState.update { it.copy(selectedHours = totalMinutes / 60, selectedMinutes = totalMinutes % 60) }
    }

    fun updateSettings(settings: ServiceSettings) = _uiState.update { it.copy(settings = settings) }

    fun startTimer() {
        val app = getApplication<Application>()
        val state = _uiState.value
        val totalSeconds = state.selectedHours * 3600L + state.selectedMinutes * 60L
        if (totalSeconds <= 0L) return
        val intent = Intent(app, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION_SECONDS, totalSeconds)
            putExtra(TimerService.EXTRA_WIFI_OFF, state.settings.turnOffWifi)
            putExtra(TimerService.EXTRA_MOBILE_OFF, state.settings.turnOffMobileData)
            putExtra(TimerService.EXTRA_MUTE, state.settings.muteSound)
            putExtra(TimerService.EXTRA_AIRPLANE, state.settings.enableAirplaneMode)
            putExtra(TimerService.EXTRA_POWER_SAVE, state.settings.enablePowerSaving)
            putExtra(TimerService.EXTRA_CLOSE_APPS, state.settings.closeRunningApps)
        }
        ContextCompat.startForegroundService(app, intent)
        _uiState.update { it.copy(isRunning = true, remainingSeconds = totalSeconds) }
    }

    fun stopTimer() {
        val app = getApplication<Application>()
        val intent = Intent(app, TimerService::class.java).apply { action = TimerService.ACTION_STOP }
        app.startService(intent)
        _uiState.update { it.copy(isRunning = false, remainingSeconds = 0L) }
    }

    override fun onCleared() {
        super.onCleared()
        try { getApplication<Application>().unregisterReceiver(tickReceiver) } catch (_: Exception) {}
    }
}
