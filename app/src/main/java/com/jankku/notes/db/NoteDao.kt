package com.jankku.notes.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun getAll(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update(onConflict = OnConflictStrategy.REPLACE, entity = Note::class)
    suspend fun update(note: Note)

    @Query("UPDATE notes SET title=:title, body=:body, editedOn=:editedOn WHERE id=:id")
    suspend fun partialUpdate(id: Long, title: String, body: String, editedOn: Long)

    @Query("DELETE from notes WHERE id=:id")
    suspend fun delete(id: Long)
}