package com.codea.domain.ApkManager

import android.content.Context
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Environment
import java.io.File

data class InstallSession(
    var packageName: String,
    var context: Context,
    var statusMessage: String
) {
    var version: String? = null
    var downloadApkUrl: String? = null
    var downloadDescription: String? = null
    val apkFolderLocation: File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    var fileExists: Boolean = false
    val apkFileName: String
        get() = "$packageName.apk"
    var apkURI: Uri? = null
    var PGPSignatureURL: String? = null
    var apkIsInstalled: Boolean = false
    var installStatus: Int = PackageInstaller.STATUS_PENDING_USER_ACTION
}
