package com.timetask.pro.v2.presentation.tools.chrono

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.domain.model.chrono.Lap
import com.timetask.pro.v2.ui.theme.TitaniumError
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.ui.theme.TitaniumSuccess
import kotlin.math.roundToInt

enum class SwipeAnchor {
    START_ACTIONS, // Left-to-Right pull (shows Edit)
    CENTER,        // Default state
    END_ACTIONS    // Right-to-Left pull (shows Delete)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LapSwipeableItem(
    lap: Lap,
    isSwipedOpen: Boolean = false,
    onSwipeStateChanged: (Boolean) -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Sizes of the action areas
    val startActionSize = 60.dp // 1 button: Edit
    val endActionSize = 180.dp  // 3 buttons: Tag, Category, Delete (60dp each)

    val startActionPx = with(density) { startActionSize.toPx() }
    val endActionPx = with(density) { -endActionSize.toPx() }

    val anchors = DraggableAnchors<SwipeAnchor> {
        SwipeAnchor.START_ACTIONS at startActionPx
        SwipeAnchor.CENTER at 0f
        SwipeAnchor.END_ACTIONS at endActionPx
    }

    val state = remember {
        AnchoredDraggableState<SwipeAnchor>(
            initialValue = SwipeAnchor.CENTER,
            anchors = anchors,
            positionalThreshold = { totalDistance -> totalDistance * 0.4f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(300),
            decayAnimationSpec = exponentialDecay()
        )
    }

    // Sync external state with internal state
    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeAnchor.CENTER) {
            onSwipeStateChanged(true)
        }
    }

    LaunchedEffect(isSwipedOpen) {
        if (!isSwipedOpen && state.currentValue != SwipeAnchor.CENTER) {
            state.animateTo(SwipeAnchor.CENTER)
        }
    }

    // Auto-close if needed
    val scope = rememberCoroutineScope()
    val closeMenu = {
        scope.launch { state.snapTo(SwipeAnchor.CENTER) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        // BACKGROUND LAYER (Action Buttons)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // START actions (Left to Right)
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(startActionSize)
                    .background(TitaniumPrimary)
                    .clickable { 
                        onEdit() 
                        closeMenu()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }

            // END actions (Right to Left)
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(endActionSize)
            ) {
                // Category Button (Placeholder)
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { closeMenu() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Category, contentDescription = "Category", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Tag Button (Placeholder)
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable { closeMenu() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Label, contentDescription = "Tag", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Delete Button
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(TitaniumError)
                        .clickable { 
                            onDelete() 
                            closeMenu()
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        }

        // FOREGROUND LAYER (Lap Data)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = state.requireOffset().roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(state, Orientation.Horizontal)
                .background(MaterialTheme.colorScheme.surface) // Solid background
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null // No ripple effect
                ) {
                    if (isSwipedOpen) onSwipeStateChanged(false)
                }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val lapColor = when {
                lap.colorARGB != null -> Color(lap.colorARGB) 
                lap.isBest -> Color(0xFF4CAF50)
                lap.isWorst -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.onSurface
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "#${lap.lapNumber} ${lap.title.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = lapColor
                    )
                    if (lap.categoryText.isNotBlank() || lap.tagsText.isNotBlank()) {
                        val metaText = listOf(lap.categoryText, lap.tagsText).filter { it.isNotBlank() }.joinToString(" • ")
                        Text(
                            text = metaText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatStopwatch(lap.lapTimeMs),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        color = lapColor
                    )
                    Text(
                        text = formatStopwatch(lap.totalTimeMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
