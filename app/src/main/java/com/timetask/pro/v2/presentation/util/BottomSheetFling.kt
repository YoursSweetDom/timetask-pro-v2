package com.timetask.pro.v2.presentation.util

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Custom fling-to-close modifier for ModalBottomSheet.
 * This injects custom drag physics which listens for high velocity downward swipes
 * to hide the sheet directly instead of dropping into PartiallyExpanded state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Modifier.bottomSheetFlingToClose(
    sheetState: SheetState,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
    velocityThreshold: Float = 1000f // pixels per second
): Modifier {
    return this.then(
        Modifier.draggable(
            state = rememberDraggableState { delta ->
                // Let the BottomSheet native gesture handle the actual drag offset
            },
            orientation = Orientation.Vertical,
            onDragStopped = { velocity ->
                if (velocity > velocityThreshold) {
                    scope.launch {
                        sheetState.hide()
                        if (!sheetState.isVisible) {
                            onDismiss()
                        }
                    }
                }
            }
        )
    )
}
