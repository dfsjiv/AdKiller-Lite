package io.github.adkillerlite.data

data class AppRule(val packageName: String, val enabled: Boolean = true, val delayMs: Long = 0)
data class DailyStats(val date: String, val count: Int) { fun forDate(today: String) = if (date == today) this else DailyStats(today, 0) }
data class CloseLog(val timestampMs: Long, val packageName: String, val keyword: String, val success: Boolean)
