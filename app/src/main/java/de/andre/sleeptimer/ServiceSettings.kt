package de.andre.sleeptimer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceSettings(
    val turnOffWifi: Boolean = false,
    val turnOffMobileData: Boolean = false,
    val muteSound: Boolean = true,
    val enableAirplaneMode: Boolean = false,
    val enablePowerSaving: Boolean = false,
    val closeRunningApps: Boolean = false
) : Parcelable
