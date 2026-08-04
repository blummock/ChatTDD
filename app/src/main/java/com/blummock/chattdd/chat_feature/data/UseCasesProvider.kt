package com.blummock.chattdd.chat_feature.data

import android.content.Context
import androidx.room.Room
import com.blummock.chattdd.chat_feature.data.local.MessagesDatabase
import com.blummock.chattdd.chat_feature.data.mappers.ExceptionMapper
import com.blummock.chattdd.chat_feature.data.mappers.MessagesMapper
import com.blummock.chattdd.chat_feature.data.remote.api.MessagesApiImpl
import com.blummock.chattdd.chat_feature.data.repositories.MessagesRepositoryImpl
import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.UserInfo
import com.blummock.chattdd.chat_feature.domain.repositories.UserInfoRepository
import com.blummock.chattdd.chat_feature.domain.use_cases.ObserveMessagesUseCase
import com.blummock.chattdd.chat_feature.domain.use_cases.SendMessageUseCase
import kotlinx.coroutines.Dispatchers
import kotlin.LazyThreadSafetyMode.NONE

class UseCasesProvider(context: Context) {

    private val db by lazy(NONE) {
        Room.inMemoryDatabaseBuilder(context.applicationContext, MessagesDatabase::class.java).allowMainThreadQueries()
            .build()
    }
    private val messagesDao by lazy(NONE) {
        db.getMessagesDao()
    }
    private val messagesApi by lazy(NONE) { MessagesApiImpl() }
    private val userInfoRepository by lazy {
        object : UserInfoRepository {
            override suspend fun getUserInfo(): ChatResult<UserInfo> {
                return ChatResult.Success(UserInfo("suavitate"))
            }

        }
    }
    private val mapper by lazy(NONE) { MessagesMapper() }
    private val exceptionMapper by lazy(NONE) { ExceptionMapper() }
    private val repository by lazy(NONE) {
        MessagesRepositoryImpl(
            messagesDao = messagesDao,
            messagesApi = messagesApi,
            userInfoRepository = userInfoRepository,
            mapper = mapper,
            exceptionMapper = exceptionMapper,
            dispatcher = Dispatchers.IO

        )
    }

    fun observeMessagesUseCase() = ObserveMessagesUseCase(repository)

    fun sendMessageUseCase() = SendMessageUseCase(repository)
}