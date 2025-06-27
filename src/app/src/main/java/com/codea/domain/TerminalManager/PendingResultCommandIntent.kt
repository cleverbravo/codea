package com.codea.domain.TerminalManager

import android.app.IntentService
import android.content.Intent
import com.termux.shared.termux.TermuxConstants
import kotlin.coroutines.resume

class PendingResultCommandIntent() :
    IntentService(PENDING_RESULT_COMMAND_INTENT) {
    companion object {
        private var executionId = 1000;
        val COMMAND_INFO = "COMMAND_INFO"
        val EXECUTION_ID = "execution_id"
        val RESULT = "result"
        val PENDING_RESULT_COMMAND_INTENT = "PendingResultCommandIntent"
        fun getNextExecutionId(): Int {
            executionId += 1
            return executionId
        }
    }

    //var commandInfo: CommandInfo? = null
    lateinit var commandInfo: CommandInfo

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null)
            return
        commandInfo = intent.getParcelableExtra(COMMAND_INFO)!!
        val executionId = intent.getIntExtra(EXECUTION_ID, -1)
        val cont = BashCommandExecutor.continuationMap.remove(executionId)
//        if (cont != null) {
//            val result: Result<CommandInfo> = Result.success(commandInfo)
//            cont.resume(result)
//        }

        commandInfo.state = CommandState.STOPPED
        val resultBundle = intent.getBundleExtra(RESULT) ?: return

        commandInfo.state = CommandState.FINISHED
        commandInfo.stdout = resultBundle.getString(
            TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT,
            ""
        )
        commandInfo.stderr = resultBundle.getString(
            TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR,
            ""
        )

        commandInfo.exitCode =
            resultBundle.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE)
        commandInfo.errorCode =
            resultBundle.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERR)
        commandInfo.errorMessage = resultBundle.getString(
            TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG,
            ""
        )
        println("*************************   ${commandInfo.command} exitCode=${commandInfo.exitCode} ${commandInfo.stdout}  errorMessage:${commandInfo.errorMessage}  stderr=${commandInfo.stderr}")
//        println("${commandInfo.command} exitCode=${commandInfo.exitCode}")
        if (commandInfo.exitCode != 0)
            cont?.resume(Result.failure(RuntimeException("${commandInfo.command} exitCode=${commandInfo.exitCode}")))
        else
            cont?.resume(Result.success(commandInfo))
    }
}