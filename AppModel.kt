package com.mahin.azu.data.model

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

/**
 * One launchable app. Equality is by [packageName] only, so list
 * diffing in LazyVerticalGrid is cheap. Icons are loaded lazily and
 * are never stored in a list that gets copied around.
 */
data class AppModel(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val icon: Drawable? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is AppModel && other.packageName == packageName

    override fun hashCode(): Int = packageName.hashCode()

    companion object {
        fun fromResolveInfo(pm: PackageManager, info: ResolveInfo): AppModel {
            val appInfo: ApplicationInfo = info.activityInfo.applicationInfo
            val label = info.loadLabel(pm)?.toString() ?: appInfo.packageName
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            return AppModel(
                packageName = appInfo.packageName,
                label = label,
                isSystemApp = isSystem,
                icon = null, // Loaded lazily by AppRepository / the UI
            )
        }
    }
}
