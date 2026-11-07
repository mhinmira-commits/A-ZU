package com.mahin.azu.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mahin.azu.ui.viewmodel.LauncherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home screen: clock, date, app count. Tap anywhere to open the drawer.
 */
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onOpenDrawer: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val date = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(onOpenDrawer) {
                detectTapGestures(onTap = { onOpenDrawer() })
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = time, style = MaterialTheme.typography.displayLarge)
            Text(text = date, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${state.visibleApps.size} apps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
            Text(
                text = "Tap anywhere to open the app drawer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }
}
