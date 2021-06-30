package com.jankku.notes.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "createdOn") val createdOn: Long,
    @ColumnInfo(name = "editedOn") val editedOn: Long?
) {
    fun getTruncatedBody(): String {
        val maxLength = if (body.length >= 300) 300 else body.length
        return this.body.substring(0, maxLength)
    }
}