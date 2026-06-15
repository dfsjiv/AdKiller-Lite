package io.github.adkillerlite.data

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val rules: Flow<List<AppRule>>
    suspend fun ruleFor(packageName: String): AppRule?
    suspend fun setRule(rule: AppRule)
    suspend fun removeRule(packageName: String)
}
interface StatsRepository {
    val stats: Flow<DailyStats>
    val logs: Flow<List<CloseLog>>
    suspend fun record(log: CloseLog)
}
