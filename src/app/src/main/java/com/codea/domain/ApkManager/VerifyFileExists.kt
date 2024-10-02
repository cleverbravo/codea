package com.codea.domain.ApkManager

import java.io.File

class VerifyFileExists : BaseInstallChain() {
    override suspend fun execute(session: InstallSession): Result<InstallSession> {
        if (session.apkIsInstalled)
            return super.execute(session)
        println("Verifying if file exists: ${session.apkFileName}")
        val apkFile = File(session.apkFolderLocation, session.apkFileName)
        if (!apkFile.exists()) {
            println("apk File does not exist.")
            session.fileExists = false
        } else {
            println("apk File already exists.")
            session.fileExists = true
        }
        return super.execute(session)
    }
}