package io.github.adkillerlite.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore("ad_killer")
private val RULES = stringSetPreferencesKey("rules")
private val COUNT_DATE = stringPreferencesKey("count_date")
private val COUNT = intPreferencesKey("count")
private val LOGS = stringSetPreferencesKey("logs")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {
    override val rules = context.dataStore.data.map { p -> p[RULES].orEmpty().mapNotNull(::decodeRule).sortedBy { it.packageName } }
    override suspend fun ruleFor(packageName: String) = rules.first().firstOrNull { it.packageName == packageName }
    override suspend fun setRule(rule: AppRule) { context.dataStore.edit { p -> p[RULES] = p[RULES].orEmpty().filterNot { it.startsWith("${rule.packageName}|") }.toSet() + encodeRule(rule.copy(delayMs=rule.delayMs.coerceIn(0,10_000))) } }
    override suspend fun removeRule(packageName: String) { context.dataStore.edit { p -> p[RULES] = p[RULES].orEmpty().filterNot { it.startsWith("$packageName|") }.toSet() } }
    private fun encodeRule(r: AppRule)="${r.packageName}|${r.enabled}|${r.delayMs}"
    private fun decodeRule(s:String):AppRule?=s.split('|').takeIf{it.size==3}?.let{
        val storedDelay = it[2].toLongOrNull() ?: return null
        AppRule(
            it[0],
            it[1].toBooleanStrictOrNull() ?: return null,
            if (storedDelay == 1_000L || storedDelay == 150L) 0L else storedDelay,
        )
    }
}

class DataStoreStatsRepository(private val context: Context) : StatsRepository {
    override val stats = context.dataStore.data.map { p -> val today=LocalDate.now().toString(); DailyStats(p[COUNT_DATE]?:today,p[COUNT]?:0).forDate(today) }
    override val logs = context.dataStore.data.map { p -> p[LOGS].orEmpty().mapNotNull(::decodeLog).sortedByDescending{it.timestampMs} }
    override suspend fun record(log: CloseLog) { context.dataStore.edit { p ->
        val today=LocalDate.now().toString(); val current=DailyStats(p[COUNT_DATE]?:today,p[COUNT]?:0).forDate(today)
        if(log.success){p[COUNT_DATE]=today;p[COUNT]=current.count+1}
        p[LOGS]=(p[LOGS].orEmpty()+encodeLog(log)).mapNotNull{decodeLog(it)}.sortedByDescending{it.timestampMs}.take(100).map(::encodeLog).toSet()
    } }
    private fun encodeLog(l:CloseLog)="${l.timestampMs}|${l.packageName}|${l.keyword}|${l.success}"
    private fun decodeLog(s:String):CloseLog?=s.split('|').takeIf{it.size==4}?.let{CloseLog(it[0].toLongOrNull()?:return null,it[1],it[2],it[3].toBooleanStrictOrNull()?:return null)}
}
