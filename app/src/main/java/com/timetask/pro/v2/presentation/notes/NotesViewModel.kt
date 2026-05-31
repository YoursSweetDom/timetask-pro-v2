package com.timetask.pro.v2.presentation.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.entity.NoteColor
import com.timetask.pro.v2.data.local.db.entity.NoteEntity
import com.timetask.pro.v2.data.repository.NoteRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository.getInstance(application)

    // ============================================================
    // Search
    // ============================================================

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ============================================================
    // Notes — реактивный список, переключается между search и all
    // ============================================================

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<ImmutableList<NoteEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllNotes()
            } else {
                repository.searchNotes(query)
            }
        }
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    // ============================================================
    // UI State
    // ============================================================

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

    // ============================================================
    // Actions
    // ============================================================

    fun showAddSheet() { _showAddSheet.value = true }
    fun hideAddSheet() { _showAddSheet.value = false }

    fun search(query: String) { _searchQuery.value = query }

    fun addNote(title: String, content: String, color: NoteColor) {
        viewModelScope.launch {
            repository.addNote(title = title, content = content, color = color)
            _showAddSheet.value = false
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun pinNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.pinNote(note.id, !note.isPinned)
        }
    }

    fun changeColor(note: NoteEntity, color: NoteColor) {
        viewModelScope.launch {
            repository.changeColor(note.id, color)
        }
    }
}
