package com.codea.domain.ApkManager

interface InstallChain {
    suspend fun execute(session: InstallSession): Result<InstallSession>
    fun chain(next: InstallChain): InstallChain
}

abstract class BaseInstallChain : InstallChain {
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
