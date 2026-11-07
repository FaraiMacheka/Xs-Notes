package za.co.stripedape.noughtscrosses.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.stripedape.noughtscrosses.data.Settings
import za.co.stripedape.noughtscrosses.data.SettingsRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val settings: StateFlow<Settings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    fun setSound(enabled: Boolean) {
        viewModelScope.launch { repository.setSound(enabled) }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch { repository.setVibration(enabled) }
    }
}
