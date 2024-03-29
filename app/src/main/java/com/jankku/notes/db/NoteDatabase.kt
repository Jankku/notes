package com.jankku.notes.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.jankku.notes.db.model.Note
import com.jankku.notes.db.model.NoteFts

@Database(
    entities = [Note::class, NoteFts::class], version = 5, autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NoteDatabase? = null
        const val DATABASE_NAME: String = "note_database"

        fun getDatabase(
            context: Context
        ): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(Migration(4, 5) {
                    it.execSQL("INSERT INTO notes_fts(notes_fts) VALUES ('rebuild')")
                }).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        fun destroyInstance() {
            if (INSTANCE?.isOpen == true) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
