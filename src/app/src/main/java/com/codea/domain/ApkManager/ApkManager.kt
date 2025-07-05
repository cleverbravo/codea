package com.codea.domain.ApkManager

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.*

class ApkManager() : IApkManager {
    var statusMessage by mutableStateOf("Installing Termux…")
    override suspend fun installTools(
        context: Context,
        packageName: String
    ): Result<InstallSession> {
        val installSession = InstallSession(packageName, context)
        var result = Result.success(installSession)

        val installChain = CheckIfInstalled()
        var statusMessage by installChain.statusMessageState
        statusMessage = this.statusMessage

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
