package com.timetask.pro.v2.presentation.tools.alarms

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetask.pro.v2.service.alarm.AlarmRingingService
import com.timetask.pro.v2.ui.theme.TimeTaskProV2Theme
import kotlin.math.abs
import kotlin.math.roundToInt

class AlarmRingingActivity : ComponentActivity() {

    companion object {
        const val ACTION_FINISH_ACTIVITY = "com.timetask.pro.v2.FINISH_ALARM_ACTIVITY"
    }

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH_ACTIVITY) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lockscreen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // AOSP-паттерн: убрать notification из шторки, т.к. popup уже видим.
        val notifManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notifManager.cancel(AlarmRingingService.NOTIFICATION_ID)

        val alarmId = intent.getStringExtra(AlarmRingingService.EXTRA_ALARM_ID)
        val alarmName = intent.getStringExtra(AlarmRingingService.EXTRA_ALARM_NAME) ?: "Будильник"
        val alarmNotes = intent.getStringExtra(AlarmRingingService.EXTRA_ALARM_NOTES) ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(finishReceiver, IntentFilter(ACTION_FINISH_ACTIVITY), Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(finishReceiver, IntentFilter(ACTION_FINISH_ACTIVITY))
        }

        setContent {
            TimeTaskProV2Theme {
                AlarmRingingScreen(
                    name = alarmName,
                    notes = alarmNotes,
                    onDismiss = {
                        val serviceIntent = Intent(this, AlarmRingingService::class.java).apply {
                            action = AlarmRingingService.ACTION_DISMISS
                            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
                        }
                        startService(serviceIntent)
                        finish()
                    },
                    onSnooze = {
                        val serviceIntent = Intent(this, AlarmRingingService::class.java).apply {
                            action = AlarmRingingService.ACTION_SNOOZE
                            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
                        }
                        startService(serviceIntent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(finishReceiver)
        } catch (e: Exception) {
            // Ignored
        }
    }
}

@Composable
fun AlarmRingingScreen(
    name: String,
    notes: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val maxDragDist = 600f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)
        ) {
            Text(
                name.ifBlank { "Будильник" },
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            // TODO: In Phase 2, get actual real-time formatted time
            Text(
                "07:00",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                            MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📝 Заметка:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        notes,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Snooze Button
        Button(
            onClick = onSnooze,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 100.dp)
        ) {
            Text("Отложить (5 мин)")
        }

        // Swipe up to dismiss slider
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            Text(
                "Свайпните вверх для отключения",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-60).dp)
                    .alpha(1f - abs(offsetY) / maxDragDist),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .offset { IntOffset(0, offsetY.roundToInt()) }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            offsetY = (offsetY + delta).coerceIn(-maxDragDist, 0f)
                        },
                        onDragStopped = {
                            if (offsetY <= -maxDragDist * 0.8f) {
                                onDismiss()
                            } else {
                                offsetY = 0f // Snap back
                            }
                        }
                    )
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(vertical = 16.dp, horizontal = 32.dp)
            ) {
                Text(
                    "ОТКЛЮЧИТЬ",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
