@file:Suppress("UNCHECKED_CAST")

package integrationtest

import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.service.YamlSerializationService
import com.github.shynixn.petblocks.core.logic.business.service.YamlSerializationServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.AIAfraidOfWaterEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.AIAmbientSoundEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.AIFeedingEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.AIFleeInCombatEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.io.StringWriter

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
class PersistenceSerializationIT {
    /**
     * Given
     *      an entity to serialize and deserialize
     * When
     *      serialize and deserialize is called
     * Then
     *     the origin data should be correctly fetched.
     */
    @Test
    fun serializeDeserialize_AIAfraidOfWaterEntity_ShouldSerializeAndDeserialize() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val entity = AIAfraidOfWaterEntity()
        entity.id = 4
        entity.userId = "Yeah"
        entity.particle.materialName = "SPONGE"
        entity.stoppingDelay = 7

        // Act
        val writer = StringWriter()
        classUnderTest.serialize(entity, writer)
        val textContent = writer.toString()
        val reader = StringReader(textContent)
        val actual = classUnderTest.deserialize<AIAfraidOfWaterEntity>(AIAfraidOfWaterEntity::class.java, reader)

        // Assert
        Assertions.assertEquals(4, actual.id)
        Assertions.assertEquals("Yeah", actual.userId)
        Assertions.assertEquals(7, actual.stoppingDelay)
        Assertions.assertEquals("SPONGE", actual.particle.materialName)
    }

    /**
     * Given
     *      an entity to serialize and deserialize
     * When
     *      serialize and deserialize is called
     * Then
     *     the origin data should be correctly fetched.
     */
    @Test
    fun serializeDeserialize_AIAmbientSoundEntity_ShouldSerializeAndDeserialize() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val entity = AIAmbientSoundEntity()
        entity.id = 4
        entity.userId = "Yeah"
        entity.sound.name = "Experience"

        // Act
        val writer = StringWriter()
        classUnderTest.serialize(entity, writer)
        val textContent = writer.toString()
        val reader = StringReader(textContent)
        val actual = classUnderTest.deserialize<AIAmbientSoundEntity>(AIAmbientSoundEntity::class.java, reader)

        // Assert
        Assertions.assertEquals(4, actual.id)
        Assertions.assertEquals("Yeah", actual.userId)
        Assertions.assertEquals("Experience", actual.sound.name)
    }

    /**
     * Given
     *      an entity to serialize and deserialize
     * When
     *      serialize and deserialize is called
     * Then
     *     the origin data should be correctly fetched.
     */
    @Test
    fun serializeDeserialize_AIFeedingEntity_ShouldSerializeAndDeserialize() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val entity = AIFeedingEntity()
        entity.id = 4
        entity.userId = "Yeah"
        entity.itemId = 4
        entity.clickParticle.type = ParticleType.BARRIER
        entity.clickSound.pitch = 93.0
        entity.dataValue = 42
        entity.typeName = "Funny"

        // Act
        val writer = StringWriter()
        classUnderTest.serialize(entity, writer)
        val textContent = writer.toString()
        val reader = StringReader(textContent)
        val actual = classUnderTest.deserialize<AIFeedingEntity>(AIFeedingEntity::class.java, reader)

        // Assert
        Assertions.assertEquals(4, actual.id)
        Assertions.assertEquals("Yeah", actual.userId)
        Assertions.assertEquals(4, actual.itemId)
        Assertions.assertEquals(ParticleType.BARRIER, actual.clickParticle.type)
        Assertions.assertEquals(93.0, actual.clickSound.pitch)
        Assertions.assertEquals(42, actual.dataValue)
        Assertions.assertEquals("Funny", actual.typeName)
    }

    /**
     * Given
     *      an entity to serialize and deserialize
     * When
     *      serialize and deserialize is called
     * Then
     *     the origin data should be correctly fetched.
     */
    @Test
    fun serializeDeserialize_AIFleeInCombat_ShouldSerializeAndDeserialize() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val entity = AIFleeInCombatEntity()
        entity.id = 4
        entity.userId = "Yeah"
        entity.reAppearsInSeconds = 774

        // Act
        val writer = StringWriter()
        classUnderTest.serialize(entity, writer)
        val textContent = writer.toString()
        val reader = StringReader(textContent)
        val actual = classUnderTest.deserialize<AIFleeInCombatEntity>(AIFleeInCombatEntity::class.java, reader)

        // Assert
        Assertions.assertEquals(4, actual.id)
        Assertions.assertEquals("Yeah", actual.userId)
        Assertions.assertEquals(774, actual.reAppearsInSeconds)
    }

    companion object {
        fun createWithDependencies(): YamlSerializationService {
            return YamlSerializationServiceImpl()
        }
    }
}