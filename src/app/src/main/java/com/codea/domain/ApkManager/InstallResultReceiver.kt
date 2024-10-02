package com.codea.domain.ApkManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.widget.Toast

class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status =
            intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // This status means the user needs to confirm the installation, so we need to show the installation prompt
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                context.startActivity(confirmIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "Installation successful", Toast.LENGTH_LONG).show()
            }

            PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED -> {
                Toast.makeText(context, "Installation failed: $message", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}