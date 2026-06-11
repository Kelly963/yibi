package com.yibi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibi.util.UpdateChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AboutViewModel : ViewModel() {

    private val _releases = MutableStateFlow<List<UpdateChecker.ReleaseInfo>>(emptyList())
    val releases: StateFlow<List<UpdateChecker.ReleaseInfo>> = _releases

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentVersion = MutableStateFlow("")
    val currentVersion: StateFlow<String> = _currentVersion

    fun loadData(context: Context) {
        _currentVersion.value = UpdateChecker.getCurrentVersionName(context)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _releases.value = UpdateChecker.fetchAllReleases()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkUpdate(context: Context) {
        UpdateChecker.checkAndShowUpdateDialog(context)
    }
}
