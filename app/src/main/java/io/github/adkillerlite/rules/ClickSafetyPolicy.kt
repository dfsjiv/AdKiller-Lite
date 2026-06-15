package io.github.adkillerlite.rules

data class CandidateKey(val packageName: String, val windowId: Int, val keyword: String)

class ClickSafetyPolicy(private val cooldownMs: Long = 2_000) {
    private val clickedAt = mutableMapOf<CandidateKey, Long>()
    fun canSchedule(key: CandidateKey, nowMs: Long): Boolean =
        clickedAt[key]?.let { nowMs - it > cooldownMs } ?: true
    fun recordClick(key: CandidateKey, nowMs: Long) { clickedAt[key] = nowMs }
}
