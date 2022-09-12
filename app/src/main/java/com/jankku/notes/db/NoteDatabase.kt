package com.jankku.notes.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jankku.notes.db.model.Note

@Database(
    entities = [Note::class], version = 3, autoMigrations = [
        AutoMigration(from = 2, to = 3)
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
                ).build()
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
