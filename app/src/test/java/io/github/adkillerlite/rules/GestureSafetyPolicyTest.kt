package io.github.adkillerlite.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureSafetyPolicyTest {
    private val policy = GestureSafetyPolicy()

    @Test
    fun acceptsSkipLabelsNearTopOrBottomRightCorner() {
        assertTrue(policy.canGestureClick("跳过", Bounds(800, 100, 890, 160), Bounds(0, 0, 900, 2000)))
        assertTrue(policy.canGestureClick("跳过", Bounds(720, 1840, 870, 1920), Bounds(0, 0, 900, 2000)))
    }

    @Test
    fun rejectsCenterSkipLabelsAndLargeNodes() {
        assertFalse(policy.canGestureClick("跳过", Bounds(350, 900, 550, 1000), Bounds(0, 0, 900, 2000)))
        assertFalse(policy.canGestureClick("跳过", Bounds(400, 0, 900, 1000), Bounds(0, 0, 900, 2000)))
    }

    @Test
    fun rejectsGenericCloseTextForCoordinateFallback() {
        assertFalse(policy.canGestureClick("关闭", Bounds(800, 100, 890, 160), Bounds(0, 0, 900, 2000)))
    }
}
