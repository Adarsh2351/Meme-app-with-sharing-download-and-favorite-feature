package com.adarsh.memeshareapplication.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MemeDao {

    @Insert
    fun insertMeme(memeEntity: MemeEntity)

    @Delete
    fun deleteMeme(memeEntity: MemeEntity)

    @Query("SELECT * FROM memes")
    fun getAllMemes(): List<MemeEntity>

    @Query("SELECT * FROM memes WHERE ups = :memeId")
    fun getMemeById(memeId: String): MemeEntity
}