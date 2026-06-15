package io.github.adkillerlite.rules

class PendingClickPolicy {
    private var pendingKey: CandidateKey? = null

    fun tryStart(key: CandidateKey): Boolean {
        if (pendingKey == key) return false
        pendingKey = key
        return true
    }

    fun complete(key: CandidateKey) {
        if (pendingKey == key) pendingKey = null
    }

    fun clear() {
        pendingKey = null
    }
}
