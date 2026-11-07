package com.mahin.azu.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import com.mahin.azu.data.model.AppModel
import com.mahin.azu.data.store.HiddenAppsStore
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Loads launchable apps from PackageManager.
 * All querying runs on Dispatchers.Default, never the main thread.
 *
 * Icons: decoded ONCE per package to a 96x96 RGB_565 Bitmap (~18 KB)
 * and kept in a 64-entry LruCache, with in-flight de-duplication.
 * Before this fix the drawer called PackageManager directly from
 * every grid item, so scrolling re-decoded the same icons over and
 * over — that was the lag. Now a scrolled-back-to icon is a cache
 * hit and returns immediately on the calling thread.
 */
class AppRepository(context: Context) {

    private val appContext = context.applicationContext
    private val pm: PackageManager = appContext.packageManager
    private val scope = CoroutineScope(Dispatchers.Default)

    val hiddenAppsStore = HiddenAppsStore(appContext)

    private val _apps = MutableStateFlow<List<AppModel>>(emptyList())
    val apps: StateFlow<List<AppModel>> = _apps.asStateFlow()

    // 64 entries x ~18 KB per 96x96 RGB_565 icon = ~1.2 MB max.
    // Safe on 2 GB Android Go devices. sizeOf returns 1 per entry,
    // so maxSize is a plain entry count.
    private val iconCache = object : LruCache<String, Bitmap>(64) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int = 1
    }
    private val inFlight = ConcurrentHashMap<String, Deferred<Bitmap?>>()

    init {
        refresh()
    }

    fun requestRefresh() = refresh()

    fun refresh() {
        scope.launch { _apps.value = loadApps() }
    }

    private suspend fun loadApps(): List<AppModel> = withContext(Dispatchers.Default) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val infos: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        val apps = infos.map { AppModel.fromResolveInfo(pm, it) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
        hiddenAppsStore.prune(apps.map { it.packageName }.toSet())
        apps
    }

    /**
     * Returns the app's icon as a cached 96x96 RGB_565 Bitmap, or null.
     * Concurrent calls for the same package share one decode.
     */
    suspend fun loadIconBitmap(packageName: String): Bitmap? {
        synchronized(iconCache) { iconCache.get(packageName) }?.let { return it }
        val deferred = inFlight.getOrPut(packageName) {
            scope.async(Dispatchers.Default) { decodeIcon(packageName) }
        }
        return try {
            val bitmap = deferred.await()
            if (bitmap != null) {
                synchronized(iconCache) { iconCache.put(packageName, bitmap) }
            }
            bitmap
        } finally {
            inFlight.remove(packageName, deferred)
        }
    }

    private fun decodeIcon(packageName: String): Bitmap? = try {
        pm.getApplicationIcon(packageName)
            .toBitmap(96, 96, Bitmap.Config.RGB_565)
    } catch (_: Exception) {
        null
    }

    fun launchApp(packageName: String): Boolean {
        val intent = pm.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            appContext.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun appInfoIntent(packageName: String): Intent =
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    fun uninstallIntent(packageName: String): Intent =
        Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
