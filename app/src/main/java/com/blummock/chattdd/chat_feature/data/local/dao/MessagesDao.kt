package com.blummock.chattdd.chat_feature.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.blummock.chattdd.chat_feature.data.local.models.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MessagesDao {

    @Query("SELECT * FROM messages")
    fun observeMessages(): Flow<List<MessageEntity>>

    @Transaction
    suspend fun clearAndInsertMessages(messages: List<MessageEntity>) {
        deleteAll()
        insert(messages)
    }

    @Insert(onConflict = REPLACE)
    suspend fun insert(messages: List<MessageEntity>)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}