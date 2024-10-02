package com.codea.domain.ApkManager

class FindSuggestedVersion : BaseInstallChain() {
    override suspend fun execute(session: InstallSession): Result<InstallSession> {
        println("Finding suggested version for ${session.packageName}...")
        // Simulating version lookup (replace with actual implementation)
        when (session.packageName) {
            ToolsApkNames.termux -> {
                session.version = "0.118.1"
                session.downloadApkUrl = "https://f-droid.org/repo/com.termux_1000.apk"
                session.PGPSignatureURL = "https://f-droid.org/repo/com.termux_1000.apk.asc"
            }

            ToolsApkNames.termuxAPI -> {
                session.version = "0.118.1"
                session.downloadApkUrl = "https://f-droid.org/repo/com.termux_1000.apk"
                session.PGPSignatureURL = "https://f-droid.org/repo/com.termux_1000.apk.asc"
            }
        }

        println("Suggested version: ${session.version}")
        return nextLink.execute(session)
        TODO("we need to implement a correct functionality")
    }
}
