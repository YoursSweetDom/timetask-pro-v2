package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timetask.pro.v2.data.local.db.entity.NoteColor
import com.timetask.pro.v2.data.local.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // ============================================================
    // Queries (Flow-based — реактивные)
    // ============================================================

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, `order` ASC, updatedAt DESC")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getById(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY isPinned DESC, `order` ASC, updatedAt DESC")
    fun getByFolder(folderId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE color = :color ORDER BY isPinned DESC, updatedAt DESC")
    fun getByColor(color: NoteColor): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun search(query: String): Flow<List<NoteEntity>>

    // ============================================================
    // Mutations
    // ============================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePinned(id: Long, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET color = :color, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateColor(id: Long, color: NoteColor, updatedAt: Long = System.currentTimeMillis())
}
