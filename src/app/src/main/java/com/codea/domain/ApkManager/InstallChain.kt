package com.codea.domain.ApkManager

import androidx.compose.runtime.*

interface InstallChain {
    suspend fun execute(session: InstallSession): Result<InstallSession>
    fun chain(next: InstallChain): InstallChain
    var statusMessageState: MutableState<String>
}

abstract class BaseInstallChain : InstallChain {
    override var statusMessageState = mutableStateOf("")
    protected lateinit var nextLink: InstallChain

    override suspend fun execute(session: InstallSession): Result<InstallSession> {
        if (this::nextLink.isInitialized)
            return nextLink.execute(session)
        return Result.success(session)
    }

    override fun chain(next: InstallChain): InstallChain {
        nextLink = next
        return nextLink
    }
}

class NullInstallLink : BaseInstallChain() {
    override suspend fun execute(session: InstallSession): Result<InstallSession> =
        Result.success(session)
}
