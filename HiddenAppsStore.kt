package com.mahin.azu.data.store

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stores the set of hidden package names in SharedPreferences.
 *
 * Scope, stated plainly: hiding only removes an app from THIS
 * launcher's drawer and search. It does NOT hide the app from
 * Android Settings, the Play Store, other launchers, recents, or
 * notifications, and it does NOT uninstall or disable anything.
 *
 * Hidden apps are never undiscoverable: the drawer has a visible
 * "Hidden apps" button that lists them, lets you launch them, and
 * lets you unhide them. There is no secret gesture or code.
 */
class HiddenAppsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "hidden_apps",
        Context.MODE_PRIVATE,
    )

    private val _hiddenPackages = MutableStateFlow(load())
    val hiddenPackages: StateFlow<Set<String>> = _hiddenPackages.asStateFlow()

    private fun load(): Set<String> =
        prefs.getStringSet(KEY_HIDDEN, emptySet())?.toSet() ?: emptySet()

    fun isHidden(packageName: String): Boolean =
        _hiddenPackages.value.contains(packageName)

    fun hide(packageName: String) {
        if (packageName == SELF_PACKAGE) return // Never hide A-ZU itself.
        update(_hiddenPackages.value + packageName)
    }

    fun unhide(packageName: String) {
        update(_hiddenPackages.value - packageName)
    }

    fun toggle(packageName: String) {
        if (isHidden(packageName)) unhide(packageName) else hide(packageName)
    }

    /** Drop entries for packages that are no longer installed. */
    fun prune(installedPackages: Set<String>) {
        val pruned = _hiddenPackages.value.intersect(installedPackages)
        if (pruned.size != _hiddenPackages.value.size) update(pruned)
    }

    private fun update(newSet: Set<String>) {
        _hiddenPackages.value = newSet
        prefs.edit().putStringSet(KEY_HIDDEN, newSet).apply()
    }

    companion object {
        private const val KEY_HIDDEN = "hidden_package_names"
        private const val SELF_PACKAGE = "com.mahin.azu"
    }
}
