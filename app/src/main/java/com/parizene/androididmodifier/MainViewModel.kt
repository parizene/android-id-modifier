package com.parizene.androididmodifier

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parizene.androididmodifier.xml.AppInfo
import com.parizene.androididmodifier.xml.AppInfoRepository
import com.parizene.androididmodifier.xml.SettingInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appInfoRepository: AppInfoRepository
) : ViewModel() {
    val appInfoList = MutableLiveData<List<Pair<SettingInfo, AppInfo?>>>()

    init {
        viewModelScope.launch {
            load()
        }
    }

    private fun load() {
        appInfoList.value = appInfoRepository.load()
    }

    fun handleUpdateValue(packageName: String, newValue: String) {
        viewModelScope.launch {
            appInfoRepository.updateValue(packageName, newValue)
            load()
        }
    }
}