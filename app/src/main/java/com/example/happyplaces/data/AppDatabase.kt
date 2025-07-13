package com.example.happyplaces.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Annotates class to be a Room Database with a table (entity) of the Place class
@Database(entities = [Place::class], version = 1, exportSchema = false)
// exportSchema = false is okay for now, but for production apps, consider schema exportation.
abstract class AppDatabase : RoomDatabase() {

    // The database exposes DAOs through an abstract "getter" method for each @Dao.
    abstract fun placeDao(): PlaceDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "happy_place_database" // Name of your database file
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not covered in this basic example.
                    // For a real app, you'd want to implement proper migrations.
                    .fallbackToDestructiveMigration() // Use this for development for now
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
