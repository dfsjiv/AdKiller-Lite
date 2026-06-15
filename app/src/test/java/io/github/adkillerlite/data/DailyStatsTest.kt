package io.github.adkillerlite.data
import org.junit.Assert.assertEquals
import org.junit.Test
class DailyStatsTest {
 @Test fun resetsCountWhenDateChanges(){assertEquals(DailyStats("2026-06-15",0),DailyStats("2026-06-14",12).forDate("2026-06-15"))}
 @Test fun preservesCountForSameDate(){val s=DailyStats("2026-06-15",12);assertEquals(s,s.forDate("2026-06-15"))}
}
