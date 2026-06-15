package io.github.adkillerlite.rules

class ClickTargetPolicy {
    fun canClickTarget(keyword: String, label: Bounds, target: Bounds, screen: Bounds): Boolean {
        if (screen.width <= 0 || screen.height <= 0 || target.width <= 0 || target.height <= 0) return false
        if (keyword !in setOf("跳过", "关闭", "关闭广告", "×")) return false

        val isReasonablySmall =
            target.width <= screen.width * 50 / 100 &&
                target.height <= screen.height * 20 / 100
        val containsLabelCenter =
            label.centerX in target.left..target.right &&
                label.centerY in target.top..target.bottom

        return isReasonablySmall && containsLabelCenter
    }
}
