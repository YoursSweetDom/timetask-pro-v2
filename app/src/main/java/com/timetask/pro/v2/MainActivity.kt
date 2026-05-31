package com.timetask.pro.v2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timetask.pro.v2.data.repository.FolderRepository
import com.timetask.pro.v2.data.repository.TagRepository
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.repository.CategoryRepository
import com.timetask.pro.v2.data.repository.FilterRepository
import com.timetask.pro.v2.presentation.calendar.CalendarViewModel
import com.timetask.pro.v2.presentation.home.HomeViewModel
import com.timetask.pro.v2.presentation.matrix.MatrixViewModel
import com.timetask.pro.v2.presentation.navigation.AddFolderDialog
import com.timetask.pro.v2.presentation.navigation.AppNavHost
import com.timetask.pro.v2.presentation.navigation.BottomNavBar
import com.timetask.pro.v2.presentation.navigation.DrawerContent
import com.timetask.pro.v2.presentation.navigation.HomeRoute
import com.timetask.pro.v2.presentation.navigation.SettingsRoute
import com.timetask.pro.v2.presentation.navigation.TemplatesRoute
import com.timetask.pro.v2.presentation.navigation.ToolsRoute
import com.timetask.pro.v2.presentation.navigation.TrashRoute
import com.timetask.pro.v2.presentation.navigation.CompletedRoute
import com.timetask.pro.v2.presentation.navigation.WontDoRoute
import com.timetask.pro.v2.presentation.navigation.TasksRoute
import com.timetask.pro.v2.presentation.notes.NotesViewModel
import com.timetask.pro.v2.presentation.settings.SettingsViewModel
import com.timetask.pro.v2.presentation.tasks.TasksViewModel
import com.timetask.pro.v2.ui.theme.LocalWindowSizeClass
import com.timetask.pro.v2.ui.theme.TimeTaskProV2Theme
import com.timetask.pro.v2.presentation.util.LocalTopBarState
import com.timetask.pro.v2.presentation.util.TopBarState
import com.timetask.pro.v2.presentation.util.LocalLongPressDelay
import com.timetask.pro.v2.presentation.util.LocalLatchMode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import com.timetask.pro.v2.domain.model.TaskFilter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.timetask.pro.v2.service.TimerNotificationHelper
import com.timetask.pro.v2.presentation.sidebar.components.CreateTagSheet
import com.timetask.pro.v2.presentation.sidebar.components.CreateCategorySheet
import com.timetask.pro.v2.presentation.sidebar.components.CreateFilterSheet

val LocalSnackbarHostState = compositionLocalOf<androidx.compose.material3.SnackbarHostState> {
    error("No SnackbarHostState provided")
}

class MainActivity : ComponentActivity() {

    // ============================================================
    // Activity-scoped ViewModels — persist across tab switches
    // for instant navigation (no empty-state flash)
    // ============================================================
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val tasksViewModel: TasksViewModel by viewModels()
    private val notesViewModel: NotesViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()
    private val matrixViewModel: MatrixViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Обработать intent (если пришли из уведомления)
        handleNavigationIntent(intent)

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            val windowSizeClass = calculateWindowSizeClass(this)
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            // Навигация из уведомления → вкладка Таймеров
            LaunchedEffect(pendingNavigation) {
                if (pendingNavigation == TimerNotificationHelper.NAVIGATE_TIMERS) {
                    pendingNavigation = null
                    navController.navigate(ToolsRoute(initialTab = 0)) {
                        popUpTo<HomeRoute> { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }

            // Folders
            val folderRepository = remember { FolderRepository.getInstance(applicationContext) }
            val tagRepository = remember { TagRepository.getInstance(applicationContext) }
            val categoryRepository = remember { CategoryRepository.getInstance(applicationContext) }
            val filterRepository = remember { FilterRepository.getInstance(applicationContext) }

            val folders by remember {
                folderRepository.getAllFolders().map { it.toImmutableList() }
            }.collectAsState(initial = persistentListOf())
            val tags by remember {
                tagRepository.getAllTags().map { it.toImmutableList() }
            }.collectAsState(initial = persistentListOf())
            val categories by remember {
                categoryRepository.getAllCategories().map { it.toImmutableList() }
            }.collectAsState(initial = persistentListOf())
            val filters by remember {
                filterRepository.getAllFilters().map { it.toImmutableList() }
            }.collectAsState(initial = persistentListOf())

            var currentFilter: TaskFilter? by remember { mutableStateOf(TaskFilter.Inbox) }
            var showAddFolderDialog by remember { mutableStateOf(false) }
            var folderToEdit by remember { mutableStateOf<FolderEntity?>(null) }
            var showCreateTagSheet by remember { mutableStateOf(false) }
            var tagToEdit by remember { mutableStateOf<TagEntity?>(null) }
            var showCreateCategorySheet by remember { mutableStateOf(false) }
            var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
            var showCreateFilterSheet by remember { mutableStateOf(false) }
            var filterToEdit by remember { mutableStateOf<FilterEntity?>(null) }
            
            val rawQuickListItems by settingsViewModel.quickListItems.collectAsStateWithLifecycle()
            val quickListItems = remember(rawQuickListItems, folders, tags, categories, filters) {
                rawQuickListItems.filter { id ->
                    when {
                        id in listOf("ALL", "TODAY", "TOMORROW", "NEXT_7_DAYS", "INBOX") -> true
                        id.startsWith("FOLDER_") -> folders.any { it.id == (id.removePrefix("FOLDER_").toLongOrNull() ?: 0L) }
                        id.startsWith("TAG_") -> tags.any { it.id == (id.removePrefix("TAG_").toLongOrNull() ?: 0L) }
                        id.startsWith("CATEGORY_") -> categories.any { it.id == (id.removePrefix("CATEGORY_").toLongOrNull() ?: 0L) }
                        id.startsWith("FILTER_") -> filters.any { it.id == (id.removePrefix("FILTER_").toLongOrNull() ?: 0L) }
                        else -> false
                    }
                }
            }
            
            val longPressDelayMs by settingsViewModel.longPressDelayMs.collectAsStateWithLifecycle()
            val latchMode by settingsViewModel.latchMode.collectAsStateWithLifecycle()

            val topBarState = remember { TopBarState() }
            
            DisposableEffect(navController) {
                val listener = androidx.navigation.NavController.OnDestinationChangedListener { _, _, _ ->
                    topBarState.title = null
                    topBarState.subtitle = null
                    topBarState.navigationIcon = null
                    topBarState.actions = {}
                }
                navController.addOnDestinationChangedListener(listener)
                onDispose {
                    navController.removeOnDestinationChangedListener(listener)
                }
            }
            
            val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
            
            val viewConfiguration = LocalViewConfiguration.current
            val customViewConfiguration = remember(viewConfiguration, longPressDelayMs) {
                object : ViewConfiguration by viewConfiguration {
                    override val longPressTimeoutMillis: Long
                        get() = longPressDelayMs.toLong()
                }
            }

            CompositionLocalProvider(
                LocalWindowSizeClass provides windowSizeClass,
                LocalTopBarState provides topBarState,
                LocalSnackbarHostState provides snackbarHostState,
                LocalLongPressDelay provides longPressDelayMs,
                LocalLatchMode provides latchMode,
                LocalViewConfiguration provides customViewConfiguration,
            ) {
                TimeTaskProV2Theme(themeMode = themeMode) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            val currentBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = currentBackStackEntry?.destination?.route?.let { route ->
                                when {
                                    route.contains("CompletedRoute") -> "completed"
                                    route.contains("WontDoRoute") -> "wont_do"
                                    route.contains("TrashRoute") -> "trash"
                                    route.contains("TemplatesRoute") -> "templates"
                                    else -> null
                                }
                            }

                            DrawerContent(
                                folders = folders,
                                tags = tags,
                                categories = categories,
                                filters = filters,
                                quickListItems = quickListItems,
                                currentFilter = currentFilter,
                                selectedFooterItem = currentRoute,
                                onFilterSelected = { filter ->
                                    currentFilter = filter
                                    scope.launch { drawerState.close() }
                                    // Make sure we actually go back to the tasks screen if we are in Trash/Completed
                                    navController.navigate(TasksRoute) {
                                        popUpTo(HomeRoute) { saveState = true }
                                        launchSingleTop = true
                                    }
                                },
                                onCreateFolder = {
                                    folderToEdit = null
                                    showAddFolderDialog = true
                                },
                                onAddTemplate = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(TemplatesRoute)
                                },
                                onAddTag = {
                                    tagToEdit = null
                                    showCreateTagSheet = true
                                },
                                onAddCategory = {
                                    categoryToEdit = null
                                    showCreateCategorySheet = true
                                },
                                onAddFilter = {
                                    filterToEdit = null
                                    showCreateFilterSheet = true
                                },
                                // CRUD callbacks for context menu
                                onEditFolder = { folder ->
                                    folderToEdit = folder
                                    showAddFolderDialog = true
                                },
                                onDeleteFolder = { folder ->
                                    scope.launch { folderRepository.deleteFolderById(folder.id) }
                                },
                                onPinFolder = { folder ->
                                    scope.launch { folderRepository.updateFolder(folder.copy(isPinned = !folder.isPinned)) }
                                },
                                onAddSubfolder = { parentFolder ->
                                    // TODO: Open AddFolderDialog with parentId
                                },
                                onEditTag = { tag ->
                                    tagToEdit = tag
                                    showCreateTagSheet = true
                                },
                                onDeleteTag = { tag ->
                                    scope.launch { tagRepository.deleteTag(tag) }
                                },
                                onPinTag = { tag ->
                                    scope.launch { tagRepository.updateTag(tag.copy(isPinned = !tag.isPinned)) }
                                },
                                onEditCategory = { category ->
                                    categoryToEdit = category
                                    showCreateCategorySheet = true
                                },
                                onDeleteCategory = { category ->
                                    scope.launch { categoryRepository.deleteCategory(category) }
                                },
                                onEditFilter = { filter ->
                                    filterToEdit = filter
                                    showCreateFilterSheet = true
                                },
                                onDeleteFilter = { filter ->
                                    scope.launch { filterRepository.deleteFilter(filter) }
                                },
                                onPinFilter = { filter ->
                                    scope.launch { filterRepository.updateFilter(filter.copy(isPinned = !filter.isPinned)) }
                                },
                                onSaveQuickListItems = { items ->
                                    settingsViewModel.setQuickListItems(items)
                                },
                                onReorderTags = { reordered ->
                                    scope.launch { tagRepository.updateTagOrders(reordered) }
                                },
                                onReorderCategories = { reordered ->
                                    scope.launch { categoryRepository.updateCategoryOrders(reordered) }
                                },
                                onReorderFilters = { reordered ->
                                    scope.launch { filterRepository.updateFilterOrders(reordered) }
                                },
                                onReorderFolders = { reordered ->
                                    scope.launch { folderRepository.updateFolderOrders(reordered) }
                                },
                                onTrashClick = {
                                    currentFilter = null // Reset filter so sidebar doesn't highlight old item
                                    scope.launch { drawerState.close() }
                                    navController.navigate(TrashRoute) {
                                        popUpTo(HomeRoute) { saveState = true }
                                        launchSingleTop = true
                                    }
                                },
                                onCompletedClick = {
                                    currentFilter = null
                                    scope.launch { drawerState.close() }
                                    navController.navigate(CompletedRoute) {
                                        popUpTo(HomeRoute) { saveState = true }
                                        launchSingleTop = true
                                    }
                                },
                                onWontDoClick = {
                                    currentFilter = null
                                    scope.launch { drawerState.close() }
                                    navController.navigate(WontDoRoute) {
                                        popUpTo(HomeRoute) { saveState = true }
                                        launchSingleTop = true
                                    }
                                },
                                onTemplatesClick = {
                                    currentFilter = null
                                    scope.launch { drawerState.close() }
                                    navController.navigate(TemplatesRoute) {
                                        popUpTo(HomeRoute) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        },
                    ) {
                        Scaffold(
                            snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
                            topBar = {
                                TopAppBar(
                                    title = {
                                        if (topBarState.title != null) {
                                            topBarState.title?.let { it() }
                                        } else {
                                            // Живые часы с секундами
                                            var currentTime by remember { mutableStateOf("") }
                                            LaunchedEffect(Unit) {
                                                while (true) {
                                                    val now = java.util.Calendar.getInstance()
                                                    currentTime = String.format(
                                                        "%02d:%02d:%02d",
                                                        now.get(java.util.Calendar.HOUR_OF_DAY),
                                                        now.get(java.util.Calendar.MINUTE),
                                                        now.get(java.util.Calendar.SECOND)
                                                    )
                                                    kotlinx.coroutines.delay(500L)
                                                }
                                            }
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    currentTime,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                )
                                                
                                                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                                                val isTasksRoute = currentBackStackEntry?.destination?.route?.contains("TasksRoute") == true
                                                
                                                // Only show subtitle if we are not overriding the title (e.g., in Multi-Select Mode)
                                                if (topBarState.title == null) {
                                                    if (isTasksRoute) {
                                                        val currentFilterTitle by tasksViewModel.currentFilterTitle.collectAsStateWithLifecycle()
                                                        Text(
                                                            text = currentFilterTitle,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    } else if (topBarState.subtitle != null) {
                                                        topBarState.subtitle?.let { it() }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    navigationIcon = {
                                        if (topBarState.navigationIcon != null) {
                                            topBarState.navigationIcon?.let { it() }
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    scope.launch { drawerState.open() }
                                                },
                                            ) {
                                                Icon(
                                                    Icons.Filled.Menu,
                                                    contentDescription = "Меню",
                                                )
                                            }
                                        }
                                    },
                                    actions = {
                                        topBarState.actions(this)
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                )
                            },
                            bottomBar = {
                                BottomNavBar(
                                    navController = navController,
                                    quickListItems = quickListItems,
                                    folders = folders,
                                    tags = tags,
                                    categories = categories,
                                    filters = filters,
                                    onTaskFilterSelected = { filter ->
                                        currentFilter = filter
                                        navController.navigate(TasksRoute) {
                                            popUpTo(HomeRoute) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate(SettingsRoute)
                                    },
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.background,
                        ) { innerPadding ->
                            AppNavHost(
                                navController = navController,
                                settingsViewModel = settingsViewModel,
                                homeViewModel = homeViewModel,
                                tasksViewModel = tasksViewModel,
                                notesViewModel = notesViewModel,
                                calendarViewModel = calendarViewModel,
                                matrixViewModel = matrixViewModel,
                                currentFilter = currentFilter,
                                folders = folders,
                                modifier = Modifier.padding(innerPadding),
                            )
                        }
                    }

                    // Add folder dialog
                    if (showAddFolderDialog) {
                        AddFolderDialog(
                            onDismiss = { 
                                showAddFolderDialog = false 
                                folderToEdit = null
                            },
                            initialName = folderToEdit?.name ?: "",
                            initialEmoji = folderToEdit?.emoji ?: "",
                            initialColorHex = folderToEdit?.color,
                            onAdd = { name, emoji, color ->
                                scope.launch {
                                    folderToEdit?.let { folder ->
                                        folderRepository.updateFolder(folder.copy(name = name, emoji = emoji, color = color))
                                    } ?: run {
                                        folderRepository.addFolder(name = name, emoji = emoji, color = color)
                                    }
                                    showAddFolderDialog = false
                                    folderToEdit = null
                                }
                            },
                        )
                    }

                    // Create Tag sheet
                    if (showCreateTagSheet) {
                        CreateTagSheet(
                            onDismiss = { 
                                showCreateTagSheet = false 
                                tagToEdit = null
                            },
                            initialName = tagToEdit?.name ?: "",
                            initialEmoji = tagToEdit?.emoji ?: "",
                            initialColorHex = tagToEdit?.color,
                            onAdd = { name, emoji, color ->
                                scope.launch {
                                    tagToEdit?.let { tag ->
                                        tagRepository.updateTag(tag.copy(name = name, emoji = emoji, color = color))
                                    } ?: run {
                                        tagRepository.addTag(name = name, emoji = emoji, color = color)
                                    }
                                    showCreateTagSheet = false
                                    tagToEdit = null
                                }
                            }
                        )
                    }

                    // Create Category sheet
                    if (showCreateCategorySheet) {
                        CreateCategorySheet(
                            onDismiss = { 
                                showCreateCategorySheet = false 
                                categoryToEdit = null
                            },
                            initialName = categoryToEdit?.name ?: "",
                            initialIcon = categoryToEdit?.icon ?: "",
                            initialColorHex = categoryToEdit?.color,
                            onAdd = { name, icon, color ->
                                scope.launch {
                                    categoryToEdit?.let { category ->
                                        categoryRepository.updateCategory(category.copy(name = name, icon = icon, color = color))
                                    } ?: run {
                                        categoryRepository.addCategory(name = name, icon = icon, color = color)
                                    }
                                    showCreateCategorySheet = false
                                    categoryToEdit = null
                                }
                            }
                        )
                    }

                    // Create Filter sheet
                    if (showCreateFilterSheet) {
                        CreateFilterSheet(
                            onDismiss = { 
                                showCreateFilterSheet = false 
                                filterToEdit = null
                            },
                            initialName = filterToEdit?.name ?: "",
                            initialIcon = filterToEdit?.icon ?: "",
                            initialLogicJson = filterToEdit?.logicJson ?: "",
                            onAdd = { name, icon, emoji, logicJson ->
                                scope.launch {
                                    filterToEdit?.let { filter ->
                                        filterRepository.updateFilter(filter.copy(name = name, icon = icon, emoji = emoji, logicJson = logicJson))
                                    } ?: run {
                                        filterRepository.addFilter(name = name, icon = icon, emoji = emoji, logicJson = logicJson)
                                    }
                                    showCreateFilterSheet = false
                                    filterToEdit = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Вызывается когда Activity уже существует и приходит новый Intent
     * (например, при клике на уведомление с FLAG_ACTIVITY_SINGLE_TOP).
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    /**
     * Обрабатывает навигационный extra из уведомления.
     * Если `navigate_to_tab == "timers"` → переходим на ToolsRoute(initialTab = 0).
     */
    private var pendingNavigation: String? = null

    private fun handleNavigationIntent(intent: Intent?) {
        val navigateTo = intent?.getStringExtra(TimerNotificationHelper.EXTRA_NAVIGATE_TO)
        if (navigateTo == TimerNotificationHelper.NAVIGATE_TIMERS) {
            pendingNavigation = navigateTo
            // Clear extra to prevent re-navigation on configuration change
            intent?.removeExtra(TimerNotificationHelper.EXTRA_NAVIGATE_TO)
        }
    }
}