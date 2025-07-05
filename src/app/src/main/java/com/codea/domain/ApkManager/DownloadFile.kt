package com.codea.domain.ApkManager

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class DownloadFile : BaseInstallChain() {
    override suspend fun execute(session: InstallSession): Result<InstallSession> {
        if (session.apkIsInstalled) return super.execute(session)

        val apkFile = prepareApkFile(session)
        statusMessageState.value = "Downloading ${session.apkFileName} ..."
        println("Downloading file from ${session.downloadApkUrl} to ${apkFile.path}")

        session.apkURI = Uri.fromFile(apkFile)
        beginDownload(session)
        return super.execute(session)
    }

    private fun prepareApkFile(session: InstallSession): File {
        val apkFile = File(session.apkFolderLocation, session.apkFileName)
        if (session.fileExists) apkFile.delete()
        return apkFile
    }

    private suspend fun beginDownload(session: InstallSession): Result<InstallSession> {

        val downloadManager =
            session.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = buildDownloadRequest(session)

        val downloadId = downloadManager.enqueue(request)
        registerReceiverAndAwaitResult(session, downloadId, downloadManager)

        return Result.success(session)
    }

    private fun buildDownloadRequest(session: InstallSession): DownloadManager.Request {
        val uri = Uri.parse(session.downloadApkUrl)
        return DownloadManager.Request(uri).apply {
            setTitle(session.packageName)
            setDescription(session.downloadDescription)
            setDestinationUri(session.apkURI)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverRoaming(false)
        }
    }

    private suspend fun registerReceiverAndAwaitResult(
        session: InstallSession,
        downloadId: Long,
        downloadManager: DownloadManager
    ) {

        return suspendCancellableCoroutine { continuation ->
            val receiver =
                createDownloadReceiver(session, downloadId, downloadManager, continuation)

            // this is the receiver
            session.context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )

            continuation.invokeOnCancellation {
                session.context.unregisterReceiver(receiver)
            }
        }
    }

    private fun createDownloadReceiver(
        session: InstallSession,
        downloadId: Long,
        downloadManager: DownloadManager,
        continuation: CancellableContinuation<Unit>
    ): BroadcastReceiver {
        return object : BroadcastReceiver() {
            @SuppressLint("Range")
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val status =
                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                session.fileExists = true
                                continuation.resume(Unit)
                            }

                            DownloadManager.STATUS_FAILED -> {
                                TODO("handle this case")
                            }
                        }
                    } else {
                        TODO("handle the reason: probably was canceled")
                    }
                    cursor.close()
                    session.context.unregisterReceiver(this)
                }
            }
        }
    }
}

