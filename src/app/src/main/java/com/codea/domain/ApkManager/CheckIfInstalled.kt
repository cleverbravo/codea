package com.codea.domain.ApkManager

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

class CheckIfInstalled : BaseInstallChain() {

    override suspend fun execute(session: InstallSession): Result<InstallSession> {
        println("Checking if ${session.packageName} is installed...")
        session.apkIsInstalled = false
        try {
            var packageInfo = session.context.packageManager.getPackageInfo(
                session.packageName,
                PackageManager.MATCH_ALL
            )
            session.apkIsInstalled = true
//            TODO("which version is installed?")
        } catch (e: PackageManager.NameNotFoundException) {
            println("The package ${session.packageName} is not installed.")
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
        return super.execute(session)
    }

    fun isAppInstalledRobust(session: InstallSession): Boolean {
        val pm = session.context.packageManager
        val lista = pm.getInstalledPackages(0)
        val t = lista.size
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(session.packageName)
        }
        val resolveInfoList: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        return resolveInfoList.size > 0
    }
}