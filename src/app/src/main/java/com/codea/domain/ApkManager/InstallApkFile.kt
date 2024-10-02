package com.codea.domain.ApkManager

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import java.io.FileInputStream
import java.io.IOException

class InstallApkFile : BaseInstallChain() {
    override suspend fun execute(session: InstallSession): Result<InstallSession> {
        if (session.apkIsInstalled)
            return super.execute(session)
        val packageInstaller = session.context.packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        var packageInstallerSession: PackageInstaller.Session? = null
        var inputStream: FileInputStream? = null

        if (session.apkURI == null) {
            val message = "the apk Uri is null"
            print(message)
            return Result.failure(NullPointerException(message))
        }

        try {
            val sessionId = packageInstaller.createSession(params)
            packageInstallerSession = packageInstaller.openSession(sessionId)

            inputStream =
                session.context.contentResolver.openInputStream(session.apkURI!!) as FileInputStream
            val outputStream = packageInstallerSession.openWrite("package", 0, -1)
            val buffer = ByteArray(65536)
            var c: Int

            while (inputStream.read(buffer).also { c = it } != -1) {
                outputStream.write(buffer, 0, c)
            }

            packageInstallerSession.fsync(outputStream)
            outputStream.close()

            // Create an intent for the installation result
            val intent = Intent(session.context, InstallResultReceiver::class.java)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    session.context,
                    sessionId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    session.context,
                    sessionId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            // Commit the session (this will start the installation)
            packageInstallerSession.commit(pendingIntent.intentSender)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            packageInstallerSession?.close()
            inputStream?.close()
        }
        return super.execute(session)

    }
}