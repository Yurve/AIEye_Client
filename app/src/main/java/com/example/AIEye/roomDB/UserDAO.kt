package com.example.AIEye.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDAO {
    @Query("SELECT * FROM ID")
    fun getAll(): List<ID?>?

    //id 삽입
    @Insert
    fun insert(id: ID)

    //기존의 id 삭제
    @Delete
    fun delete(id: ID)
}