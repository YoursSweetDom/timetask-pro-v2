package com.timetask.pro.v2.data.repository

import android.content.Context
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.NoteColor
import com.timetask.pro.v2.data.local.db.entity.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository для заметок.
 * Обёртка над NoteDao с выполнением на Dispatchers.IO.
 */
class NoteRepository(context: Context) {

    private val noteDao = TimeTaskDatabase.getInstance(context).noteDao()

    // ============================================================
    // Queries (Flow — реактивные)
    // ============================================================

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAll()

    fun getNoteById(id: Long): Flow<NoteEntity?> = noteDao.getById(id)

    fun getNotesByFolder(folderId: Long): Flow<List<NoteEntity>> = noteDao.getByFolder(folderId)

    fun getNotesByColor(color: NoteColor): Flow<List<NoteEntity>> = noteDao.getByColor(color)

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.search(query)

    // ============================================================
    // Mutations (suspend — выполняются на IO)
    // ============================================================

    suspend fun addNote(
        title: String,
        content: String = "",
        color: NoteColor = NoteColor.DEFAULT,
    ): Long {
        return withContext(Dispatchers.IO) {
            noteDao.insert(
                NoteEntity(
                    title = title,
                    content = content,
                    color = color,
                )
            )
        }
    }

    suspend fun updateNote(note: NoteEntity) {
        withContext(Dispatchers.IO) {
            noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteNote(note: NoteEntity) {
        withContext(Dispatchers.IO) {
            noteDao.delete(note)
        }
    }

    suspend fun deleteNoteById(id: Long) {
        withContext(Dispatchers.IO) {
            noteDao.deleteById(id)
        }
    }

    suspend fun pinNote(id: Long, isPinned: Boolean) {
        withContext(Dispatchers.IO) {
            noteDao.updatePinned(id, isPinned)
        }
    }

    suspend fun changeColor(id: Long, color: NoteColor) {
        withContext(Dispatchers.IO) {
            noteDao.updateColor(id, color)
        }
    }

    companion object {
        @Volatile
        private var instance: NoteRepository? = null

        fun getInstance(context: Context): NoteRepository {
            return instance ?: synchronized(this) {
                instance ?: NoteRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
