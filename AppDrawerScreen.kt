package com.mahin.azu.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mahin.azu.data.model.AppModel
import com.mahin.azu.ui.viewmodel.LauncherViewModel

/**
 * App drawer: 4-column LazyVerticalGrid, search, tap to launch,
 * long-press for App info / Uninstall / Hide.
 *
 * Hidden apps: long-press -> Hide removes the app from this
 * launcher's drawer and search only. They are always visible in
 * the "Hidden apps" list (eye icon in the top bar), where they
 * can be launched or unhidden. Nothing is hidden from Android
 * Settings, the Play Store, other launchers, recents, or
 * notifications, and nothing is uninstalled or disabled.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedApp by remember { mutableStateOf<AppModel?>(null) }
    var showHidden by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                actions = {
                    // Visible entry point to hidden apps — never a secret.
                    IconButton(onClick = { showHidden = true }) {
                        Icon(
                            Icons.Filled.VisibilityOff,
                            contentDescription = "Hidden apps (${state.hiddenApps.size})",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search apps") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (state.isLoading) {
                Text("Loading apps…", modifier = Modifier.padding(16.dp))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = state.visibleApps,
                        key = { it.packageName },
                        contentType = { "app" },
                    ) { app ->
                        AppGridItem(
                            app = app,
                            viewModel = viewModel,
                            onClick = { viewModel.launchApp(app.packageName) },
                            onLongClick = { selectedApp = app },
                        )
                    }
                }
            }
        }
    }

    selectedApp?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            title = { Text(app.label) },
            text = {
                Column {
                    Text(app.packageName)
                    // Actions live in the body, not the button row,
                    // so Hide / Uninstall / App info never overflow
                    // on a narrow phone screen.
                    TextButton(
                        onClick = {
                            viewModel.hideApp(app.packageName)
                            selectedApp = null
                        },
                        modifier = Modifier.padding(top = 8.dp),
                    ) { Text("Hide app") }
                    if (!app.isSystemApp) {
                        TextButton(onClick = {
                            context.startActivity(viewModel.uninstallIntent(app.packageName))
                            selectedApp = null
                        }) { Text("Uninstall") }
                    }
                    TextButton(onClick = {
                        context.startActivity(viewModel.appInfoIntent(app.packageName))
                        selectedApp = null
                    }) { Text("App info") }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedApp = null }) { Text("Close") }
            },
        )
    }

    if (showHidden) {
        AlertDialog(
            onDismissRequest = { showHidden = false },
            title = { Text("Hidden apps (${state.hiddenApps.size})") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Hidden only from this launcher's drawer and search. " +
                            "They are still installed and visible in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (state.hiddenApps.isEmpty()) {
                        Text(
                            "No hidden apps. Long-press an app and tap Hide app.",
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    } else {
                        state.hiddenApps.forEach { app ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    app.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = {
                                    viewModel.launchApp(app.packageName)
                                }) { Text("Open") }
                                TextButton(onClick = {
                                    viewModel.unhideApp(app.packageName)
                                }) { Text("Unhide") }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHidden = false }) { Text("Done") }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppGridItem(
    app: AppModel,
    viewModel: LauncherViewModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    // Icons come from the repository's shared Bitmap LruCache, so
    // scrolling back to an app is a cache hit — no re-decode, no
    // PackageManager call, no new coroutine storm per item.
    var iconBitmap by remember(app.packageName) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(app.packageName) {
        iconBitmap = viewModel.loadIconBitmap(app.packageName)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap!!.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier.size(48.dp),
            )
        } else {
            // Fixed-size placeholder: no layout shift while loading.
            Box(modifier = Modifier.size(48.dp))
        }
        Text(
            text = app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
        )
    }
}
