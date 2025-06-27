package com.codea.domain.ApkManager

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApkManager(var statusMessage: String) : IApkManager {
    override suspend fun installTools(
        context: Context,
        packageName: String
    ): Result<InstallSession> {
        val installSession = InstallSession(packageName, context, statusMessage)
        var result = Result.success(installSession)

        val installChain = CheckIfInstalled()
        installChain.chain(FindSuggestedVersion())
            .chain(VerifyFileExists())
            .chain(DownloadFile())
            .chain(InstallApkFile())
        result = installChain.execute(installSession)
        return result
    }
}

interface IApkManager {
    suspend fun installTools(context: Context, packageName: String): Result<InstallSession>
}

object ToolsApkNames {
    val termux: String = "com.termux"
    val termuxAPI: String = "com.termux.api"
}
