package com.parizene.androididmodifier.xml

data class SettingInfo(
    val id: String,
    val name: String,
    val value: String,
    val packageName: String,
    val defaultValue: String,
    val defaultSysSet: Boolean,
    val tag: String?
)
