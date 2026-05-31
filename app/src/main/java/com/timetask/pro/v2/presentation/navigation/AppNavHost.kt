package com.timetask.pro.v2.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.timetask.pro.v2.presentation.calendar.CalendarScreen
import com.timetask.pro.v2.presentation.calendar.CalendarViewModel
import com.timetask.pro.v2.presentation.home.HomeScreen
import com.timetask.pro.v2.presentation.home.HomeViewModel
import com.timetask.pro.v2.presentation.matrix.MatrixScreen
import com.timetask.pro.v2.presentation.matrix.MatrixViewModel
import com.timetask.pro.v2.presentation.notes.NotesScreen
import com.timetask.pro.v2.presentation.notes.NotesViewModel
import com.timetask.pro.v2.presentation.planner.PlannerScreen
import com.timetask.pro.v2.presentation.settings.SettingsScreen
import com.timetask.pro.v2.presentation.settings.SettingsViewModel
import com.timetask.pro.v2.presentation.tasks.TaskDetailScreen
import com.timetask.pro.v2.presentation.tasks.TaskDetailViewModel
import com.timetask.pro.v2.presentation.tasks.TasksScreen
import com.timetask.pro.v2.presentation.tasks.TasksViewModel
import com.timetask.pro.v2.presentation.templates.TemplatesScreen
import com.timetask.pro.v2.presentation.templates.TemplatesViewModel
import com.timetask.pro.v2.presentation.templates.CreateTemplateSheet
import com.timetask.pro.v2.presentation.templates.CreateTemplateViewModel
import com.timetask.pro.v2.presentation.tools.ToolsScreen
import com.timetask.pro.v2.presentation.tasks.trash.TrashScreen
import com.timetask.pro.v2.presentation.tasks.completed.CompletedScreen
import com.timetask.pro.v2.presentation.tasks.wontdo.WontDoScreen
import com.timetask.pro.v2.domain.model.TaskFilter
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Navigation host for the app.
 *
 * Main tab ViewModels are Activity-scoped (created in MainActivity)
 * to keep data "hot" and ensure instant tab switching with no
 * empty-state flash.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    homeViewModel: HomeViewModel,
    tasksViewModel: TasksViewModel,
    notesViewModel: NotesViewModel,
    calendarViewModel: CalendarViewModel,
    matrixViewModel: MatrixViewModel,
    currentFilter: TaskFilter?,
    folders: ImmutableList<FolderEntity> = persistentListOf(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        // Smooth crossfade transitions for premium feel
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
    ) {
        composable<HomeRoute> {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToTasks = {
                    navController.navigate(TasksRoute) {
                        popUpTo(HomeRoute) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<TasksRoute> {
            tasksViewModel.setFilter(currentFilter ?: TaskFilter.Inbox)
            TasksScreen(
                viewModel = tasksViewModel,
                folders = folders,
                onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) },
            )
        }
        composable<NotesRoute> {
            NotesScreen(viewModel = notesViewModel)
        }
        composable<CalendarRoute> {
            CalendarScreen(viewModel = calendarViewModel)
        }
        composable<PlannerRoute> { PlannerScreen() }
        composable<ToolsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ToolsRoute>()
            ToolsScreen(initialTab = route.initialTab)
        }
        composable<MatrixRoute> {
            MatrixScreen(viewModel = matrixViewModel)
        }
        composable<SettingsRoute> {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable<TaskDetailRoute> {
            // TaskDetail stays navigation-scoped — each detail page is unique
            val detailViewModel: TaskDetailViewModel = viewModel()
            TaskDetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable<TemplatesRoute> {
            val templatesViewModel: TemplatesViewModel = viewModel()
            val createTemplateViewModel: com.timetask.pro.v2.presentation.templates.CreateTemplateViewModel = viewModel()
            
            var showCreateSheet by remember { mutableStateOf(false) }

            TemplatesScreen(
                viewModel = templatesViewModel,
                onBackClick = { navController.popBackStack() },
                onCreateTemplateClick = { showCreateSheet = true },
                onTemplateClick = { _ -> 
                    // TODO: implemented editing logic
                }
            )

            if (showCreateSheet) {
                CreateTemplateSheet(
                    viewModel = createTemplateViewModel,
                    onDismiss = { showCreateSheet = false }
                )
            }
        }
        composable<TrashRoute> {
            TrashScreen(
                onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) }
            )
        }
        composable<CompletedRoute> {
            CompletedScreen(
                onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) }
            )
        }
        composable<WontDoRoute> {
            WontDoScreen(
                onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) }
            )
        }
    }
}
