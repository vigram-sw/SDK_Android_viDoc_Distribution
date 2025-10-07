package com.vigram.sdkgh1.data

import android.content.Context
import com.vigram.sdk.Authentication.Authentication
import com.vigram.sdk.Authentication.AuthenticationResult
import com.vigram.sdk.Vigram
import com.vigram.sdkgh1.domain.repository.AuthenticationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

object AuthenticationRepositoryImpl : AuthenticationRepository {

    @Volatile
    private var _auth: Authentication? = null

    private val authentication: Authentication
        get() = _auth
            ?: throw IllegalStateException("Authentication is not initialized")

    override fun init(context: Context, token: String) {
        if (_auth == null) {
            synchronized(this) {
                if (_auth == null) {
                    _auth = Vigram.init(context, token)
                }
            }
        }
    }

    override fun check(): Flow<AuthenticationResult> = channelFlow {
        authentication.check { result ->
            trySend(result).isSuccess
        }
        awaitClose {}
    }
}
