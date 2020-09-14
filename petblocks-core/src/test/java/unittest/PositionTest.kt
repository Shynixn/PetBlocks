package unittest

import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PositionTest {
    /**
     * Given a normal position in world,
     * when relativePosition is called
     * then the correct relative position should be returned.
     */
    @Test
    fun relativePosition_NormalPosition_ShouldReturnCorrectRelative() {
        val classUnderTest = PositionEntity()
        classUnderTest.x = 5.0
        classUnderTest.y = 50.0
        classUnderTest.z = 10.0

        classUnderTest.relativePosition(2.0, 0.0, 3.0)

        assertEquals(8.0, classUnderTest.x)
        assertEquals(50.0, classUnderTest.y)
        assertEquals(12.0, classUnderTest.z)
    }
}
