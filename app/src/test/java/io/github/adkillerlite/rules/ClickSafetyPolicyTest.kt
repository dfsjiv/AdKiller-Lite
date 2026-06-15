package io.github.adkillerlite.rules

import org.junit.Assert.*
import org.junit.Test

class ClickSafetyPolicyTest {
    @Test fun rejectsDuplicateCandidateInsideCooldown() {
        val policy = ClickSafetyPolicy(2_000); val key = CandidateKey("app.pkg", 7, "跳过")
        assertTrue(policy.canSchedule(key, 1_000)); policy.recordClick(key, 1_500)
        assertFalse(policy.canSchedule(key, 2_000)); assertTrue(policy.canSchedule(key, 3_501))
    }
    @Test fun allowsDifferentPackage() {
        val policy = ClickSafetyPolicy(2_000); policy.recordClick(CandidateKey("a", 1, "关闭"), 1_000)
        assertTrue(policy.canSchedule(CandidateKey("b", 1, "关闭"), 1_100))
    }
}
