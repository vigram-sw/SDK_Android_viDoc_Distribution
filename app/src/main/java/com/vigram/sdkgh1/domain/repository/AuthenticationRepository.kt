package com.vigram.sdkgh1.domain.repository

import android.content.Context
import com.vigram.sdk.Authentication.AuthenticationResult
import kotlinx.coroutines.flow.Flow

interface AuthenticationRepository {

    fun init(context: Context, token: String)

    fun check(): Flow<AuthenticationResult>
}