package io.github.adkillerlite.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import io.github.adkillerlite.rules.Bounds
import io.github.adkillerlite.rules.CandidateKey
import io.github.adkillerlite.rules.ClickTargetPolicy
import io.github.adkillerlite.rules.GestureSafetyPolicy
import io.github.adkillerlite.rules.KeywordMatcher
import java.util.ArrayDeque

data class NodeCandidate(
    val key: CandidateKey,
    val clickableTarget: AccessibilityNodeInfo?,
    val gestureX: Float?,
    val gestureY: Float?,
)

class NodeFinder(
    private val matcher: KeywordMatcher = KeywordMatcher(),
    private val gestureSafety: GestureSafetyPolicy = GestureSafetyPolicy(),
    private val clickTargetPolicy: ClickTargetPolicy = ClickTargetPolicy(),
) {
    fun find(root: AccessibilityNodeInfo, packageName: String): NodeCandidate? {
        val screenBounds = Rect().also(root::getBoundsInScreen).toBounds()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        var gestureFallback: NodeCandidate? = null
        queue.add(root)
        var seen = 0

        while (queue.isNotEmpty() && seen++ < 500) {
            val node = queue.removeFirst()
            val keyword = matcher.match(node.text) ?: matcher.match(node.contentDescription)
            if (keyword != null) {
                val key = CandidateKey(packageName, node.windowId, keyword)
                val nodeBounds = Rect().also(node::getBoundsInScreen).toBounds()
                findClickableTarget(node, keyword, nodeBounds, screenBounds)?.let {
                    return NodeCandidate(key, it, null, null)
                }

                if (
                    gestureFallback == null &&
                    gestureSafety.canGestureClick(keyword, nodeBounds, screenBounds)
                ) {
                    gestureFallback = NodeCandidate(
                        key = key,
                        clickableTarget = null,
                        gestureX = nodeBounds.centerX.toFloat(),
                        gestureY = nodeBounds.centerY.toFloat(),
                    )
                }
            }
            for (index in 0 until node.childCount) node.getChild(index)?.let(queue::add)
        }
        return gestureFallback
    }

    private fun findClickableTarget(
        node: AccessibilityNodeInfo,
        keyword: String,
        nodeBounds: Bounds,
        screenBounds: Bounds,
    ): AccessibilityNodeInfo? {
        var target: AccessibilityNodeInfo? = node
        var hops = 0
        while (target != null && !target.isClickable && hops++ < 8) target = target.parent
        val clickable = target?.takeIf(AccessibilityNodeInfo::isClickable) ?: return null
        val targetBounds = Rect().also(clickable::getBoundsInScreen).toBounds()
        return clickable.takeIf {
            clickTargetPolicy.canClickTarget(keyword, nodeBounds, targetBounds, screenBounds)
        }
    }

    private fun Rect.toBounds() = Bounds(left, top, right, bottom)
}
