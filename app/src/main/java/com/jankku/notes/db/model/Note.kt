package com.jankku.notes.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jankku.notes.util.Constants.BODY_MAX_LENGTH

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val body: String,
    val createdOn: Long,
    val editedOn: Long?,
    val position: Int
) {
    fun getTruncatedBody(): String {
        val maxLength = if (body.length >= BODY_MAX_LENGTH) BODY_MAX_LENGTH else body.length
        return this.body.substring(0, maxLength)
    }
}