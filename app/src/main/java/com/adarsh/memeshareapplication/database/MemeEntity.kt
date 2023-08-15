package com.adarsh.memeshareapplication.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memes")
data class MemeEntity(
    @PrimaryKey
    val ups: Long,

    @ColumnInfo(name = "meme_image")
    val memeImage: String
)
