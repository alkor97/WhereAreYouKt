package info.alkor.whereareyou.common

import junit.framework.Assert
import org.junit.Test

class DistanceTest {
    @Test
    fun testConversions() {
        Assert.assertEquals(meters(1000), kilometers(1))
        val converted = meters(1000).convertTo(Kilometer)
        Assert.assertEquals(1.0, converted.value)
    }
}