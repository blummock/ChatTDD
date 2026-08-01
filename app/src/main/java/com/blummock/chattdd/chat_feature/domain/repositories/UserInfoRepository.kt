package com.blummock.chattdd.chat_feature.domain.repositories

import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.UserInfo

interface UserInfoRepository {

    suspend fun getUserInfo(): ChatResult<UserInfo>
}