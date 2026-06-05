package com.blummock.chattdd.chat_feature.core

import com.blummock.chattdd.chat_feature.domain.entity.ChatResult

interface UserInfoRepository {

    suspend fun getUserInfo(): ChatResult<UserInfo>
}