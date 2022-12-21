package unittest

import com.github.shynixn.petblocks.api.legacy.business.service.YamlService
import com.github.shynixn.petblocks.bukkit.logic.business.service.YamlServiceImpl
import org.junit.Assert
import org.junit.jupiter.api.Test


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
class YamlServiceTest {
    /**
     * Given
     *    a serialized object with null keyValue pairs
     * When
     *    deserialize is called
     * Then
     *    the null values should also be deSerialized.
     */
    @Test
    fun readFromString_ArrayWithNullValues_ShouldReturnListWithNulLValues() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val expectedAmount = 1
        val sourceString = "items:\n" +
                "  '1': null\n" +
                "  '2':\n" +
                "    v: 1976\n" +
                "    type: ARROW\n" +
                "    amount: 16\n" +
                "  '3': null\n" +
                "  '4':\n" +
                "    v: 1976\n" +
                "    type: BOW\n" +
                "  '5': null\n" +
                "  '6': null\n" +
                "  '7': null\n" +
                "  '8': null\n" +
                "  '9': 'null'\n" +
                "  '10': null\n" +
                "  '11': null\n" +
                "  '12': null\n" +
                "  '13': null\n" +
                "  '14': null\n" +
                "  '15': null\n" +
                "  '16': null\n" +
                "  '17': null\n" +
                "  '18': null\n" +
                "  '19': null\n" +
                "  '20': null\n" +
                "  '21': null\n" +
                "  '22': null\n" +
                "  '23': null\n" +
                "  '24': null\n" +
                "  '25': null\n" +
                "  '26': null\n" +
                "  '27': null\n"

        // Act
        val keyValuePair = classUnderTest.readFromString(sourceString)
        val nullItem = (keyValuePair["items"] as Map<*, *>)["9"] as String

        // Assert
        Assert.assertEquals(expectedAmount, keyValuePair.size)
        Assert.assertEquals("null", nullItem)
    }

    private fun createWithDependencies(): YamlService {
        return YamlServiceImpl()
    }
}
