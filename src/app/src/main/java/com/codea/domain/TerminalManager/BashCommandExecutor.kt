package com.codea.domain.TerminalManager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.termux.shared.termux.TermuxConstants
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation

class BashCommandExecutor(val context: Context) : IShellCommandExecutor {
    companion object {
        val BASH_PATH = "/data/data/com.termux/files/usr/bin/bash"
        val HOME = "/data/data/com.termux/files/home"
        internal val continuationMap = ConcurrentHashMap<Int, Continuation<Result<CommandInfo>>>()
    }

    override suspend fun executeCommand(command: String): Result<CommandInfo> =
        suspendCancellableCoroutine { cont ->
            val commandInfo = CommandInfo(command)

            val intent = Intent().apply {
                setClassName(
                    TermuxConstants.TERMUX_PACKAGE_NAME,
                    TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE_NAME
                )
                action = TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND
                putExtra(
                    TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH,
                    BASH_PATH
                )
                putExtra(
                    TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS,
                    arrayOf("-c", command)
                )
                putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_WORKDIR, HOME)
                putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_BACKGROUND, true)
                putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_SESSION_ACTION, "0")
                //putExtra("com.termux.RUN_COMMAND_COMMAND_LABEL", "install command")
                //putExtra("com.termux.RUN_COMMAND_COMMAND_DESCRIPTION", "description")
            }

            val pendingResultIntentService = Intent(context, PendingResultCommandIntent::class.java)
            pendingResultIntentService.putExtra(
                PendingResultCommandIntent.COMMAND_INFO,
                commandInfo
            )
            val executionId = PendingResultCommandIntent.getNextExecutionId();
            continuationMap[executionId] = cont

            pendingResultIntentService.putExtra(
                PendingResultCommandIntent.EXECUTION_ID,
                executionId
            );

            val pendingIntent = PendingIntent.getService(
                context, executionId, pendingResultIntentService,
                PendingIntent.FLAG_ONE_SHOT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            )
            intent.putExtra(
                TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_PENDING_INTENT,
                pendingIntent
            )

            context.startService(intent)
            commandInfo.state = CommandState.RUNNING
        }
}