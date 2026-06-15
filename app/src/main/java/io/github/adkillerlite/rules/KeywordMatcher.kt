package io.github.adkillerlite.rules

class KeywordMatcher(
    private val exactKeywords: Map<String, String> = mapOf(
        "跳过" to "跳过",
        "关闭" to "关闭",
        "关闭广告" to "关闭广告",
        "×" to "×",
        "✕" to "×",
        "✖" to "×",
        "skip" to "跳过",
        "skip ad" to "跳过",
        "close" to "关闭",
        "close ad" to "关闭",
    ),
) {
    fun match(value: CharSequence?): String? {
        val normalized = value?.toString()?.trim()?.replace("\\s+".toRegex(), " ") ?: return null
        exactKeywords[normalized.lowercase()]?.let { return it }

        if (SKIP_PATTERN.matches(normalized)) return "跳过"
        if (COUNTDOWN_SKIP_PATTERN.matches(normalized)) return "跳过"
        if (CLOSE_PATTERN.matches(normalized)) return "关闭"
        return null
    }

    private companion object {
        val SKIP_PATTERN = Regex(
            pattern = """^跳过(?:广告)?(?:\s*\d+(?:\.\d+)?\s*(?:s|秒)?)?$""",
            option = RegexOption.IGNORE_CASE,
        )
        val COUNTDOWN_SKIP_PATTERN = Regex(
            pattern = """^\d+(?:\.\d+)?\s*(?:s|秒)(?:\s*[|｜·]\s*|\s*后\s*)跳过$""",
            option = RegexOption.IGNORE_CASE,
        )
        val CLOSE_PATTERN = Regex("""^关闭(?:此)?广告(?:按钮)?$""")
    }
}
