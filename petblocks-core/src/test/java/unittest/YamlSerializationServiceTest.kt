package unittest

import com.github.shynixn.petblocks.api.legacy.business.service.YamlSerializationService
import com.github.shynixn.petblocks.api.legacy.persistence.entity.AIHopping
import com.github.shynixn.petblocks.core.logic.business.service.YamlSerializationServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.AIHoppingEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.io.StringWriter

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class YamlSerializationServiceTest {

    /**
     * Given
     *      an hopping ai
     * When
     *      serialize is called
     * Then
     *     the hopping ai should be correctly serialized.
     */
    @Test
    fun serialize_HoppingAi_ShouldSerializeCorrectly() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val ai = AIHoppingEntity()
        val expectedResult = "climbing-height: 1.0\n" +
                "id: 0\n" +
                "speed: 0.53\n" +
                "offset-y: 20.5\n" +
                "particle:\n" +
                "  name: NONE\n" +
                "  amount: 1\n" +
                "  speed: 1.0\n" +
                "  offx: 1.0\n" +
                "  offy: 1.0\n" +
                "  offz: 1.0\n" +
                "  data: 0\n" +
                "sound:\n" +
                "  name: PIG_IDLE\n" +
                "  volume: 20.2\n" +
                "  pitch: 0.1\n"

        ai.movementSpeed = 0.53
        ai.movementYOffSet = 20.5
        ai.movementSound.name = "PIG_IDLE"
        ai.movementSound.pitch = 0.1
        ai.movementSound.volume = 20.2

        // Act
        val writer = StringWriter()
        classUnderTest.serialize(ai, writer)
        val actualResult = writer.toString()

        // Assert
        Assertions.assertEquals(expectedResult, actualResult)
    }

    /**
     * Given
     *      an serialized hopping ai
     * When
     *      deserialize is called
     * Then
     *     the hopping ai should be correctly deSerialized.
     */
    @Test
    fun deserialize_HoppingAi_ShouldDeSerializeCorrectly() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val aiText = "climbing-height: 1.0\n" +
                "id: 0\n" +
                "speed: 0.53\n" +
                "offset-y: 20.5\n" +
                "particle:\n" +
                "  name: NONE\n" +
                "  amount: 1\n" +
                "  speed: 1.0\n" +
                "  offx: 1.0\n" +
                "  offy: 1.0\n" +
                "  offz: 1.0\n" +
                "  data: 0\n" +
                "sound:\n" +
                "  name: PIG_IDLE\n" +
                "  volume: 20.2\n" +
                "  pitch: 0.1\n"
        val expectedAi = AIHoppingEntity()
        expectedAi.movementSpeed = 0.53
        expectedAi.movementYOffSet = 20.5
        expectedAi.movementSound.name = "PIG_IDLE"
        expectedAi.movementSound.pitch = 0.1
        expectedAi.movementSound.volume = 20.2

        // Act
        val reader = StringReader(aiText)
        val actualAi = classUnderTest.deserialize<AIHopping>(AIHoppingEntity::class.java, reader)

        // Assert
        Assertions.assertEquals(expectedAi.movementSpeed, actualAi.movementSpeed)
        Assertions.assertEquals(expectedAi.movementYOffSet, actualAi.movementYOffSet)
        Assertions.assertEquals(expectedAi.movementSound.name, actualAi.movementSound.name)
        Assertions.assertEquals(expectedAi.movementSound.pitch, actualAi.movementSound.pitch)
        Assertions.assertEquals(expectedAi.movementSound.volume, actualAi.movementSound.volume)
    }

    companion object {
        fun createWithDependencies(): YamlSerializationService {
            return YamlSerializationServiceImpl()
        }
    }
}
