package za.co.octagon.noughtscrosses.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import za.co.octagon.noughtscrosses.BuildConfig
import za.co.octagon.noughtscrosses.data.Settings

/** Public URL of the privacy policy: docs/privacy-policy.html served by GitHub Pages. */
const val PRIVACY_POLICY_URL = "https://faraimacheka.github.io/Xs-Notes/privacy-policy.html"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onSoundChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val itemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ListItem(
                headlineContent = { Text("Sound") },
                supportingContent = { Text("Click when a mark is placed") },
                trailingContent = { Switch(checked = settings.sound, onCheckedChange = null) },
                colors = itemColors,
                modifier = Modifier.toggleable(settings.sound, role = Role.Switch, onValueChange = onSoundChange),
            )
            ListItem(
                headlineContent = { Text("Vibration") },
                supportingContent = { Text("Buzz when someone wins") },
                trailingContent = { Switch(checked = settings.vibration, onCheckedChange = null) },
                colors = itemColors,
                modifier = Modifier.toggleable(settings.vibration, role = Role.Switch, onValueChange = onVibrationChange),
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Privacy policy") },
                colors = itemColors,
                modifier = Modifier.clickable(role = Role.Button) { uriHandler.openUri(PRIVACY_POLICY_URL) },
            )
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                colors = itemColors,
            )
        }
    }
}
