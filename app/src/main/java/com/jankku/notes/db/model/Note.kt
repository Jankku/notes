package com.jankku.notes.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jankku.notes.util.Constants.BODY_MAX_LENGTH

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "createdOn") val createdOn: Long,
    @ColumnInfo(name = "editedOn") val editedOn: Long?
) {
    fun getTruncatedBody(): String {
        val maxLength = if (body.length >= BODY_MAX_LENGTH) BODY_MAX_LENGTH else body.length
        return this.body.substring(0, maxLength)
    }
}