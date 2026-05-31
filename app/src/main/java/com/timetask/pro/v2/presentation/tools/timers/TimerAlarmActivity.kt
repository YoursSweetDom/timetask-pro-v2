package com.timetask.pro.v2.presentation.tools.timers

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.timetask.pro.v2.service.TimerAlarmService
import com.timetask.pro.v2.service.TimerService
import com.timetask.pro.v2.ui.theme.TimeTaskProV2Theme

/**
 * Полноэкранная Activity-будильник.
 *
 * Действия:
 * - Готово (Dismiss)
 * - Стоп (Reset)
 * - Рестарт (Restart)
 * - Овертайм (Overtime)
 * - +X мин (Quick Add)
 * - +Custom (Custom Add)
 */
class TimerAlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Показать поверх Lock Screen и включить экран ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // AOSP-паттерн: убрать notification из шторки, т.к. popup уже видим.
        // Notification и popup — одна сущность. Показываем только popup.
        val notifManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notifManager.cancel(TimerAlarmService.ALARM_NOTIFICATION_ID)

        val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: ""
        val timerName = intent.getStringExtra(EXTRA_TIMER_NAME) ?: "Таймер"
        val quickAddSec = intent.getIntExtra(EXTRA_QUICK_ADD_SEC, 60)

        setContent {
            TimeTaskProV2Theme {
                AlarmScreen(
                    timerName = timerName,
                    quickAddSec = quickAddSec,
                    onDismiss = {
                        stopServiceAndFinish()
                    },
                    onStop = {
                        if (timerId.isNotEmpty()) {
                            TimerService.stopTimer(this, timerId)
                        }
                        stopServiceAndFinish()
                    },
                    onRestart = {
                        if (timerId.isNotEmpty()) {
                            TimerService.restartTimer(this, timerId)
                        }
                        stopServiceAndFinish()
                    },
                    onOvertime = {
                        if (timerId.isNotEmpty()) {
                            TimerService.startOvertime(this, timerId)
                        }
                        stopServiceAndFinish()
                    },
                    onAddQuick = {
                        if (timerId.isNotEmpty()) {
                            TimerService.addTime(this, timerId, quickAddSec)
                        }
                        stopServiceAndFinish()
                    },
                    onAddCustom = { addedSeconds ->
                        if (timerId.isNotEmpty()) {
                            TimerService.addTime(this, timerId, addedSeconds)
                        }
                        stopServiceAndFinish()
                    },
                )
            }
        }
    }

    private fun stopServiceAndFinish() {
        TimerAlarmService.stop(this)
        finish()
    }

    companion object {
        const val EXTRA_TIMER_ID = "timer_id"
        const val EXTRA_TIMER_NAME = "timer_name"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val EXTRA_QUICK_ADD_SEC = "quick_add_sec"

        fun createIntent(
            context: Context,
            timerId: String,
            timerName: String,
            soundUri: String? = null,
            quickAddSec: Int = 60
        ): Intent {
            return Intent(context, TimerAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TIMER_ID, timerId)
                putExtra(EXTRA_TIMER_NAME, timerName)
                putExtra(EXTRA_SOUND_URI, soundUri)
                putExtra(EXTRA_QUICK_ADD_SEC, quickAddSec)
            }
        }
    }
}

// ============================================================
// Compose UI
// ============================================================

@Composable
private fun AlarmScreen(
    timerName: String,
    quickAddSec: Int,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onOvertime: () -> Unit,
    onAddQuick: () -> Unit,
    onAddCustom: (Int) -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    var showCustomAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Пульсирующий эмодзи
            Text(
                text = "⏰",
                fontSize = (72 * pulseScale).sp,
                modifier = Modifier.alpha(pulseAlpha),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Имя таймера
            Text(
                text = timerName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
            )
            
            Text(
                text = "Таймер завершён",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Сетка кнопок действий
            // Row 1: Quick Add | Custom Add
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Quick Add
                val quickLabel = when {
                    quickAddSec < 60 -> "+${quickAddSec}s"
                    else -> "+${quickAddSec / 60}m"
                }
                FilledTonalButton(
                    onClick = onAddQuick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.MoreTime, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(quickLabel, fontSize = 16.sp)
                }

                // Custom Add
                FilledTonalButton(
                    onClick = { showCustomAddDialog = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Time...", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Overtime | Restart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Overtime
                FilledTonalButton(
                    onClick = onOvertime,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    // Используем Timer иконку для Overtime
                    Icon(Icons.Default.Timer, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Overtime", fontSize = 16.sp)
                }

                // Restart
                FilledTonalButton(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Большая кнопка Done
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE94560),
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Готово",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка Reset (Stop) - Второстепенная
            TextButton(
                onClick = onStop,
            ) {
                Icon(Icons.Default.Stop, null, tint = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Сбросить (Stop)",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showCustomAddDialog) {
            AddCustomTimeDialog(
                onDismiss = { showCustomAddDialog = false },
                onConfirm = { h, m, s ->
                    val totalSeconds = h * 3600 + m * 60 + s
                    if (totalSeconds > 0) {
                        onAddCustom(totalSeconds)
                    }
                    showCustomAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun AddCustomTimeDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int) -> Unit,
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(5) } // Default 5 mins
    var seconds by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Добавить время",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(24.dp))

                TimeScrollPicker(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                    onTimeChange = { h, m, s ->
                        hours = h
                        minutes = m
                        seconds = s
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(hours, minutes, seconds) }) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}
