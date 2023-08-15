package com.adarsh.memeshareapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [MemeEntity::class], version = 2)
abstract class MemeDatabase : RoomDatabase() {
    abstract fun memeDao(): MemeDao
}
