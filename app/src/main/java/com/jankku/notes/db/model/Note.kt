package com.jankku.notes.db.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "notes")
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val body: String,
    val createdOn: Long,
    val editedOn: Long?,
    @ColumnInfo(defaultValue = "0") val pinned: Boolean,
) : Parcelable {
    fun getTruncatedBody(): String {
        val maxLength = if (body.length >= 300) 300 else body.length
        return this.body.substring(0, maxLength)
    }
}
