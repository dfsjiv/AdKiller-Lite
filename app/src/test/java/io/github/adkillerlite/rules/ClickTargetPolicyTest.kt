package io.github.adkillerlite.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClickTargetPolicyTest {
    private val policy = ClickTargetPolicy()
    private val screen = Bounds(0, 0, 900, 2000)

    @Test
    fun acceptsSmallClickableTargetAroundSkipLabel() {
        assertTrue(
            policy.canClickTarget(
                keyword = "跳过",
                label = Bounds(760, 100, 860, 160),
                target = Bounds(720, 70, 890, 190),
                screen = screen,
            ),
        )
    }

    @Test
    fun rejectsFullScreenClickableAdContainer() {
        assertFalse(
            policy.canClickTarget(
                keyword = "跳过",
                label = Bounds(760, 100, 860, 160),
                target = screen,
                screen = screen,
            ),
        )
    }

    @Test
    fun acceptsExactCloseButtonButRejectsHugeCloseAncestor() {
        assertTrue(policy.canClickTarget("关闭", Bounds(800, 80, 870, 150), Bounds(780, 60, 890, 170), screen))
        assertFalse(policy.canClickTarget("关闭", Bounds(800, 80, 870, 150), screen, screen))
    }
}
