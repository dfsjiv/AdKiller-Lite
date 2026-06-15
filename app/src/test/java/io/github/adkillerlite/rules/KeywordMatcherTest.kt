package io.github.adkillerlite.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeywordMatcherTest {
    private val matcher = KeywordMatcher()

    @Test
    fun matchesInitialKeywordsAfterNormalization() {
        assertEquals("跳过", matcher.match(" 跳过 "))
        assertEquals("关闭", matcher.match("关闭"))
        assertEquals("关闭广告", matcher.match("关闭广告"))
        assertEquals("×", matcher.match(" × "))
    }

    @Test
    fun rejectsSubstringsAndUnrelatedText() {
        assertNull(matcher.match("关闭页面"))
        assertNull(matcher.match("跳过登录"))
        assertNull(matcher.match(null))
    }
}
