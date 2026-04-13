package de.andre.sleeptimer.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.andre.sleeptimer.ServiceSettings
import de.andre.sleeptimer.TimerUiState
import de.andre.sleeptimer.ui.theme.*

@Composable
fun MainScreen(
    uiState: TimerUiState,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onPreset: (Int) -> Unit,
    onSettingsChange: (ServiceSettings) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1117), Color(0xFF0A0E1A))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(text = "\uD83C\uDF19", fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Timer Display
            TimerDisplay(
                isRunning = uiState.isRunning,
                remainingSeconds = uiState.remainingSeconds,
                totalSeconds = uiState.selectedHours * 3600L + uiState.selectedMinutes * 60L
            )

            Spacer(Modifier.height(28.dp))

            if (!uiState.isRunning) {
                TimePickerSection(
                    hours = uiState.selectedHours,
                    minutes = uiState.selectedMinutes,
                    onHoursChange = onHoursChange,
                    onMinutesChange = onMinutesChange
                )
                Spacer(Modifier.height(20.dp))
                PresetRow(onPreset = onPreset)
                Spacer(Modifier.height(24.dp))
            }

            ActionsCard(
                settings = uiState.settings,
                isRunning = uiState.isRunning,
                onSettingsChange = onSettingsChange
            )

            Spacer(Modifier.height(28.dp))

            StartStopButton(
                isRunning = uiState.isRunning,
                enabled = uiState.selectedHours * 60 + uiState.selectedMinutes > 0,
                onStart = onStart,
                onStop = onStop
            )
        }
    }
}

@Composable
fun TimerDisplay(
    isRunning: Boolean,
    remainingSeconds: Long,
    totalSeconds: Long
) {
    val progress = if (totalSeconds > 0L && isRunning)
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = LinearEasing),
        label = "progress"
    )

    val ringColor by animateColorAsState(
        targetValue = if (isRunning) AccentPurple else AccentPurple.copy(alpha = 0.3f),
        animationSpec = tween(500),
        label = "ring"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = DarkCard, radius = radius, center = center, style = Stroke(strokeWidth))
            if (animatedProgress > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isRunning) {
                val h = remainingSeconds / 3600
                val m = (remainingSeconds % 3600) / 60
                val s = remainingSeconds % 60
                Text(
                    text = if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
                           else String.format("%02d:%02d", m, s),
                    fontSize = if (h > 0) 30.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(text = "verbleibend", fontSize = 12.sp, color = TextSecondary)
            } else {
                Icon(
                    imageVector = Icons.Outlined.Bedtime,
                    contentDescription = null,
                    tint = AccentPurple.copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(text = "bereit", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun TimePickerSection(
    hours: Int,
    minutes: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        TimePicker(label = "Stunden", value = hours, range = 0..8, onValueChange = onHoursChange, modifier = Modifier.weight(1f))
        TimePicker(label = "Minuten", value = minutes, range = 0..59, onValueChange = onMinutesChange, modifier = Modifier.weight(1f))
    }
}

@Composable
fun TimePicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(DarkCard, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { if (value > range.first) onValueChange(value - 1) },
                modifier = Modifier.size(38.dp).background(DarkElevated, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null,
                    tint = if (value > range.first) AccentPurple else TextDisabled,
                    modifier = Modifier.size(18.dp))
            }
            Text(
                text = String.format("%02d", value),
                fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { if (value < range.last) onValueChange(value + 1) },
                modifier = Modifier.size(38.dp).background(DarkElevated, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = null,
                    tint = if (value < range.last) AccentPurple else TextDisabled,
                    modifier = Modifier.size(18.dp))
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = AccentPurple,
                activeTrackColor = AccentPurple,
                inactiveTrackColor = DarkElevated
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun PresetRow(onPreset: (Int) -> Unit) {
    val presets = listOf(15 to "15 Min", 30 to "30 Min", 45 to "45 Min", 60 to "1 Std", 90 to "1,5 Std", 120 to "2 Std")
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
    ) {
        presets.forEach { (min, lbl) ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCard)
                    .border(1.dp, DividerColor, RoundedCornerShape(20.dp))
                    .clickable { onPreset(min) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = lbl, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AccentPurpleLight)
            }
        }
    }
}

@Composable
fun ActionsCard(settings: ServiceSettings, isRunning: Boolean, onSettingsChange: (ServiceSettings) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Icon(Icons.Outlined.Tune, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Aktionen beim Timer-Ende", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = DividerColor)
        Spacer(Modifier.height(8.dp))

        ActionToggle(Icons.Outlined.Wifi, "WLAN ausschalten", "Deaktiviert das WiFi",
            settings.turnOffWifi, !isRunning) { onSettingsChange(settings.copy(turnOffWifi = it)) }
        ActionToggle(Icons.Outlined.SignalCellularAlt, "Mobile Daten ausschalten", "Erfordert ADB-Berechtigung",
            settings.turnOffMobileData, !isRunning) { onSettingsChange(settings.copy(turnOffMobileData = it)) }
        ActionToggle(Icons.Outlined.VolumeOff, "Ton stummschalten", "Klingel & Medien auf lautlos",
            settings.muteSound, !isRunning) { onSettingsChange(settings.copy(muteSound = it)) }
        ActionToggle(Icons.Outlined.AirplanemodeActive, "Flugmodus aktivieren", "Erfordert ADB-Berechtigung",
            settings.enableAirplaneMode, !isRunning) { onSettingsChange(settings.copy(enableAirplaneMode = it)) }
        ActionToggle(Icons.Outlined.BatterySaver, "Ultra-Energiesparmodus", "Erfordert ADB-Berechtigung",
            settings.enablePowerSaving, !isRunning) { onSettingsChange(settings.copy(enablePowerSaving = it)) }
        ActionToggle(Icons.Outlined.AppsOutage, "Apps beenden", "Schliesst Hintergrund-Apps",
            settings.closeRunningApps, !isRunning, isLast = true) { onSettingsChange(settings.copy(closeRunningApps = it)) }
    }
}

@Composable
fun ActionToggle(
    icon: ImageVector,
    label: String,
    subLabel: String,
    checked: Boolean,
    enabled: Boolean,
    isLast: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    val iconBgColor by animateColorAsState(
        targetValue = if (checked) AccentPurple.copy(alpha = 0.15f) else DarkElevated, label = "iconBg")
    val iconTint by animateColorAsState(
        targetValue = if (checked) AccentPurpleLight else TextSecondary, label = "iconTint")

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).background(iconBgColor, RoundedCornerShape(10.dp))
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) TextPrimary else TextDisabled, fontWeight = FontWeight.Medium)
                Text(subLabel, fontSize = 12.sp, color = if (enabled) TextSecondary else TextDisabled)
            }
            Switch(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentPurple,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = DarkElevated,
                    disabledCheckedTrackColor = AccentPurple.copy(alpha = 0.4f),
                    disabledUncheckedTrackColor = DarkElevated.copy(alpha = 0.4f)
                )
            )
        }
        if (!isLast) HorizontalDivider(color = DividerColor, modifier = Modifier.padding(start = 54.dp))
    }
}

@Composable
fun StartStopButton(isRunning: Boolean, enabled: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    val btnBg by animateColorAsState(
        targetValue = if (isRunning) AccentRed.copy(alpha = 0.15f) else AccentPurple.copy(alpha = 0.15f),
        animationSpec = tween(400), label = "btnBg")
    val btnBorder by animateColorAsState(
        targetValue = if (isRunning) AccentRed else AccentPurple, animationSpec = tween(400), label = "btnBorder")
    val btnText by animateColorAsState(
        targetValue = if (isRunning) AccentRed else AccentPurple, animationSpec = tween(400), label = "btnText")

    Button(
        onClick = if (isRunning) onStop else onStart,
        enabled = enabled || isRunning,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = btnBg,
            contentColor = btnText,
            disabledContainerColor = DarkCard,
            disabledContentColor = TextDisabled
        ),
        border = BorderStroke(1.5.dp, if (enabled || isRunning) btnBorder else DividerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = if (isRunning) "Timer stoppen" else "Timer starten",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
