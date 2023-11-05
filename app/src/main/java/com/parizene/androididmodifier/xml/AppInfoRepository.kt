package com.parizene.androididmodifier.xml

import android.content.pm.PackageManager
import timber.log.Timber

class AppInfoRepository(
    private val packageManager: PackageManager,
    private val xmlParser: XmlParser
) {
    fun load(): List<Pair<SettingInfo, AppInfo?>> {
        val settingInfoList = xmlParser.parseXml()
        val pairs = mutableListOf<Pair<SettingInfo, AppInfo?>>()

        for (settingInfo in settingInfoList) {
            var appInfo: AppInfo? = null
            try {
                val packageInfo = packageManager.getPackageInfo(settingInfo.packageName, 0)
                val applicationInfo = packageInfo.applicationInfo
                val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                val appIcon = packageManager.getApplicationIcon(applicationInfo)
                appInfo = AppInfo(appName, appIcon)
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.w(e)
            }
            pairs.add(Pair(settingInfo, appInfo))
        }

        return pairs
    }

    fun updateValue(packageName: String, newValue: String) {
        xmlParser.updateXml(packageName, newValue)?.also {
            xmlParser.writeXml(it)
        }
    }
}