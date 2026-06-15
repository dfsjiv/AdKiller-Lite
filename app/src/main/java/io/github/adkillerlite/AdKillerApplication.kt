package io.github.adkillerlite

import android.app.Application
import io.github.adkillerlite.apps.InstalledAppsRepository
import io.github.adkillerlite.data.*

class AdKillerApplication:Application(){
 val settings by lazy{DataStoreSettingsRepository(this)}
 val stats by lazy{DataStoreStatsRepository(this)}
 val installedApps by lazy{InstalledAppsRepository(this)}
}
