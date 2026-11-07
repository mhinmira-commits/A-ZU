package com.mahin.azu.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mahin.azu.AzuApp

/**
 * Manifest-registered receiver for install / uninstall / update.
 * It only asks the repository to refresh — it does no heavy work
 * itself and never starts a service.
 */
class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_REPLACED,
                Intent.ACTION_PACKAGE_CHANGED,
            )
        ) return
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
        val app = context.applicationContext as? AzuApp ?: return
        app.appRepository.requestRefresh()
    }
}
