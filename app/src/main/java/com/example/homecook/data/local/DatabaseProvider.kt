package com.example.homecook.data.local


import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var INSTANCE: HomeCookDatabase? = null

    fun get(context: Context): HomeCookDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                HomeCookDatabase::class.java,
                "homecook.db"
            ).build().also { INSTANCE = it }
        }
    }
}
