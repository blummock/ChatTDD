package com.blummock.chattdd.chat_feature.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blummock.chattdd.chat_feature.data.local.dao.MessagesDao
import com.blummock.chattdd.chat_feature.data.local.models.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class MessagesDatabase : RoomDatabase() {
    abstract fun getMessagesDao(): MessagesDao
}