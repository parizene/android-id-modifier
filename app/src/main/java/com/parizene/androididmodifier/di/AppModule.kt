package com.parizene.androididmodifier.di

import android.content.Context
import com.parizene.androididmodifier.xml.AppInfoRepository
import com.parizene.androididmodifier.xml.XmlParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAppInfoRepository(
        @ApplicationContext context: Context
    ): AppInfoRepository {
        val packageManager = context.packageManager
        val xmlParser = XmlParser(context)
        return AppInfoRepository(packageManager, xmlParser)
    }
}