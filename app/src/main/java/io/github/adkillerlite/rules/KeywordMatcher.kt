package io.github.adkillerlite.rules

class KeywordMatcher(
    private val keywords: Set<String> = setOf("跳过", "关闭", "关闭广告", "×"),
) {
    fun match(value: CharSequence?): String? {
        val normalized = value?.toString()?.trim()?.replace("\\s+".toRegex(), " ")
        return normalized?.takeIf(keywords::contains)
    }
}
