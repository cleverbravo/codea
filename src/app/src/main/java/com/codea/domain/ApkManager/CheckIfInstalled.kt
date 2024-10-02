package com.codea.domain.ApkManager

import android.content.pm.PackageManager

class CheckIfInstalled : BaseInstallChain() {

    override suspend fun execute(session: InstallSession): Result<InstallSession> {
        println("Checking if ${session.packageName} is installed...")
        session.apkIsInstalled = false
        try {
            var packageInfo = session.context.packageManager.getPackageInfo(session.packageName, 0)
            session.apkIsInstalled = true
            TODO("which version is installed?")
        } catch (e: PackageManager.NameNotFoundException) {
            println("The package ${session.packageName} is not installed.")
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
        return super.execute(session)
    }
}