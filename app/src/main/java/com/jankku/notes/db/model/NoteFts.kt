package com.jankku.notes.db.model

import androidx.room.*

@Fts4(contentEntity = Note::class, tokenizer = FtsOptions.TOKENIZER_PORTER)
@Entity(tableName = "notes_fts")
data class NoteFts(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    val title: String,
    val body: String,
)
