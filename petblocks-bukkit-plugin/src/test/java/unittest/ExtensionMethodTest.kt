package unittest

import com.github.shynixn.petblocks.bukkit.logic.business.extension.distanceSafely
import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class ExtensionMethodTest {
    /**
     * Given
     *   two different locations in the same world
     * When
     *    distanceSafely is called
     * Then
     *    should return the correct distance.
     */
    @Test
    fun distanceSafely_LocationsInSameWorld_ShouldMeasureDistanceCorrectly() {
        // Arrange
        val world = Mockito.mock(World::class.java)
        Mockito.`when`(world.name).thenReturn("TestWorld")

        val location1 = Location(world, 20.0, 5.0, 20.0)
        val location2 = Location(world, 100.0, 7.0, -10.0)

        // Act
        val distance = location1.distanceSafely(location2).toInt()

        // Assert
        Assertions.assertEquals(85, distance)
    }

    /**
     * Given
     *   two different locations in the different worlds
     * When
     *    distanceSafely is called
     * Then
     *    should return max distance.
     */
    @Test
    fun distanceSafely_LocationsInDifferentWorld_ShouldMeasureDistanceCorrectly() {
        // Arrange
        val world1 = Mockito.mock(World::class.java)
        Mockito.`when`(world1.name).thenReturn("NetherWorld")
        val world2 = Mockito.mock(World::class.java)
        Mockito.`when`(world2.name).thenReturn("SkyBlockWorld")

        val location1 = Location(world1, 20.0, 5.0, 20.0)
        val location2 = Location(world2, 100.0, 7.0, -10.0)

        // Act
        val distance = location1.distanceSafely(location2).toInt()

        // Assert
        Assertions.assertEquals(Double.MAX_VALUE.toInt(), distance)
    }
}