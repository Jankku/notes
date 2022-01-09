package com.jankku.notes.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jankku.notes.db.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun getNotes(): LiveData<List<Note>>

    @Query("SELECT count(id) FROM notes")
    fun getNoteCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Query("UPDATE notes SET title=:title, body=:body, editedOn=:editedOn WHERE id=:id")
    suspend fun update(id: Long, title: String, body: String, editedOn: Long)

    @Query("DELETE from notes WHERE id=:id")
    suspend fun delete(id: Long)
}