package com.timetask.pro.v2.presentation.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.presentation.util.LocalLongPressDelay
import com.timetask.pro.v2.presentation.util.LocalLatchMode

/**
 * Bottom Navigation Bar с 7 табами + кнопка «⋯» (popup-меню).
 * Кнопка «Инструменты» поддерживает:
 * - Короткий клик → обычный переход
 * - Long press + drag → Quick Access меню
 *
 * @param longPressDelayMs задержка long press из настроек (200-500ms)
 */
@Composable
fun BottomNavBar(
    navController: NavHostController,
    quickListItems: List<String> = emptyList(),
    folders: List<com.timetask.pro.v2.data.local.db.entity.FolderEntity> = emptyList(),
    tags: List<com.timetask.pro.v2.data.local.db.entity.TagEntity> = emptyList(),
    categories: List<com.timetask.pro.v2.data.local.db.entity.CategoryEntity> = emptyList(),
    filters: List<com.timetask.pro.v2.data.local.db.entity.FilterEntity> = emptyList(),
    onTaskFilterSelected: (com.timetask.pro.v2.domain.model.TaskFilter) -> Unit = {},
    onNavigateToSettings: () -> Unit,
) {
    val longPressDelayMs = LocalLongPressDelay.current
    val latchMode = LocalLatchMode.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var showMoreMenu by remember { mutableStateOf(false) }

    // Quick Access state
    var showQuickAccess by remember { mutableStateOf(false) } // Drag overlay
    var showLatchedPopup by remember { mutableStateOf(false) } // Clickable popup (Latch Mode)
    var highlightedIndex by remember { mutableIntStateOf(-1) }
    var previousHighlightedIndex by remember { mutableIntStateOf(-1) }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Track Tools button position for popup positioning
    var toolsButtonPosition by remember { mutableStateOf(Offset.Zero) }
    var toolsButtonWidth by remember { mutableStateOf(0) }

    // Track Tasks button position for popup positioning
    var tasksButtonPosition by remember { mutableStateOf(Offset.Zero) }
    var tasksButtonWidth by remember { mutableStateOf(0) }

    var showTasksQuickAccess by remember { mutableStateOf(false) } // Drag overlay for Tasks
    var showTasksLatchedPopup by remember { mutableStateOf(false) } // Clickable popup for Tasks
    var tasksHighlightedIndex by remember { mutableIntStateOf(-1) }

    // Valid parent coordinates for relative positioning
    var startBoxPosition by remember { mutableStateOf(Offset.Zero) }

    // Popup item layout (approximate widths for hit-testing)
    val toolsItemCount = quickAccessItems.size
    val tasksItemCount = quickListItems.size
    val popupItemWidth = with(density) { 60.dp.toPx() } // approximate per-item width
    val toolsPopupTotalWidth = popupItemWidth * toolsItemCount
    val tasksPopupTotalWidth = popupItemWidth * tasksItemCount
    val popupHeight = with(density) { 70.dp.toPx() }

    Box(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            startBoxPosition = coordinates.positionInRoot()
        }
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            // ============================================================
            // Navigation items (before Tools)
            // ============================================================
            bottomNavItems.forEach { item ->
                val isToolsItem = item.route is ToolsRoute
                val isTasksItem = item.route == TasksRoute
                val selected = when {
                    isToolsItem -> currentDestination?.hasRoute(ToolsRoute::class) == true
                    else -> currentDestination?.hasRoute(item.route::class) == true
                }

                if (isToolsItem || isTasksItem) {
                    val itemCount = if (isToolsItem) toolsItemCount else tasksItemCount
                    
                    // For Tasks, we now use a vertical list. The popup total width is fixed to 200.dp,
                    // and the height depends on the item count.
                    val tasksPopupFixedTotalWidth = with(density) { 200.dp.toPx() }
                    val tasksPopupItemHeight = with(density) { 40.dp.toPx() } // Approx 40dp per item
                    val tasksPopupMaxHeight = with(density) { 400.dp.toPx() }
                    val calculatedTasksHeight = tasksPopupItemHeight * tasksItemCount + with(density) { 16.dp.toPx() } // paddings
                    val tasksPopupTotalHeight = calculatedTasksHeight.coerceAtMost(tasksPopupMaxHeight)

                    val popupTotalWidth = if (isToolsItem) toolsPopupTotalWidth else tasksPopupFixedTotalWidth
                    
                    val currentShowLatchedPopup = if (isToolsItem) showLatchedPopup else showTasksLatchedPopup
                    
                    // ============================================================
                    // 🔧 Custom gesture handling for Tools and Tasks
                    // ============================================================
                    NavigationBarItem(
                        selected = selected,
                        onClick = { /* handled by pointerInput */ },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        ),
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                if (isToolsItem) {
                                    toolsButtonPosition = coordinates.positionInRoot()
                                    toolsButtonWidth = coordinates.size.width
                                } else {
                                    tasksButtonPosition = coordinates.positionInRoot()
                                    tasksButtonWidth = coordinates.size.width
                                }
                            }
                            .pointerInput(longPressDelayMs, latchMode, currentShowLatchedPopup) {
                                // If Popup is showing in interactive mode, let it handle touches (prevent pass-through)
                                if (currentShowLatchedPopup) return@pointerInput

                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val dragStartPosition = down.position
                                    var dragCurrentPosition = dragStartPosition
                                    var isLongPress = false
                                    var isTap = false
                                    
                                    val touchSlop = viewConfiguration.touchSlop

                                    // 1. Wait for Long Press
                                    val longPressResult = withTimeoutOrNull(longPressDelayMs.toLong()) {
                                        do {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull()
                                            
                                            if (change == null || !change.pressed) {
                                                isTap = true
                                                return@withTimeoutOrNull
                                            }
                                            
                                            val currentPos = change.position
                                            val dist = (currentPos - dragStartPosition).getDistance()
                                            
                                            if (dist > touchSlop) {
                                                return@withTimeoutOrNull
                                            }
                                        } while (true)
                                    }

                                    if (longPressResult == null) {
                                        // LONG PRESS DETECTED
                                        isLongPress = true
                                        if (isToolsItem) {
                                            showQuickAccess = true
                                            showLatchedPopup = false
                                            highlightedIndex = -1
                                        } else {
                                            showTasksQuickAccess = true
                                            showTasksLatchedPopup = false
                                            tasksHighlightedIndex = -1
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                        // 2. Drag Logic
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            
                                            if (event.type == PointerEventType.Move) {
                                                val change = event.changes.firstOrNull() ?: continue
                                                dragCurrentPosition = change.position
                                                change.consume()
                                                
                                                // Calculate Hit Test using GLOBAL coordinates
                                                // 1. Popup Location (Global)
                                                val buttonPos = if (isToolsItem) toolsButtonPosition else tasksButtonPosition
                                                val buttonWidth = if (isToolsItem) toolsButtonWidth else tasksButtonWidth
                                                
                                                val centerX = buttonPos.x + buttonWidth / 2f
                                                val safePopupLeftGlobal = if (isToolsItem) {
                                                    (centerX - (popupTotalWidth / 2f)).coerceIn(16f, with(density) { (screenWidth - 16.dp).toPx() } - popupTotalWidth)
                                                } else {
                                                    // For tasks, shift it slightly right from the button center
                                                    (centerX - (popupTotalWidth / 2f) + with(density) { 24.dp.toPx() })
                                                        .coerceIn(with(density){ 32.dp.toPx() }, with(density) { (screenWidth - 16.dp).toPx() } - popupTotalWidth)
                                                }
                                                
                                                val popupTopGlobal = buttonPos.y - (if (isToolsItem) popupHeight else tasksPopupTotalHeight) - with(density) { 16.dp.toPx() }

                                                // 2. Touch Location (Global)
                                                val touchGlobalX = buttonPos.x + dragCurrentPosition.x
                                                val touchGlobalY = buttonPos.y + dragCurrentPosition.y
                                                
                                                // 3. Relative (Local to Popup)
                                                val relX = touchGlobalX - safePopupLeftGlobal
                                                val relY = touchGlobalY - popupTopGlobal

                                                val newIdx = if (isToolsItem) {
                                                    if (relY > -100 && relY < popupHeight + 100 && relX >= -50 && relX <= popupTotalWidth + 50) {
                                                        (relX / popupItemWidth).toInt().coerceIn(0, itemCount - 1)
                                                    } else {
                                                        -1
                                                    }
                                                } else {
                                                    if (relX > -50 && relX < popupTotalWidth + 50 && relY >= -50 && relY <= tasksPopupTotalHeight + 50) {
                                                        ((relY - with(density){8.dp.toPx()}) / tasksPopupItemHeight).toInt().coerceIn(0, itemCount - 1)
                                                    } else {
                                                        -1
                                                    }
                                                }

                                                val currentHighlighted = if (isToolsItem) highlightedIndex else tasksHighlightedIndex
                                                if (newIdx != currentHighlighted) {
                                                    if (isToolsItem) {
                                                        highlightedIndex = newIdx
                                                    } else {
                                                        tasksHighlightedIndex = newIdx
                                                    }
                                                    if (newIdx >= 0) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                            } else if (event.type == PointerEventType.Release) {
                                                break // Stop dragging
                                            }
                                        }
                                    }

                                    // 3. Handle Release
                                    if (isToolsItem) {
                                        showQuickAccess = false // Hide drag overlay
                                    } else {
                                        showTasksQuickAccess = false
                                    }

                                    if (isLongPress) {
                                        val currentHighlighted = if (isToolsItem) highlightedIndex else tasksHighlightedIndex
                                        if (currentHighlighted in 0 until itemCount) {
                                            // Selected item -> Navigate
                                            if (isToolsItem) {
                                                navController.navigate(ToolsRoute(initialTab = currentHighlighted)) {
                                                    popUpTo(HomeRoute) { saveState = true }
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                if (currentHighlighted < quickListItems.size) {
                                                    val filterId = quickListItems[currentHighlighted]
                                                    val filter = getTaskQuickAccessItem(filterId, folders, tags, categories, filters).filter
                                                    onTaskFilterSelected(filter)
                                                }
                                            }
                                        } else {
                                            // Released without selection -> Check Latch Mode
                                            if (latchMode) {
                                                if (isToolsItem) showLatchedPopup = true else showTasksLatchedPopup = true
                                            }
                                        }
                                        if (isToolsItem) highlightedIndex = -1 else tasksHighlightedIndex = -1
                                    } else if (isTap) {
                                        // Short tap -> Open Tools Main (Default Tab)
                                        if (isToolsItem) {
                                            navController.navigate(ToolsRoute()) {
                                                popUpTo(HomeRoute) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        } else {
                                            navController.navigate(TasksRoute) {
                                                popUpTo(HomeRoute) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                }
                            },
                    )
                } else {
                    // ============================================================
                    // Normal navigation items (non-Tools)
                    // ============================================================
                    val onClick = remember(item.route) {
                        {
                            navController.navigate(item.route) {
                                popUpTo(HomeRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    NavigationBarItem(
                        selected = selected,
                        onClick = { if (!selected) onClick() },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.12f,
                            ),
                        ),
                    )
                }
            }

            // ============================================================
            // «⋯» — popup menu (продолжение панели)
            // ============================================================
            NavigationBarItem(
                selected = false,
                onClick = { showMoreMenu = true },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "Ещё",
                    )

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text("Поиск")
                                }
                            },
                            onClick = {
                                showMoreMenu = false
                                // TODO: navigate to search
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text("Настройки")
                                }
                            },
                            onClick = {
                                showMoreMenu = false
                                onNavigateToSettings()
                            },
                        )
                    }
                },
                label = {
                    Text(
                        text = "Ещё",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ),
            )
        }

        // ============================================================
        // ============================================================
        // Quick Access Popup — Logic for positioning & Latch Mode
        // ============================================================
        
        // Calculate centered position
        val toolsCenterX = toolsButtonPosition.x + toolsButtonWidth / 2f
        
        // Ensure popup doesn't bleed off screen edges
        // We want it centered on toolsCenterX, but clamped.
        val safeToolsPopupLeft = (toolsCenterX - (toolsPopupTotalWidth / 2f))
            .coerceIn(16f, with(density) { (screenWidth - 16.dp).toPx() } - toolsPopupTotalWidth)

        // Unified Popup Logic (101% Phase 2)
        val isToolsPopupVisible = showQuickAccess || showLatchedPopup
        
        if (isToolsPopupVisible) {
            // FIX: Calculate offset RELATIVE to the Anchor Box
            val offsetX = safeToolsPopupLeft - startBoxPosition.x
            val offsetY = (toolsButtonPosition.y - popupHeight - with(density){ 16.dp.toPx() }) - startBoxPosition.y

            androidx.compose.ui.window.Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = offsetX.roundToInt(),
                    y = offsetY.roundToInt()
                ),
                onDismissRequest = { 
                    if (showLatchedPopup) showLatchedPopup = false 
                },
                properties = androidx.compose.ui.window.PopupProperties(
                    focusable = showLatchedPopup, // Only focusable (interactive) in Latch Mode
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    clippingEnabled = false,
                )
            ) {
                 ToolsQuickAccessPopup(
                    visible = true,
                    highlightedIndex = if (showLatchedPopup) -1 else highlightedIndex, 
                    onItemClick = if (showLatchedPopup) { index ->
                        val tab = index
                        navController.navigate(ToolsRoute(initialTab = tab)) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = false // Explicitly do NOT restore state for Quick Access
                        }
                        showLatchedPopup = false
                    } else null,
                    modifier = Modifier,
                )
            }
        }

        // ============================================================
        // Tasks Quick Access Popup
        // ============================================================
        val tasksPopupFixedTotalWidth = with(density) { 200.dp.toPx() }
        val tasksPopupItemHeight = with(density) { 40.dp.toPx() }
        val tasksPopupMaxHeight = with(density) { 400.dp.toPx() }
        val calculatedTasksHeight = tasksPopupItemHeight * tasksItemCount + with(density) { 16.dp.toPx() }
        val tasksPopupTotalHeight = calculatedTasksHeight.coerceAtMost(tasksPopupMaxHeight)

        val tasksCenterX = tasksButtonPosition.x + tasksButtonWidth / 2f
        // Shift a bit to the right (+24dp from center alignment), minimum 32dp from screen edge
        val safeTasksPopupLeft = (tasksCenterX - (tasksPopupFixedTotalWidth / 2f) + with(density){ 24.dp.toPx() })
            .coerceIn(with(density){ 32.dp.toPx() }, with(density) { (screenWidth - 16.dp).toPx() } - tasksPopupFixedTotalWidth)

        val isTasksPopupVisible = showTasksQuickAccess || showTasksLatchedPopup
        
        if (isTasksPopupVisible && quickListItems.isNotEmpty()) {
            val offsetX = safeTasksPopupLeft - startBoxPosition.x
            val offsetY = (tasksButtonPosition.y - tasksPopupTotalHeight - with(density){ 16.dp.toPx() }) - startBoxPosition.y

            androidx.compose.ui.window.Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = offsetX.roundToInt(),
                    y = offsetY.roundToInt()
                ),
                onDismissRequest = { 
                    if (showTasksLatchedPopup) showTasksLatchedPopup = false 
                },
                properties = androidx.compose.ui.window.PopupProperties(
                    focusable = showTasksLatchedPopup,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    clippingEnabled = false,
                )
            ) {
                 TasksQuickAccessPopup(
                     visible = true,
                     highlightedIndex = if (showTasksLatchedPopup) -1 else tasksHighlightedIndex,
                     quickListItems = quickListItems,
                     folders = folders,
                     tags = tags,
                     categories = categories,
                     filters = filters,
                     onItemClick = if (showTasksLatchedPopup) { index ->
                         if (index < quickListItems.size) {
                             val filterId = quickListItems[index]
                             val filter = getTaskQuickAccessItem(filterId, folders, tags, categories, filters).filter
                             onTaskFilterSelected(filter)
                         }
                         showTasksLatchedPopup = false
                     } else null
                 )
            }
        }
    }
}

// ============================================================
// Navigation Items (7 основных)
// ============================================================

private data class BottomNavItem(
    val route: Any,
    val icon: ImageVector,
    val label: String,
)

private val bottomNavItems = listOf(
    BottomNavItem(HomeRoute, Icons.Outlined.Home, "Главная"),
    BottomNavItem(TasksRoute, Icons.Outlined.CheckCircle, "Задачи"),
    BottomNavItem(NotesRoute, Icons.Outlined.StickyNote2, "Заметки"),
    BottomNavItem(CalendarRoute, Icons.Outlined.CalendarMonth, "Календ."),
    BottomNavItem(PlannerRoute, Icons.Outlined.ViewTimeline, "Планер"),
    BottomNavItem(ToolsRoute(), Icons.Outlined.Construction, "Инстр."),
    BottomNavItem(MatrixRoute, Icons.Outlined.GridView, "Матрица"),
)
