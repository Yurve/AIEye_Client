package com.example.AIEye.roomDB

import android.content.Context
import androidx.room.*

@Database(entities = [ID::class], version = 1)
abstract class RoomDB : RoomDatabase() {

    abstract fun userDAO(): UserDAO

    companion object {
        private var INSTANCE: RoomDB? = null

        //db 생성
        fun getInstance(context: Context): RoomDB? {
            if (INSTANCE == null) {
                INSTANCE =
                    Room.databaseBuilder(context.applicationContext, RoomDB::class.java, "ID.db")
                        .allowMainThreadQueries() //메인 쓰레드에서도 찾을 수 있게
                        .build()
            }
            return INSTANCE
        }
    }
}