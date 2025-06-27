package com.codea

import android.content.Context
import android.webkit.JavascriptInterface
import com.codea.domain.TerminalManager.BashCommandExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JSEventsBridge(val reload: () -> Unit, val context: Context) {
    @JavascriptInterface
    fun retryAndReload() {
        val command = "code-server --auth none "
        val installCodeServer = BashCommandExecutor(context)
        CoroutineScope(Dispatchers.Default).launch {
            val commandResult = installCodeServer.executeCommand(command)
            commandResult.onSuccess {
                withContext(Dispatchers.Main) {
                    reload()
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    reload()
                }
                print("************************** Falla con el comando:${it.message}")
            }
        }
    }
}