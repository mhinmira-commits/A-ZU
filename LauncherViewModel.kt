package com.mahin.azu.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahin.azu.data.model.AppModel
import com.mahin.azu.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val visibleApps: List<AppModel> = emptyList(),
    val hiddenApps: List<AppModel> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = "",
)

@OptIn(FlowPreview::class)
class LauncherViewModel(
    private val appRepository: AppRepository,
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    /**
     * Single source of truth for the drawer.
     * Hidden apps are removed from visibleApps (and from search)
     * but kept in hiddenApps, so the "Hidden apps" screen can
     * always list and unhide them. Search is debounced 250 ms
     * and filtering runs on Dispatchers.Default, so typing does
     * not stutter on low-end devices.
     */
    val uiState: StateFlow<LauncherUiState> = combine(
        appRepository.apps,
        appRepository.hiddenAppsStore.hiddenPackages,
        queryFlow.debounce(250),
    ) { apps, hidden, query ->
        val visible = apps.filterNot { hidden.contains(it.packageName) }
        val hiddenApps = apps.filter { hidden.contains(it.packageName) }
        val filtered = if (query.isBlank()) {
            visible
        } else {
            val q = query.trim().lowercase()
            visible.filter {
                it.label.lowercase().contains(q) ||
                    it.packageName.lowercase().contains(q)
            }
        }
        LauncherUiState(
            visibleApps = filtered,
            hiddenApps = hiddenApps,
            isLoading = apps.isEmpty(),
            query = query,
        )
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherUiState())

    fun onQueryChange(query: String) {
        queryFlow.value = query
    }

    fun refresh() = appRepository.requestRefresh()

    fun launchApp(packageName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            appRepository.launchApp(packageName)
        }
    }

    fun hideApp(packageName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            appRepository.hiddenAppsStore.hide(packageName)
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            appRepository.hiddenAppsStore.unhide(packageName)
        }
    }

    suspend fun loadIconBitmap(packageName: String) =
        appRepository.loadIconBitmap(packageName)

    fun appInfoIntent(packageName: String) = appRepository.appInfoIntent(packageName)
    fun uninstallIntent(packageName: String) = appRepository.uninstallIntent(packageName)
}
