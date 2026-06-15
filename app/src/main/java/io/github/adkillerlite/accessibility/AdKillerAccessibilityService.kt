package io.github.adkillerlite.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.github.adkillerlite.AdKillerApplication
import io.github.adkillerlite.data.CloseLog
import io.github.adkillerlite.rules.ClickSafetyPolicy
import io.github.adkillerlite.rules.PendingClickPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AdKillerAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pending: Job? = null
    private val finder = NodeFinder()
    private val safety = ClickSafetyPolicy()
    private val pendingPolicy = PendingClickPolicy()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return

        scope.launch {
            val app = application as AdKillerApplication
            val rule = app.settings.ruleFor(packageName) ?: return@launch
            if (!rule.enabled) return@launch

            val candidate = finder.find(root, packageName) ?: return@launch
            if (!safety.canSchedule(candidate.key, System.currentTimeMillis())) return@launch

            // Content-change events can fire continuously during an ad countdown.
            // Keep the original timer for the same candidate instead of restarting it.
            if (!pendingPolicy.tryStart(candidate.key)) return@launch
            pending?.cancel()

            pending = launch {
                try {
                    delay(rule.delayMs)
                    val currentRoot = rootInActiveWindow
                    val refreshed = currentRoot
                        ?.takeIf {
                            it.packageName?.toString() == packageName &&
                                it.windowId == candidate.key.windowId
                        }
                        ?.let { finder.find(it, packageName) }
                        ?.takeIf { it.key == candidate.key }

                    val success = refreshed?.let { click(it) } == true
                    if (success) {
                        safety.recordClick(candidate.key, System.currentTimeMillis())
                    }
                    app.stats.record(
                        CloseLog(
                            timestampMs = System.currentTimeMillis(),
                            packageName = packageName,
                            keyword = candidate.key.keyword,
                            success = success,
                        ),
                    )
                } finally {
                    pendingPolicy.complete(candidate.key)
                }
            }
        }
    }

    override fun onInterrupt() {
        pending?.cancel()
        pendingPolicy.clear()
    }

    override fun onDestroy() {
        pendingPolicy.clear()
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun click(candidate: NodeCandidate): Boolean {
        if (
            candidate.clickableTarget
                ?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
        ) {
            return true
        }
        return tap(
            candidate.gestureX ?: return false,
            candidate.gestureY ?: return false,
        )
    }

    private suspend fun tap(x: Float, y: Float): Boolean = suspendCancellableCoroutine { continuation ->
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        val accepted = dispatchGesture(
            gesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    if (continuation.isActive) continuation.resume(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    if (continuation.isActive) continuation.resume(false)
                }
            },
            null,
        )
        if (!accepted && continuation.isActive) continuation.resume(false)
    }
}
