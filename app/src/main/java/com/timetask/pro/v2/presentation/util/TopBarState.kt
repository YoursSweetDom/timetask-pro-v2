package com.timetask.pro.v2.presentation.util

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TopBarState {
    var actions: @Composable RowScope.() -> Unit by mutableStateOf({})
    var title: (@Composable () -> Unit)? by mutableStateOf(null)
    var subtitle: (@Composable () -> Unit)? by mutableStateOf(null)
    var navigationIcon: (@Composable () -> Unit)? by mutableStateOf(null)
}

val LocalTopBarState = compositionLocalOf { TopBarState() }
