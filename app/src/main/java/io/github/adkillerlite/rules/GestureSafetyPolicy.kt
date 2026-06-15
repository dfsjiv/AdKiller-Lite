package io.github.adkillerlite.rules

data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val centerX: Int get() = left + width / 2
    val centerY: Int get() = top + height / 2
}

class GestureSafetyPolicy {
    fun canGestureClick(keyword: String, node: Bounds, screen: Bounds): Boolean {
        if (keyword != "跳过" && keyword != "×") return false
        if (screen.width <= 0 || screen.height <= 0 || node.width <= 0 || node.height <= 0) return false

        val isRightSide = node.centerX >= screen.left + screen.width * 55 / 100
        val isTopOrBottom =
            node.centerY <= screen.top + screen.height * 30 / 100 ||
                node.centerY >= screen.top + screen.height * 70 / 100
        val isSmallTarget = node.width <= screen.width * 40 / 100 &&
            node.height <= screen.height * 15 / 100

        return isRightSide && isTopOrBottom && isSmallTarget
    }
}
