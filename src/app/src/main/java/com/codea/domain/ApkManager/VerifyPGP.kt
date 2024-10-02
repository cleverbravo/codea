package com.codea.domain.ApkManager

class VerifyPGP : BaseInstallChain() {
    override suspend fun execute(session: InstallSession): Result<InstallSession> {
        TODO("implement to check the hash")
    }
}