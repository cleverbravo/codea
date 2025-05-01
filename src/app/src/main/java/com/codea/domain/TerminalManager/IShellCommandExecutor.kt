package com.codea.domain.TerminalManager

import kotlinx.coroutines.flow.Flow

interface IShellCommandExecutor {
    suspend fun executeCommand(command: String): Result<CommandInfo>
}