package io.github.adkillerlite.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingClickPolicyTest {
    @Test
    fun repeatedEventsForSameCandidateDoNotRestartPendingClick() {
        val policy = PendingClickPolicy()
        val candidate = CandidateKey("app.pkg", 7, "跳过")

        assertTrue(policy.tryStart(candidate))
        assertFalse(policy.tryStart(candidate))
        assertFalse(policy.tryStart(candidate))
    }

    @Test
    fun completionAllowsCandidateToBeScheduledAgain() {
        val policy = PendingClickPolicy()
        val candidate = CandidateKey("app.pkg", 7, "跳过")

        assertTrue(policy.tryStart(candidate))
        policy.complete(candidate)

        assertTrue(policy.tryStart(candidate))
    }

    @Test
    fun aDifferentCandidateReplacesThePendingCandidate() {
        val policy = PendingClickPolicy()

        assertTrue(policy.tryStart(CandidateKey("app.pkg", 7, "跳过")))
        assertTrue(policy.tryStart(CandidateKey("app.pkg", 8, "关闭")))
    }
}
